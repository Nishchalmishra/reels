package arivihan.technologies.doubtbuzzter2;

import android.util.Log;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import arivihan.technologies.doubtbuzzter2.adapter.ItemTouchHelperAdapter;

public class MyItemTouchHelper extends ItemTouchHelper.Callback {

    private final ItemTouchHelperAdapter mAdapter;
    private boolean mDraggable = true;

    public MyItemTouchHelper(ItemTouchHelperAdapter mAdapter) {
        this.mAdapter = mAdapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        LinearLayout linearLayout = viewHolder.itemView.findViewById(R.id.match_these_linear_layout);
        if (linearLayout != null) {
            linearLayout.setBackgroundColor(ContextCompat.getColor(linearLayout.getContext(), R.color.white));
        } else {
            Log.e("ItemTouchHelper", "LinearLayout not found in the view");
        }

        LinearLayout linearLayout2 = viewHolder.itemView.findViewById(R.id.card);
        if (linearLayout2 != null) {
            linearLayout2.setBackgroundColor(ContextCompat.getColor(linearLayout2.getContext(), R.color.white));
        } else {
            Log.e("ItemTouchHelper", "LinearLayout not found in the view");
        }

        try {
            super.clearView(recyclerView, viewHolder);
            mAdapter.clearView();
        } catch (Exception e) {
        }
    }

    @Override
    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            LinearLayout linearLayout = viewHolder.itemView.findViewById(R.id.match_these_linear_layout);
            if (linearLayout != null) {
                linearLayout.setBackgroundColor(ContextCompat.getColor(linearLayout.getContext(), R.color.light_blue));
            } else {
                Log.e("ItemTouchHelper", "LinearLayout not found in the view");
            }

            LinearLayout linearLayout2 = viewHolder.itemView.findViewById(R.id.card);
            if (linearLayout2 != null) {
                linearLayout2.setBackgroundColor(ContextCompat.getColor(linearLayout2.getContext(), R.color.light_blue));
            } else {
                Log.e("ItemTouchHelper", "LinearLayout not found in the view");
            }

        }
        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        final int dragFlag = mDraggable ? ItemTouchHelper.UP | ItemTouchHelper.DOWN : 0;
        return makeMovementFlags(dragFlag, 0);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        mAdapter.OnItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

    }

    public void setDraggable(boolean value) {
        mDraggable = value;
    }
}
