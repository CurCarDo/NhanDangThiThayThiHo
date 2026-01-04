package com.faceproctoring.util;

import com.faceproctoring.model.Student;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class StudentDatabase {
    private static StudentDatabase instance;
    private Map<String, Student> students;
    private static final String DB_FILE = ".venv_face_arc/students.json";

    private StudentDatabase() {
        students = new HashMap<>();
        loadDatabase();
    }

    public static StudentDatabase getInstance() {
        if (instance == null) {
            instance = new StudentDatabase();
        }
        return instance;
    }

    private void loadDatabase() {
        try {
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, Student>>(){}.getType();
            FileReader reader = new FileReader(DB_FILE);
            students = gson.fromJson(reader, type);
            reader.close();
            System.out.println("Loaded " + students.size() + " students from database");
        } catch (Exception e) {
            System.err.println("Error loading student database: " + e.getMessage());
            students = new HashMap<>();
        }
    }

    /**
     * Get student by student ID (e.g., "SV001")
     */
    public Student getStudentById(String studentId) {
        return students.get(studentId);
    }

    /**
     * Get student by face recognition ID (e.g., "126")
     */
    public Student getStudentByFaceId(String faceId) {
        for (Student student : students.values()) {
            if (faceId.equals(student.getFaceId())) {
                return student;
            }
        }
        return null;
    }

    /**
     * Verify if recognized face matches logged-in student
     */
    public boolean verifyStudent(String loggedInStudentId, String recognizedFaceId) {
        Student student = getStudentById(loggedInStudentId);
        if (student == null) {
            System.err.println("Student not found: " + loggedInStudentId);
            return false;
        }
        
        boolean matches = recognizedFaceId.equals(student.getFaceId());
        System.out.println("Verify: " + loggedInStudentId + " -> FaceID: " + student.getFaceId() + 
                          " vs Recognized: " + recognizedFaceId + " = " + matches);
        return matches;
    }

    /**
     * Get all students
     */
    public Map<String, Student> getAllStudents() {
        return new HashMap<>(students);
    }
}
