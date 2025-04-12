package com.example.giaothong.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.giaothong.BuildConfig;
import com.example.giaothong.R;
import com.example.giaothong.adapter.TrafficSignAdapter;
import com.example.giaothong.api.RoboflowApiClient;
import com.example.giaothong.model.TrafficSign;
import com.example.giaothong.utils.SearchHistoryManager;
import com.example.giaothong.utils.SharedPreferencesManager;
import com.example.giaothong.viewmodel.TrafficSignViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchFragment extends Fragment implements TrafficSignDetailBottomSheet.OnPinStatusChangeListener {

    private static final int REQUEST_CAMERA_PERMISSION = 101;
    private static final int REQUEST_STORAGE_PERMISSION = 102;
    
    private static final String TAG = "SearchFragment";
    
    // Roboflow API parameters - Cập nhật để sử dụng BuildConfig
    private static final String ROBOFLOW_API_URL = "https://detect.roboflow.com";
    // Sử dụng API key từ BuildConfig thay vì hard-coded
    private static final String ROBOFLOW_API_KEY = BuildConfig.ROBOFLOW_API_KEY;
    // Cập nhật model ID chính xác
    private static final String ROBOFLOW_MODEL_ID = "vn-traffic-sign/1";  // Thay đổi tên model và version
    
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
    private boolean isTextClearedByCloseButton = false;
    
    // Card hiển thị kết quả nhận diện
    private View cardDetectionResult;
    private ImageView imageDetectionResult;
    private TextView textDetectionInfo;
    private MaterialButton buttonCloseDetection;
    
    // Camera và Gallery
    private ImageButton buttonCameraDetection;
    private Uri currentPhotoUri;
    private String currentPhotoPath;
    
    // ActivityResultLauncher cho camera và gallery
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    // Hằng số cho loại hành động
    private static final int ACTION_TAKE_PHOTO = 1;
    private static final int ACTION_PICK_GALLERY = 2;
    
    // Lưu trữ hành động hiện tại được chọn
    private int currentAction = 0;

    // Progress indicator for API request
    private CircularProgressIndicator progressIndicator;
    
    // Thread pool for background tasks
    private ExecutorService executorService;
    
    // Roboflow API Client
    private RoboflowApiClient roboflowApiClient;

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
        
        // Initialize executor service
        executorService = Executors.newSingleThreadExecutor();
        
        // Initialize Roboflow API client
        roboflowApiClient = new RoboflowApiClient(
                ROBOFLOW_API_URL,
                ROBOFLOW_API_KEY,
                ROBOFLOW_MODEL_ID,
                null);  // workflowId không còn được sử dụng
        
        // Initialize views
        setupViews(view);
        
        // Setup result launchers
        setupResultLaunchers();
        
        // Initialize ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(TrafficSignViewModel.class);
        viewModel.setPreferencesManager(prefsManager);
        
        // Set up adapter
        adapter = new TrafficSignAdapter(requireContext(), new ArrayList<>());
        adapter.setOnItemClickListener(this::showTrafficSignDetail);
        adapter.setOnItemLongClickListener(this::togglePinStatus);
        recyclerView.setAdapter(adapter);
        
        // Check for category filter from arguments
        Bundle args = getArguments();
        if (args != null && args.containsKey("category")) {
            String category = args.getString("category", "");
            if (!category.isEmpty()) {
                // Apply category filter
                viewModel.setCategory(category);
                
                // Select the correct chip
                if (category.equals("bien_bao_cam")) {
                    chipCam.setChecked(true);
                } else if (category.equals("bien_nguy_hiem_va_canh_bao")) {
                    chipNguyHiem.setChecked(true);
                } else if (category.equals("bien_hieu_lenh")) {
                    chipHieuLenh.setChecked(true);
                } else if (category.equals("bien_chi_dan")) {
                    chipChiDan.setChecked(true);
                } else if (category.equals("bien_phu")) {
                    chipPhu.setChecked(true);
                } else {
                    chipAll.setChecked(true);
                }
            }
        }
        
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
        
        // Khởi tạo các view xử lý nhận diện
        cardDetectionResult = view.findViewById(R.id.cardDetectionResult);
        imageDetectionResult = view.findViewById(R.id.imageDetectionResult);
        textDetectionInfo = view.findViewById(R.id.textDetectionInfo);
        buttonCloseDetection = view.findViewById(R.id.buttonCloseDetection);
        buttonCameraDetection = view.findViewById(R.id.buttonCameraDetection);
        progressIndicator = view.findViewById(R.id.progressIndicator);
        
        // Thiết lập sự kiện cho nút nhận diện biển báo
        buttonCameraDetection.setOnClickListener(v -> showImageOptionsBottomSheet());
        
        // Thiết lập sự kiện cho nút đóng kết quả nhận diện
        buttonCloseDetection.setOnClickListener(v -> cardDetectionResult.setVisibility(View.GONE));
        
        // Setup RecyclerView with LinearLayoutManager for single-column list
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        
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
        
        // Tìm và thiết lập click listener cho nút close
        View closeButton = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        if (closeButton != null) {
            closeButton.setOnClickListener(v -> {
                // Xử lý trước khi gọi xóa mặc định của SearchView
                isTextClearedByCloseButton = true;
                
                // Xóa text trong searchView (sẽ gọi onQueryTextChange)
                searchView.setQuery("", false);
                
                // Ẩn lịch sử tìm kiếm
                hideSearchHistory();
                
                // Đặt lại focus (tùy chọn)
                searchView.clearFocus();
            });
        }
        
        // Setup search view query listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Add query to search history and perform search
                if (!query.trim().isEmpty()) {
                    searchHistoryManager.addSearchQuery(query);
                    hideSearchHistory();
                }
                return true;
            }
            
            @Override
            public boolean onQueryTextChange(String newText) {
                // Hiển thị lịch sử khi không có text và ẩn khi có text
                if (newText.trim().isEmpty()) {
                    if (searchView.hasFocus() && !isTextClearedByCloseButton) {
                        showSearchHistory();
                    }
                    // Reset flag sau khi đã kiểm tra
                    isTextClearedByCloseButton = false;
                    
                    // Xóa bộ lọc tìm kiếm khi text rỗng
                    viewModel.setSearchQuery("");
                } else {
                    hideSearchHistory();
                    
                    // Thực hiện tìm kiếm theo từng ký tự đang nhập
                    viewModel.setSearchQuery(newText);
                }
                return true;
            }
        });
        
        // Show search history when search view is focused but empty
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && searchView.getQuery().length() == 0 && !isTextClearedByCloseButton) {
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
                viewModel.setCategory("bien_bao_cam");
            } else if (checkedId == R.id.chipNguyHiem) {
                viewModel.setCategory("bien_nguy_hiem_va_canh_bao");
            } else if (checkedId == R.id.chipHieuLenh) {
                viewModel.setCategory("bien_hieu_lenh");
            } else if (checkedId == R.id.chipChiDan) {
                viewModel.setCategory("bien_chi_dan");
            } else if (checkedId == R.id.chipPhu) {
                viewModel.setCategory("bien_phu");
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

    /**
     * Thiết lập các launcher xử lý kết quả từ camera và gallery
     */
    private void setupResultLaunchers() {
        // Xử lý kết quả chụp ảnh từ camera
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Hiển thị ảnh đã chụp
                        if (currentPhotoUri != null) {
                            showDetectionResult(currentPhotoUri.toString());
                        }
                    }
                });
        
        // Xử lý kết quả chọn ảnh từ thư viện
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        if (selectedImage != null) {
                            showDetectionResult(selectedImage.toString());
                        }
                    }
                });
                
        // Xử lý kết quả yêu cầu quyền
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // Quyền được cấp, tiếp tục với hành động đã chọn
                        executeActionAfterPermissionGranted();
                    } else {
                        // Kiểm tra xem người dùng đã từ chối vĩnh viễn chưa
                        if (!shouldShowRequestPermissionRationale(getRequiredPermissionForAction())) {
                            // Người dùng đã chọn "Don't ask again", hiển thị dialog hướng dẫn vào cài đặt
                            showSettingsDialog();
                        } else {
                            // Quyền bị từ chối nhưng không phải vĩnh viễn
                            Toast.makeText(requireContext(),
                                    "Cần quyền truy cập để thực hiện chức năng này",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    
    /**
     * Thực hiện hành động sau khi quyền được cấp
     */
    private void executeActionAfterPermissionGranted() {
        switch (currentAction) {
            case ACTION_TAKE_PHOTO:
                dispatchTakePictureIntent();
                break;
            case ACTION_PICK_GALLERY:
                openGallery();
                break;
        }
    }
    
    /**
     * Lấy quyền cần thiết dựa vào hành động hiện tại
     */
    private String getRequiredPermissionForAction() {
        if (currentAction == ACTION_TAKE_PHOTO) {
            return Manifest.permission.CAMERA;
        } else if (currentAction == ACTION_PICK_GALLERY) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return Manifest.permission.READ_MEDIA_IMAGES;
            } else {
                return Manifest.permission.READ_EXTERNAL_STORAGE;
            }
        }
        return "";
    }
    
    /**
     * Hiển thị dialog hướng dẫn người dùng vào cài đặt để cấp quyền
     */
    private void showSettingsDialog() {
        String message = currentAction == ACTION_TAKE_PHOTO ? 
                "Cần quyền truy cập camera để chụp ảnh. Vui lòng vào cài đặt để cấp quyền." :
                "Cần quyền truy cập thư viện ảnh. Vui lòng vào cài đặt để cấp quyền.";
                
        new AlertDialog.Builder(requireContext())
            .setTitle("Yêu cầu quyền")
            .setMessage(message)
            .setPositiveButton("Đi đến Cài đặt", (dialog, which) -> {
                // Mở màn hình cài đặt ứng dụng
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", requireActivity().getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            })
            .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
            .create()
            .show();
    }
    
    /**
     * Hiển thị bottom sheet cho phép người dùng chọn chụp ảnh hoặc lấy từ thư viện
     */
    private void showImageOptionsBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View bottomSheetView = LayoutInflater.from(requireContext())
                .inflate(R.layout.bottom_sheet_image_options, null);
        
        // Thiết lập sự kiện cho nút chụp ảnh
        bottomSheetView.findViewById(R.id.optionCamera).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            currentAction = ACTION_TAKE_PHOTO;
            checkCameraPermissionAndTakePicture();
        });
        
        // Thiết lập sự kiện cho nút chọn ảnh từ thư viện
        bottomSheetView.findViewById(R.id.optionGallery).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            currentAction = ACTION_PICK_GALLERY;
            checkStoragePermissionAndOpenGallery();
        });
        
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }
    
    /**
     * Kiểm tra quyền camera và thực hiện chụp ảnh
     */
    private void checkCameraPermissionAndTakePicture() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Cần yêu cầu quyền
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            // Đã có quyền, tiến hành chụp ảnh
            dispatchTakePictureIntent();
        }
    }
    
    /**
     * Kiểm tra quyền bộ nhớ và mở thư viện ảnh
     */
    private void checkStoragePermissionAndOpenGallery() {
        // Kiểm tra quyền dựa vào phiên bản Android
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }
        
        if (ContextCompat.checkSelfPermission(requireContext(), permission)
                != PackageManager.PERMISSION_GRANTED) {
            // Cần yêu cầu quyền
            requestPermissionLauncher.launch(permission);
        } else {
            // Đã có quyền, mở thư viện ảnh
            openGallery();
        }
    }
    
    /**
     * Mở ứng dụng camera để chụp ảnh
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            // Tạo file để lưu ảnh
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Lỗi khi tạo file
                Toast.makeText(requireContext(), 
                        "Không thể tạo file hình ảnh", 
                        Toast.LENGTH_SHORT).show();
            }
            
            // Nếu tạo file thành công, tiếp tục
            if (photoFile != null) {
                currentPhotoUri = FileProvider.getUriForFile(requireContext(),
                        "com.example.giaothong.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
                cameraLauncher.launch(takePictureIntent);
            }
        } else {
            Toast.makeText(requireContext(), 
                    "Không tìm thấy ứng dụng camera", 
                    Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Tạo file tạm thời để lưu ảnh chụp
     */
    private File createImageFile() throws IOException {
        // Tạo tên file dựa trên thời gian
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        
        // Lưu đường dẫn để sử dụng
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    
    /**
     * Mở thư viện ảnh để chọn ảnh
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }
    
    /**
     * Hiển thị kết quả nhận diện biển báo
     */
    private void showDetectionResult(String imageUri) {
        // Hiển thị card kết quả
        cardDetectionResult.setVisibility(View.VISIBLE);
        
        // Hiển thị ảnh đã chọn/chụp
        imageDetectionResult.setImageURI(Uri.parse(imageUri));
        
        // Hiện progress indicator
        progressIndicator.setVisibility(View.VISIBLE);
        textDetectionInfo.setText("Đang xử lý ảnh...");
        
        // Tải ảnh và gọi API trong background thread
        loadBitmapAndDetect(imageUri);
    }
    
    /**
     * Tải bitmap từ URI và gọi API nhận diện
     */
    private void loadBitmapAndDetect(String imageUri) {
        Uri uri = Uri.parse(imageUri);
        
        // Sử dụng Glide để tải bitmap từ URI
        Glide.with(this)
                .asBitmap()
                .load(uri)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                        // Bitmap đã tải xong, gọi API nhận diện trong background thread
                        detectTrafficSignInBackground(bitmap);
                    }
                    
                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        // Lỗi khi tải bitmap
                        showDetectionError("Không thể tải ảnh. Vui lòng thử lại.");
                    }
                    
                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // Do nothing
                    }
                });
    }
    
    /**
     * Thực hiện nhận diện biển báo trong background thread
     */
    private void detectTrafficSignInBackground(Bitmap bitmap) {
        executorService.execute(() -> {
            try {
                // Scale bitmap để giảm kích thước trước khi gửi API
                Bitmap scaledBitmap = scaleBitmapDown(bitmap, 640);
                
                // Gọi API nhận diện
                List<RoboflowApiClient.DetectionResult> results = roboflowApiClient.detectTrafficSign(scaledBitmap);
                
                // Cập nhật UI trong main thread
                requireActivity().runOnUiThread(() -> handleDetectionResults(results));
            } catch (IOException e) {
                Log.e(TAG, "Error detecting traffic sign: " + e.getMessage());
                // Cập nhật UI trong main thread
                requireActivity().runOnUiThread(() -> 
                        showDetectionError("Lỗi khi kết nối đến server: " + e.getMessage()));
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error: " + e.getMessage());
                // Cập nhật UI trong main thread
                requireActivity().runOnUiThread(() -> 
                        showDetectionError("Lỗi không xác định: " + e.getMessage()));
            }
        });
    }
    
    /**
     * Giảm kích thước của bitmap để tối ưu việc upload
     */
    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = originalWidth;
        int resizedHeight = originalHeight;
        
        if (originalHeight > maxDimension || originalWidth > maxDimension) {
            if (originalHeight > originalWidth) {
                resizedHeight = maxDimension;
                resizedWidth = (int) (originalWidth * (float) resizedHeight / originalHeight);
            } else {
                resizedWidth = maxDimension;
                resizedHeight = (int) (originalHeight * (float) resizedWidth / originalWidth);
            }
        }
        
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }
    
    /**
     * Xử lý kết quả nhận diện
     */
    private void handleDetectionResults(List<RoboflowApiClient.DetectionResult> results) {
        // Ẩn progress indicator
        progressIndicator.setVisibility(View.GONE);
        
        if (results.isEmpty()) {
            textDetectionInfo.setText("Không phát hiện biển báo nào trong ảnh.");
            return;
        }
        
        // Lấy kết quả có độ tin cậy cao nhất
        RoboflowApiClient.DetectionResult bestResult = results.get(0);
        for (RoboflowApiClient.DetectionResult result : results) {
            if (result.getConfidence() > bestResult.getConfidence()) {
                bestResult = result;
            }
        }
        
        // Hiển thị kết quả
        String resultText = String.format("Phát hiện: %s (%.1f%% tin cậy)",
                bestResult.getClassName(),
                bestResult.getConfidence() * 100);
                
        // Tìm kiếm biển báo phù hợp với kết quả nhận diện
        findAndSelectMatchingTrafficSign(bestResult.getClassName());
        
        textDetectionInfo.setText(resultText);
    }
    
    /**
     * Tìm và chọn biển báo phù hợp với kết quả nhận diện
     */
    private void findAndSelectMatchingTrafficSign(String detectedClass) {
        // Đặt query cho searchView để tìm biển báo phù hợp
        searchView.setQuery(detectedClass, true);
        
        // TODO: Cải thiện logic tìm kiếm và hiển thị biển báo phù hợp
    }
    
    /**
     * Hiển thị lỗi nhận diện
     */
    private void showDetectionError(String errorMessage) {
        // Ẩn progress indicator
        progressIndicator.setVisibility(View.GONE);
        
        // Hiển thị thông báo lỗi
        textDetectionInfo.setText("Lỗi: " + errorMessage);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Tắt executor service khi fragment bị hủy
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
} 