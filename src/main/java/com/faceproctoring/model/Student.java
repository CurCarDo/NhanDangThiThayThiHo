package com.faceproctoring.model;

public class Student {
    private String id;
    private String name;
    private String clazz;
    private String room;
    private String faceId;  // ID for face recognition matching

    public Student(String id, String name, String clazz) {
        this.id = id;
        this.name = name;
        this.clazz = clazz;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getClazz() { return clazz; }
    public String getRoom() { return room; }
    public String getFaceId() { return faceId; }
    
    public void setFaceId(String faceId) { this.faceId = faceId; }

    // convenience constructor with room
    public Student(String id, String name, String clazz, String room) {
        this.id = id;
        this.name = name;
        this.clazz = clazz;
        this.room = room;
    }
    
    // full constructor with faceId
    public Student(String id, String name, String clazz, String room, String faceId) {
        this.id = id;
        this.name = name;
        this.clazz = clazz;
        this.room = room;
        this.faceId = faceId;
    }
}
