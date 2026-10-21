package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.UserSyllabusOrder;

public class RoutineEditFragmentAdapter extends RecyclerView.Adapter<RoutineEditFragmentAdapter.ViewHolder>
implements ItemTouchHelperAdapter {

    List<UserSyllabusOrder> routineDefaultOrders;
    private ItemTouchHelper mTouchHelper;
    private Context context;

    public RoutineEditFragmentAdapter(List<UserSyllabusOrder> routineDefaultOrders, Context context) {
        this.routineDefaultOrders = routineDefaultOrders;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.edit_routine_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserSyllabusOrder routineDefaultOrder = routineDefaultOrders.get(position);

        setStatus(holder, routineDefaultOrder);
        holder.subject.setText(routineDefaultOrder.getChapterName());

        holder.checkBox.setOnClickListener(view -> {
            routineDefaultOrder.setSelected(!routineDefaultOrder.isSelected());
            setStatus(holder, routineDefaultOrder);
        });

    }

    private void setStatus(@NonNull ViewHolder holder, UserSyllabusOrder routineDefaultOrder) {
        if (routineDefaultOrder.isSelected()) {
            holder.checkBox.setChecked(true);
           // holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.navy_blue));
        } else {
            holder.checkBox.setChecked(false);
           // holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.grey_500));
        }
    }

    @Override
    public int getItemCount() {
        return routineDefaultOrders.size();
    }

    @Override
    public void OnItemMove(int fromposition, int toposition) {
        UserSyllabusOrder tm = routineDefaultOrders.get(fromposition);
        routineDefaultOrders.remove(tm);
        routineDefaultOrders.add(toposition,tm);
        notifyItemMoved(fromposition,toposition);
    }

    public  void setTouchHelper(ItemTouchHelper touchHelper){
        this.mTouchHelper =  touchHelper;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnTouchListener, GestureDetector.OnGestureListener {

        TextView subject;
        ImageView icon;
        MaterialCardView cardView;
        CheckBox checkBox;
        GestureDetector gestureDetector;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            subject = itemView.findViewById(R.id.text);
            icon = itemView.findViewById(R.id.icon);
            cardView = itemView.findViewById(R.id.card);
            checkBox = itemView.findViewById(R.id.checkbox);
            gestureDetector = new GestureDetector(context,this);
            itemView.setOnTouchListener(this);
        }


        @Override
        public boolean onDown(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {

            return true;
        }

        @Override
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent motionEvent) {
            mTouchHelper.startDrag(this);
        }

        @Override
        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            return true;
        }

        @Override
        public void onClick(View view) {

        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            gestureDetector.onTouchEvent(motionEvent);
            return true;
        }
    }

}
