package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.UserSyllabusOrder;

public class ToDoChapterSelectionAdapter extends RecyclerView.Adapter<ToDoChapterSelectionAdapter.ToDoChapterSelectionViewHolder>
        implements ItemTouchHelperAdapter {

    private Context context;
    private ArrayList<UserSyllabusOrder> list;
    private ItemTouchHelper mTouchHelper;
    private ToDoChapterSelectionListener listener;
    private String checkedText, unCheckedText;

    public ToDoChapterSelectionAdapter(Context context, ArrayList<UserSyllabusOrder> list, String checkedText, String unCheckedText, ToDoChapterSelectionListener listener) {
        this.context = context;
        this.list = list;
        this.checkedText = checkedText;
        this.unCheckedText = unCheckedText;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ToDoChapterSelectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.to_do_list_chapter_selection_item, parent, false);
        return new ToDoChapterSelectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ToDoChapterSelectionViewHolder holder, int position) {

        UserSyllabusOrder userSyllabusOrder = list.get(position);
//        setStatus(holder,userSyllabusOrder,position);
        if (position == 0)
            holder.topDash.setVisibility(View.INVISIBLE);
        else
            holder.topDash.setVisibility(View.VISIBLE);

        if (position == list.size() - 1) {
            holder.bottomDash.setVisibility(View.INVISIBLE);
            holder.divider.setVisibility(View.INVISIBLE);
        }else{
            holder.bottomDash.setVisibility(View.VISIBLE);
            holder.divider.setVisibility(View.VISIBLE);
        }

        holder.tv_chapter_title.setText(userSyllabusOrder.getChapterName());
        holder.chapter_number.setText((position+1)>=10 ? String.valueOf(position+1) : "0"+(position+1));
        holder.cv_selection.setStrokeColor(context.getColor((userSyllabusOrder.isSelected() && userSyllabusOrder.isEnabled() )? R.color.subscription_blue : R.color.gray_d1));
        holder.cv_selection.setCardBackgroundColor(context.getColor(userSyllabusOrder.isSelected()? (userSyllabusOrder.isEnabled()? R.color.subscription_blue : R.color.gray_d1): R.color.white));
        holder.iv_selection.setImageResource(userSyllabusOrder.isSelected()? R.drawable.correct : 0);

        if(userSyllabusOrder.isEnabled()) {
            holder.tv_chapter_marker.setText(userSyllabusOrder.isSelected()?checkedText:unCheckedText);
        }

        holder.ll_parent_layout.setOnClickListener(view -> {
            if(userSyllabusOrder.isEnabled()) {
                listener.onChapterSelection(position, userSyllabusOrder.isSelected());
            }
        });
    }

    private void setStatus(@NonNull ToDoChapterSelectionViewHolder holder, UserSyllabusOrder routineDefaultOrder,int position) {
        if (routineDefaultOrder.isSelected()) {
            holder.cv_selection.setCardBackgroundColor(context.getColor(R.color.subscription_blue));
        } else {
            holder.cv_selection.setCardBackgroundColor(context.getColor(R.color.white));
        }
        list.set(position,routineDefaultOrder);
        listener.onChapterListChange(list);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void OnItemMove(int fromposition, int toposition) {
        UserSyllabusOrder tm = list.get(fromposition);
        list.remove(tm);
        list.add(toposition, tm);
        notifyItemMoved(fromposition, toposition);
    }

    @Override
    public void clearView() {
        notifyDataSetChanged();
    }

    public void setTouchHelper(ItemTouchHelper touchHelper) {
        this.mTouchHelper = touchHelper;
    }

    public class ToDoChapterSelectionViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener, GestureDetector.OnGestureListener {

        TextView chapter_number;
        TextView tv_chapter_title, tv_chapter_marker;
        LinearLayout topDash;
        LinearLayout bottomDash;
        MaterialCardView cv_selection;
        LinearLayout selection_card_layout, ll_parent_layout;
        LinearLayout divider;
        ImageView iv_selection;
        GestureDetector gestureDetector;

        public ToDoChapterSelectionViewHolder(@NonNull View itemView) {
            super(itemView);
            chapter_number = itemView.findViewById(R.id.chapter_number);
            tv_chapter_title = itemView.findViewById(R.id.tv_chapter_title);
            tv_chapter_marker = itemView.findViewById(R.id.tv_chapter_marker);
            topDash = itemView.findViewById(R.id.top_dash);
            bottomDash = itemView.findViewById(R.id.bottom_dash);
            cv_selection = itemView.findViewById(R.id.cv_selection);
            selection_card_layout = itemView.findViewById(R.id.selection_card_layout);
            divider = itemView.findViewById(R.id.divider);
            ll_parent_layout = itemView.findViewById(R.id.ll_parent_layout);
            iv_selection = itemView.findViewById(R.id.iv_selection);
//            gestureDetector = new GestureDetector(context, this);
//            itemView.setOnTouchListener(this);
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
        public boolean onScroll(@Nullable MotionEvent motionEvent, @NonNull MotionEvent motionEvent1, float v, float v1) {
            return false;
        }

        @Override
        public void onLongPress(@NonNull MotionEvent motionEvent) {
            mTouchHelper.startDrag(this);
        }

        @Override
        public boolean onFling(@Nullable MotionEvent motionEvent, @NonNull MotionEvent motionEvent1, float v, float v1) {
            return true;
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            gestureDetector.onTouchEvent(motionEvent);
            view.performClick();
            return true;
        }
    }

    public interface ToDoChapterSelectionListener{
        void onChapterListChange(ArrayList<UserSyllabusOrder> userSyllabusOrders);
        void onChapterSelection(int position, boolean isSelected);
    }
}
