package com.example.giaothong.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.example.giaothong.R;
import com.example.giaothong.data.DailyProgressManager;
import com.example.giaothong.repository.TrafficSignRepository;
import com.example.giaothong.utils.OfflineDataManager;
import com.example.giaothong.utils.SharedPreferencesManager;
import com.example.giaothong.utils.ThemeUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * Fragment màn hình cài đặt
 */
public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";
    private SharedPreferencesManager prefsManager;
    private OfflineDataManager offlineDataManager;
    private TrafficSignRepository repository;
    private SwitchMaterial switchDarkMode;
    private Slider sliderDailyGoal;
    private TextView textDailyGoalValue;
    private DailyProgressManager progressManager;
    
    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        
        prefsManager = new SharedPreferencesManager(requireContext());
        repository = TrafficSignRepository.getInstance(requireContext());
        offlineDataManager = repository.getOfflineDataManager();
        progressManager = new DailyProgressManager(
                PreferenceManager.getDefaultSharedPreferences(requireContext()));
        
        // Chế độ tối
        switchDarkMode = view.findViewById(R.id.switch_dark_mode);
        switchDarkMode.setChecked(prefsManager.isDarkMode());
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefsManager.setDarkMode(isChecked);
            ThemeUtils.setDarkMode(isChecked);
            Toast.makeText(
                    requireContext(),
                    isChecked ? R.string.dark_mode_on : R.string.dark_mode_off,
                    Toast.LENGTH_SHORT
            ).show();
        });
        
        // Thiết lập mục tiêu học tập hàng ngày
        sliderDailyGoal = view.findViewById(R.id.slider_daily_goal);
        textDailyGoalValue = view.findViewById(R.id.text_daily_goal_value);
        
        // Lấy giá trị mục tiêu hiện tại
        int currentGoal = progressManager.getDailyGoal();
        sliderDailyGoal.setValue(currentGoal);
        textDailyGoalValue.setText(String.valueOf(currentGoal));
        
        // Cập nhật khi người dùng thay đổi giá trị
        sliderDailyGoal.addOnChangeListener((slider, value, fromUser) -> {
            int goalValue = (int) value;
            textDailyGoalValue.setText(String.valueOf(goalValue));
            if (fromUser) {
                progressManager.setDailyGoal(goalValue);
                Toast.makeText(
                        requireContext(),
                        "Đã cập nhật mục tiêu: " + goalValue + " biển báo mỗi ngày",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
        
        // Quản lý dữ liệu offline
        MaterialButton btnOfflineData = view.findViewById(R.id.btn_offline_data);
        btnOfflineData.setOnClickListener(v -> showOfflineDataDialog());
        
        return view;
    }
    
    /**
     * Hiển thị dialog quản lý dữ liệu offline
     */
    private void showOfflineDataDialog() {
        // Tạo và hiển thị dialog
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_offline_data, null);
        
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();
        
        // Lấy các view trong dialog
        SwitchMaterial switchOfflineMode = dialogView.findViewById(R.id.switch_offline_mode);
        TextView textLastDownload = dialogView.findViewById(R.id.text_last_download);
        Button btnDownload = dialogView.findViewById(R.id.btn_download_data);
        ProgressBar progressDownload = dialogView.findViewById(R.id.progress_download);
        
        // Thiết lập trạng thái ban đầu
        switchOfflineMode.setChecked(offlineDataManager.isOfflineEnabled());
        String lastDownloadTime = offlineDataManager.getLastDownloadTimeFormatted();
        if (lastDownloadTime != null && !lastDownloadTime.isEmpty()) {
            textLastDownload.setText(getString(R.string.offline_last_download, lastDownloadTime));
        } else {
            textLastDownload.setText("No data downloaded yet");
        }
        
        // Xử lý sự kiện bật/tắt chế độ offline
        switchOfflineMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            offlineDataManager.setOfflineEnabled(isChecked);
            Toast.makeText(
                    requireContext(),
                    isChecked ? R.string.offline_mode_enabled : R.string.offline_mode_disabled,
                    Toast.LENGTH_SHORT
            ).show();
        });
        
        // Xử lý sự kiện tải dữ liệu
        btnDownload.setOnClickListener(v -> {
            btnDownload.setEnabled(false);
            progressDownload.setVisibility(View.VISIBLE);
            
            // Thực hiện tải dữ liệu
            repository.downloadDataForOffline(success -> {
                // Chạy trên main thread
                btnDownload.setEnabled(true);
                progressDownload.setVisibility(View.GONE);
                
                if (success) {
                    // Cập nhật thời gian tải
                    String newDownloadTime = offlineDataManager.getLastDownloadTimeFormatted();
                    textLastDownload.setText(getString(R.string.offline_last_download, newDownloadTime));
                    
                    Toast.makeText(requireContext(), R.string.offline_download_success, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), R.string.offline_download_failed, Toast.LENGTH_SHORT).show();
                }
            });
        });
        
        dialog.show();
    }
} 