package com.example.giaothong.ui.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.giaothong.R;
import com.example.giaothong.repository.TrafficSignRepository;
import com.example.giaothong.utils.OfflineDataManager;
import com.example.giaothong.utils.SharedPreferencesManager;
import com.example.giaothong.utils.ThemeUtils;
import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * Fragment để quản lý cài đặt ứng dụng
 */
public class SettingsFragment extends Fragment {

    private SwitchMaterial switchDarkMode;
    private Button btnOfflineData;
    private Button btnClearHistory;
    private Button btnClearPins;
    private SharedPreferencesManager prefsManager;
    private OfflineDataManager offlineDataManager;
    private TrafficSignRepository repository;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo các thành phần
        prefsManager = new SharedPreferencesManager(requireContext());
        repository = TrafficSignRepository.getInstance(requireContext());
        offlineDataManager = repository.getOfflineDataManager();

        // Liên kết các view
        switchDarkMode = view.findViewById(R.id.switch_dark_mode);
        btnOfflineData = view.findViewById(R.id.btn_offline_data);
        btnClearHistory = view.findViewById(R.id.btn_clear_history);
        btnClearPins = view.findViewById(R.id.btn_clear_pins);

        // Thiết lập trạng thái Dark Mode
        boolean isDarkMode = prefsManager.isDarkMode();
        switchDarkMode.setChecked(isDarkMode);

        // Xử lý sự kiện chuyển đổi Dark Mode
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefsManager.setDarkMode(isChecked);
            ThemeUtils.applyThemeFromPreferences(prefsManager);
            Toast.makeText(requireContext(), 
                    isChecked ? R.string.dark_mode_enabled : R.string.dark_mode_disabled, 
                    Toast.LENGTH_SHORT).show();
        });

        // Xử lý sự kiện nút quản lý dữ liệu ngoại tuyến
        btnOfflineData.setOnClickListener(v -> {
            showOfflineDataDialog();
        });

        // TODO: Xử lý sự kiện xóa lịch sử tìm kiếm
        btnClearHistory.setOnClickListener(v -> {
            // Xử lý xóa lịch sử tìm kiếm
        });

        // TODO: Xử lý sự kiện xóa biển báo đã ghim
        btnClearPins.setOnClickListener(v -> {
            // Xử lý xóa các biển báo đã ghim
        });
    }

    /**
     * Hiển thị dialog quản lý dữ liệu ngoại tuyến
     */
    private void showOfflineDataDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_offline_data, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Liên kết các view trong dialog
        SwitchMaterial switchOfflineMode = dialogView.findViewById(R.id.switch_offline_mode);
        TextView tvLastDownload = dialogView.findViewById(R.id.text_last_download);
        Button btnDownload = dialogView.findViewById(R.id.btn_download_data);
        ProgressBar progressBar = dialogView.findViewById(R.id.progress_download);
        TextView tvStatus = dialogView.findViewById(R.id.text_download_status);
        Button btnClearData = dialogView.findViewById(R.id.btn_clear_offline_data);

        // Thiết lập trạng thái hiện tại
        switchOfflineMode.setChecked(offlineDataManager.isOfflineEnabled());
        tvLastDownload.setText(getString(R.string.offline_last_download, 
                offlineDataManager.getLastDownloadTimeFormatted()));

        // Xử lý sự kiện chuyển đổi chế độ ngoại tuyến
        switchOfflineMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            offlineDataManager.setOfflineEnabled(isChecked);
        });

        // Xử lý sự kiện nút tải dữ liệu
        btnDownload.setOnClickListener(v -> {
            // Kiểm tra kết nối mạng
            if (!offlineDataManager.isNetworkAvailable()) {
                Toast.makeText(requireContext(), 
                        R.string.offline_no_network, 
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Hiển thị trạng thái tải
            progressBar.setVisibility(View.VISIBLE);
            tvStatus.setVisibility(View.VISIBLE);
            tvStatus.setText(R.string.offline_downloading);
            btnDownload.setEnabled(false);

            // Tải dữ liệu
            repository.downloadDataForOffline(success -> {
                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnDownload.setEnabled(true);

                    if (success) {
                        tvStatus.setText(R.string.offline_download_success);
                        tvLastDownload.setText(getString(R.string.offline_last_download, 
                                offlineDataManager.getLastDownloadTimeFormatted()));
                    } else {
                        tvStatus.setText(R.string.offline_download_fail);
                    }
                });
            });
        });

        // Xử lý sự kiện nút xóa dữ liệu
        btnClearData.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.confirm_title)
                    .setMessage(R.string.confirm_clear_offline_data)
                    .setPositiveButton(android.R.string.yes, (d, which) -> {
                        boolean cleared = offlineDataManager.clearOfflineData();
                        if (cleared) {
                            tvLastDownload.setText(getString(R.string.offline_last_download, 
                                    offlineDataManager.getLastDownloadTimeFormatted()));
                            Toast.makeText(requireContext(), 
                                    R.string.offline_data_cleared, 
                                    Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        });
    }
} 