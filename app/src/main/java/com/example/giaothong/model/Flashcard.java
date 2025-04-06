package com.example.giaothong.model;

import java.io.Serializable;

/**
 * Model dữ liệu cho một thẻ ghi nhớ
 */
public class Flashcard implements Serializable {
    private long id;
    private long deckId;
    private String question; // Mặt trước - câu hỏi
    private String answer;   // Mặt sau - câu trả lời
    private String imageUrl; // Đường dẫn hình ảnh (nếu có)
    private int difficulty;  // Mức độ khó (1-5)
    private int reviewCount;   // Số lần đã xem lại
    private long lastReviewed; // Thời gian lần cuối xem lại
    private long nextReviewTime;

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

    public long getDeckId() {
        return deckId;
    }

    public void setDeckId(long deckId) {
        this.deckId = deckId;
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
        if (difficulty >= 1 && difficulty <= 5) {
            this.difficulty = difficulty;
        }
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

    public long getLastReviewed() {
        return lastReviewed;
    }

    public void setLastReviewed(long lastReviewed) {
        this.lastReviewed = lastReviewed;
    }

    public long getNextReviewTime() {
        return nextReviewTime;
    }

    public void setNextReviewTime(long nextReviewTime) {
        this.nextReviewTime = nextReviewTime;
    }

    /**
     * Tính toán thời gian xem lại tiếp theo dựa trên thuật toán spaced repetition đơn giản
     * Công thức: nextReviewTime = lastReviewed + (factor * 24 * 60 * 60 * 1000)
     * Trong đó factor phụ thuộc vào độ khó và số lần xem lại.
     */
    public void calculateNextReviewTime() {
        if (lastReviewed == 0) {
            // Chưa từng xem lại, đặt thời gian xem lại ngay lập tức
            this.nextReviewTime = System.currentTimeMillis();
            return;
        }
        
        // Hệ số cơ bản dựa trên độ khó (khó hơn = xem lại sớm hơn)
        double factor = 5.0 - difficulty; // 1.0 - 4.0
        
        // Tăng hệ số dựa trên số lần xem lại (xem nhiều hơn = khoảng cách lâu hơn)
        factor = factor + Math.min(reviewCount, 10) * 0.5; // Tối đa +5.0
        
        // Tính thời gian xem lại (factor ngày sau thời điểm cuối cùng xem lại)
        long delayMillis = (long) (factor * 24 * 60 * 60 * 1000);
        this.nextReviewTime = lastReviewed + delayMillis;
    }
} 