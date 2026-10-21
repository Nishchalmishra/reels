package arivihan.technologies.doubtbuzzter2.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.enums.asat.ASATStep;
import arivihan.technologies.doubtbuzzter2.model.asat.ASATStepsInfo;

public class AsatAdapter extends RecyclerView.Adapter<AsatAdapter.AsatViewHolder> {

    Context context;
    ArrayList<ASATStepsInfo> list;
    AsatListener asatListener;

    float minText1Size = Float.MAX_VALUE;
    float minText2Size = Float.MAX_VALUE;
    boolean calculated = false;
    private final WeakReference<Context> contextRef;

    public AsatAdapter(Context context, ArrayList<ASATStepsInfo> list, AsatListener asatListener) {
        this.context = context;
        this.list = list;
        this.asatListener = asatListener;
        this.contextRef = new WeakReference<>(context);
    }

    @NonNull
    @Override
    public AsatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.asat_item,parent,false);
        return new AsatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AsatViewHolder holder, int position) {
         ASATStepsInfo info = list.get(position);
         holder.number.setText(String.valueOf(info.getStepNumber()));
         holder.text1.setText(info.getStepText1());
         holder.text2.setText(info.getStepText2());

         holder.iv_done.setVisibility(View.GONE);
         if(info.isDone()) {
             holder.iv_done.setVisibility(View.VISIBLE);
         }

        int placeholderResId = R.drawable.step1; // default

        if (info.getStepNumber() == ASATStep.UPLOAD_MARKSHEET.index() + 1) {
            placeholderResId = R.drawable.step1;
        } else if (info.getStepNumber() == ASATStep.COMPLETE_TEST.index() + 1) {
            placeholderResId = R.drawable.step2;
        } else if (info.getStepNumber() == ASATStep.TEST_RESULT.index() + 1) {
            placeholderResId = R.drawable.result_stat;
        }

        Glide.with(context)
                .load(info.getIcon())           // this is icon URL
                .placeholder(placeholderResId)  // shown while loading as placeholder
                .error(placeholderResId)       // in case of error showing placeholder local
                .into(holder.icon);

         holder.itemView.setOnClickListener(v->{
             if(asatListener!=null) {
                 asatListener.stepsClicked(info.getStepNumber() - 1);
             }
         });

        if (!calculated) {
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                    holder.text1, 12, 19, 1, TypedValue.COMPLEX_UNIT_SP
            );
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                    holder.text2, 6, 14, 1, TypedValue.COMPLEX_UNIT_SP
            );

            holder.text1.post(() -> {

                Context context = contextRef.get();
                if (context == null || (context instanceof Activity && (((Activity) context).isFinishing() || ((Activity) context).isDestroyed()))) {
                    return;
                }

                float size1 = holder.text1.getTextSize();
                float size2 = holder.text2.getTextSize();

                if (size1 < minText1Size) minText1Size = size1;
                if (size2 < minText2Size) minText2Size = size2;

                if (position == getItemCount() - 1) {
                    calculated = true;
                    try {
                        notifyDataSetChanged();
                    } catch (IllegalStateException ignored) {}
                }
            });

        } else {
            // applying the same fixed size to all text views
            TextViewCompat.setAutoSizeTextTypeWithDefaults(holder.text1, TextViewCompat.AUTO_SIZE_TEXT_TYPE_NONE);
            holder.text1.setTextSize(TypedValue.COMPLEX_UNIT_PX, minText1Size);

            TextViewCompat.setAutoSizeTextTypeWithDefaults(holder.text2, TextViewCompat.AUTO_SIZE_TEXT_TYPE_NONE);
            holder.text2.setTextSize(TypedValue.COMPLEX_UNIT_PX, minText2Size);
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class AsatViewHolder extends RecyclerView.ViewHolder{
        TextView number, text1, text2;
        ImageView icon, iv_done;

        public AsatViewHolder(@NonNull View itemView) {
            super(itemView);
            number = itemView.findViewById(R.id.step_number);
            text1 = itemView.findViewById(R.id.text1);
            text2 = itemView.findViewById(R.id.text2);
            icon = itemView.findViewById(R.id.icon);
            iv_done = itemView.findViewById(R.id.iv_done);
        }
    }

    public interface AsatListener{
        default void stepsClicked(int step){}
    }
}
