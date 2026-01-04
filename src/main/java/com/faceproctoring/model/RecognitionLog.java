package com.faceproctoring.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RecognitionLog {
    private String studentId;
    private String fullName;
    private String className;
    private String room;
    private String status;  // "Đã xác thực", "Nghi ngờ", "Thất bại"
    private double percentage;
    private String recognitionTime;
    private String recognizedPerson;
    
    public RecognitionLog() {
        this.recognitionTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
    
    public RecognitionLog(String studentId, String fullName, String className, String room, 
                         String status, double percentage, String recognizedPerson) {
        this.studentId = studentId;
        this.fullName = fullName;
        this.className = className;
        this.room = room;
        this.status = status;
        this.percentage = percentage;
        this.recognizedPerson = recognizedPerson;
        this.recognitionTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
    
    // Getters and Setters
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    
    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }
    
    public String getRecognitionTime() { return recognitionTime; }
    public void setRecognitionTime(String recognitionTime) { this.recognitionTime = recognitionTime; }
    
    public String getRecognizedPerson() { return recognizedPerson; }
    public void setRecognizedPerson(String recognizedPerson) { this.recognizedPerson = recognizedPerson; }
    
    public String getStatusColor() {
        if ("Đã xác thực".equals(status)) return "green";
        if ("Nghi ngờ".equals(status)) return "orange";
        return "red";
    }
}
