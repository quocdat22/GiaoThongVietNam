package com.example.giaothong.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.giaothong.model.Flashcard;
import com.example.giaothong.model.FlashcardDeck;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Quản lý lưu trữ và truy xuất các bộ thẻ ghi nhớ
 */
public class FlashcardDeckManager {
    private static final String TAG = "FlashcardDeckManager";
    private static final String PREF_NAME = "flashcard_prefs";
    private static final String KEY_NEXT_DECK_ID = "next_deck_id";
    private static final String KEY_LAST_UPDATED = "last_updated";
    private static final String DECKS_FILE_NAME = "flashcard_decks.json";
    
    private static FlashcardDeckManager instance;
    private final Context context;
    private final SharedPreferences prefs;
    private final Gson gson;
    private final ExecutorService executor;
    
    private List<FlashcardDeck> flashcardDecks;
    private long nextDeckId;
    
    private FlashcardDeckManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.executor = Executors.newSingleThreadExecutor();
        this.nextDeckId = prefs.getLong(KEY_NEXT_DECK_ID, 1);
        
        // Tải dữ liệu khi khởi tạo
        loadDecks();
    }
    
    /**
     * Lấy instance của FlashcardDeckManager (Singleton)
     */
    public static synchronized FlashcardDeckManager getInstance(Context context) {
        if (instance == null) {
            instance = new FlashcardDeckManager(context);
        }
        return instance;
    }
    
    /**
     * Tải danh sách bộ thẻ từ bộ nhớ
     */
    private void loadDecks() {
        Log.d(TAG, "Đang tải danh sách bộ thẻ ghi nhớ từ bộ nhớ");
        
        // Khởi tạo với danh sách trống
        flashcardDecks = new ArrayList<>();
        
        try {
            File file = new File(context.getFilesDir(), DECKS_FILE_NAME);
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                byte[] data = new byte[(int) file.length()];
                fis.read(data);
                fis.close();
                
                String json = new String(data, "UTF-8");
                Type listType = new TypeToken<ArrayList<FlashcardDeck>>(){}.getType();
                List<FlashcardDeck> loadedDecks = gson.fromJson(json, listType);
                
                if (loadedDecks != null) {
                    flashcardDecks = loadedDecks;
                    Log.d(TAG, "Đã tải " + flashcardDecks.size() + " bộ thẻ ghi nhớ");
                }
            } else {
                Log.d(TAG, "Không tìm thấy file dữ liệu bộ thẻ ghi nhớ");
            }
        } catch (IOException e) {
            Log.e(TAG, "Lỗi khi tải bộ thẻ ghi nhớ: " + e.getMessage(), e);
        }
    }
    
    /**
     * Lưu danh sách bộ thẻ vào bộ nhớ
     */
    public void saveDecks() {
        Log.d(TAG, "Đang lưu " + flashcardDecks.size() + " bộ thẻ ghi nhớ");
        
        // Thực hiện lưu trong background thread
        executor.execute(() -> {
            try {
                String json = gson.toJson(flashcardDecks);
                File file = new File(context.getFilesDir(), DECKS_FILE_NAME);
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(json.getBytes("UTF-8"));
                fos.close();
                
                // Cập nhật thời gian sửa đổi cuối cùng
                prefs.edit()
                     .putLong(KEY_LAST_UPDATED, System.currentTimeMillis())
                     .putLong(KEY_NEXT_DECK_ID, nextDeckId)
                     .apply();
                
                Log.d(TAG, "Đã lưu thành công bộ thẻ ghi nhớ");
            } catch (IOException e) {
                Log.e(TAG, "Lỗi khi lưu bộ thẻ ghi nhớ: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Lấy danh sách tất cả các bộ thẻ
     */
    public List<FlashcardDeck> getAllDecks() {
        return flashcardDecks;
    }
    
    /**
     * Lấy bộ thẻ theo ID
     */
    public FlashcardDeck getDeckById(long id) {
        for (FlashcardDeck deck : flashcardDecks) {
            if (deck.getId() == id) {
                return deck;
            }
        }
        return null;
    }
    
    /**
     * Thêm bộ thẻ mới
     * @return ID của bộ thẻ mới
     */
    public long addDeck(FlashcardDeck deck) {
        // Đặt ID nếu chưa có
        if (deck.getId() <= 0) {
            deck.setId(nextDeckId++);
        }
        
        flashcardDecks.add(0, deck); // Thêm vào đầu danh sách
        saveDecks();
        return deck.getId();
    }
    
    /**
     * Cập nhật bộ thẻ
     * @return true nếu cập nhật thành công
     */
    public boolean updateDeck(FlashcardDeck updatedDeck) {
        for (int i = 0; i < flashcardDecks.size(); i++) {
            if (flashcardDecks.get(i).getId() == updatedDeck.getId()) {
                flashcardDecks.set(i, updatedDeck);
                saveDecks();
                return true;
            }
        }
        return false;
    }
    
    /**
     * Xoá bộ thẻ theo ID
     * @return true nếu xoá thành công
     */
    public boolean deleteDeck(long deckId) {
        for (int i = 0; i < flashcardDecks.size(); i++) {
            if (flashcardDecks.get(i).getId() == deckId) {
                flashcardDecks.remove(i);
                saveDecks();
                return true;
            }
        }
        return false;
    }
    
    /**
     * Lấy thời gian cập nhật cuối cùng
     */
    public long getLastUpdatedTime() {
        return prefs.getLong(KEY_LAST_UPDATED, 0);
    }
    
    /**
     * Thêm Flashcard vào bộ thẻ
     */
    public boolean addCardToDeck(long deckId, Flashcard card) {
        FlashcardDeck deck = getDeckById(deckId);
        if (deck != null) {
            deck.addCard(card);
            saveDecks();
            return true;
        }
        return false;
    }
    
    /**
     * Xoá Flashcard khỏi bộ thẻ
     */
    public boolean removeCardFromDeck(long deckId, int cardPosition) {
        FlashcardDeck deck = getDeckById(deckId);
        if (deck != null && deck.removeCard(cardPosition) != null) {
            saveDecks();
            return true;
        }
        return false;
    }
    
    /**
     * Cập nhật một thẻ ghi nhớ trong bộ thẻ
     */
    public boolean updateCardInDeck(long deckId, int cardPosition, Flashcard updatedCard) {
        FlashcardDeck deck = getDeckById(deckId);
        if (deck != null) {
            boolean success = deck.updateCard(cardPosition, updatedCard);
            if (success) {
                saveDecks();
            }
            return success;
        }
        return false;
    }
    
    /**
     * Tạo bản sao của một bộ thẻ
     */
    public long duplicateDeck(long deckId) {
        FlashcardDeck originalDeck = getDeckById(deckId);
        if (originalDeck != null) {
            FlashcardDeck newDeck = new FlashcardDeck(
                    originalDeck.getName() + " (bản sao)",
                    originalDeck.getDescription(),
                    originalDeck.getCategory()
            );
            
            // Sao chép các thẻ
            for (Flashcard card : originalDeck.getCards()) {
                Flashcard copyCard = new Flashcard(card.getQuestion(), card.getAnswer(), card.getImageUrl());
                copyCard.setDifficulty(card.getDifficulty());
                newDeck.addCard(copyCard);
            }
            
            // Thêm vào danh sách
            return addDeck(newDeck);
        }
        return -1;
    }
} 