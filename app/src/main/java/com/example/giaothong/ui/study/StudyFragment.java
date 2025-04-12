package com.example.giaothong.ui.study;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.giaothong.R;
import com.example.giaothong.ui.FlashcardsFragment;
import com.example.giaothong.ui.quiz.QuizFragment;
import com.google.android.material.button.MaterialButton;

/**
 * Fragment that contains both Flashcards and Quiz modes with a toggle to switch between them
 */
public class StudyFragment extends Fragment {

    private MaterialButton buttonFlashcards;
    private MaterialButton buttonQuiz;
    
    private static final int MODE_FLASHCARDS = 1;
    private static final int MODE_QUIZ = 2;
    private int currentMode = MODE_FLASHCARDS;

    public StudyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_study, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize toggle buttons
        buttonFlashcards = view.findViewById(R.id.buttonFlashcards);
        buttonQuiz = view.findViewById(R.id.buttonQuiz);
        
        // Set click listeners for toggle buttons
        buttonFlashcards.setOnClickListener(v -> switchMode(MODE_FLASHCARDS));
        buttonQuiz.setOnClickListener(v -> switchMode(MODE_QUIZ));
        
        // Check if a specific mode was requested through arguments
        Bundle args = getArguments();
        if (args != null && args.containsKey("study_mode")) {
            currentMode = args.getInt("study_mode", MODE_FLASHCARDS);
        }
        // Otherwise, if restoring state, use saved mode
        else if (savedInstanceState != null) {
            currentMode = savedInstanceState.getInt("current_mode", MODE_FLASHCARDS);
        }
        
        // Initial mode setup
        switchMode(currentMode);
    }
    
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("current_mode", currentMode);
    }
    
    /**
     * Switch between Flashcards and Quiz modes
     * @param mode The mode to switch to (MODE_FLASHCARDS or MODE_QUIZ)
     */
    private void switchMode(int mode) {
        currentMode = mode;
        
        // Update toggle button checked states to use the selector-based styling
        if (mode == MODE_FLASHCARDS) {
            buttonFlashcards.setChecked(true);
            buttonQuiz.setChecked(false);
        } else {
            buttonFlashcards.setChecked(false);
            buttonQuiz.setChecked(true);
        }
        
        // Replace the fragment in the container
        Fragment fragment = mode == MODE_FLASHCARDS 
                ? new FlashcardsFragment() 
                : new QuizFragment();
        
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.studyContentContainer, fragment);
        transaction.commit();
    }
} 