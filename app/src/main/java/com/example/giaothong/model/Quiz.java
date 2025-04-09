package com.example.giaothong.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lớp đại diện cho một bài trắc nghiệm
 */
public class Quiz implements Serializable {
    private static final int DEFAULT_QUIZ_SIZE = 10;
    
    private String id;
    private String title;
    private String description;
    private List<QuizQuestion> questions;
    private int currentQuestionIndex;
    private int correctAnswers;
    private boolean isCompleted;
    private int[] userAnswers;
    
    public Quiz(String id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.questions = new ArrayList<>();
        this.currentQuestionIndex = 0;
        this.correctAnswers = 0;
        this.isCompleted = false;
        this.userAnswers = null; // Sẽ được khởi tạo khi có câu hỏi
    }
    
    /**
     * Tạo một bài trắc nghiệm từ danh sách biển báo
     * @param title Tiêu đề của bài
     * @param description Mô tả
     * @param signs Danh sách biển báo
     * @param numberOfQuestions Số lượng câu hỏi
     * @return Bài trắc nghiệm đã tạo
     */
    public static Quiz createFromTrafficSigns(String title, String description, 
                                               List<TrafficSign> signs, int numberOfQuestions) {
        // Nếu số lượng yêu cầu lớn hơn số lượng thực tế, lấy số lượng tối đa có thể
        int quizSize = Math.min(numberOfQuestions, signs.size());
        
        // Tạo ID duy nhất dựa trên thời gian
        String quizId = "quiz_" + System.currentTimeMillis();
        
        Quiz quiz = new Quiz(quizId, title, description);
        
        // Trộn danh sách biển báo để lấy ngẫu nhiên
        List<TrafficSign> shuffledSigns = new ArrayList<>(signs);
        Collections.shuffle(shuffledSigns);
        
        // Lấy danh sách tên tất cả các biển báo để làm đáp án nhiễu
        List<String> allSignNames = new ArrayList<>();
        for (TrafficSign sign : signs) {
            allSignNames.add(sign.getName());
        }
        
        // Tạo các câu hỏi từ biển báo
        for (int i = 0; i < quizSize; i++) {
            TrafficSign sign = shuffledSigns.get(i);
            QuizQuestion question = QuizQuestion.fromTrafficSign(sign);
            
            // Thêm các đáp án giả
            question.addFakeOptions(allSignNames);
            
            quiz.addQuestion(question);
        }
        
        return quiz;
    }
    
    /**
     * Tạo một bài trắc nghiệm mặc định (10 câu)
     */
    public static Quiz createFromTrafficSigns(String title, String description, 
                                               List<TrafficSign> signs) {
        return createFromTrafficSigns(title, description, signs, DEFAULT_QUIZ_SIZE);
    }
    
