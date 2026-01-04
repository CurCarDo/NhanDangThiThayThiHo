import os
import json
import base64
import io
import numpy as np
from datetime import datetime, timedelta
from flask import Flask, request, jsonify, g
from flask_sqlalchemy import SQLAlchemy
from flask_bcrypt import Bcrypt
from flask_socketio import SocketIO
from PIL import Image
import jwt
from functools import wraps
from insightface.app import FaceAnalysis
from sklearn.metrics.pairwise import cosine_similarity

# ==============================
# CONFIG
# ==============================
app = Flask(__name__)
app.config["SECRET_KEY"] = "super_secret_key_here"
app.config["SQLALCHEMY_DATABASE_URI"] = r"sqlite:///D:/NhanDangThiThayThiHo/FaceProctoring/python_backend/instance/faceproctor.db"
app.config["SQLALCHEMY_TRACK_MODIFICATIONS"] = False

db = SQLAlchemy(app)
bcrypt = Bcrypt(app)
socketio = SocketIO(app, cors_allowed_origins="*")

JWT_SECRET = app.config["SECRET_KEY"]
JWT_ALGO = "HS256"

# ==============================
# DB MODELS
# ==============================
class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(120), unique=True, nullable=False)
    password_hash = db.Column(db.String(128), nullable=False)
    fullname = db.Column(db.String(200))
    role = db.Column(db.String(20), default="user")
    created_at = db.Column(db.DateTime, default=datetime.utcnow)

class RecognitionLog(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(120))
    score = db.Column(db.Float)
    status = db.Column(db.String(50))
    name = db.Column(db.String(200))
    timestamp = db.Column(db.DateTime, default=datetime.utcnow)

# ==============================
# AUTH HELPERS
# ==============================
def create_token(user):
    payload = {
        "sub": str(user.id),
        "username": user.username,
        "role": user.role,
        "exp": datetime.utcnow() + timedelta(hours=8)
    }
    return jwt.encode(payload, JWT_SECRET, algorithm=JWT_ALGO)

def auth_required(role=None):
    def decorator(f):
        @wraps(f)
        def wrapper(*args, **kwargs):
            token = None
            if "Authorization" in request.headers:
                token = request.headers["Authorization"].split(" ")[-1]
            if not token:
                return jsonify({"error": "Missing token"}), 401
            try:
                data = jwt.decode(token, JWT_SECRET, algorithms=[JWT_ALGO])
                g.user = User.query.get(int(data["sub"]))
                if role and g.user.role != role:
                    return jsonify({"error": "Forbidden"}), 403
            except Exception as e:
                return jsonify({"error": "Invalid token", "details": str(e)}), 401
            return f(*args, **kwargs)
        return wrapper
    return decorator

# ==============================
# LOAD MODEL + DATA
# ==============================
print("Loading insightface buffalo_l...")
face_app = FaceAnalysis(name='buffalo_l')
face_app.prepare(ctx_id=0)
print("Model ready.")

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
EMB_PATH = os.path.join(SCRIPT_DIR, "emb_arc.npy")
LABELS_PATH = os.path.join(SCRIPT_DIR, "labels_arc.npy")
NAMES_PATH = os.path.join(SCRIPT_DIR, "student_names.json")

emb_db = None
labels_db = None
student_names = {}

if os.path.exists(EMB_PATH) and os.path.exists(LABELS_PATH):
    emb_db = np.load(EMB_PATH)
    labels_db = np.load(LABELS_PATH, allow_pickle=True)
    labels_db = np.asarray(labels_db).reshape(-1)
    print(f"Loaded embeddings: {len(labels_db)} entries")
else:
    print("⚠️ embeddings/labels missing")

if os.path.exists(NAMES_PATH):
    with open(NAMES_PATH, 'r', encoding='utf-8') as f:
        student_names = json.load(f)
    print(f"Loaded student_names.json entries: {len(student_names)}")
else:
    print("⚠️ student_names.json missing")

# Helper to lookup fullname by face label or by username mapping
def lookup_fullname(label):
    # case 1: mapping keyed by face_id string -> fullname
    if isinstance(student_names, dict):
        if str(label) in student_names and isinstance(student_names[str(label)], str):
            return student_names[str(label)]
        # case 2: mapping keyed by username -> {face_id, fullname}
        for k, v in student_names.items():
            if isinstance(v, dict):
                if str(v.get("face_id")) == str(label):
                    return v.get("fullname") or v.get("name")
    return None

