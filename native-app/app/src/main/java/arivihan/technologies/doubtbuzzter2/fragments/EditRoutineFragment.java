package arivihan.technologies.doubtbuzzter2.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.MyItemTouchHelper;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.RoutineEditFragmentAdapter;
import arivihan.technologies.doubtbuzzter2.model.RoutineDefaultOrder;
import arivihan.technologies.doubtbuzzter2.model.UserSyllabusOrder;

/*
 * File: EditRoutineFragment Copyright 23/08/23, 1:44 AM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 23/08/23, 1:44 AM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditRoutineFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditRoutineFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    // TODO: Rename and change types of parameters
    private RecyclerView toughnmessrecyclerview;
    private List<UserSyllabusOrder> userSyllabusOrders;
    public EditRoutineFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param mContext
     * @param param1   Parameter 1.
     * @return A new instance of fragment EditRoutineFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EditRoutineFragment newInstance(Context mContext, List<UserSyllabusOrder> userSyllabusOrders) {
        EditRoutineFragment fragment = new EditRoutineFragment();
        Bundle args = new Bundle();

        args.putParcelableArrayList(ARG_PARAM1, (ArrayList<? extends Parcelable>) userSyllabusOrders);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userSyllabusOrders = getArguments().getParcelableArrayList(ARG_PARAM1);
        }
        if (CollectionUtils.isEmpty(userSyllabusOrders)) {
            userSyllabusOrders = new ArrayList<>();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_routine2, container, false);

        toughnmessrecyclerview = view.findViewById(R.id.toughness_recyclerview);
        toughnmessrecyclerview.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL ,false));
        rearrangeOrder();
        return view;
    }

    private void rearrangeOrder() {
        RoutineEditFragmentAdapter adapter = new RoutineEditFragmentAdapter(userSyllabusOrders, getContext());
        ItemTouchHelper.Callback callback = new MyItemTouchHelper(adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        adapter.setTouchHelper(itemTouchHelper);
        itemTouchHelper.attachToRecyclerView(toughnmessrecyclerview);
        toughnmessrecyclerview.setAdapter(adapter);
    }


}