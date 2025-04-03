package com.example.giaothong.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.giaothong.R;
import com.example.giaothong.adapter.TrafficSignAdapter;
import com.example.giaothong.model.TrafficSign;
import com.example.giaothong.utils.SearchHistoryManager;
import com.example.giaothong.utils.SharedPreferencesManager;
import com.example.giaothong.viewmodel.TrafficSignViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;

public class SearchFragment extends Fragment implements TrafficSignDetailBottomSheet.OnPinStatusChangeListener {

    private TrafficSignViewModel viewModel;
    private TrafficSignAdapter adapter;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ChipGroup chipGroupCategories;
    private Chip chipAll, chipCam, chipNguyHiem, chipHieuLenh, chipChiDan, chipPhu, chipPinned;
    private TextView textEmptyState;
    private SharedPreferencesManager prefsManager;
    private SearchHistoryManager searchHistoryManager;
    private SearchView searchView;
    private SearchHistoryPopup searchHistoryPopup;
    private View cardSearch;

    public SearchFragment() {
        // Required empty public constructor
    }

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize shared preferences
        prefsManager = new SharedPreferencesManager(requireContext());
        
        // Initialize search history manager
        searchHistoryManager = new SearchHistoryManager(requireContext());
        
        // Initialize views
        setupViews(view);
        
        // Initialize ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(TrafficSignViewModel.class);
        viewModel.setPreferencesManager(prefsManager);
        
        // Set up adapter
        adapter = new TrafficSignAdapter(requireContext(), new ArrayList<>());
        adapter.setOnItemClickListener(this::showTrafficSignDetail);
        adapter.setOnItemLongClickListener(this::togglePinStatus);
        recyclerView.setAdapter(adapter);
        
        // Observe traffic signs
        viewModel.getTrafficSigns().observe(getViewLifecycleOwner(), trafficSigns -> {
            adapter.setTrafficSigns(trafficSigns);
            // Stop refreshing animation if it was started
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
            
            // Show/hide empty state if needed
            if (trafficSigns.isEmpty()) {
                textEmptyState.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                textEmptyState.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewSigns);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        textEmptyState = view.findViewById(R.id.textEmptyState);
        searchView = view.findViewById(R.id.searchView);
        cardSearch = view.findViewById(R.id.cardSearch);
        chipGroupCategories = view.findViewById(R.id.chipGroupCategories);
        
        // Setup RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        
        // Initialize chip references
        chipAll = view.findViewById(R.id.chipAll);
        chipPinned = view.findViewById(R.id.chipPinned);
        chipCam = view.findViewById(R.id.chipCam);
        chipNguyHiem = view.findViewById(R.id.chipNguyHiem);
        chipHieuLenh = view.findViewById(R.id.chipHieuLenh);
        chipChiDan = view.findViewById(R.id.chipChiDan);
        chipPhu = view.findViewById(R.id.chipPhu);
        
        // Setup SwipeRefreshLayout
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(requireContext(), R.color.colorPrimary),
                ContextCompat.getColor(requireContext(), R.color.colorAccent)
        );
        
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Refresh traffic signs data
            viewModel.refreshTrafficSigns();
        });
        
        // Setup search view
        setupSearchView();
        
        // Setup chip group for filtering categories
        setupCategoryFilters();
    }

    private void setupSearchView() {
        // Initialize search history popup
        searchHistoryPopup = new SearchHistoryPopup(requireContext(), cardSearch, searchHistoryManager);
        searchHistoryPopup.setOnHistoryItemClickListener(query -> {
            searchView.setQuery(query, true);
            hideSearchHistory();
        });
        
        // Setup search view query listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Add query to search history and perform search
                if (!query.trim().isEmpty()) {
                    searchHistoryManager.addSearchQuery(query);
                    hideSearchHistory();
                    viewModel.setSearchQuery(query);
                }
                return true;
            }
            
            @Override
            public boolean onQueryTextChange(String newText) {
                // Only show history when there's text but not for empty queries
                if (newText.trim().isEmpty()) {
                    hideSearchHistory();
                } else if (searchView.hasFocus()) {
                    showSearchHistory();
                }
                return true;
            }
        });
        
        // Show search history when search view is focused
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && searchView.getQuery().length() > 0) {
                showSearchHistory();
            } else {
                hideSearchHistory();
            }
        });
    }

    private void setupCategoryFilters() {
        chipGroupCategories.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipAll) {
                viewModel.setCategory("");
            } else if (checkedId == R.id.chipPinned) {
                viewModel.setShowOnlyPinned(true);
                viewModel.setCategory("");
                return;
            } else if (checkedId == R.id.chipCam) {
                viewModel.setCategory("P");
            } else if (checkedId == R.id.chipNguyHiem) {
                viewModel.setCategory("W");
            } else if (checkedId == R.id.chipHieuLenh) {
                viewModel.setCategory("R");
            } else if (checkedId == R.id.chipChiDan) {
                viewModel.setCategory("I");
            } else if (checkedId == R.id.chipPhu) {
                viewModel.setCategory("S");
            }
            
            // Ensure showing all if a category is selected (not just pinned)
            viewModel.setShowOnlyPinned(false);
        });
    }

    /**
     * Show search history popup
     */
    private void showSearchHistory() {
        searchHistoryPopup.updateHistoryData();
        searchHistoryPopup.show(cardSearch);
    }

    /**
     * Hide search history popup
     */
    private void hideSearchHistory() {
        if (searchHistoryPopup.isShowing()) {
            searchHistoryPopup.dismiss();
        }
    }

    /**
     * Show traffic sign detail when clicked
     */
    private void showTrafficSignDetail(TrafficSign sign, int position) {
        TrafficSignDetailBottomSheet bottomSheet = TrafficSignDetailBottomSheet.newInstance(sign);
        bottomSheet.show(getChildFragmentManager(), "TrafficSignDetail");
    }

    /**
     * Toggle pin status when long press
     */
    private boolean togglePinStatus(TrafficSign sign, int position) {
        viewModel.togglePinStatus(sign);
        
        // Show toast message
        Toast.makeText(
                requireContext(), 
                sign.isPinned() ? R.string.sign_pinned : R.string.sign_unpinned, 
                Toast.LENGTH_SHORT
        ).show();
        
        return true;
    }

    @Override
    public void onPinStatusChanged(TrafficSign sign, boolean isPinned) {
        // Update UI if needed (adapter will get updates via LiveData)
    }
} 