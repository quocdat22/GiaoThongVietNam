package com.example.giaothong.api;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Client để gọi API Roboflow nhận diện biển báo
 */
public class RoboflowApiClient {
    private static final String TAG = "RoboflowApiClient";
    
    // API parameters
    private final String apiUrl;
    private final String apiKey;
    private final String modelId;
    private final String workflowId;  // Optional, có thể null
    
    // OkHttpClient for network requests
    private final OkHttpClient client;
    
    /**
     * Constructor with Roboflow API parameters
     * @param apiUrl URL cơ sở của API
     * @param apiKey API key
     * @param modelId ID model (format: project-name/version-number)
     * @param workflowId ID workflow (tùy chọn, có thể null)
     */
    public RoboflowApiClient(String apiUrl, String apiKey, String modelId, String workflowId) {
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.modelId = modelId;
        this.workflowId = workflowId;
        
        // Khởi tạo OkHttpClient với timeout
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }
    
    /**
     * Kết quả nhận diện biển báo
     */
    public static class DetectionResult {
        private final String className;
        private final float confidence;
        private final int x;
        private final int y;
        private final int width;
        private final int height;
        
        public DetectionResult(String className, float confidence, int x, int y, int width, int height) {
            this.className = className;
            this.confidence = confidence;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
        
        public String getClassName() {
            return className;
        }
        
        public float getConfidence() {
            return confidence;
        }
        
        public int getX() {
            return x;
        }
        
        public int getY() {
            return y;
        }
        
        public int getWidth() {
            return width;
        }
        
        public int getHeight() {
            return height;
        }
        
        @Override
        public String toString() {
            return className + " (" + String.format("%.1f", confidence * 100) + "%)";
        }
    }
    
    /**
     * Gửi ảnh lên Roboflow API để nhận diện
     * @param bitmap Ảnh cần nhận diện
     * @return Danh sách các kết quả nhận diện
     */
    public List<DetectionResult> detectTrafficSign(Bitmap bitmap) throws IOException {
        // Chuẩn bị ảnh để gửi đi
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
        byte[] byteArray = stream.toByteArray();
        
        // Tạo URL tùy thuộc vào loại API (detection hoặc workflow)
        String url;
        if (workflowId != null && !workflowId.isEmpty()) {
            // Sử dụng workflow API
            url = String.format("%s/workflow/%s?api_key=%s", apiUrl, workflowId, apiKey);
            Log.d(TAG, "Using workflow API: " + url);
        } else {
            // Sử dụng model detection API (standard)
            url = String.format("%s/%s?api_key=%s", apiUrl, modelId, apiKey);
            Log.d(TAG, "Using model detection API: " + url);
        }
        
        // Tạo MultipartBody để gửi hình ảnh
        MultipartBody.Builder multipartBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "image.jpg",
                        RequestBody.create(MediaType.parse("image/jpeg"), byteArray));
        
        RequestBody requestBody = multipartBuilder.build();
        
        // Tạo request với header đúng
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(requestBody);
        
        Request request = requestBuilder.build();
        
        // Thực hiện request
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                Log.e(TAG, "API response code: " + response.code());
                if (response.body() != null) {
                    String errorBody = response.body().string();
                    Log.e(TAG, "Error body: " + errorBody);
                    throw new IOException("API error: " + errorBody);
                }
                throw new IOException("Unexpected response code: " + response);
            }
            
            // Phân tích kết quả
            if (response.body() != null) {
                String responseBody = response.body().string();
                Log.d(TAG, "Response body: " + responseBody);
                return parseDetectionResults(responseBody);
            } else {
                throw new IOException("Empty response body");
            }
        }
    }
    
    /**
     * Phân tích kết quả JSON từ Roboflow API
     */
    private List<DetectionResult> parseDetectionResults(String jsonResponse) {
        List<DetectionResult> results = new ArrayList<>();
        
        try {
            JsonElement jsonElement = JsonParser.parseString(jsonResponse);
            
            // Kiểm tra nếu response là một mảng (như trong mẫu)
            if (jsonElement.isJsonArray()) {
                JsonArray responseArray = jsonElement.getAsJsonArray();
                if (responseArray.size() > 0) {
                    JsonObject firstResponse = responseArray.get(0).getAsJsonObject();
                    
                    // Kiểm tra trường detection_predictions
                    if (firstResponse.has("detection_predictions")) {
                        JsonObject detectionPredictions = firstResponse.getAsJsonObject("detection_predictions");
                        
                        if (detectionPredictions.has("predictions")) {
                            JsonArray predictions = detectionPredictions.getAsJsonArray("predictions");
                            
                            for (JsonElement prediction : predictions) {
                                JsonObject predObj = prediction.getAsJsonObject();
                                
                                if (predObj.has("class") && predObj.has("confidence") && 
                                    predObj.has("x") && predObj.has("y") && 
                                    predObj.has("width") && predObj.has("height")) {
                                    
                                    String className = predObj.get("class").getAsString();
                                    float confidence = predObj.get("confidence").getAsFloat();
                                    int x = predObj.get("x").getAsInt();
                                    int y = predObj.get("y").getAsInt();
                                    int width = predObj.get("width").getAsInt();
                                    int height = predObj.get("height").getAsInt();
                                    
                                    // Tạo kết quả và thêm vào danh sách
                                    DetectionResult result = new DetectionResult(
                                            className, confidence, x, y, width, height);
                                    results.add(result);
                                }
                            }
                        }
                    }
                }
            } else if (jsonElement.isJsonObject()) {
                // Handle trường hợp response là một object
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                
                // Kiểm tra các định dạng khác nhau của API response
                if (jsonObject.has("predictions")) {
                    // Format cũ
                    JsonArray predictions = jsonObject.getAsJsonArray("predictions");
                    for (JsonElement prediction : predictions) {
                        JsonObject predObj = prediction.getAsJsonObject();
                        
                        String className = predObj.get("class").getAsString();
                        float confidence = predObj.get("confidence").getAsFloat();
                        
                        // Lấy tọa độ và kích thước của bounding box
                        int x = 0, y = 0, width = 0, height = 0;
                        
                        if (predObj.has("x")) x = predObj.get("x").getAsInt();
                        if (predObj.has("y")) y = predObj.get("y").getAsInt();
                        if (predObj.has("width")) width = predObj.get("width").getAsInt();
                        if (predObj.has("height")) height = predObj.get("height").getAsInt();
                        
                        // Support cho trường hợp API trả về bbox
                        if (predObj.has("bbox")) {
                            JsonObject bbox = predObj.getAsJsonObject("bbox");
                            x = bbox.get("x").getAsInt();
                            y = bbox.get("y").getAsInt();
                            width = bbox.get("width").getAsInt();
                            height = bbox.get("height").getAsInt();
                        }
                        
                        // Tạo kết quả và thêm vào danh sách
                        DetectionResult result = new DetectionResult(
                                className, confidence, x, y, width, height);
                        results.add(result);
                    }
                }
            }
            
            // Log số lượng kết quả tìm được
            Log.d(TAG, "Detected " + results.size() + " traffic signs");
            
        } catch (Exception e) {
            Log.e(TAG, "Error parsing detection result: " + e.getMessage(), e);
        }
        
        return results;
    }
} 