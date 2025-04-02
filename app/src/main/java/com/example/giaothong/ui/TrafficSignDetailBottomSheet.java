package com.example.giaothong.ui;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.giaothong.R;
import com.example.giaothong.model.TrafficSign;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * Bottom sheet dialog để hiển thị chi tiết biển báo
 */
public class TrafficSignDetailBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_TRAFFIC_SIGN = "traffic_sign";
    
    private TrafficSign trafficSign;
    
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
        
        // Khởi tạo views
        TextView textCategory = view.findViewById(R.id.textSignCategory);
        //TextView textId = view.findViewById(R.id.textSignId);
        ImageView imageSign = view.findViewById(R.id.imageSign);
        TextView textName = view.findViewById(R.id.textSignName);
        TextView textDescription = view.findViewById(R.id.textSignDescription);
        
        // Gán dữ liệu
        textCategory.setText(trafficSign.getCategory());
        //textId.setText(trafficSign.getId());
        textName.setText(trafficSign.getName());
        textDescription.setText(trafficSign.getDescription());
        
        // Thiết lập màu sắc dựa trên danh mục
        int categoryColor = getCategoryColor(trafficSign.getCategory());
        textCategory.setTextColor(categoryColor);
        
        // Load hình ảnh
        String imageUrl = trafficSign.getImagePath();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            if (imageUrl.startsWith("http")) {
                // Load hình ảnh từ URL
                Glide.with(this)
                    .load(imageUrl)
                    .apply(new RequestOptions()
                            .fitCenter())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(imageSign);
            } else {
                // Load hình ảnh từ assets
                String assetPath = "file:///android_asset/" + imageUrl;
                Glide.with(this)
                    .load(assetPath)
                    .apply(new RequestOptions()
                            .fitCenter())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(imageSign);
            }
        } else {
            imageSign.setImageResource(R.drawable.ic_launcher_foreground);
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
        
        if (category.equals("Biển báo cấm")) {
            return ContextCompat.getColor(getContext(), R.color.colorProhibitory);
        } else if (category.equals("Biển báo nguy hiểm")) {
            return ContextCompat.getColor(getContext(), R.color.colorWarning);
        } else if (category.equals("Biển báo hiệu lệnh")) {
            return ContextCompat.getColor(getContext(), R.color.colorMandatory);
        } else if (category.equals("Biển báo chỉ dẫn")) {
            return ContextCompat.getColor(getContext(), R.color.colorGuide);
        } else {
            return ContextCompat.getColor(getContext(), R.color.colorTextSecondary);
        }
    }
} 