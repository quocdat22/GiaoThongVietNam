package com.example.giaothong.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.giaothong.model.TrafficSign;
import com.example.giaothong.utils.DataUtils;

import java.util.List;

/**
 * ViewModel to manage traffic sign data
 */
public class TrafficSignViewModel extends ViewModel {
    
    private final MutableLiveData<List<TrafficSign>> trafficSignsLiveData = new MutableLiveData<>();
    
    public TrafficSignViewModel() {
        // Load sample data
        loadTrafficSigns();
    }
    
    /**
     * Loads traffic signs from data source
     * In a real app, this might involve a repository pattern and database/network calls
     */
    private void loadTrafficSigns() {
        // For now, we're using sample data
        List<TrafficSign> signs = DataUtils.getSampleTrafficSigns();
        trafficSignsLiveData.setValue(signs);
    }
    
    /**
     * Get LiveData object containing traffic signs
     */
    public LiveData<List<TrafficSign>> getTrafficSigns() {
        return trafficSignsLiveData;
    }
} 