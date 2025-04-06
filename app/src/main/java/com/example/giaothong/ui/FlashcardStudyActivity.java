package com.example.giaothong.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.example.giaothong.R;
import com.example.giaothong.adapter.FlashcardAdapter;
import com.example.giaothong.model.Flashcard;
import com.example.giaothong.model.FlashcardDeck;
import com.example.giaothong.utils.FlashcardDeckManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FlashcardStudyActivity extends AppCompatActivity {

    private static final String EXTRA_DECK_ID = "extra_deck_id";
    
    private Toolbar toolbar;
    private TextView textProgress;
    private ProgressBar progressBar;
    private ViewPager2 viewPagerCards;
    private Button buttonPrevious;
    private Button buttonNext;
    private Button buttonFlip;
    
    private FlashcardAdapter adapter;
    private FlashcardDeck deck;
    private List<Flashcard> flashcards;
    private int currentPosition = 0;
    private FlashcardDeckManager deckManager;
    
    /**
     * Tạo Intent để khởi động Activity này
     */
    public static Intent createIntent(Context context, long deckId) {
        Intent intent = new Intent(context, FlashcardStudyActivity.class);
        intent.putExtra(EXTRA_DECK_ID, deckId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flashcard_study_screen);
        
        // Khởi tạo FlashcardDeckManager
        deckManager = FlashcardDeckManager.getInstance(this);
        
        // Lấy deck ID từ intent
        long deckId = -1;
        if (getIntent().hasExtra(EXTRA_DECK_ID)) {
            deckId = getIntent().getLongExtra(EXTRA_DECK_ID, -1);
        }
        
        // Lấy deck từ ID
        if (deckId >= 0) {
            deck = deckManager.getDeckById(deckId);
        }
        
        if (deck == null || deck.getCards() == null || deck.getCards().isEmpty()) {
            Toast.makeText(this, "Không có thẻ nào trong bộ thẻ này", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Khởi tạo danh sách flashcard
        flashcards = new ArrayList<>(deck.getCards());
        
        // Nếu chưa có thẻ nào, tạo một số thẻ mẫu
        if (flashcards == null || flashcards.isEmpty()) {
            flashcards = createSampleFlashcards();
        }
        
        // Trộn ngẫu nhiên các thẻ nếu deck được đặt ở chế độ random
        if (deck.isRandomOrder()) {
            Collections.shuffle(flashcards);
        }
        
        // Khởi tạo các view
        initViews();
        
        // Thiết lập toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(deck.getName());
        }
        
        // Thiết lập adapter cho ViewPager
        adapter = new FlashcardAdapter(this, flashcards);
        adapter.setOnCardActionListener(new FlashcardAdapter.OnCardActionListener() {
            @Override
            public void onCardFlip(int position, boolean isFlipped) {
                updateButtonsState();
                
                // Cập nhật lần xem lại thẻ
                if (isFlipped && position < flashcards.size()) {
                    Flashcard card = flashcards.get(position);
                    card.incrementReviewCount();
                    card.setLastReviewed(System.currentTimeMillis());
                    card.calculateNextReviewTime();
                    
                    // Lưu lại thay đổi
                    deckManager.updateDeck(deck);
                }
            }
        });
        
        viewPagerCards.setAdapter(adapter);
        
        // Cập nhật thanh tiến trình
        updateProgress();
        
        // Thiết lập sự kiện cho các nút
        setupButtons();
        
        // Thiết lập sự kiện cho ViewPager
        viewPagerCards.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentPosition = position;
                updateProgress();
                updateButtonsState();
                
                // Đặt lại trạng thái thẻ khi chuyển trang
                adapter.resetCardState(position);
            }
        });
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        textProgress = findViewById(R.id.textProgress);
        progressBar = findViewById(R.id.progressBar);
        viewPagerCards = findViewById(R.id.viewPagerCards);
        buttonPrevious = findViewById(R.id.buttonPrevious);
        buttonNext = findViewById(R.id.buttonNext);
        buttonFlip = findViewById(R.id.buttonFlip);
    }
    
    private void setupButtons() {
        buttonPrevious.setOnClickListener(v -> {
            if (currentPosition > 0) {
                viewPagerCards.setCurrentItem(currentPosition - 1, true);
            }
        });
        
        buttonNext.setOnClickListener(v -> {
            moveToNextCard();
        });
        
        buttonFlip.setOnClickListener(v -> {
            adapter.flipCard(currentPosition);
        });
        
        // Cập nhật trạng thái nút ban đầu
        updateButtonsState();
    }
    
    private void moveToNextCard() {
        if (currentPosition < flashcards.size() - 1) {
            viewPagerCards.setCurrentItem(currentPosition + 1, true);
        } else {
            // Đã hoàn thành tất cả các thẻ
            Toast.makeText(this, R.string.study_complete, Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateProgress() {
        int total = flashcards.size();
        int current = currentPosition + 1;
        
        // Cập nhật text hiển thị tiến trình
        textProgress.setText(getString(R.string.card_progress, current, total));
        
        // Cập nhật thanh tiến trình
        progressBar.setMax(total);
        progressBar.setProgress(current);
    }
    
    private void updateButtonsState() {
        // Cập nhật trạng thái nút "Trước"
        buttonPrevious.setEnabled(currentPosition > 0);
        
        // Cập nhật trạng thái nút "Tiếp"
        buttonNext.setEnabled(currentPosition < flashcards.size() - 1);
    }
    
    /**
     * Tạo một số flashcard mẫu (chỉ sử dụng khi không có thẻ nào)
     */
    private List<Flashcard> createSampleFlashcards() {
        List<Flashcard> sampleCards = new ArrayList<>();
        
        Flashcard card1 = new Flashcard("Biển báo cấm đỗ xe có ý nghĩa gì?", 
                "Biển báo cấm đỗ xe là biển báo cấm để xe dừng lại trong một khoảng thời gian, không được phép đỗ xe tại khu vực có biển báo.");
        card1.setDeckId(deck.getId());
        
        Flashcard card2 = new Flashcard("Biển báo nguy hiểm đường giao nhau là gì?", 
                "Biển báo nguy hiểm đường giao nhau cảnh báo phía trước là nơi giao nhau của các tuyến đường cùng cấp, người lái xe phải đi chậm và nhường đường theo quy định.");
        card2.setDeckId(deck.getId());
        
        Flashcard card3 = new Flashcard("Biển báo tốc độ tối đa cho phép là gì?", 
                "Biển báo tốc độ tối đa cho phép là biển báo chỉ dẫn tốc độ cao nhất mà các phương tiện được phép chạy.");
        card3.setDeckId(deck.getId());
        
        sampleCards.add(card1);
        sampleCards.add(card2);
        sampleCards.add(card3);
        
        return sampleCards;
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Lưu lại trạng thái của bộ thẻ khi rời khỏi màn hình
        if (deckManager != null && deck != null) {
            deckManager.updateDeck(deck);
        }
    }
} 