# 🎓 HƯỚNG DẪN SỬ DỤNG HỆ THỐNG NHẬN DIỆN KHUÔN MẶT

## 📋 Tổng Quan Hệ Thống

Hệ thống nhận diện khuôn mặt sử dụng **InsightFace buffalo_l** để xác thực thí sinh trước khi vào phòng thi.

### 🔍 Phân Loại Kết Quả

| Độ chính xác | Trạng thái | Màu sắc | Ý nghĩa |
|--------------|------------|---------|---------|
| **76-100%** | HỢP LỆ | 🟢 Xanh lá | Xác thực thành công, cho phép vào thi |
| **50-75%** | NGHI NGỜ | 🟠 Cam | Cần kiểm tra lại, có thể cho vào thi với giám sát |
| **0-49%** | KHÔNG HỢP LỆ | 🔴 Đỏ | Từ chối, không khớp với dữ liệu |

---

## 🚀 Khởi Động Hệ Thống

### Bước 1: Khởi động Python API Server

```powershell
cd D:\NhanDangThiThayThiHo\FaceProctoring
py -3.10 python_backend\face_recognition_api.py
```

**Chờ đến khi thấy:**
```
Model loaded successfully!
Loaded 1416 faces in database
 * Running on http://127.0.0.1:5000
```

### Bước 2: Khởi động ứng dụng JavaFX

**Mở terminal mới:**
```powershell
cd D:\NhanDangThiThayThiHo\FaceProctoring
mvn javafx:run
```

---

## 📸 Quy Trình Nhận Diện (Dành Cho Thí Sinh)

1. **Đăng nhập vào hệ thống**
   - Nhập mã sinh viên
   - Nhập thông tin cá nhân

2. **Chọn "Bắt đầu nhận diện"**
   - Camera sẽ tự động mở
   - Hướng mặt thẳng vào camera
   - Đảm bảo ánh sáng đủ

3. **Bấm nút "📸 Chụp ảnh"**
   - Hệ thống chụp ảnh tự động
   - Gửi ảnh đến server nhận diện
   - Xử lý bằng buffalo_l model

4. **Xem kết quả**
   - **HỢP LỆ (76-100%)**: ✅ Được vào phòng thi
   - **NGHI NGỜ (50-75%)**: ⚠️ Kiểm tra thêm
   - **KHÔNG HỢP LỆ (<50%)**: ❌ Liên hệ giám thị

---

## 🔧 Cấu Hình Hệ Thống

### File Dữ Liệu Nhận Diện

```
D:\NhanDangThiThayThiHo\FaceProctoring\.venv_face_arc\
├── emb_arc.npy      # 1416 khuôn mặt embeddings (512-dim vectors)
└── labels_arc.npy   # Tên/ID tương ứng với mỗi embedding
```

### Cấu Trúc Dự Án

```
FaceProctoring/
├── python_backend/
│   ├── face_recognition_api.py    # Flask API với buffalo_l
│   └── requirements.txt
├── src/main/java/
│   └── com/faceproctoring/
│       ├── controller/
│       │   ├── FaceRecognitionController.java  # Chụp ảnh + gửi API
│       │   └── ResultController.java           # Hiển thị kết quả
│       └── util/
│           ├── CameraHelper.java    # Điều khiển camera
│           └── PythonBridge.java    # Gọi Python API
└── .venv_face_arc/                 # Database embeddings
```

---

## 🛠️ Xử Lý Lỗi Thường Gặp

### ❌ Lỗi: "Connect to 127.0.0.1:5000 failed"
**Nguyên nhân:** Python server chưa chạy  
**Giải pháp:** 
```powershell
py -3.10 python_backend\face_recognition_api.py
```

### ❌ Lỗi: "Không thể truy cập camera"
**Nguyên nhân:** Camera đang bị ứng dụng khác sử dụng  
**Giải pháp:**
- Tắt các app dùng camera (Zoom, Teams, Skype)
- Bấm "Chụp lại" để thử kết nối lại

### ❌ Lỗi: "emb_arc.npy not found"
**Nguyên nhân:** File database không đúng vị trí  
**Giải pháp:**
```powershell
# Kiểm tra file tồn tại
ls D:\NhanDangThiThayThiHo\FaceProctoring\.venv_face_arc\
```

### ❌ Lỗi: "Không phát hiện khuôn mặt"
**Nguyên nhân:** Ảnh chụp quá tối/mờ hoặc không có mặt  
**Giải pháp:**
- Bật đèn
- Hướng mặt thẳng camera
- Bấm "Chụp lại"

---

## 📊 Thông Tin Kỹ Thuật

### Python API (Flask)
- **Framework:** Flask 3.1.2
- **AI Model:** InsightFace buffalo_l
- **Face Detection:** RetinaFace (det_10g.onnx)
- **Face Recognition:** ArcFace (w600k_r50.onnx)
- **Embedding Size:** 512 dimensions
- **Similarity Metric:** Cosine Similarity
- **Database Size:** 1416 người

### Java Application (JavaFX)
- **Java Version:** 21
- **JavaFX Version:** 21.0.1
- **Camera Library:** JavaCV (OpenCVFrameGrabber)
- **HTTP Client:** Apache HttpClient
- **JSON Parser:** Gson

### Ngưỡng Nhận Diện
```python
if percentage >= 76:
    status = "HỢP LỆ" (green)
elif percentage >= 50:
    status = "NGHI NGỜ" (orange)
else:
    status = "KHÔNG HỢP LỆ" (red)
```

---

## 👨‍💻 Quản Trị Hệ Thống

### Cập Nhật Database Khuôn Mặt

1. **Huấn luyện model mới với ArcFace**
2. **Export embeddings:**
   ```python
   np.save('emb_arc.npy', embeddings)
   np.save('labels_arc.npy', labels)
   ```
3. **Copy vào thư mục `.venv_face_arc/`**
4. **Khởi động lại Python server**

### Thay Đổi Ngưỡng Nhận Diện

**File:** `python_backend/face_recognition_api.py`

```python
# Line ~40-50
if percentage >= 76:  # Thay đổi ngưỡng HỢP LỆ (mặc định 76%)
    status = "HỢP LỆ"
elif percentage >= 50:  # Thay đổi ngưỡng NGHI NGỜ (mặc định 50%)
    status = "NGHI NGỜ"
```

---

## 📞 Hỗ Trợ

- **Email:** support@example.com
- **Hotline:** 1900-xxxx
- **Tài liệu:** [InsightFace Documentation](https://github.com/deepinsight/insightface)

---

**Phiên bản:** 1.0  
**Cập nhật:** 04/11/2025
