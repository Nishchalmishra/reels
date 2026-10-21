package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.os.Build;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.BoardQuestionActivity;
import arivihan.technologies.doubtbuzzter2.fragments.LongAnswerQuestionFragment2;
import arivihan.technologies.doubtbuzzter2.model.MatchTheseRightOption;

public class MatchTheseAdapterRight extends RecyclerView.Adapter<MatchTheseAdapterRight.MatchTheseViewHolderRight> implements ItemTouchHelperAdapter {

    ArrayList<MatchTheseRightOption> list ;
    Context context;
    private ItemTouchHelper mTouchHelper;
    private Integer height;

    public MatchTheseAdapterRight(ArrayList<MatchTheseRightOption> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public MatchTheseViewHolderRight onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.match_these_item,parent,false);
        return new MatchTheseViewHolderRight(view);
    }

    public void stopDragging() {
       // mTouchHelper.attachToRecyclerView(null);
    }
    @Override
    public void onBindViewHolder(@NonNull MatchTheseViewHolderRight holder, int position) {
       holder.textView.setText( list.get(position).getOption_number() + ". " + list.get(position).getValue());
        if (height != null) {
            holder.textView.setHeight(height);
        }
       if(BoardQuestionActivity.isMatchTheseSubmitted || LongAnswerQuestionFragment2.isMatchTheseSubmittedMock) {
           if (list.get(position).isCorrectlyMatched()) {
               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                   holder.cardView.setOutlineAmbientShadowColor(context.getResources().getColor(R.color.match_green));
                   holder.cardView.setOutlineSpotShadowColor(context.getResources().getColor(R.color.match_green));
               }
               holder.linearLayout.setBackground(AppCompatResources.getDrawable(context, R.drawable.match_these_border_green));
           } else {
               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                   holder.cardView.setOutlineAmbientShadowColor(context.getResources().getColor(R.color.match_red));
                   holder.cardView.setOutlineSpotShadowColor(context.getResources().getColor(R.color.match_red));

               }
               holder.linearLayout.setBackground(AppCompatResources.getDrawable(context, R.drawable.match_these_border_red));           }
       }

    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void OnItemMove(int fromposition, int toposition) {
        MatchTheseRightOption option = list.get(fromposition);
        list.remove(option);
        list.add(toposition,option);
        notifyItemMoved(fromposition,toposition);
    }

    public  void setTouchHelper(ItemTouchHelper touchHelper){
        this.mTouchHelper =  touchHelper;
    }
    public class MatchTheseViewHolderRight extends RecyclerView.ViewHolder implements View.OnTouchListener,GestureDetector.OnGestureListener {

        CardView cardView;
        TextView textView;
        GestureDetector gestureDetector;
        LinearLayout linearLayout;
        boolean draggable;

        public MatchTheseViewHolderRight(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.match_these_cardView);
            textView = itemView.findViewById(R.id.option_mt);
            linearLayout = itemView.findViewById(R.id.match_these_linear_layout);
            gestureDetector = new GestureDetector(context, this);
            itemView.setOnTouchListener(this);
        }

        @Override
        public boolean onDown(@NonNull MotionEvent motionEvent) {
            return false;
        }

        @Override
        public void onShowPress(@NonNull MotionEvent motionEvent) {

        }

        @Override
        public boolean onSingleTapUp(@NonNull MotionEvent motionEvent) {
            return true;
        }

        @Override
        public boolean onScroll(@NonNull MotionEvent motionEvent, @NonNull MotionEvent motionEvent1, float v, float v1) {
            return false;
        }

        @Override
        public void onLongPress(@NonNull MotionEvent motionEvent) {
            if (mTouchHelper != null) {
                mTouchHelper.startDrag(this);
            }
        }

        @Override
        public boolean onFling(@NonNull MotionEvent motionEvent, @NonNull MotionEvent motionEvent1, float v, float v1) {
            return true;
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (gestureDetector != null)
                gestureDetector.onTouchEvent(motionEvent);
            return false;
        }
    }

}
