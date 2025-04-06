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

import com.example.giaothong.R;
import com.example.giaothong.repository.TrafficSignRepository;
import com.example.giaothong.utils.OfflineDataManager;
import com.example.giaothong.utils.SharedPreferencesManager;
import com.example.giaothong.utils.ThemeUtils;
import com.google.android.material.button.MaterialButton;
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
    
    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        
        prefsManager = new SharedPreferencesManager(requireContext());
        repository = TrafficSignRepository.getInstance(requireContext());
        offlineDataManager = repository.getOfflineDataManager();
        
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
        
        // Quản lý dữ liệu offline
        MaterialButton btnOfflineData = view.findViewById(R.id.btn_offline_data);
        btnOfflineData.setOnClickListener(v -> showOfflineDataDialog());
        
        return view;
    }
    
    /**
     * Hiển thị dialog quản lý dữ liệu offline
     */
    private void showOfflineDataDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_offline_data, null);
        
        SwitchMaterial switchOfflineMode = dialogView.findViewById(R.id.switch_offline_mode);
        TextView textLastDownload = dialogView.findViewById(R.id.text_last_download);
        MaterialButton btnDownloadData = dialogView.findViewById(R.id.btn_download_data);
        MaterialButton btnClearData = dialogView.findViewById(R.id.btn_clear_offline_data);
        ProgressBar progressDownload = dialogView.findViewById(R.id.progress_download);
        TextView textStatus = dialogView.findViewById(R.id.text_download_status);
        
        // Hiển thị trạng thái hiện tại
        switchOfflineMode.setChecked(offlineDataManager.isOfflineEnabled());
        textLastDownload.setText(getString(R.string.offline_last_download, 
                offlineDataManager.getLastDownloadTimeFormatted()));
        
        // Nếu chưa có dữ liệu, disable nút xóa
        btnClearData.setEnabled(offlineDataManager.hasOfflineData());
        
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton(R.string.close, null)
            .create();
        
        // Xử lý sự kiện bật/tắt chế độ offline
        switchOfflineMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            offlineDataManager.setOfflineEnabled(isChecked);
        });
        
        // Xử lý sự kiện tải dữ liệu
        btnDownloadData.setOnClickListener(v -> {
            // Kiểm tra kết nối mạng
            if (!offlineDataManager.isNetworkAvailable()) {
                Toast.makeText(requireContext(), 
                        "Không có kết nối mạng. Vui lòng kết nối mạng để tải dữ liệu.", 
                        Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Hiển thị tiến trình
            btnDownloadData.setEnabled(false);
            btnClearData.setEnabled(false);
            progressDownload.setVisibility(View.VISIBLE);
            textStatus.setVisibility(View.GONE);
            
            // Bắt đầu tải dữ liệu
            repository.downloadDataForOffline(success -> {
                requireActivity().runOnUiThread(() -> {
                    progressDownload.setVisibility(View.GONE);
                    
                    if (success) {
                        textStatus.setText(R.string.offline_download_success);
                        textStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                        textLastDownload.setText(getString(R.string.offline_last_download, 
                                offlineDataManager.getLastDownloadTimeFormatted()));
                    } else {
                        textStatus.setText(R.string.offline_download_fail);
                        textStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    }
                    
                    textStatus.setVisibility(View.VISIBLE);
                    btnDownloadData.setEnabled(true);
                    btnClearData.setEnabled(offlineDataManager.hasOfflineData());
                });
            });
        });
        
        // Xử lý sự kiện xóa dữ liệu
        btnClearData.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                .setTitle("Xóa dữ liệu ngoại tuyến")
                .setMessage("Bạn có chắc chắn muốn xóa dữ liệu đã tải không?")
                .setPositiveButton("Xóa", (d, w) -> {
                    offlineDataManager.clearOfflineData();
                    Toast.makeText(requireContext(), R.string.offline_data_cleared, Toast.LENGTH_SHORT).show();
                    textLastDownload.setText(getString(R.string.offline_last_download, 
                            offlineDataManager.getLastDownloadTimeFormatted()));
                    btnClearData.setEnabled(false);
                })
                .setNegativeButton("Hủy", null)
                .show();
        });
        
        dialog.show();
    }
} 