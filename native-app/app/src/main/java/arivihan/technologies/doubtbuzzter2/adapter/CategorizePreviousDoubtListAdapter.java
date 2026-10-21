package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.DoubtCategoryObject;

public class CategorizePreviousDoubtListAdapter
        extends RecyclerView.Adapter<CategorizePreviousDoubtListAdapter.CategorizePreviousDoubtListViewHolder> {

    private static final String TAG = "CategorizePrevAdapter";

    private Context context;
    private ArrayList<DoubtCategoryObject> lists;
    private boolean allowBack;

    // ✅ Pool for nested RecyclerView (Android 6 performance fix)
    private RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();

    public CategorizePreviousDoubtListAdapter(Context context,
                                              ArrayList<DoubtCategoryObject> lists,
                                              boolean allowBack) {

        this.context = context;
        this.lists = lists;
        this.allowBack = allowBack;

        Log.d(TAG, "Adapter Initialized");
        Log.d(TAG, "Total Categories Received = " + (lists != null ? lists.size() : 0));
        Log.d(TAG, "AllowBack = " + allowBack);
    }

    @NonNull
    @Override
    public CategorizePreviousDoubtListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        Log.d(TAG, "onCreateViewHolder called");

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.doubt_categorization, parent, false);

        return new CategorizePreviousDoubtListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategorizePreviousDoubtListViewHolder holder, int position) {

        Log.d(TAG, "onBindViewHolder called for position = " + position);

        DoubtCategoryObject doubtCategoryObject = lists.get(position);

        if (doubtCategoryObject == null) {
            Log.e(TAG, "Category Object is NULL at position: " + position);
            return;
        }

        // -------------------------------
        // ✅ CATEGORY TITLE SETUP
        // -------------------------------
        String categoryKey = doubtCategoryObject.getTitle();
        Log.d(TAG, "Category Title Key = " + categoryKey);

        // categoryKey can be null when the record has no historyCategory (e.g. arivihan-sessions data).
        // Resources.getIdentifier(null, ...) throws NullPointerException, so guard it.
        int resId = categoryKey != null
                ? context.getResources().getIdentifier(categoryKey, "string", context.getPackageName())
                : 0;

        if (resId != 0) {
            holder.category_title.setText(context.getString(resId));
            Log.d(TAG, "Category Title Set from Resource");
        } else {
            holder.category_title.setText(categoryKey != null ? categoryKey : "");
            Log.w(TAG, "String Resource NOT FOUND → Using Raw Title: " + categoryKey);
        }

        // -------------------------------
        // ✅ INNER LIST SETUP
        // -------------------------------
        int recordSize = doubtCategoryObject.getWebChatRecords() != null
                ? doubtCategoryObject.getWebChatRecords().size()
                : 0;

        Log.d(TAG, "Total Doubts inside Category = " + recordSize);

        PreviousDoubtListAdapterNew innerAdapter =
                new PreviousDoubtListAdapterNew(
                        context,
                        doubtCategoryObject.getWebChatRecords(),
                        allowBack
                );

        // Layout Manager for Inner RecyclerView
        holder.category_rv.setLayoutManager(
                new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        );

        // -------------------------------
        // ✅ ANDROID 6 FIXES FOR NESTED RV
        // -------------------------------
        holder.category_rv.setRecycledViewPool(viewPool); // Performance boost
        holder.category_rv.setNestedScrollingEnabled(false); // Fix scroll issue
//        holder.category_rv.setHasFixedSize(true); // Fix layout measuring

        holder.category_rv.setAdapter(innerAdapter);

        Log.d(TAG, "Inner RecyclerView Adapter Attached Successfully");
    }

    @Override
    public int getItemCount() {

        int count = lists != null ? lists.size() : 0;
        Log.d(TAG, "getItemCount called → Total Categories = " + count);

        return count;
    }

    // -------------------------------
    // ✅ IMPORTANT: Update Adapter After API Response
    // -------------------------------
    public void updateList(ArrayList<DoubtCategoryObject> newList) {

        Log.d(TAG, "updateList called → New Category Size = " +
                (newList != null ? newList.size() : 0));

        this.lists = newList;
        notifyDataSetChanged();
    }

    // -------------------------------
    // VIEW HOLDER
    // -------------------------------
    public class CategorizePreviousDoubtListViewHolder extends RecyclerView.ViewHolder {

        TextView category_title;
        RecyclerView category_rv;

        public CategorizePreviousDoubtListViewHolder(@NonNull View itemView) {
            super(itemView);

            Log.d(TAG, "ViewHolder Initialized");

            category_title = itemView.findViewById(R.id.category_title);
            category_rv = itemView.findViewById(R.id.category_rv);
        }
    }
}
