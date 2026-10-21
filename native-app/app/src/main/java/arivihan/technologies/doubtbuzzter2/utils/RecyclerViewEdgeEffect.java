package arivihan.technologies.doubtbuzzter2.utils;

import android.content.Context;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewEdgeEffect {


    public static void edgeEffectRecyclerView(RecyclerView rv, Context context, boolean isHorizontal) {
        if (context == null) return;

        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, context.getResources().getDisplayMetrics());
        rv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int pos = parent.getChildAdapterPosition(view);
                assert parent.getAdapter() != null;
                int last = parent.getAdapter().getItemCount() - 1;
                if (isHorizontal) {
                    outRect.left = (pos == 0) ? padding : 0;
                    outRect.right = (pos == last) ? padding : 0;
                } else {
                    outRect.top = (pos == 0) ? padding : 0;
                    outRect.bottom = (pos == last) ? padding : 0;
                }
            }
        });
    }


}
