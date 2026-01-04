import sys, os
sys.path.append(os.path.dirname(__file__))

from server import create_app, User, bcrypt

app, db, _ = create_app()

with app.app_context():
    db.create_all()

    users = [
        ("admin", "123456", "Quản trị viên", "admin"),
        ("sv001", "sv001", "Huỳnh Lâm Gia Linh", "user"),
        ("sv002", "sv002", "Nguyễn Thị Kim Đoan", "user"),
        ("sv003", "sv003", "Lê Văn Sơn", "user"),
        ("sv004", "sv004", "Ngô Thị Thanh Huyền", "user")
    ]

    for username, password, fullname, role in users:
        if not User.query.filter_by(username=username).first():
            hash_pw = bcrypt.generate_password_hash(password).decode()
            db.session.add(User(username=username, password_hash=hash_pw, fullname=fullname, role=role))

    db.session.commit()
    print("✅ Đã seed xong 5 tài khoản mặc định")