# ==============================
# RECOGNIZE ENDPOINT
# ==============================
@app.route("/recognize-stream", methods=["POST"])
@auth_required()
def recognize_stream():
    global emb_db, labels_db, student_names
    if emb_db is None or labels_db is None:
        return jsonify({"error": "Embedding DB not loaded"}), 500

    data = request.get_json()
    frame_b64 = data.get("frame")
    if not frame_b64:
        return jsonify({"error": "Missing frame"}), 400

    try:
        img_bytes = base64.b64decode(frame_b64)
        image = Image.open(io.BytesIO(img_bytes)).convert("RGB")
        frame = np.array(image)[:, :, ::-1]
    except Exception as e:
        return jsonify({"error": "Invalid image", "details": str(e)}), 400

    faces = face_app.get(frame)
    if len(faces) == 0:
        return jsonify({
            "match": False,
            "status": "KHÔNG PHÁT HIỆN KHUÔN MẶT",
            "warning": "❌ Không phát hiện khuôn mặt",
            "percentage": 0.0,
            "color": "red"
        }), 200

    try:
        emb = faces[0].embedding

        # Get the expected face_id for the logged-in user
        expected_face_id = None
        user_entry = student_names.get(g.user.username)
        if isinstance(user_entry, dict):
            expected_face_id = str(user_entry.get("face_id"))

        if not expected_face_id:
            return jsonify({
                "match": False,
                "status": "LỖI CẤU HÌNH",
                "warning": f"❌ Không tìm thấy face_id cho sinh viên {g.user.username}",
                "percentage": 0.0,
                "color": "red"
            }), 200

        # Find the embedding for the expected user
        expected_indices = np.where(labels_db.astype(str) == expected_face_id)[0]
        if len(expected_indices) == 0:
            return jsonify({
                "match": False,
                "status": "LỖI DỮ LIỆU",
                "warning": f"❌ Face ID {expected_face_id} của sinh viên không có trong kho dữ liệu.",
                "percentage": 0.0,
                "color": "red"
            }), 200

        expected_idx = expected_indices[0]
        expected_emb = emb_db[expected_idx]

        # Calculate similarity ONLY with the expected user's embedding
        score = cosine_similarity([emb], [expected_emb])[0][0]
        percent = round(float(score) * 100, 2)
        best_label = expected_face_id
        best_name = lookup_fullname(best_label) or g.user.username

        # For logging/warning purposes, find the best overall match as well
        sims_all = cosine_similarity([emb], emb_db)[0]
        best_overall_idx = int(np.argmax(sims_all))
        best_overall_score = float(sims_all[best_overall_idx])
        best_overall_label = str(labels_db[best_overall_idx])

        print(f"[DEBUG] user={g.user.username} expected={expected_face_id} score_vs_expected={score:.4f} | best_overall_match={best_overall_label} score={best_overall_score:.4f}")

        # Decision logic based on the score against the EXPECTED user
        status = "KHÔNG KHỚP"
        color = "red"
        warning = f"❌ Không khớp với sinh viên {best_name} ({percent}%)."

        if percent >= 75:
            status = "HỢP LỆ"
            color = "green"
            warning = f"✅ Xác thực thành công: {best_name}."
        elif percent >= 40:
            status = "NGHI NGỜ"
            color = "orange"
            warning = f"⚠️ Độ khớp với {best_name} ở mức trung bình ({percent}%)."

        # Strong warning if the face strongly matches SOMEONE ELSE
        if status != "HỢP LỆ" and best_overall_score > 0.75 and best_overall_label != expected_face_id:
            imposter_name = lookup_fullname(best_overall_label) or "người khác"
            warning = f"🚨 CẢNH BÁO: Khuôn mặt rất giống với {imposter_name} ({round(best_overall_score*100, 2)}%) nhưng bạn đang đăng nhập với tư cách {best_name}."
            status = "KHÔNG KHỚP"
            color = "red"

        # Save recognition log
        log = RecognitionLog(
            username=g.user.username,
            score=score,
            status=status,
            name=best_name
        )
        db.session.add(log)
        db.session.commit()

        # Emit realtime event
        event = {
            "id": best_label,
            "name": best_name,
            "status": status,
            "score": score,
            "warning": warning,
            "color": color,
            "time": datetime.now().strftime("%H:%M:%S")
        }
        socketio.emit("student_update", event)

        return jsonify({
            "match": status == "HỢP LỆ",
            "status": status,
            "warning": warning,
            "person": best_name,
            "percentage": percent,
            "color": color
        })

    except Exception as e:
        print(f"Error in recognition stream: {e}")
        db.session.rollback()
        return jsonify({"error": "Processing error during verification", "details": str(e)}), 500


# ==============================
# Other endpoints (login/register/logs)
# ==============================
@app.route("/auth/login", methods=["POST"])
def login():
    data = request.get_json()
    u = User.query.filter_by(username=data.get("username")).first()
    if not u or not bcrypt.check_password_hash(u.password_hash, data.get("password")):
        return jsonify({"error": "Invalid credentials"}), 401
    token = create_token(u)
    return jsonify({"token": token, "user": {"username": u.username, "fullname": u.fullname, "role": u.role}})

@app.route("/auth/register", methods=["POST"])
@auth_required(role="admin")
def register():
    data = request.get_json()
    username = data.get("username")
    password = data.get("password")
    fullname = data.get("fullname", "")
    if User.query.filter_by(username=username).first():
        return jsonify({"error": "Username exists"}), 400
    hash_pw = bcrypt.generate_password_hash(password).decode()
    u = User(username=username, password_hash=hash_pw, fullname=fullname)
    db.session.add(u)
    db.session.commit()
    return jsonify({"msg": "User created", "id": u.id})

@app.route("/admin/logs", methods=["GET"])
@auth_required(role="admin")
def get_logs():
    logs = RecognitionLog.query.order_by(RecognitionLog.timestamp.desc()).limit(200).all()
    return jsonify([{
        "id": l.id,
        "username": l.username,
        "score": l.score,
        "status": l.status,
        "name": l.name,
        "timestamp": l.timestamp.isoformat()
    } for l in logs])

@socketio.on("connect")
def on_connect():
    print("Admin connected to realtime")

def create_app():
    with app.app_context():
        db.create_all()
    return app, db, socketio

if __name__ == "__main__":
    with app.app_context():
        db.create_all()
    print("Server running")
    socketio.run(app, host="127.0.0.1", port=5000)
