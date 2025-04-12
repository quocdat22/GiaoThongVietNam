package com.example.giaothong.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Lớp mô hình cho câu hỏi trắc nghiệm
 */
public class QuizQuestion implements Serializable {
    private String id;
    private String question;
    private String imageUrl;
    private List<String> options;
    private int correctAnswerIndex;
    private String explanation;
    
    public QuizQuestion(String id, String question, String imageUrl, List<String> options, 
                      int correctAnswerIndex, String explanation) {
        this.id = id;
        this.question = question;
        this.imageUrl = imageUrl;
        this.options = options;
        this.correctAnswerIndex = correctAnswerIndex;
        this.explanation = explanation;
    }

    public QuizQuestion(String id, String question, String imageUrl, String[] options, 
                      int correctAnswerIndex, String explanation) {
        this(id, question, imageUrl, Arrays.asList(options), correctAnswerIndex, explanation);
    }
    
    // Tạo từ đối tượng TrafficSign
    public static QuizQuestion fromTrafficSign(TrafficSign sign) {
        String question = "Đây là biển báo gì?";
        String imageUrl = sign.getImagePath();
        
        // Tạo các phương án trả lời (1 đúng, 3 sai)
        List<String> options = new ArrayList<>();
        options.add(sign.getName()); // Phương án đúng
        
        // Phương án giải thích
        String explanation = sign.getDescription();
        
        return new QuizQuestion(
                sign.getId(),
                question,
                imageUrl,
                options,
                0, // Chỉ số phương án đúng (luôn là 0 vì chưa trộn đáp án)
                explanation
        );
    }
    
    /**
     * Trộn thứ tự các phương án trả lời
     * @return Chỉ số mới của đáp án đúng
     */
    public int shuffleOptions() {
        // Lưu đáp án đúng
        String correctAnswer = options.get(correctAnswerIndex);
        
        // Trộn danh sách
        Collections.shuffle(options);
        
        // Tìm chỉ số mới của đáp án đúng
        int newCorrectIndex = options.indexOf(correctAnswer);
        correctAnswerIndex = newCorrectIndex;
        
        return newCorrectIndex;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    public void setCorrectAnswerIndex(int correctAnswerIndex) {
        this.correctAnswerIndex = correctAnswerIndex;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
    
    /**
     * Thêm các phương án trả lời giả để đủ 4 phương án
     * @param allOptions Danh sách tất cả các phương án có thể
     */
    public void addFakeOptions(List<String> allOptions) {
        // Đảm bảo có đáp án đúng
        String correctOption = options.get(correctAnswerIndex);
        
        // Xóa các phương án hiện tại
        options.clear();
        
        // Thêm lại đáp án đúng
        options.add(correctOption);
        
        // Tạo danh sách tạm không chứa đáp án đúng
        List<String> tempOptions = new ArrayList<>(allOptions);
        tempOptions.remove(correctOption);
        
        // Trộn ngẫu nhiên
        Collections.shuffle(tempOptions);
        
        // Thêm 3 đáp án giả
        for (int i = 0; i < 3 && i < tempOptions.size(); i++) {
            options.add(tempOptions.get(i));
        }
        
        // Đảm bảo đáp án đúng ở vị trí 0
        correctAnswerIndex = 0;
        
        // Trộn lại các phương án
        shuffleOptions();
    }

    /**
     * Add a method to get the traffic sign ID associated with this question
     */
    public String getTrafficSignId() {
        // Simply return the ID field since we already have it
        return id;
    }
} 