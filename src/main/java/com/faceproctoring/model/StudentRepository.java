package com.faceproctoring.model;

import java.util.HashMap;
import java.util.Map;

public class StudentRepository {
    private static final Map<String, Student> STORE = new HashMap<>();

    static {
        // sample students (same data as AdminStudentsController sample rows)
        STORE.put("2021600123", new Student("2021600123", "Nguyễn Văn A", "DHTH16A", "A101"));
        STORE.put("2021600124", new Student("2021600124", "Nguyễn Thị Kim Đoan", "DHTH16A", "A101"));
        STORE.put("2021600125", new Student("2021600125", "Lê Văn C", "DHTH16B", "A102"));
        STORE.put("2021600126", new Student("2021600126", "Phạm Thị D", "DHTH16B", "A102"));
        STORE.put("2021600127", new Student("2021600127", "Hoàng Văn E", "DHTH16C", "A103"));
        STORE.put("2021600128", new Student("2021600128", "Vũ Thị F", "DHTH16C", "A103"));
        STORE.put("2021600129", new Student("2021600129", "Đặng Văn G", "DHTH16A", "A101"));
        STORE.put("2021600130", new Student("2021600130", "Bùi Thị H", "DHTH16B", "A102"));
        // add the login test account used in StudentLoginController
        STORE.put("SV001", new Student("SV001", "Test Sinh Viên", "DHTH_TEST", "A999"));
    }

    public static Student findById(String id) {
        if (id == null)
            return null;
        return STORE.get(id.trim());
    }
}
