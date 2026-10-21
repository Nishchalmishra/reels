package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;


import java.util.List;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;

public class InstructionBodyAdapter extends RecyclerView.Adapter<InstructionBodyAdapter.ViewHolder> {

    Context context;
    List<String> list;
    private boolean hideNumber;
    private boolean white;

    public InstructionBodyAdapter(List<String> list, boolean hideNumber, boolean white) {
        this.list = list;
        this.hideNumber = hideNumber;
        this.white = white;
    }


    public InstructionBodyAdapter(Context context,List<String> list, boolean hideNumber) {
        this.context=context;
        this.list = list;
        this.hideNumber = hideNumber;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.instruction_body_layout, parent, false);
        return new InstructionBodyAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if (hideNumber) {
            holder.text.setText(list.get(position));
            Typeface typeface = ResourcesCompat.getFont(MyApplication.getInstance(), R.font.urbanist_medium);
            holder.text.setTypeface(typeface);
            holder.text.setTextSize(12);
        } else {
            holder.text.setText((position + 1) + ") " + list.get(position));
        }

        if (white) {
            //holder.text.setTextColor(Color.parseColor(Colors.WHITE.getColorValue().asHex()));
        }

        if (hideNumber) {
            holder.divider.setVisibility(View.VISIBLE);
            holder.dotView.setVisibility(View.VISIBLE);
            if (position == 1) {
//                holder.lottieAnimationView.setVisibility(View.VISIBLE);
//                holder.lottieAnimationView.setAnimation(R.raw.lottie_edit_routine_instruction_dialog);
//                holder.lottieAnimationView.playAnimation();
            }

            if (position == getItemCount() - 1) {
                holder.divider.setVisibility(View.INVISIBLE);
            }
        }
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView text;
        private View divider;
        private LottieAnimationView lottieAnimationView;
        private View dotView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.body);
            divider = itemView.findViewById(R.id.divider);
            lottieAnimationView = itemView.findViewById(R.id.lottie_animation);
            dotView = itemView.findViewById(R.id.dotview);

        }
    }
}
