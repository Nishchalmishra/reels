package arivihan.technologies.doubtbuzzter2.adapter;

/*
 * File: DisambiguationAdapter Copyright 4/10/22 3:00 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 4/10/22 3:00 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.MLResponse;

public class DisambiguationAdapter extends RecyclerView.Adapter<DisambiguationAdapter.ViewHolder> {

    private List<MLResponse> mlResponseList;
    private OptionOnClick optionOnClick;
    private String askedDoubt;

    public DisambiguationAdapter(List<MLResponse> mlResponseList, OptionOnClick optionOnClick, String s) {
        this.mlResponseList = mlResponseList;
        this.optionOnClick = optionOnClick;
        this.askedDoubt = s;
    }

    @NonNull
    @Override
    public DisambiguationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.new_optionitem, parent, false);
        return new DisambiguationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DisambiguationAdapter.ViewHolder holder, int position) {
        String doubt = mlResponseList.get(position).getTextToDisplay();
        if (StringUtils.isNotEmpty(doubt) && doubt.length() > 1) {
            doubt = doubt.substring(0, 1).toUpperCase() + doubt.substring(1);
        }
        holder.textView.setText(doubt);
        holder.cardView.setOnClickListener(view -> {
            optionOnClick.intentClicked(mlResponseList.get(position), askedDoubt);
        });
    }

    @Override
    public int getItemCount() {
        return mlResponseList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;
        private CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_dis);
            textView = itemView.findViewById(R.id.option_something_else);
        }
    }

    public interface OptionOnClick {
        void intentClicked(MLResponse mlResponse, String doubt);
    }
}
