package com.example.giaothong.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.giaothong.R;
import com.example.giaothong.adapter.FlashcardEditAdapter;
import com.example.giaothong.model.Flashcard;
import com.example.giaothong.model.FlashcardDeck;
import com.example.giaothong.utils.FlashcardDeckManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class FlashcardEditorActivity extends AppCompatActivity {

    private static final String EXTRA_DECK_ID = "deck_id";

    private Toolbar toolbar;
    private TextView textDeckName;
    private TextView textCardCount;
    private RecyclerView recyclerViewFlashcards;
    private TextView textEmptyState;
    private FloatingActionButton fabAddFlashcard;

    private FlashcardDeck deck;
    private FlashcardEditAdapter adapter;
    
    // Xử lý kết quả trả về khi chọn ảnh từ thư viện
    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        updateSelectedImage(selectedImageUri.toString());
                    }
                }
            });

    // Dialog hiện tại đang hiển thị (nếu có)
    private Dialog currentDialog;
    
    // Thẻ đang được chỉnh sửa (nếu có)
    private Flashcard currentEditingCard;
    
    // Vị trí của thẻ đang chỉnh sửa
    private int currentEditingPosition = -1;
    
    // Uri của hình ảnh đã chọn
    private String selectedImageUri;

    private FlashcardDeckManager deckManager;

    /**
     * Tạo intent để khởi động activity này
     */
    public static Intent createIntent(Context context, long deckId) {
        Intent intent = new Intent(context, FlashcardEditorActivity.class);
        intent.putExtra(EXTRA_DECK_ID, deckId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard_editor);

        // Khởi tạo views
        initViews();
        
        // Khởi tạo FlashcardDeckManager
        deckManager = FlashcardDeckManager.getInstance(this);

        // Lấy dữ liệu deck từ intent
        long deckId = -1;
        if (getIntent().hasExtra(EXTRA_DECK_ID)) {
            deckId = getIntent().getLongExtra(EXTRA_DECK_ID, -1);
        }
        
        // Lấy deck từ ID
        if (deckId >= 0) {
            deck = deckManager.getDeckById(deckId);
        }

        if (deck == null) {
            Toast.makeText(this, "Không tìm thấy bộ thẻ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Thiết lập toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.edit_flashcards);
        }

        // Hiển thị thông tin deck
        textDeckName.setText(deck.getName());
        updateCardCount();

        // Đảm bảo cards không null
        if (deck.getCards() == null) {
            deck.setCards(new ArrayList<>());
        }

        // Thiết lập RecyclerView và Adapter
        recyclerViewFlashcards.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FlashcardEditAdapter(this, deck.getCards());
        adapter.setOnFlashcardActionListener(new FlashcardEditAdapter.OnFlashcardActionListener() {
            @Override
            public void onEditClick(Flashcard flashcard, int position) {
                showEditFlashcardDialog(flashcard, position);
            }

            @Override
            public void onDeleteClick(Flashcard flashcard, int position) {
                confirmDeleteFlashcard(flashcard, position);
            }
        });
        recyclerViewFlashcards.setAdapter(adapter);

        // Thiết lập FAB
        fabAddFlashcard.setOnClickListener(v -> showEditFlashcardDialog(null, -1));

        // Cập nhật trạng thái UI
        updateEmptyState();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        textDeckName = findViewById(R.id.textDeckName);
        textCardCount = findViewById(R.id.textCardCount);
        recyclerViewFlashcards = findViewById(R.id.recyclerViewFlashcards);
        textEmptyState = findViewById(R.id.textEmptyState);
        fabAddFlashcard = findViewById(R.id.fabAddFlashcard);
    }

    private void updateCardCount() {
        if (deck != null) {
            deck.updateCardCount();
            textCardCount.setText(getString(R.string.cards_count, deck.getCardCount()));
        }
    }

    private void updateEmptyState() {
        if (deck.getCards() == null || deck.getCards().isEmpty()) {
            recyclerViewFlashcards.setVisibility(View.GONE);
            textEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerViewFlashcards.setVisibility(View.VISIBLE);
            textEmptyState.setVisibility(View.GONE);
        }
    }

    /**
     * Hiển thị dialog thêm/sửa thẻ
     * @param card Thẻ cần chỉnh sửa (null để thêm mới)
     * @param position Vị trí của thẻ trong danh sách (bỏ qua nếu thêm mới)
     */
    private void showEditFlashcardDialog(@Nullable Flashcard card, int position) {
        // Lưu lại thẻ và vị trí hiện tại đang chỉnh sửa
        currentEditingCard = card;
        currentEditingPosition = position;
        selectedImageUri = card != null ? card.getImageUrl() : null;

        final Dialog dialog = new Dialog(this);
        currentDialog = dialog;
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_flashcard);

        // Khởi tạo các view
        TextView textDialogTitle = dialog.findViewById(R.id.textDialogTitle);
        TextInputEditText editTextQuestion = dialog.findViewById(R.id.editTextQuestion);
        TextInputEditText editTextAnswer = dialog.findViewById(R.id.editTextAnswer);
        Button buttonSelectImage = dialog.findViewById(R.id.buttonSelectImage);
        ImageView imagePreview = dialog.findViewById(R.id.imagePreview);
        Button buttonRemoveImage = dialog.findViewById(R.id.buttonRemoveImage);
        Button buttonCancel = dialog.findViewById(R.id.buttonCancel);
        Button buttonSave = dialog.findViewById(R.id.buttonSave);

        // Thiết lập tiêu đề và dữ liệu nếu đang sửa thẻ
        if (card != null) {
            textDialogTitle.setText(R.string.edit_flashcard);
            editTextQuestion.setText(card.getQuestion());
            editTextAnswer.setText(card.getAnswer());

            if (!TextUtils.isEmpty(card.getImageUrl())) {
                imagePreview.setVisibility(View.VISIBLE);
                buttonRemoveImage.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .load(card.getImageUrl())
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_error)
                        .into(imagePreview);
            }
        }

        // Thiết lập sự kiện cho các nút
        buttonSelectImage.setOnClickListener(v -> openImagePicker());
        
        buttonRemoveImage.setOnClickListener(v -> {
            selectedImageUri = null;
            imagePreview.setVisibility(View.GONE);
            buttonRemoveImage.setVisibility(View.GONE);
        });
        
        buttonCancel.setOnClickListener(v -> dialog.dismiss());
        
        buttonSave.setOnClickListener(v -> {
            String question = editTextQuestion.getText() != null ? editTextQuestion.getText().toString().trim() : "";
            String answer = editTextAnswer.getText() != null ? editTextAnswer.getText().toString().trim() : "";
            
            // Kiểm tra dữ liệu hợp lệ
            if (TextUtils.isEmpty(question)) {
                editTextQuestion.setError(getString(R.string.question_required));
                return;
            }
            
            if (TextUtils.isEmpty(answer)) {
                editTextAnswer.setError(getString(R.string.answer_required));
                return;
            }
            
            // Lưu thẻ
            if (card == null) {
                // Thêm mới
                Flashcard newCard = new Flashcard(question, answer);
                newCard.setImageUrl(selectedImageUri);
                deck.addCard(newCard);
                
                // Cập nhật UI
                adapter.notifyItemInserted(deck.getCards().size() - 1);
                
                // Lưu thay đổi
                deckManager.updateDeck(deck);
            } else {
                // Cập nhật
                card.setQuestion(question);
                card.setAnswer(answer);
                card.setImageUrl(selectedImageUri);
                
                // Cập nhật UI
                if (position >= 0) {
                    adapter.notifyItemChanged(position);
                }
                
                // Lưu thay đổi
                deckManager.updateDeck(deck);
            }
            
            // Cập nhật số lượng thẻ
            updateCardCount();
            
            // Cập nhật trạng thái rỗng
            updateEmptyState();
            
            // Đóng dialog
            dialog.dismiss();
        });
        
        // Hiển thị dialog
        dialog.show();
        
        // Thiết lập kích thước dialog
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
    
    /**
     * Mở trình chọn ảnh
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }
    
    /**
     * Cập nhật hình ảnh đã chọn
     */
    private void updateSelectedImage(String uri) {
        if (currentDialog == null) return;
        
        selectedImageUri = uri;
        
        ImageView imagePreview = currentDialog.findViewById(R.id.imagePreview);
        Button buttonRemoveImage = currentDialog.findViewById(R.id.buttonRemoveImage);
        
        if (imagePreview != null && buttonRemoveImage != null) {
            imagePreview.setVisibility(View.VISIBLE);
            buttonRemoveImage.setVisibility(View.VISIBLE);
            
            Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_error)
                    .into(imagePreview);
        }
    }
    
    /**
     * Xác nhận xóa thẻ
     */
    private void confirmDeleteFlashcard(Flashcard card, int position) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete)
                .setMessage(R.string.confirm_delete_flashcard)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    // Xóa thẻ khỏi bộ
                    deck.removeCard(card);
                    
                    // Cập nhật UI
                    adapter.notifyItemRemoved(position);
                    updateCardCount();
                    updateEmptyState();
                    
                    // Lưu thay đổi
                    deckManager.updateDeck(deck);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
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
    public void onBackPressed() {
        // Lưu thay đổi vào bộ thẻ trước khi quay lại
        if (deckManager != null && deck != null) {
            deckManager.updateDeck(deck);
        }
        super.onBackPressed();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Lưu thay đổi khi rời khỏi activity
        if (deckManager != null && deck != null) {
            deckManager.updateDeck(deck);
        }
    }
} 