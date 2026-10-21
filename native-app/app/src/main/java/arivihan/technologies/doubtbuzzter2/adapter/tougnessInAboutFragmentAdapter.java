package arivihan.technologies.doubtbuzzter2.adapter;

import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.ToughnessInAboutFragmentModel;

public class tougnessInAboutFragmentAdapter extends RecyclerView.Adapter<tougnessInAboutFragmentAdapter.ViewHolder>
implements ItemTouchHelperAdapter {

    ArrayList<ToughnessInAboutFragmentModel> toughnessInAboutFragmentModels;
    private ItemTouchHelper mTouchHelper;

    public tougnessInAboutFragmentAdapter(ArrayList<ToughnessInAboutFragmentModel> toughnessInAboutFragmentModels) {
        this.toughnessInAboutFragmentModels = toughnessInAboutFragmentModels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.toughnesslayoutinaboutfragment,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ToughnessInAboutFragmentModel tm = toughnessInAboutFragmentModels.get(position);

        holder.subject.setText(tm.getSubject());


    }

    @Override
    public int getItemCount() {
        return toughnessInAboutFragmentModels.size();
    }

    @Override
    public void OnItemMove(int fromposition, int toposition) {
        ToughnessInAboutFragmentModel tm = toughnessInAboutFragmentModels.get(fromposition);
        toughnessInAboutFragmentModels.remove(tm);
        toughnessInAboutFragmentModels.add(toposition,tm);
        notifyItemMoved(fromposition,toposition);
    }

    public  void setTouchHelper(ItemTouchHelper touchHelper){
        this.mTouchHelper =  touchHelper;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnTouchListener, GestureDetector.OnGestureListener {

        TextView subject;
        ImageView icon;
        GestureDetector gestureDetector;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            subject = itemView.findViewById(R.id.text);
            icon = itemView.findViewById(R.id.icon);

            gestureDetector = new GestureDetector(itemView.getContext(),this);
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
