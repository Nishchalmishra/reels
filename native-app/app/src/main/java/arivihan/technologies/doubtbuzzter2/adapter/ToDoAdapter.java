package arivihan.technologies.doubtbuzzter2.adapter;

/*
 * File: CouponAdapter Copyright 22/3/22 5:01 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 22/3/22 5:01 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.activity.PersonalisedLecture;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.MicrolectureListActivity;
import arivihan.technologies.doubtbuzzter2.model.ToDoContentType;
import arivihan.technologies.doubtbuzzter2.model.UserDailyRoutineDto;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.ViewHolder> {

    List<UserDailyRoutineDto> toDoRecyclerItemLists;
    private Context context;
    boolean hideTapTarget;
    boolean useOldLayout;
    TodoItemClickListener listener;

    public ToDoAdapter(List<UserDailyRoutineDto> toDoRecyclerItemList, Context context, boolean hideTapTarget, boolean useOldLayout, TodoItemClickListener listener) {
        this.toDoRecyclerItemLists = toDoRecyclerItemList;
        this.context = context;
        this.hideTapTarget = hideTapTarget;
        this.useOldLayout = useOldLayout;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ToDoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(useOldLayout ? R.layout.to_do_list_recycler_item_old : R.layout.to_do_list_recycler_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ToDoAdapter.ViewHolder holder, int position) {
        UserDailyRoutineDto userDailyRoutineDto = toDoRecyclerItemLists.get(position);
        holder.tv_title.setText(userDailyRoutineDto.getTitle());
        holder.tv_sub_title.setText(userDailyRoutineDto.getSubtitle());
        holder.ll_time_layout.setVisibility(View.GONE);

        if (userDailyRoutineDto.getTaskCompleted() == null) {
            holder.cv_task_status.setVisibility(View.GONE);
            holder.ll_time_layout.setAlpha(1.0f);
            holder.view_marker.setBackgroundColor(StringUtils.isNotEmpty(userDailyRoutineDto.getSubjectColorHex()) ? Color.parseColor(userDailyRoutineDto.getSubjectColorHex()) : ((userDailyRoutineDto.getCompleted() == null || !userDailyRoutineDto.getCompleted()) ? context.getColor(R.color.grey_300) : context.getColor(R.color.arivihan_secondary_color)));
        } else {
            holder.cv_task_status.setVisibility(View.VISIBLE);
            holder.iv_task_status.setImageResource(userDailyRoutineDto.getTaskCompleted() ? R.drawable.img_right_green : R.drawable.img_cancel_close_red);
            holder.ll_time_layout.setAlpha(userDailyRoutineDto.getTaskCompleted() ? 1.0f : 0.5f);
            holder.tv_sub_title.setAlpha(userDailyRoutineDto.getTaskCompleted() ? 1.0f : 0.5f);
            holder.view_marker.setBackgroundColor(userDailyRoutineDto.getTaskCompleted() ? (StringUtils.isNotEmpty(userDailyRoutineDto.getSubjectColorHex()) ? Color.parseColor(userDailyRoutineDto.getSubjectColorHex()) : context.getColor(R.color.arivihan_secondary_color)) : context.getColor(R.color.grey_400));
        }

        if (userDailyRoutineDto.getCompleted() == null || !userDailyRoutineDto.getCompleted()) {
//            holder.view_marker.setBackgroundColor(context.getColor(R.color.grey_300));
            holder.iv_icon.setImageResource(R.drawable.ic_done_white);
        } else {
//            holder.view_marker.setBackgroundColor(context.getColor(R.color.arivihan_secondary_color));
            holder.iv_icon.setImageResource(R.drawable.ic_done_blue);
        }

        if (StringUtils.isNotEmpty(userDailyRoutineDto.getStartTime()) && StringUtils.isNotEmpty(userDailyRoutineDto.getEndTime())) {
            holder.ll_time_layout.setVisibility(View.VISIBLE);
            holder.tv_start_date.setText(userDailyRoutineDto.getStartTime());
            holder.tv_end_date.setText(userDailyRoutineDto.getEndTime());
        }

        holder.cv_parent.setOnClickListener(view -> {
            Intent intent;
            if (listener != null) {
                listener.onTodoItemClick(userDailyRoutineDto);
            }
            if ((userDailyRoutineDto.getToDoContentType() == ToDoContentType.TEST_SERIES && StringUtils.isNotEmpty(userDailyRoutineDto.getDeeplink()))
                    || userDailyRoutineDto.getToDoContentType() == ToDoContentType.NOTES && StringUtils.isNotEmpty(userDailyRoutineDto.getDeeplink())) {
                if (StringUtils.isNotEmpty(userDailyRoutineDto.getClassName())) {
                    MyApplication.openActivity(context, userDailyRoutineDto.getClassName(), userDailyRoutineDto.getActivityParams());
                } else if (StringUtils.isNotEmpty(userDailyRoutineDto.getDeeplink())) {
                    MyApplication.openDeepLink(context, userDailyRoutineDto.getDeeplink());
                }
                Map<String, String> params = new HashMap<>();
                params.put("cardType", (userDailyRoutineDto.getToDoContentType() == ToDoContentType.TEST_SERIES) ? "test-series" : "note");
                params.put("className", userDailyRoutineDto.getClassName());
                params.put("activityParams", userDailyRoutineDto.getActivityParams().toString());
                params.put("deeplink", userDailyRoutineDto.getDeeplink());
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.todo_main_screen_task_click.getActionName(), params);
            } else {
                if (userDailyRoutineDto.getCustomChapter() != null && userDailyRoutineDto.getCustomChapter() == Boolean.TRUE) {
                    intent = new Intent(context.getApplicationContext(), PersonalisedLecture.class);
                } else if ((userDailyRoutineDto.getBoard() != null && userDailyRoutineDto.getBoard() == Boolean.TRUE) ||
                        (userDailyRoutineDto.getBoard() == null && MyApplication.getBoolData(PreferenceKeys.BOARD_SWITCH.getKey()))) {
                    intent = new Intent(context.getApplicationContext(), MicrolectureListActivity.class);
                } else {
                    intent = new Intent(context.getApplicationContext(), MicrolectureListActivity.class);
                }
                intent.putExtra("chapterId", userDailyRoutineDto.getChapterId());
                intent.putExtra("chapterName", userDailyRoutineDto.getChapterTitle());
                intent.putExtra("hideTapTarget", hideTapTarget);
                intent.putExtra("microLectureId", userDailyRoutineDto.getMicroLectureId());
                MyApplication.saveData(PreferenceKeys.MICROLECTURE_ID.getKey(), userDailyRoutineDto.getMicroLectureId());
                MyApplication.saveData(PreferenceKeys.MICROLECTURE_DATA_RELOAD.getKey(), false);

                Map<String, String> params = new HashMap<>();
//                params.put("dto", userDailyRoutineDto.toString());
                if (userDailyRoutineDto.getToDoContentType() == ToDoContentType.VIDEO) {
                    intent.putExtra("openVideo", true);
                    MyApplication.saveData(userDailyRoutineDto.getChapterTitle() + PreferenceKeys.TYPE.getKey(), 0);
                    params.put("cardType", "lecture");
                    context.startActivity(intent);
                } else {
                    intent.putExtra("openVideo", false);
                    MyApplication.saveData(userDailyRoutineDto.getChapterTitle() + PreferenceKeys.TYPE.getKey(), 1);
                    params.put("cardType", "practice");
                    context.startActivity(intent);
                }
                params.put("parametersDetail", String.valueOf(intent));
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.todo_main_screen_task_click.getActionName(), params);
            }
        });
    }

    @Override
    public int getItemCount() {
        return toDoRecyclerItemLists.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView iv_icon;
        private LinearLayout cv_parent, ll_time_layout;
        private TextView tv_title, tv_sub_title, tv_start_date, tv_end_date;
        private View view_marker;
        private CardView cv_task_status;
        private ImageView iv_task_status;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_title = itemView.findViewById(R.id.tv_title);
            tv_sub_title = itemView.findViewById(R.id.tv_sub_title);
            iv_icon = itemView.findViewById(R.id.iv_icon);
            cv_parent = itemView.findViewById(R.id.cv_parent);
            view_marker = itemView.findViewById(R.id.view_marker);
            ll_time_layout = itemView.findViewById(R.id.ll_time_layout);
            tv_start_date = itemView.findViewById(R.id.tv_start_date);
            tv_end_date = itemView.findViewById(R.id.tv_end_date);
            cv_task_status = itemView.findViewById(R.id.cv_task_status);
            iv_task_status = itemView.findViewById(R.id.iv_task_status);
        }
    }

    public static interface TodoItemClickListener {
        void onTodoItemClick(UserDailyRoutineDto userDailyRoutineDto);
    }

}
