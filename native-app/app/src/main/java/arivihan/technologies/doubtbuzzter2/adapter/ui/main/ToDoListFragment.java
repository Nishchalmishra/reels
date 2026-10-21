package arivihan.technologies.doubtbuzzter2.adapter.ui.main;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.adapter.ToDoAdapter;
import arivihan.technologies.doubtbuzzter2.databinding.FragmentToDoListMainBinding;
import arivihan.technologies.doubtbuzzter2.model.UserDailyRoutineDto;



/*
 * File: ToDoListFragment Copyright 19/07/23, 1:28 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 19/07/23, 1:28 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

/**
 * A placeholder fragment containing a simple view.
 */
public class ToDoListFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String USER_DAILY_ROUTINE = "daily_routine";

    private PageViewModel pageViewModel;
    private ToDoAdapter toDoAdapter;
    private int index;
    private FragmentToDoListMainBinding binding;
    private List<UserDailyRoutineDto> userDailyRoutineDtos;

    public static ToDoListFragment newInstance(int index, List<UserDailyRoutineDto> userDailyRoutineDtos) {
        ToDoListFragment fragment = new ToDoListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        bundle.putParcelableArrayList(USER_DAILY_ROUTINE,
                (ArrayList<? extends Parcelable>) userDailyRoutineDtos);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // pageViewModel = new ViewModelProvider(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
            userDailyRoutineDtos = getArguments().getParcelableArrayList(USER_DAILY_ROUTINE);
        }
        //pageViewModel.setIndex(index);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentToDoListMainBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        RecyclerView toDoRecyclerItem = binding.toDoRecyclerItem;

        toDoAdapter = new ToDoAdapter(userDailyRoutineDtos, getContext(),true, true, null);
        toDoRecyclerItem.setLayoutManager(new LinearLayoutManager(getContext()));
        toDoRecyclerItem.setAdapter(toDoAdapter);
//        final TextView textView = binding.sectionLabel;
//        pageViewModel.getText().observe(getViewLifecycleOwner(), s -> textView.setText(s));
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}