    /**
     * Tạo một bài trắc nghiệm từ danh sách biển báo theo danh mục
     * @param title Tiêu đề của bài
     * @param description Mô tả
     * @param signs Danh sách tất cả biển báo
     * @param category Danh mục biển báo (ví dụ: "P.", "W.", "I.")
     * @param categoryName Tên hiển thị của danh mục
     * @param numberOfQuestions Số lượng câu hỏi mong muốn
     * @return Bài trắc nghiệm đã tạo hoặc null nếu không có đủ biển báo trong danh mục
     */
    public static Quiz createFromCategory(String title, String description,
                                          List<TrafficSign> signs, String category,
                                          String categoryName, int numberOfQuestions) {
        if (signs == null || signs.isEmpty()) {
            return null;
        }
        
        // Map mã danh mục tới tiền tố ID thực tế
        String prefix;
        switch (category) {
            case "P.": // Biển báo cấm
                prefix = "bien_bao_cam";
                break;
            case "W.": // Biển báo nguy hiểm
                prefix = "bien_nguy_hiem_va_canh_bao";
                break;
            case "I.": // Biển báo chỉ dẫn
                prefix = "bien_chi_dan";
                break;
            case "R.": // Biển báo hiệu lệnh
                prefix = "bien_hieu_lenh";
                break;
            case "S.": // Biển báo phụ
                prefix = "bien_phu";
                break;
            default:
                return null; // Trả về null nếu danh mục không hợp lệ
        }
        
        // Lọc các biển báo thuộc danh mục
        List<TrafficSign> categorySigns = new ArrayList<>();
        for (TrafficSign sign : signs) {
            if (sign.getId().startsWith(prefix)) {
                categorySigns.add(sign);
            }
        }
        
        // Nếu không có đủ biển báo thuộc danh mục
        if (categorySigns.size() < 4) { // Cần ít nhất 4 biển để tạo câu hỏi với 3 đáp án nhiễu
            return null;
        }
        
        // Giới hạn số lượng câu hỏi nếu nhiều hơn số biển báo có sẵn
        int quizSize = Math.min(numberOfQuestions, categorySigns.size());
        
        // Tạo ID duy nhất dựa trên thời gian và danh mục
        String quizId = "quiz_" + category + "_" + System.currentTimeMillis();
        
        // Tên mô tả cho bài trắc nghiệm
        String quizTitle = title + " - " + categoryName;
        
        Quiz quiz = new Quiz(quizId, quizTitle, description);
        
        // Trộn danh sách biển báo để lấy ngẫu nhiên
        List<TrafficSign> shuffledSigns = new ArrayList<>(categorySigns);
        Collections.shuffle(shuffledSigns);
        
        // Lấy danh sách tên tất cả các biển báo thuộc danh mục để làm đáp án nhiễu
        List<String> allCategorySignNames = new ArrayList<>();
        for (TrafficSign sign : categorySigns) {
            allCategorySignNames.add(sign.getName());
        }
        
        // Tạo các câu hỏi từ biển báo
        for (int i = 0; i < quizSize; i++) {
            TrafficSign sign = shuffledSigns.get(i);
            QuizQuestion question = QuizQuestion.fromTrafficSign(sign);
            
            // Thêm các đáp án giả từ danh sách biển báo cùng danh mục
            question.addFakeOptions(allCategorySignNames);
            
            quiz.addQuestion(question);
        }
        
        return quiz;
    }
    
    /**
     * Tạo một bài trắc nghiệm từ danh sách biển báo theo danh mục với kích thước mặc định (10 câu)
     */
    public static Quiz createFromCategory(String title, String description,
                                          List<TrafficSign> signs, String category,
                                          String categoryName) {
        return createFromCategory(title, description, signs, category, categoryName, DEFAULT_QUIZ_SIZE);
    }
    
    /**
     * Thêm câu hỏi vào bài trắc nghiệm
     * @param question Câu hỏi cần thêm
     */
    public void addQuestion(QuizQuestion question) {
        questions.add(question);
        // Khởi tạo lại mảng userAnswers mỗi khi thêm câu hỏi
        initUserAnswers();
    }
    
    /**
     * Trả về câu hỏi hiện tại
     * @return Câu hỏi hiện tại hoặc null nếu không có
     */
    public QuizQuestion getCurrentQuestion() {
        if (currentQuestionIndex < 0 || currentQuestionIndex >= questions.size()) {
            return null;
        }
        return questions.get(currentQuestionIndex);
    }
    
    /**
     * Chuyển tới câu hỏi tiếp theo
     * @return true nếu có câu hỏi tiếp theo, false nếu đã hết
     */
    public boolean moveToNextQuestion() {
        if (currentQuestionIndex < questions.size() - 1) {
            currentQuestionIndex++;
            return true;
        }
        return false;
    }
    
