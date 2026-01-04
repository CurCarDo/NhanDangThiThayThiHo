# 📚 HƯỚNG DẪN LƯU TRỮ VÀ KHỚP DỮ LIỆU SINH VIÊN

## 🎯 Mục tiêu

Khi sinh viên đăng nhập với **Mã SV** (ví dụ: SV001), hệ thống cần:
1. ✅ Hiển thị thông tin sinh viên (Họ tên, Lớp, Phòng thi)
2. ✅ Nhận diện khuôn mặt
3. ✅ **Kiểm tra khớp**: Face ID có đúng với sinh viên đã đăng nhập không?

---

## 📁 Cấu trúc dữ liệu

### File: `students.json`
**Vị trí:** `.venv_face_arc/students.json`

```json
{
  "SV001": {
    "studentId": "SV001",
    "fullName": "Test Sinh Viên",
    "className": "DHTH_TEST",
    "room": "A999",
    "faceId": "126"  ← ID khuôn mặt trong labels_arc.npy
  },
  "2021600126": {
    "studentId": "2021600126",
    "fullName": "Nguyễn Thị Kim Đoan",
    "className": "DHTH15B",
    "room": "A102",
    "faceId": "126"
  }
}
```

### Giải thích các trường:
- **studentId**: Mã sinh viên đăng nhập (key chính)
- **fullName**: Họ và tên đầy đủ
- **className**: Lớp học
- **room**: Phòng thi được phân công
- **faceId**: ID khớp với `labels_arc.npy` (126, 127, 128...)

---

## 🔄 Luồng hoạt động

### 1. **Đăng nhập**
```
Sinh viên nhập: SV001
↓
StudentDatabase.getStudentById("SV001")
↓
Lấy thông tin: {
  fullName: "Test Sinh Viên",
  className: "DHTH_TEST",
  room: "A999",
  faceId: "126"
}
↓
Hiển thị trên UI
```

### 2. **Nhận diện khuôn mặt**
```
Chụp ảnh
↓
Buffalo_l nhận diện → FaceID: "126"
↓
Python API trả về: {
  person: "Nguyễn Thị Kim Đoan",  ← Từ student_names.json
  percentage: 83.7%
}
```

### 3. **Xác thực khớp**
```java
String loggedInStudentId = "SV001";
String recognizedFaceId = "126";

StudentDatabase db = StudentDatabase.getInstance();
boolean isValid = db.verifyStudent(loggedInStudentId, recognizedFaceId);

if (isValid) {
    // ✅ Đúng sinh viên → Cho vào thi
    navigateToExamScreen();
} else {
    // ❌ Không khớp → Cảnh báo gian lận
    showWarning("Nghi ngờ thi hộ!");
}
```

---

## 💻 Sử dụng trong Code

### **StudentLoginController.java**
```java
@FXML
public void onLogin(ActionEvent e) {
    String studentId = txtStudentId.getText();
    
    StudentDatabase db = StudentDatabase.getInstance();
    Student student = db.getStudentById(studentId);
    
    if (student == null) {
        lblError.setText("Mã sinh viên không tồn tại!");
        return;
    }
    
    // Chuyển sang màn hình nhận diện với thông tin sinh viên
    navigateToFaceRecognition(student);
}
```

### **FaceRecognitionController.java**
```java
private Student loggedInStudent;

public void setStudentInfo(Student student) {
    this.loggedInStudent = student;
    lblStudentId.setText(student.getId());
    lblStudentName.setText(student.getName());
    lblClass.setText(student.getClazz());
    lblRoom.setText(student.getRoom());
}

private void onRecognitionComplete(String recognizedFaceId) {
    StudentDatabase db = StudentDatabase.getInstance();
    
    // Verify match
    boolean isValid = db.verifyStudent(
        loggedInStudent.getId(), 
        recognizedFaceId
    );
    
    if (isValid) {
        navigateToResultScreen(true, "Xác thực thành công!");
    } else {
        navigateToResultScreen(false, "CẢNH BÁO: Không phải sinh viên đã đăng nhập!");
    }
}
```

---

## 📊 Mapping quan hệ

```
┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│ students    │      │ labels_arc  │      │ student_    │
│ .json       │──────│ .npy        │──────│ names.json  │
└─────────────┘      └─────────────┘      └─────────────┘
     │                     │                      │
     │ faceId: "126"       │ Index: 126           │ "126": "Nguyễn Thị Kim Đoan"
     ↓                     ↓                      ↓
SV001 ────→ FaceID:126 ────→ Buffalo_l ────→ "Nguyễn Thị Kim Đoan"
     Login    Verify         Recognition      Display
```

---

## 🔐 Bảo mật

1. **Không lưu mật khẩu**: File JSON chỉ chứa thông tin công khai
2. **Mã hóa file** (tùy chọn): Có thể encrypt `students.json`
3. **Server-side**: Production nên dùng database thật (MySQL, PostgreSQL)

---

## 🚀 Nâng cấp sau này

### Phương án 2: **MySQL Database**
```sql
CREATE TABLE students (
    student_id VARCHAR(20) PRIMARY KEY,
    full_name VARCHAR(100),
    class_name VARCHAR(50),
    room VARCHAR(10),
    face_id VARCHAR(10),
    INDEX idx_face_id (face_id)
);
```

### Phương án 3: **API Backend**
```
GET /api/students/{studentId}
POST /api/verify
  {
    "studentId": "SV001",
    "recognizedFaceId": "126"
  }
```

---

## ✅ Checklist triển khai

- [x] Tạo file `students.json`
- [x] Tạo class `Student.java`
- [x] Tạo class `StudentDatabase.java`
- [ ] Cập nhật `StudentLoginController` để load thông tin
- [ ] Cập nhật `FaceRecognitionController` để verify
- [ ] Thêm logic cảnh báo khi không khớp
- [ ] Test với nhiều sinh viên

---

**Tài liệu cập nhật:** 04/11/2025
