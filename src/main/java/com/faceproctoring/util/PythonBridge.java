package com.faceproctoring.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.nio.charset.StandardCharsets;

/**
 * PythonBridge — kết nối từ JavaFX sang Flask backend để gửi ảnh base64
 * và nhận kết quả nhận diện khuôn mặt.
 *
 * Backend Flask phải chạy ở cổng 5000 với endpoint: /recognize-stream
 */
public class PythonBridge {
    private static final String API_URL = "http://127.0.0.1:5000/recognize-stream";

    /**
     * Gửi ảnh base64 đến Flask API để nhận diện khuôn mặt
     * @param frameBase64 Ảnh base64 từ webcam
     * @return RecognitionResult (kết quả nhận diện)
     * @throws Exception nếu lỗi kết nối hoặc parse JSON
     */
    public static RecognitionResult recognizeStream(String frameBase64) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(API_URL);

            // Tạo JSON body
            JsonObject json = new JsonObject();
            json.addProperty("frame", frameBase64);
            post.setEntity(new StringEntity(json.toString(), StandardCharsets.UTF_8));
            post.setHeader("Content-Type", "application/json");

            try (CloseableHttpResponse response = client.execute(post)) {
                String result = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                // Kiểm tra mã trạng thái
                if (response.getStatusLine().getStatusCode() != 200) {
                    System.err.println("⚠️ Flask API trả về lỗi: " + response.getStatusLine());
                    return new RecognitionResult(false, "UNKNOWN", "LỖI KẾT NỐI", 0.0, "gray");
                }

                // Parse JSON trả về
                JsonObject obj = JsonParser.parseString(result).getAsJsonObject();

                boolean match = obj.has("match") && obj.get("match").getAsBoolean();
                String person = obj.has("person") ? obj.get("person").getAsString() : "Không rõ";
                String status = obj.has("status") ? obj.get("status").getAsString() : "KHÔNG XÁC ĐỊNH";
                double percentage = obj.has("percentage") ? obj.get("percentage").getAsDouble() : 0.0;
                String color = obj.has("color") ? obj.get("color").getAsString() : "gray";

                return new RecognitionResult(match, person, status, percentage, color);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Trả về giá trị mặc định khi Flask không phản hồi
            return new RecognitionResult(false, "⚠️ Lỗi kết nối Flask", "KHÔNG HỢP LỆ", 0.0, "red");
        }
    }

    /**
     * Model chứa kết quả nhận diện khuôn mặt trả về từ Flask
     */
    public static class RecognitionResult {
        public boolean match;
        public String status;
        public String warning; // ✅ thêm dòng này
        public String person;
        public double percentage;
        public String color;

        public RecognitionResult(boolean match, String status, String warning, double percentage, String color) {
            this.match = match;
            this.status = status;
            this.warning = "";
            this.percentage = percentage;
            this.color = color;
        }

        @Override
        public String toString() {
            return String.format(
                "RecognitionResult{match=%s, person='%s', status='%s', warning='%s', percentage=%.2f, color='%s'}",
                match, person, status, warning, percentage, color
            );
        }
    }
}