    /**
     * Quay lại câu hỏi trước đó
     * @return true nếu có câu hỏi trước đó, false nếu đang ở câu đầu tiên
     */
    public boolean moveToPreviousQuestion() {
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--;
            return true;
        }
        return false;
    }
    
    /**
     * Khởi tạo mảng lưu câu trả lời của người dùng
     */
    public void initUserAnswers() {
        if (questions != null) {
            userAnswers = new int[questions.size()];
            // Khởi tạo với giá trị -1 (chưa trả lời)
            for (int i = 0; i < userAnswers.length; i++) {
                userAnswers[i] = -1;
            }
        }
    }
    
    /**
     * Kiểm tra câu trả lời
     * @param answerIndex Chỉ số của câu trả lời người dùng chọn
     * @return true nếu câu trả lời đúng, false nếu sai
     */
    public boolean checkAnswer(int answerIndex) {
        QuizQuestion currentQuestion = getCurrentQuestion();
        if (currentQuestion == null) {
            return false;
        }
        
        // Đảm bảo mảng userAnswers đã được khởi tạo
        if (userAnswers == null) {
            initUserAnswers();
        }
        
        // Lưu câu trả lời của người dùng
        userAnswers[currentQuestionIndex] = answerIndex;
        
        // Kiểm tra đáp án
        boolean isCorrect = (answerIndex == currentQuestion.getCorrectAnswerIndex());
        
        // Chỉ tăng số câu đúng nếu câu này chưa được trả lời đúng trước đó
        // (tránh tăng nhiều lần nếu người dùng quay lại câu hỏi)
        if (isCorrect && !isAnswerCounted(currentQuestionIndex)) {
            correctAnswers++;
            markAnswerCounted(currentQuestionIndex);
        }
        
        return isCorrect;
    }
    
    // Mảng để đánh dấu câu hỏi đã được tính điểm chưa
    private boolean[] answeredCounted;
    
    // Kiểm tra xem câu hỏi đã được tính điểm chưa
    private boolean isAnswerCounted(int index) {
        if (answeredCounted == null) {
            answeredCounted = new boolean[questions.size()];
        }
        return answeredCounted[index];
    }
    
    // Đánh dấu câu hỏi đã được tính điểm
    private void markAnswerCounted(int index) {
        if (answeredCounted == null) {
            answeredCounted = new boolean[questions.size()];
        }
        answeredCounted[index] = true;
    }
    
    /**
     * Hoàn thành bài trắc nghiệm
     */
    public void complete() {
        isCompleted = true;
    }
    
    /**
     * Tính điểm của bài trắc nghiệm
     * @return Điểm số (0-10)
     */
    public float calculateScore() {
        if (questions.isEmpty()) {
            return 0;
        }
        return (float) correctAnswers * 10 / questions.size();
    }
    
    /**
     * Lấy mảng các câu trả lời của người dùng
     * @return Mảng chứa chỉ số câu trả lời người dùng đã chọn, -1 nếu chưa trả lời
     */
    public int[] getUserAnswers() {
        if (userAnswers == null) {
            initUserAnswers();
        }
        return userAnswers;
    }
    
    /**
     * Kiểm tra người dùng đã trả lời câu hỏi chưa
     * @param questionIndex Chỉ số câu hỏi cần kiểm tra
     * @return true nếu đã trả lời, false nếu chưa
     */
    public boolean isQuestionAnswered(int questionIndex) {
        if (userAnswers == null || questionIndex < 0 || questionIndex >= userAnswers.length) {
            return false;
        }
        return userAnswers[questionIndex] != -1;
    }
    
    /**
     * Kiểm tra câu trả lời của người dùng có đúng không
     * @param questionIndex Chỉ số câu hỏi cần kiểm tra
     * @return true nếu đúng, false nếu sai hoặc chưa trả lời
     */
    public boolean isAnswerCorrect(int questionIndex) {
        if (!isQuestionAnswered(questionIndex) || questionIndex < 0 || questionIndex >= questions.size()) {
            return false;
        }
        
        QuizQuestion question = questions.get(questionIndex);
        return userAnswers[questionIndex] == question.getCorrectAnswerIndex();
    }
    
    // Getters và Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<QuizQuestion> getQuestions() {
        return questions;
    }
    
    public void setQuestions(List<QuizQuestion> questions) {
        this.questions = questions;
    }
    
    public int getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }
    
    public void setCurrentQuestionIndex(int currentQuestionIndex) {
        this.currentQuestionIndex = currentQuestionIndex;
    }
    
    public int getCorrectAnswers() {
        return correctAnswers;
    }
    
    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }
    
    public boolean isCompleted() {
        return isCompleted;
    }
    
    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
    
    /**
     * Lấy tổng số câu hỏi
     * @return Số lượng câu hỏi
     */
    public int getQuestionCount() {
        return questions != null ? questions.size() : 0;
    }
    
    /**
     * Lấy tiến độ hiện tại
     * @return Tiến độ dạng chuỗi "x/y"
     */
    public String getProgress() {
        return (currentQuestionIndex + 1) + "/" + getQuestionCount();
    }
} 