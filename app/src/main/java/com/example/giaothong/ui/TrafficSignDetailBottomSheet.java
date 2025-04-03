package com.example.giaothong.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.giaothong.R;
import com.example.giaothong.model.TrafficSign;
import com.example.giaothong.utils.SharedPreferencesManager;
import com.example.giaothong.viewmodel.TrafficSignViewModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Bottom sheet dialog để hiển thị chi tiết biển báo
 */
public class TrafficSignDetailBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_TRAFFIC_SIGN = "traffic_sign";
    
    private TrafficSign trafficSign;
    private SharedPreferencesManager prefsManager;
    private OnPinStatusChangeListener pinStatusChangeListener;
    
    /**
     * Tạo instance mới của bottom sheet
     * @param trafficSign Biển báo cần hiển thị chi tiết
     * @return Instance của bottom sheet
     */
    public static TrafficSignDetailBottomSheet newInstance(TrafficSign trafficSign) {
        TrafficSignDetailBottomSheet fragment = new TrafficSignDetailBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TRAFFIC_SIGN, trafficSign);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme);
        
        if (getArguments() != null) {
            trafficSign = (TrafficSign) getArguments().getSerializable(ARG_TRAFFIC_SIGN);
        }
        prefsManager = new SharedPreferencesManager(requireContext());
    }
    
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        
        // Kiểm tra nếu activity implement interface OnPinStatusChangeListener
        if (context instanceof OnPinStatusChangeListener) {
            pinStatusChangeListener = (OnPinStatusChangeListener) context;
        }
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        
        dialog.setOnShowListener(dialogInterface -> {
            // Khi dialog hiển thị, mở rộng nó ra nửa màn hình
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
            View bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(false);
                behavior.setHideable(true);
            }
        });
        
        return dialog;
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_traffic_sign_detail, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (trafficSign == null) {
            dismiss();
            return;
        }
        
        // Tìm các view
        TextView textCategory = view.findViewById(R.id.textCategory);
        //TextView textSignId = view.findViewById(R.id.textSignId);
        ImageView imageSign = view.findViewById(R.id.imageSign);
        TextView textSignName = view.findViewById(R.id.textSignName);
        TextView textSignDescription = view.findViewById(R.id.textSignDescription);
        FloatingActionButton fabPin = view.findViewById(R.id.fabPin);
        
        // Đặt dữ liệu
        textCategory.setText(getCategoryDisplayName(trafficSign.getCategory()));
        //textSignId.setText(getString(R.string.sign_id_format, trafficSign.getId()));
        textSignName.setText(trafficSign.getName());
        textSignDescription.setText(trafficSign.getDescription());
        
        // Cập nhật trạng thái ghim
        updatePinButtonState(fabPin, trafficSign.isPinned());
        
        // Xử lý sự kiện khi nhấn nút ghim
        fabPin.setOnClickListener(v -> {
            // Đảo trạng thái ghim
            boolean newPinState = !trafficSign.isPinned();
            trafficSign.setPinned(newPinState);
            
            // Cập nhật trong SharedPreferences
            if (newPinState) {
                prefsManager.addPinnedSign(trafficSign.getId());
            } else {
                prefsManager.removePinnedSign(trafficSign.getId());
            }
            
            // Cập nhật giao diện nút
            updatePinButtonState(fabPin, newPinState);
            
            // Hiển thị thông báo
            Toast.makeText(
                    requireContext(),
                    newPinState ? R.string.sign_pinned : R.string.sign_unpinned,
                    Toast.LENGTH_SHORT
            ).show();
            
            // Thông báo cho activity về sự thay đổi
            if (pinStatusChangeListener != null) {
                pinStatusChangeListener.onPinStatusChanged(trafficSign, newPinState);
            }
        });
        
        // Load hình ảnh biển báo
        String imagePath = trafficSign.getImageUrl();
        if (imagePath != null && !imagePath.isEmpty()) {
            if (imagePath.startsWith("http")) {
                // Nếu là URL, sử dụng Glide để load ảnh từ internet
                Glide.with(requireContext())
                        .load(imagePath)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(imageSign);
            } else {
                try {
                    // Nếu không phải URL, load từ assets
                    Glide.with(requireContext())
                            .load("file:///android_asset/" + imagePath)
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.error_image)
                            .into(imageSign);
                } catch (Exception e) {
                    // Xử lý lỗi nếu có
                    imageSign.setImageResource(R.drawable.error_image);
                }
            }
        } else {
            // Nếu không có đường dẫn ảnh
            imageSign.setImageResource(R.drawable.placeholder_image);
        }
    }
    
    /**
     * Lấy màu dựa vào loại biển báo
     * @param category Loại biển báo
     * @return Màu tương ứng
     */
    private int getCategoryColor(String category) {
        if (getContext() == null) {
            return 0;
        }
        
        if (category.equals("bien_bao_cam")) {
            return ContextCompat.getColor(getContext(), R.color.colorCam);
        } else if (category.equals("bien_nguy_hiem_va_canh_bao")) {
            return ContextCompat.getColor(getContext(), R.color.colorNguyHiem);
        } else if (category.equals("bien_hieu_lenh")) {
            return ContextCompat.getColor(getContext(), R.color.colorHieuLenh);
        } else if (category.equals("bien_chi_dan")) {
            return ContextCompat.getColor(getContext(), R.color.colorChiDan);
        } else if (category.equals("bien_phu")) {
            return ContextCompat.getColor(getContext(), R.color.colorPhu);
        } else {
            return ContextCompat.getColor(getContext(), R.color.colorTextSecondary);
        }
    }

    /**
     * Chuyển đổi mã danh mục sang tên hiển thị thân thiện với người dùng
     * @param categoryCode Mã danh mục
     * @return Tên hiển thị của danh mục
     */
    private String getCategoryDisplayName(String categoryCode) {
        switch (categoryCode) {
            case "bien_bao_cam":
                return "Biển báo cấm";
            case "bien_nguy_hiem_va_canh_bao":
                return "Biển nguy hiểm và cảnh báo";
            case "bien_hieu_lenh":
                return "Biển hiệu lệnh";
            case "bien_chi_dan":
                return "Biển chỉ dẫn";
            case "bien_phu":
                return "Biển phụ";
            default:
                return categoryCode;
        }
    }

    /**
     * Cập nhật trạng thái nút ghim
     */
    private void updatePinButtonState(FloatingActionButton fab, boolean isPinned) {
        if (isPinned) {
            // Đã ghim: màu đỏ
            fab.setImageResource(R.drawable.ic_pin_filled);
            fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.colorPinIcon)));
            fab.setContentDescription(getString(R.string.unpin_sign));
        } else {
            // Chưa ghim: màu xám
            fab.setImageResource(R.drawable.ic_pin_outline);
            fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.colorTextSecondary)));
            fab.setContentDescription(getString(R.string.pin_sign));
        }
    }
    
    /**
     * Interface để thông báo về sự thay đổi trạng thái ghim
     */
    public interface OnPinStatusChangeListener {
        void onPinStatusChanged(TrafficSign trafficSign, boolean isPinned);
    }
} 