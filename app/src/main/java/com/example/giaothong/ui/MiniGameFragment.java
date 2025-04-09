package com.example.giaothong.ui;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipDescription;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.giaothong.R;
import com.example.giaothong.model.TrafficSign;
import com.example.giaothong.repository.TrafficSignRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MiniGameFragment extends Fragment {

    private ImageView imageSign1, imageSign2, imageSign3;
    private TextView textDesc1, textDesc2, textDesc3;
    private FrameLayout dropArea1, dropArea2, dropArea3;
    private ImageView dropImage1, dropImage2, dropImage3;
    private TextView textDragHint1, textDragHint2, textDragHint3;
    private Button buttonCheck, buttonReset, buttonNext;
    private TextView textResult;

    private List<TrafficSign> allTrafficSigns;
    private List<TrafficSign> currentGameSigns = new ArrayList<>();
    private Map<Integer, Integer> userAnswers = new HashMap<>(); // dropAreaId -> signPosition
    private Map<Integer, Integer> correctAnswers = new HashMap<>(); // dropAreaId -> correct signPosition

    private static final int GAME_SIZE = 3; // Số biển báo trong mỗi màn chơi

    public MiniGameFragment() {
        // Required empty public constructor
    }

    public static MiniGameFragment newInstance() {
        return new MiniGameFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mini_game, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Khởi tạo views
        initViews(view);
        
        // Tải dữ liệu biển báo
        loadTrafficSigns();
        
        // Thiết lập sự kiện kéo thả
        setupDragAndDrop();
        
        // Thiết lập sự kiện cho các nút
        setupButtons();
    }

    private void initViews(View view) {
        // Biển báo (nguồn kéo)
        imageSign1 = view.findViewById(R.id.imageSign1);
        imageSign2 = view.findViewById(R.id.imageSign2);
        imageSign3 = view.findViewById(R.id.imageSign3);
        
        // Mô tả
        textDesc1 = view.findViewById(R.id.textDesc1);
        textDesc2 = view.findViewById(R.id.textDesc2);
        textDesc3 = view.findViewById(R.id.textDesc3);
        
        // Vùng thả
        dropArea1 = view.findViewById(R.id.dropArea1);
        dropArea2 = view.findViewById(R.id.dropArea2);
        dropArea3 = view.findViewById(R.id.dropArea3);
        
        // Hình ảnh hiển thị trong vùng thả
        dropImage1 = view.findViewById(R.id.dropImage1);
        dropImage2 = view.findViewById(R.id.dropImage2);
        dropImage3 = view.findViewById(R.id.dropImage3);
        
        // TextView thông báo kéo biển báo
        textDragHint1 = view.findViewById(R.id.textDragHint1);
        textDragHint2 = view.findViewById(R.id.textDragHint2);
        textDragHint3 = view.findViewById(R.id.textDragHint3);
        
        // Các nút điều khiển và hiển thị kết quả
        buttonCheck = view.findViewById(R.id.buttonCheck);
        buttonReset = view.findViewById(R.id.buttonReset);
        buttonNext = view.findViewById(R.id.buttonNext);
        textResult = view.findViewById(R.id.textResult);
    }

    private void loadTrafficSigns() {
        TrafficSignRepository repository = TrafficSignRepository.getInstance(requireContext());
        
        // Đăng ký lắng nghe dữ liệu
        repository.registerDataListener(signs -> {
            if (signs != null && !signs.isEmpty()) {
                allTrafficSigns = signs;
                setupNewGame();
            } else {
                Toast.makeText(requireContext(), R.string.traffic_signs_not_available, Toast.LENGTH_SHORT).show();
            }
        });
        
        if (repository.isDataReady()) {
            // Dữ liệu đã sẵn sàng, lấy trực tiếp
            allTrafficSigns = repository.getAllTrafficSigns();
            setupNewGame();
        } else {
            // Nếu dữ liệu chưa sẵn sàng, repository sẽ gọi listener khi có dữ liệu
            repository.refreshTrafficSigns();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Hủy đăng ký listener khi fragment bị hủy
        TrafficSignRepository repository = TrafficSignRepository.getInstance(requireContext());
        repository.unregisterDataListener(signs -> {});
    }

    private void setupNewGame() {
        // Reset trạng thái game
        userAnswers.clear();
        correctAnswers.clear();
        
        // Hiển thị/ẩn các view cần thiết
        buttonCheck.setVisibility(View.VISIBLE);
        buttonReset.setVisibility(View.VISIBLE);
        buttonNext.setVisibility(View.GONE);
        textResult.setVisibility(View.GONE);
        
        // Reset các vùng thả
        resetDropAreas();
        
        // Chọn ngẫu nhiên 3 biển báo
        selectRandomSigns();
        
        // Hiển thị biển báo và mô tả
        displaySignsAndDescriptions();
    }

    private void resetDropAreas() {
        dropImage1.setVisibility(View.GONE);
        dropImage2.setVisibility(View.GONE);
        dropImage3.setVisibility(View.GONE);
        
        // Hiện lại các textview hint
        textDragHint1.setVisibility(View.VISIBLE);
        textDragHint2.setVisibility(View.VISIBLE);
        textDragHint3.setVisibility(View.VISIBLE);
        
        // Reset background của drop areas
        Drawable normalBackground = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_border_drop);
        dropArea1.setBackground(normalBackground);
        dropArea2.setBackground(normalBackground);
        dropArea3.setBackground(normalBackground);
    }

    private void selectRandomSigns() {
        currentGameSigns.clear();
        
        if (allTrafficSigns.size() < GAME_SIZE) {
            // Nếu không đủ biển báo, sử dụng tất cả
            currentGameSigns.addAll(allTrafficSigns);
        } else {
            // Chọn ngẫu nhiên 3 biển báo
            List<TrafficSign> shuffledSigns = new ArrayList<>(allTrafficSigns);
            Collections.shuffle(shuffledSigns);
            
            for (int i = 0; i < GAME_SIZE; i++) {
                currentGameSigns.add(shuffledSigns.get(i));
            }
        }
    }

    private void displaySignsAndDescriptions() {
        // Hiển thị các biển báo theo đúng thứ tự trong currentGameSigns
        displayTrafficSign(imageSign1, currentGameSigns.get(0));
        displayTrafficSign(imageSign2, currentGameSigns.get(1));
        displayTrafficSign(imageSign3, currentGameSigns.get(2));

        // Tạo bản sao của danh sách mô tả để xáo trộn
        List<String> descriptions = new ArrayList<>();
        for (TrafficSign sign : currentGameSigns) {
            descriptions.add(sign.getDescription());
        }
        
        // Xáo trộn thứ tự của các mô tả
        Collections.shuffle(descriptions);
        
        // Hiển thị mô tả đã xáo trộn
        textDesc1.setText(descriptions.get(0));
        textDesc2.setText(descriptions.get(1));
        textDesc3.setText(descriptions.get(2));
        
        // Cập nhật đáp án đúng dựa trên vị trí của các mô tả
        correctAnswers.clear();
        
        // Tìm biển báo tương ứng với từng mô tả và cập nhật đáp án
        for (int i = 0; i < GAME_SIZE; i++) {
            String desc = descriptions.get(i);
            
            // Xác định vị trí của biển báo tương ứng với mô tả này
            for (int j = 0; j < GAME_SIZE; j++) {
                if (currentGameSigns.get(j).getDescription().equals(desc)) {
                    // Biển báo tại vị trí j tương ứng với mô tả tại vị trí i
                    // Lưu đáp án đúng: dropArea[i] -> biển báo tại vị trí j
                    if (i == 0) correctAnswers.put(dropArea1.getId(), j);
                    else if (i == 1) correctAnswers.put(dropArea2.getId(), j);
                    else if (i == 2) correctAnswers.put(dropArea3.getId(), j);
                    break;
                }
            }
        }
    }

    private void displayTrafficSign(ImageView imageView, TrafficSign trafficSign) {
        if (trafficSign.getImageUrl() != null && !trafficSign.getImageUrl().isEmpty()) {
            // Đảm bảo ImageView có kích thước cố định
            imageView.getLayoutParams().width = dpToPx(110);
            imageView.getLayoutParams().height = dpToPx(110);
            
            // Xử lý các kiểu đường dẫn hình ảnh khác nhau
            if (trafficSign.getImageUrl().startsWith("http://") || trafficSign.getImageUrl().startsWith("https://")) {
                // URL đầy đủ từ API
                Glide.with(requireContext())
                    .load(trafficSign.getImageUrl())
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_error)
                    .fitCenter()
                    .into(imageView);
            } else if (trafficSign.getImageUrl().startsWith("bien_bao/")) {
                // Hình ảnh từ assets "bien_bao/pxxx.png"
                String fileName = trafficSign.getImageUrl().substring(
                        trafficSign.getImageUrl().lastIndexOf("/") + 1,
                        trafficSign.getImageUrl().lastIndexOf("."));
                
                // Thử tải từ drawable
                int resourceId = getResources().getIdentifier(
                        fileName, "drawable", requireContext().getPackageName());
                
                if (resourceId != 0) {
                    Glide.with(requireContext())
                        .load(resourceId)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_error)
                        .fitCenter()
                        .into(imageView);
                } else {
                    // Thử tải từ assets
                    Glide.with(requireContext())
                        .load("file:///android_asset/" + trafficSign.getImageUrl())
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_error)
                        .fitCenter()
                        .into(imageView);
                }
            } else {
                // Thử tải ảnh trực tiếp từ đường dẫn
                Glide.with(requireContext())
                    .load(trafficSign.getImageUrl())
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_error)
                    .fitCenter()
                    .into(imageView);
            }
        } else {
            imageView.setImageResource(R.drawable.ic_image_placeholder);
        }
        
        // Đặt tag để xác định biển báo khi kéo thả
        imageView.setTag(currentGameSigns.indexOf(trafficSign));
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupDragAndDrop() {
        // Thiết lập các nguồn kéo (biển báo)
        setupDragSource(imageSign1);
        setupDragSource(imageSign2);
        setupDragSource(imageSign3);
        
        // Thiết lập các vùng thả
        setupDropTarget(dropArea1);
        setupDropTarget(dropArea2);
        setupDropTarget(dropArea3);
    }

    private void setupDragSource(View view) {
        view.setOnLongClickListener(v -> {
            ClipData.Item item = new ClipData.Item(v.getTag().toString());
            ClipData dragData = new ClipData(
                    v.getTag().toString(),
                    new String[] { ClipDescription.MIMETYPE_TEXT_PLAIN },
                    item);
            
            View.DragShadowBuilder shadow = new View.DragShadowBuilder(v);
            v.startDragAndDrop(dragData, shadow, v, 0);
            return true;
        });
        
        view.setOnTouchListener((v, event) -> {
            // Bắt đầu kéo thả khi chạm
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                ClipData.Item item = new ClipData.Item(v.getTag().toString());
                ClipData dragData = new ClipData(
                        v.getTag().toString(),
                        new String[] { ClipDescription.MIMETYPE_TEXT_PLAIN },
                        item);
                
                View.DragShadowBuilder shadow = new View.DragShadowBuilder(v);
                v.startDragAndDrop(dragData, shadow, v, 0);
                return true;
            }
            return false;
        });
    }

    private void setupDropTarget(FrameLayout dropArea) {
        dropArea.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN);
                    
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setBackgroundResource(R.drawable.rounded_border);
                    return true;
                    
                case DragEvent.ACTION_DRAG_LOCATION:
                    return true;
                    
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setBackgroundResource(R.drawable.rounded_border_drop);
                    return true;
                    
                case DragEvent.ACTION_DROP:
                    // Lấy thông tin từ sự kiện kéo thả
                    ClipData.Item item = event.getClipData().getItemAt(0);
                    String signPosition = item.getText().toString();
                    
                    // Ghi nhớ lựa chọn của người dùng
                    userAnswers.put(v.getId(), Integer.parseInt(signPosition));
                    
                    // Lấy view gốc (biển báo)
                    View draggedView = (View) event.getLocalState();
                    
                    // Hiển thị biển báo trong vùng thả
                    displayDroppedSign(v.getId(), draggedView);
                    
                    return true;
                    
                case DragEvent.ACTION_DRAG_ENDED:
                    v.setBackgroundResource(R.drawable.rounded_border_drop);
                    return true;
                    
                default:
                    return false;
            }
        });
    }

    private void displayDroppedSign(int dropAreaId, View draggedView) {
        ImageView targetImageView;
        TextView targetHintText;
        
        if (dropAreaId == dropArea1.getId()) {
            targetImageView = dropImage1;
            targetHintText = textDragHint1;
        } else if (dropAreaId == dropArea2.getId()) {
            targetImageView = dropImage2;
            targetHintText = textDragHint2;
        } else if (dropAreaId == dropArea3.getId()) {
            targetImageView = dropImage3;
            targetHintText = textDragHint3;
        } else {
            return;
        }
        
        // Đảm bảo ImageView thả có kích thước cố định
        targetImageView.getLayoutParams().width = dpToPx(70);
        targetImageView.getLayoutParams().height = dpToPx(70);
        
        // Lấy drawable từ biển báo gốc
        ImageView sourceImageView = (ImageView) draggedView;
        Drawable drawable = sourceImageView.getDrawable();
        
        // Hiển thị trong vùng thả
        targetImageView.setImageDrawable(drawable);
        targetImageView.setVisibility(View.VISIBLE);
        
        // Ẩn text "Kéo biển báo vào đây"
        targetHintText.setVisibility(View.GONE);
    }

    private void setupButtons() {
        buttonCheck.setOnClickListener(v -> checkAnswers());
        
        buttonReset.setOnClickListener(v -> {
            resetDropAreas();
            userAnswers.clear();
            textResult.setVisibility(View.GONE);
        });
        
        buttonNext.setOnClickListener(v -> setupNewGame());
    }

    private void checkAnswers() {
        // Kiểm tra xem người dùng đã điền đủ câu trả lời chưa
        if (userAnswers.size() < GAME_SIZE) {
            Toast.makeText(requireContext(), "Vui lòng kéo đủ 3 biển báo vào các ô", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Kiểm tra đáp án
        int correctCount = 0;
        
        // Đánh dấu đáp án đúng/sai
        checkAndHighlightAnswer(dropArea1, dropImage1, correctAnswers.get(dropArea1.getId()), userAnswers.get(dropArea1.getId()));
        checkAndHighlightAnswer(dropArea2, dropImage2, correctAnswers.get(dropArea2.getId()), userAnswers.get(dropArea2.getId()));
        checkAndHighlightAnswer(dropArea3, dropImage3, correctAnswers.get(dropArea3.getId()), userAnswers.get(dropArea3.getId()));
        
        // Đếm số đáp án đúng
        for (Map.Entry<Integer, Integer> entry : userAnswers.entrySet()) {
            if (entry.getValue().equals(correctAnswers.get(entry.getKey()))) {
                correctCount++;
            }
        }
        
        // Hiển thị kết quả
        textResult.setText(getString(R.string.mini_game_result, correctCount, GAME_SIZE));
        textResult.setVisibility(View.VISIBLE);
        
        // Hiển thị nút tiếp theo, ẩn nút kiểm tra và nút reset
        buttonCheck.setVisibility(View.GONE);
        buttonReset.setVisibility(View.GONE);
        buttonNext.setVisibility(View.VISIBLE);
        
        // Hiển thị thông báo
        if (correctCount == GAME_SIZE) {
            Toast.makeText(requireContext(), R.string.mini_game_perfect, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), R.string.mini_game_good, Toast.LENGTH_SHORT).show();
        }
    }

    private void checkAndHighlightAnswer(FrameLayout dropArea, ImageView dropImage, Integer correctAnswer, Integer userAnswer) {
        if (userAnswer == null) return;
        
        boolean isCorrect = userAnswer.equals(correctAnswer);
        
        // Đánh dấu đáp án đúng/sai bằng viền màu
        if (isCorrect) {
            dropArea.setBackgroundResource(android.R.color.holo_green_light);
        } else {
            dropArea.setBackgroundResource(android.R.color.holo_red_light);
        }
    }

    /**
     * Chuyển đổi từ dp sang pixels
     */
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
} 