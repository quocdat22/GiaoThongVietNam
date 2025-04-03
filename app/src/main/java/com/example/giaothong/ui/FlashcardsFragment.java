package com.example.giaothong.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.giaothong.R;
import com.example.giaothong.adapter.FlashcardDeckAdapter;
import com.example.giaothong.model.FlashcardDeck;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FlashcardsFragment extends Fragment {

    private RecyclerView recyclerViewDecks;
    private FlashcardDeckAdapter adapter;
    private TextView textEmptyState;
    private TabLayout tabLayout;
    private FloatingActionButton fabAddDeck;
    
    private List<FlashcardDeck> myDecks;
    private List<FlashcardDeck> trafficSignDecks;
    private boolean isMyDecksTab = true;

    public FlashcardsFragment() {
        // Required empty public constructor
    }

    public static FlashcardsFragment newInstance() {
        return new FlashcardsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_flashcards, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo các view
        recyclerViewDecks = view.findViewById(R.id.recyclerViewDecks);
        textEmptyState = view.findViewById(R.id.textEmptyState);
        tabLayout = view.findViewById(R.id.tabLayout);
        fabAddDeck = view.findViewById(R.id.fabAddDeck);

        // Setup RecyclerView
        recyclerViewDecks.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        // Khởi tạo dữ liệu mẫu
        setupSampleData();
        
        // Setup adapter
        adapter = new FlashcardDeckAdapter(requireContext(), myDecks);
        adapter.setOnItemClickListener(this::onDeckClicked);
        recyclerViewDecks.setAdapter(adapter);
        
        // Setup tab selection
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    // Tab "Bộ thẻ của tôi"
                    isMyDecksTab = true;
                    adapter.updateData(myDecks);
                    fabAddDeck.show();
                    updateEmptyState();
                } else {
                    // Tab "Bộ thẻ biển báo"
                    isMyDecksTab = false;
                    adapter.updateData(trafficSignDecks);
                    fabAddDeck.hide();
                    updateEmptyState();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Không cần xử lý
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Không cần xử lý
            }
        });
        
        // Setup FAB
        fabAddDeck.setOnClickListener(v -> {
            // Hiện tại chỉ hiển thị thông báo, sau này sẽ mở dialog tạo bộ thẻ mới
            Toast.makeText(requireContext(), "Tạo bộ thẻ mới", Toast.LENGTH_SHORT).show();
        });
        
        // Hiển thị trạng thái ban đầu
        updateEmptyState();
    }
    
    private void setupSampleData() {
        // Dữ liệu mẫu cho "Bộ thẻ của tôi"
        myDecks = new ArrayList<>();
        
        // Dữ liệu mẫu cho "Bộ thẻ biển báo"
        trafficSignDecks = new ArrayList<>();
        
        // Thêm một số bộ thẻ mẫu vào danh sách biển báo
        FlashcardDeck deck1 = new FlashcardDeck("Biển báo cấm", "Các biển báo cấm phổ biến", "Biển báo");
        deck1.setLastModified(new Date().getTime() - 24 * 60 * 60 * 1000); // Hôm qua
        deck1.setCardCount(20);
        
        FlashcardDeck deck2 = new FlashcardDeck("Biển báo nguy hiểm", "Các biển báo nguy hiểm và cảnh báo", "Biển báo");
        deck2.setLastModified(new Date().getTime() - 2 * 24 * 60 * 60 * 1000); // 2 ngày trước
        deck2.setCardCount(15);
        
        FlashcardDeck deck3 = new FlashcardDeck("Biển báo hiệu lệnh", "Các biển báo hiệu lệnh phổ biến", "Biển báo");
        deck3.setLastModified(new Date().getTime() - 5 * 24 * 60 * 60 * 1000); // 5 ngày trước
        deck3.setCardCount(12);
        
        trafficSignDecks.add(deck1);
        trafficSignDecks.add(deck2);
        trafficSignDecks.add(deck3);
    }
    
    private void updateEmptyState() {
        List<FlashcardDeck> currentList = isMyDecksTab ? myDecks : trafficSignDecks;
        
        if (currentList.isEmpty()) {
            recyclerViewDecks.setVisibility(View.GONE);
            textEmptyState.setVisibility(View.VISIBLE);
            
            if (isMyDecksTab) {
                textEmptyState.setText(R.string.flashcards_placeholder);
            } else {
                textEmptyState.setText("Chưa có bộ thẻ biển báo nào");
            }
        } else {
            recyclerViewDecks.setVisibility(View.VISIBLE);
            textEmptyState.setVisibility(View.GONE);
        }
    }
    
    private void onDeckClicked(FlashcardDeck deck, int position) {
        // Hiện tại chỉ hiển thị thông báo, sau này sẽ mở màn hình học thẻ
        Toast.makeText(requireContext(), 
                "Đã chọn bộ thẻ: " + deck.getName() + " (" + deck.getCardCount() + " thẻ)", 
                Toast.LENGTH_SHORT).show();
    }
} 