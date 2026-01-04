package com.faceproctoring.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class StudentDataLoader {
    private static final String STUDENT_NAMES_FILE = "python_backend/student_names.json";
    private static Map<String, StudentInfo> studentData = new HashMap<>();

    static {
        loadStudentData();
    }

    private static void loadStudentData() {
        try {
            String content = new String(Files.readAllBytes(Paths.get(STUDENT_NAMES_FILE)));
            JsonObject jsonObject = JsonParser.parseString(content).getAsJsonObject();

            for (String key : jsonObject.keySet()) {
                JsonObject studentObj = jsonObject.getAsJsonObject(key);
                String faceId = studentObj.get("face_id").getAsString();
                String fullname = studentObj.get("fullname").getAsString();
                String clazz = studentObj.has("class") ? studentObj.get("class").getAsString() : "N/A";
                String room = studentObj.has("room") ? studentObj.get("room").getAsString() : "N/A";
                studentData.put(key.toLowerCase(), new StudentInfo(faceId, fullname, clazz, room));
            }
        } catch (IOException e) {
            System.err.println("Error loading student data: " + e.getMessage());
        }
    }

    public static String getFullName(String studentId) {
        StudentInfo info = studentData.get(studentId.toLowerCase());
        return info != null ? info.fullname : "N/A";
    }

    public static String getClazz(String studentId) {
        StudentInfo info = studentData.get(studentId.toLowerCase());
        return info != null ? info.clazz : "N/A";
    }

    public static String getRoom(String studentId) {
        StudentInfo info = studentData.get(studentId.toLowerCase());
        return info != null ? info.room : "N/A";
    }

    public static String getFaceId(String studentId) {
        StudentInfo info = studentData.get(studentId.toLowerCase());
        return info != null ? info.faceId : null;
    }

    public static class StudentInfo {
        public String faceId;
        public String fullname;
        public String clazz;
        public String room;

        public StudentInfo(String faceId, String fullname, String clazz, String room) {
            this.faceId = faceId;
            this.fullname = fullname;
            this.clazz = clazz;
            this.room = room;
        }
    }
}