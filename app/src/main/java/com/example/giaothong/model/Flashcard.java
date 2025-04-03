package com.example.giaothong.model;

import java.io.Serializable;

/**
 * Model dữ liệu cho một thẻ ghi nhớ
 */
public class Flashcard implements Serializable {
    private long id;
    private String question; // Mặt trước - câu hỏi
    private String answer;   // Mặt sau - câu trả lời
    private String imageUrl; // Đường dẫn hình ảnh (nếu có)
    private int difficulty;  // Mức độ khó (1-5)
    private long lastReviewed; // Thời gian lần cuối xem lại
    private int reviewCount;   // Số lần đã xem lại
    private long deckId;       // ID của bộ flashcard

    public Flashcard() {
        this.lastReviewed = System.currentTimeMillis();
        this.reviewCount = 0;
        this.difficulty = 3; // Mức độ khó mặc định
    }

    public Flashcard(String question, String answer) {
        this();
        this.question = question;
        this.answer = answer;
    }

    public Flashcard(String question, String answer, String imageUrl) {
        this(question, answer);
        this.imageUrl = imageUrl;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        if (difficulty < 1) difficulty = 1;
        if (difficulty > 5) difficulty = 5;
        this.difficulty = difficulty;
    }

    public long getLastReviewed() {
        return lastReviewed;
    }

    public void setLastReviewed(long lastReviewed) {
        this.lastReviewed = lastReviewed;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public void incrementReviewCount() {
        this.reviewCount++;
    }

    public long getDeckId() {
        return deckId;
    }

    public void setDeckId(long deckId) {
        this.deckId = deckId;
    }
} 