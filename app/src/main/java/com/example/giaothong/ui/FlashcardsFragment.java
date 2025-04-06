package com.example.giaothong.ui;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.giaothong.R;
import com.example.giaothong.adapter.FlashcardDeckAdapter;
import com.example.giaothong.model.Flashcard;
import com.example.giaothong.model.FlashcardDeck;
import com.example.giaothong.model.TrafficSign;
import com.example.giaothong.utils.DataManager;
import com.example.giaothong.utils.FlashcardDeckManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class FlashcardsFragment extends Fragment {

    private RecyclerView recyclerViewDecks;
    private FlashcardDeckAdapter adapter;
    private TextView textEmptyState;
    private FloatingActionButton fabAddDeck;
    
    private List<FlashcardDeck> trafficSignDecks;
    private FlashcardDeckManager deckManager;

    public FlashcardsFragment() {
        // Required empty public constructor
    }

    public static FlashcardsFragment newInstance() {
        return new FlashcardsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_flashcards, container, false);
        
        // Khởi tạo view ngay từ đầu
        recyclerViewDecks = view.findViewById(R.id.recyclerViewDecks);
        textEmptyState = view.findViewById(R.id.textEmptyState);
        fabAddDeck = view.findViewById(R.id.fabAddDeck);
        
        // Khởi tạo FlashcardDeckManager
        deckManager = FlashcardDeckManager.getInstance(getContext());
        
        // Lấy danh sách bộ thẻ đã lưu
        trafficSignDecks = deckManager.getAllDecks();
        
        // Nếu chưa có dữ liệu, khởi tạo danh sách trống
        if (trafficSignDecks == null) {
            trafficSignDecks = new ArrayList<>();
        }
        
        recyclerViewDecks.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FlashcardDeckAdapter(getContext(), trafficSignDecks);
        recyclerViewDecks.setAdapter(adapter);
        
        fabAddDeck.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(getContext(), v);
            popupMenu.getMenu().add(getString(R.string.create_random_deck));
            popupMenu.getMenu().add(getString(R.string.create_custom_deck));
            
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals(getString(R.string.create_random_deck))) {
                    createRandomFlashcardDeck();
                    return true;
                } else if (item.getTitle().equals(getString(R.string.create_custom_deck))) {
                    showCreateCustomDeckDialog();
                    return true;
                }
                return false;
            });
            
            popupMenu.show();
        });
        
        // Đăng ký lắng nghe thay đổi dữ liệu từ DataManager
        DataManager.getInstance(getContext()).registerDataListener(this::onTrafficSignsLoaded);
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Views đã được khởi tạo trong onCreateView, không cần khởi tạo lại
        
        // Setup adapter nếu chưa setup
        if (adapter == null) {
            adapter = new FlashcardDeckAdapter(requireContext(), trafficSignDecks);
        }
        adapter.setOnItemClickListener(this::onDeckClicked);
        adapter.setOnItemLongClickListener(this::onDeckLongClicked);
        
        // Đảm bảo recyclerView có adapter
        if (recyclerViewDecks.getAdapter() == null) {
            recyclerViewDecks.setAdapter(adapter);
        }
        
        // Kiểm tra dữ liệu biển báo có sẵn để tạo bộ thẻ
        List<TrafficSign> existingSigns = DataManager.getInstance(requireContext()).getAllTrafficSigns();
        if (existingSigns != null && !existingSigns.isEmpty() && trafficSignDecks.isEmpty()) {
            Log.d("FlashcardsFragment", "Đã có dữ liệu biển báo sẵn có, tạo bộ thẻ mẫu");
            setupSampleData();
        } else {
            Log.d("FlashcardsFragment", "Đã có " + trafficSignDecks.size() + " bộ thẻ hoặc chưa có dữ liệu biển báo");
            // Hiển thị trạng thái rỗng ban đầu
            updateEmptyState();
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Hủy đăng ký listener khi fragment bị hủy
        DataManager.getInstance(requireContext()).unregisterDataListener(this::onTrafficSignsLoaded);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        Log.d("FlashcardsFragment", "onResume: làm mới dữ liệu");
        
        // Làm mới dữ liệu từ API khi Fragment được resume
        if (trafficSignDecks.isEmpty()) {
            // Nếu chưa có dữ liệu, thử làm mới từ API
            DataManager.getInstance(requireContext()).refreshTrafficSigns();
        }
    }
    
    /**
     * Xử lý khi dữ liệu biển báo được tải
     */
    private void onTrafficSignsLoaded(List<TrafficSign> trafficSigns) {
        Log.d("FlashcardsFragment", "Nhận dữ liệu biển báo: " + 
              (trafficSigns != null ? trafficSigns.size() : 0) + " biển báo");
        
        if (trafficSigns != null && !trafficSigns.isEmpty()) {
            // Nếu chưa có bộ thẻ nào thì tạo mới
            if (trafficSignDecks.isEmpty()) {
                setupSampleData();
            }
            
            // Cập nhật UI
            updateEmptyState();
            adapter.notifyDataSetChanged();
        }
    }
    
    /**
     * Tạo bộ thẻ ngẫu nhiên từ dữ liệu biển báo
     */
    private void createRandomFlashcardDeck() {
        // Lấy danh sách biển báo từ DataManager
        List<TrafficSign> allSigns = DataManager.getInstance(requireContext()).getAllTrafficSigns();
        
        if (allSigns == null || allSigns.isEmpty()) {
            Toast.makeText(requireContext(), "Không có dữ liệu biển báo", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Tạo bản sao và trộn ngẫu nhiên
        List<TrafficSign> shuffledSigns = new ArrayList<>(allSigns);
        Collections.shuffle(shuffledSigns, new Random(System.currentTimeMillis()));
        
        // Lấy tối đa 20 biển báo
        int cardCount = Math.min(20, shuffledSigns.size());
        List<TrafficSign> selectedSigns = shuffledSigns.subList(0, cardCount);
        
        // Tạo bộ thẻ mới
        String deckName = "Biển báo ngẫu nhiên " + (trafficSignDecks.size() + 1);
        FlashcardDeck newDeck = new FlashcardDeck(deckName, 
                "Bộ thẻ ngẫu nhiên được tạo vào " + new Date().toString(), 
                "Biển báo");
        
        newDeck.setRandomOrder(true);
        
        // Tạo các thẻ từ biển báo
        for (TrafficSign sign : selectedSigns) {
            // Tạo câu hỏi và câu trả lời
            String question = "Đây là biển báo gì?";
            String answer = sign.getName() + "\n\n" + sign.getDescription();
            
            // Tạo flashcard mới
            Flashcard card = new Flashcard(question, answer, sign.getImageUrl());
            
            // Thêm vào bộ thẻ
            newDeck.addCard(card);
        }
        
        // Thêm vào danh sách và lưu
        long newDeckId = deckManager.addDeck(newDeck);
        
        // Cập nhật giao diện
        adapter.notifyItemInserted(0);
        updateEmptyState();
        
        // Thông báo
        Toast.makeText(requireContext(), "Đã tạo bộ thẻ ngẫu nhiên với " + cardCount + " biển báo", Toast.LENGTH_SHORT).show();
    }
    
    private void setupSampleData() {
        // Kiểm tra nếu đã có dữ liệu, thì không cần tạo lại
        if (!trafficSignDecks.isEmpty()) {
            Log.d("FlashcardsFragment", "Đã có " + trafficSignDecks.size() + " bộ thẻ, không tạo lại");
            adapter.notifyDataSetChanged();
            return;
        }
        
        Log.d("FlashcardsFragment", "Tạo dữ liệu mẫu cho bộ thẻ");
        List<TrafficSign> allSigns = DataManager.getInstance(requireContext()).getAllTrafficSigns();
        
        if (allSigns == null || allSigns.isEmpty()) {
            Log.e("FlashcardsFragment", "Không có dữ liệu biển báo để tạo bộ thẻ");
            return;
        }
        
        Log.d("FlashcardsFragment", "Số lượng biển báo: " + allSigns.size());
        
        // Tạo một bộ thẻ với tất cả các biển báo
        FlashcardDeck allSignsDeck = new FlashcardDeck("Tất cả biển báo", 
                "Bộ thẻ chứa tất cả biển báo giao thông", "Biển báo");
        
        // Tạo flashcard cho từng biển báo
        for (TrafficSign sign : allSigns) {
            String question = "Đây là biển báo gì?";
            String answer = sign.getName() + "\n\n" + sign.getDescription();
            
            Flashcard card = new Flashcard(question, answer, sign.getImageUrl());
            allSignsDeck.addCard(card);
        }
        
        // Thêm vào FlashcardDeckManager để lưu
        deckManager.addDeck(allSignsDeck);
        
        // Tạo các bộ thẻ cho từng loại biển báo
        createCategoryDeck("P.", "Biển báo cấm", "Bộ thẻ chứa các biển báo cấm");
        createCategoryDeck("W.", "Biển báo nguy hiểm", "Bộ thẻ chứa các biển báo nguy hiểm");
        createCategoryDeck("I.", "Biển báo chỉ dẫn", "Bộ thẻ chứa các biển báo chỉ dẫn");
        
        // Cập nhật adapter
        trafficSignDecks = deckManager.getAllDecks();
        adapter.updateData(trafficSignDecks);
        
        // Cập nhật trạng thái rỗng
        updateEmptyState();
    }
    
    private void createCategoryDeck(String categoryPrefix, String name, String description) {
        List<TrafficSign> allSigns = DataManager.getInstance(requireContext()).getAllTrafficSigns();
        if (allSigns == null || allSigns.isEmpty()) {
            return;
        }
        
        // Lọc biển báo theo danh mục
        List<TrafficSign> categorySigns = new ArrayList<>();
        for (TrafficSign sign : allSigns) {
            if (sign.getId().startsWith(categoryPrefix)) {
                categorySigns.add(sign);
            }
        }
        
        if (categorySigns.isEmpty()) {
            return;
        }
        
        // Tạo bộ thẻ mới
        FlashcardDeck deck = new FlashcardDeck(name, description, "Biển báo");
        
        // Thêm các flashcard
        for (TrafficSign sign : categorySigns) {
            String question = "Đây là biển báo gì?";
            String answer = sign.getName() + "\n\n" + sign.getDescription();
            
            Flashcard card = new Flashcard(question, answer, sign.getImageUrl());
            deck.addCard(card);
        }
        
        // Lưu bộ thẻ
        deckManager.addDeck(deck);
    }
    
    private void updateEmptyState() {
        // Thêm kiểm tra null để tránh NPE
        if (textEmptyState == null || recyclerViewDecks == null) return;
        
        if (trafficSignDecks.isEmpty()) {
            recyclerViewDecks.setVisibility(View.GONE);
            textEmptyState.setVisibility(View.VISIBLE);
            textEmptyState.setText(R.string.flashcards_placeholder);
        } else {
            recyclerViewDecks.setVisibility(View.VISIBLE);
            textEmptyState.setVisibility(View.GONE);
        }
    }
    
    private void onDeckClicked(FlashcardDeck deck, int position) {
        // Mở màn hình học thẻ ghi nhớ
        Intent intent = FlashcardStudyActivity.createIntent(requireContext(), deck.getId());
        startActivity(intent);
    }
    
    private boolean onDeckLongClicked(FlashcardDeck deck, int position) {
        // Tạo menu cho long click
        PopupMenu popupMenu = new PopupMenu(requireContext(), recyclerViewDecks.findViewHolderForAdapterPosition(position).itemView);
        popupMenu.getMenu().add("Chỉnh sửa");
        popupMenu.getMenu().add("Xoá");
        popupMenu.getMenu().add("Tạo bản sao");
        
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Chỉnh sửa")) {
                // Mở màn hình chỉnh sửa
                Intent intent = new Intent(getContext(), FlashcardEditorActivity.class);
                intent.putExtra("deck_id", deck.getId());
                startActivity(intent);
                return true;
            } else if (item.getTitle().equals("Xoá")) {
                confirmDeleteDeck(deck, position);
                return true;
            } else if (item.getTitle().equals("Tạo bản sao")) {
                // Tạo bản sao của bộ thẻ
                long newDeckId = deckManager.duplicateDeck(deck.getId());
                if (newDeckId > 0) {
                    trafficSignDecks = deckManager.getAllDecks();
                    adapter.updateData(trafficSignDecks);
                    Toast.makeText(requireContext(), "Đã tạo bản sao", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });
        
        popupMenu.show();
        return true;
    }
    
    private void confirmDeleteDeck(FlashcardDeck deck, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xoá bộ thẻ")
                .setMessage("Bạn có chắc chắn muốn xoá bộ thẻ này?")
                .setPositiveButton("Xoá", (dialog, which) -> {
                    // Xoá bộ thẻ
                    deckManager.deleteDeck(deck.getId());
                    // Cập nhật UI
                    trafficSignDecks.remove(position);
                    adapter.notifyItemRemoved(position);
                    updateEmptyState();
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void showCreateCustomDeckDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_create_custom_deck, null);
        builder.setView(dialogView);
        
        final AlertDialog dialog = builder.create();
        
        // Lấy các thành phần của dialog
        final TextInputEditText editTextDeckName = dialogView.findViewById(R.id.editTextDeckName);
        final Spinner spinnerSignCategory = dialogView.findViewById(R.id.spinnerSignCategory);
        final Slider sliderCardCount = dialogView.findViewById(R.id.sliderCardCount);
        final TextView textCardCount = dialogView.findViewById(R.id.textCardCount);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        Button buttonCreate = dialogView.findViewById(R.id.buttonCreate);
        
        // Thiết lập Spinner cho loại biển báo
        String[] categories = new String[]{
            getString(R.string.all_categories),
            getString(R.string.category_prohibitory),
            getString(R.string.category_warning),
            getString(R.string.category_mandatory),
            getString(R.string.category_guide),
            getString(R.string.category_auxiliary)
        };
        
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
            getContext(), 
            android.R.layout.simple_spinner_item, 
            categories
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSignCategory.setAdapter(categoryAdapter);
        
        // Đặt giá trị mặc định
        sliderCardCount.setValue(15); // Giá trị mặc định là 15 thẻ
        textCardCount.setText("15 " + getString(R.string.cards));
        
        // Cập nhật số lượng thẻ khi di chuyển thanh trượt
        sliderCardCount.addOnChangeListener((slider, value, fromUser) -> {
            int cardCount = (int) value;
            textCardCount.setText(cardCount + " " + getString(R.string.cards));
        });
        
        // Xử lý nút Hủy
        buttonCancel.setOnClickListener(v -> dialog.dismiss());
        
        // Xử lý nút Tạo mới
        buttonCreate.setOnClickListener(v -> {
            String deckName = editTextDeckName.getText().toString().trim();
            int cardCount = (int) sliderCardCount.getValue();
            String selectedCategory = spinnerSignCategory.getSelectedItem().toString();
            
            if (deckName.isEmpty()) {
                Toast.makeText(getContext(), getString(R.string.please_enter_deck_name), Toast.LENGTH_SHORT).show();
                return;
            }
            
            createCustomDeck(deckName, cardCount, selectedCategory);
            dialog.dismiss();
        });
        
        dialog.show();
    }

    private void createCustomDeck(String deckName, int cardCount, String selectedCategory) {
        DataManager dataManager = DataManager.getInstance(requireContext());
        List<TrafficSign> allSigns = dataManager.getAllTrafficSigns();
        
        if (allSigns == null || allSigns.isEmpty()) {
            Toast.makeText(getContext(), getString(R.string.no_traffic_sign_data), Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Lọc biển báo theo loại đã chọn
        List<TrafficSign> filteredSigns = new ArrayList<>();
        
        if (selectedCategory.equals(getString(R.string.all_categories))) {
            // Nếu chọn "Tất cả các loại", sử dụng tất cả biển báo
            filteredSigns.addAll(allSigns);
        } else {
            // Lọc theo danh mục (category) đã được định nghĩa trong TrafficSign
            String categoryKey = getCategoryKey(selectedCategory);
            for (TrafficSign sign : allSigns) {
                if (sign.getCategory().equals(categoryKey)) {
                    filteredSigns.add(sign);
                }
            }
        }
        
        if (filteredSigns.isEmpty()) {
            Toast.makeText(getContext(), getString(R.string.no_signs_in_category), Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Nếu số lượng thẻ lớn hơn số lượng biển báo có sẵn, giới hạn lại
        int actualCardCount = Math.min(cardCount, filteredSigns.size());
        
        // Trộn ngẫu nhiên các biển báo
        Collections.shuffle(filteredSigns);
        
        // Lấy số lượng biển báo cần thiết
        List<TrafficSign> selectedSigns = filteredSigns.subList(0, actualCardCount);
        
        // Tạo bộ thẻ mới
        FlashcardDeck newDeck = new FlashcardDeck(deckName);
        newDeck.setRandomOrder(true);
        newDeck.setCategory(selectedCategory);
        
        // Thêm các thẻ vào bộ
        for (TrafficSign sign : selectedSigns) {
            Flashcard card = new Flashcard();
            card.setQuestion("Đây là biển báo gì?");
            card.setAnswer(sign.getName() + "\n\n" + sign.getDescription());
            card.setImageUrl(sign.getImageUrl());
            newDeck.addCard(card);
        }
        
        // Thêm bộ thẻ vào danh sách
        deckManager.addDeck(newDeck);
        adapter.notifyItemInserted(0);
        updateEmptyState();
        
        Toast.makeText(getContext(), getString(R.string.deck_created, deckName, actualCardCount), Toast.LENGTH_SHORT).show();
    }

    /**
     * Lấy khóa category dựa trên tên hiển thị
     */
    private String getCategoryKey(String displayName) {
        if (displayName.equals(getString(R.string.category_prohibitory))) {
            return "bien_bao_cam";
        } else if (displayName.equals(getString(R.string.category_warning))) {
            return "bien_nguy_hiem_va_canh_bao";
        } else if (displayName.equals(getString(R.string.category_mandatory))) {
            return "bien_hieu_lenh";
        } else if (displayName.equals(getString(R.string.category_guide))) {
            return "bien_chi_dan";
        } else if (displayName.equals(getString(R.string.category_auxiliary))) {
            return "bien_phu";
        }
        return ""; // Mặc định
    }

    @Override
    public void onPause() {
        super.onPause();
        // Lưu dữ liệu khi rời khỏi fragment
        if (deckManager != null) {
            deckManager.saveDecks();
        }
        
        // Hủy đăng ký listener khi fragment bị pause
        DataManager.getInstance(requireContext()).unregisterDataListener(this::onTrafficSignsLoaded);
    }
} 