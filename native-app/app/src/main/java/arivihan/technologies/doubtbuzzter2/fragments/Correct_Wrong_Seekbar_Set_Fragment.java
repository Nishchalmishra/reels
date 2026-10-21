package arivihan.technologies.doubtbuzzter2.fragments;

import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import arivihan.technologies.doubtbuzzter2.R;

public class Correct_Wrong_Seekbar_Set_Fragment extends BottomSheetDialogFragment {

    CardView btn;
    SeekBar correct,wrong;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_correct__wrong__seekbar__set_, container, false);

        correct = view.findViewById(R.id.corectSeekbar);
        wrong = view.findViewById(R.id.wrongSeekbar);
        btn = view.findViewById(R.id.apply_card);


        int x = correct.getScrollX();
        int y = wrong.getScrollX();

        Log.i("bjhadc "+y,"asknx  "+x);

         btn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Toast.makeText(getActivity(),"  askjbj ",Toast.LENGTH_SHORT).show();
             }
         });

        return view;
    }
}