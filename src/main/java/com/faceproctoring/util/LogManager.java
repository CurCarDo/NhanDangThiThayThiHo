package com.faceproctoring.util;

import com.faceproctoring.model.RecognitionLog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LogManager {
    private static LogManager instance;
    private List<RecognitionLog> logs;
    private static final String LOG_FILE = "recognition_logs.json";
    private final Gson gson;
    
    private LogManager() {
        gson = new GsonBuilder().setPrettyPrinting().create();
        logs = new ArrayList<>();
        loadLogs();
    }
    
    public static LogManager getInstance() {
        if (instance == null) {
            instance = new LogManager();
        }
        return instance;
    }
    
    private void loadLogs() {
        File file = new File(LOG_FILE);
        if (!file.exists()) {
            logs = new ArrayList<>();
            return;
        }
        
        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<RecognitionLog>>(){}.getType();
            List<RecognitionLog> loaded = gson.fromJson(reader, listType);
            logs = loaded != null ? loaded : new ArrayList<>();
            System.out.println("Loaded " + logs.size() + " recognition logs");
        } catch (Exception e) {
            System.err.println("Error loading logs: " + e.getMessage());
            logs = new ArrayList<>();
        }
    }
    
    public void addLog(RecognitionLog log) {
        logs.add(log);
        saveLogs();
        System.out.println("Added log for student: " + log.getStudentId() + 
                          " - Status: " + log.getStatus());
    }
    
    private void saveLogs() {
        try (FileWriter writer = new FileWriter(LOG_FILE)) {
            gson.toJson(logs, writer);
        } catch (IOException e) {
            System.err.println("Error saving logs: " + e.getMessage());
        }
    }
    
    public List<RecognitionLog> getAllLogs() {
        return new ArrayList<>(logs);
    }
    
    public List<RecognitionLog> getLogsByStudentId(String studentId) {
        List<RecognitionLog> result = new ArrayList<>();
        for (RecognitionLog log : logs) {
            if (studentId.equals(log.getStudentId())) {
                result.add(log);
            }
        }
        return result;
    }
    
    public void clearLogs() {
        logs.clear();
        saveLogs();
    }
    
    public int getTotalLogs() {
        return logs.size();
    }
}
