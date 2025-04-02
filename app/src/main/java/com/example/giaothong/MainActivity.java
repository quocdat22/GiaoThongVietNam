package com.example.giaothong;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.giaothong.adapter.TrafficSignAdapter;
import com.example.giaothong.model.TrafficSign;
import com.example.giaothong.viewmodel.TrafficSignViewModel;

public class MainActivity extends AppCompatActivity {

    private TrafficSignViewModel viewModel;
    private TrafficSignAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Set up edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerViewSigns);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns
        
        // Initialize ViewModel - use the proper constructor for Java
        viewModel = new ViewModelProvider(this).get(TrafficSignViewModel.class);
        
        // Observe traffic sign data
        viewModel.getTrafficSigns().observe(this, trafficSigns -> {
            // Create and set adapter when data is available
            adapter = new TrafficSignAdapter(this, trafficSigns);
            
            // Set item click listener
            adapter.setOnItemClickListener((trafficSign, position) -> {
                // Show traffic sign details (to be implemented in next steps)
                showTrafficSignDetails(trafficSign);
            });
            
            recyclerView.setAdapter(adapter);
        });
    }
    
    /**
     * Display basic information about the traffic sign
     * In future implementations, this would navigate to a detail screen
     */
    private void showTrafficSignDetails(TrafficSign trafficSign) {
        String message = trafficSign.getName() + "\n" + trafficSign.getDescription();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}