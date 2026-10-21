package arivihan.technologies.doubtbuzzter2.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.List;

import androidx.core.content.ContextCompat;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.listener.ChapterDetailActivityListener;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureContentDto;
import arivihan.technologies.doubtbuzzter2.model.TopicModel;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class PersonaliseLectureTopicDetailAdapter extends RecyclerView.Adapter<PersonaliseLectureTopicDetailAdapter.ViewHolder> implements Filterable {

    ArrayList<TopicModel> topicModels;
    ArrayList<TopicModel> topicModelsFull; // always contain full data
    Context context;
    ChapterDetailActivityListener listener;
    String chapterName;
    PersonaliseLectureTopicDetailAdapter.OnTopicClick onTopicClick;
    private boolean isShowTapTarget = true;

    public PersonaliseLectureTopicDetailAdapter(ArrayList<TopicModel> topicModels, Context context,
                                                ChapterDetailActivityListener listener, String chapterName, PersonaliseLectureTopicDetailAdapter.OnTopicClick onTopicClick) {
        this.topicModels = topicModels;
        this.context = context;
        this.topicModelsFull = new ArrayList<>(topicModels);
        this.listener = listener;
        this.chapterName = chapterName;
        this.onTopicClick = onTopicClick;
    }

    public PersonaliseLectureTopicDetailAdapter(ArrayList<TopicModel> topicModels, Context context,
                                                ChapterDetailActivityListener listener, String chapterName, PersonaliseLectureTopicDetailAdapter.OnTopicClick onTopicClick, boolean isShowTapTarget) {
        this.topicModels = topicModels;
        this.context = context;
        this.topicModelsFull = new ArrayList<>(topicModels);
        this.listener = listener;
        this.chapterName = chapterName;
        this.onTopicClick = onTopicClick;
        this.isShowTapTarget = isShowTapTarget;
    }


    @NonNull
    @Override
    public PersonaliseLectureTopicDetailAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.topic_detail_card, parent, false);
        return new PersonaliseLectureTopicDetailAdapter.ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull PersonaliseLectureTopicDetailAdapter.ViewHolder holder, int position) {
        TopicModel tm = topicModels.get(position);
        holder.title.setText(tm.getTitle());
        holder.question.setText(tm.getNoOfQuestions() + " " + context.getString(R.string.questions));
        holder.progress_text.setText(tm.getProgress_value() + "%");
        holder.progress.setProgress(Integer.parseInt(tm.getProgress_value()));

        if (tm.getIsNotWatched() != null && tm.getIsNotWatched()) {
            holder.badge.setVisibility(View.VISIBLE);
        } else {
            holder.badge.setVisibility(View.GONE);
        }

        ArrayList<MicroLectureContentDto> questionModel = tm.getQuestionsDtos();
        ArrayList<MicroLectureContentDto> lectureModel = tm.getLecturesDtos();

        PersonaliseLectureQuestionAdapter questionsAdapter = new PersonaliseLectureQuestionAdapter(questionModel,context);
        LecturesAdapter lecturesAdapter = new LecturesAdapter(context, lectureModel, listener);

        holder.recyclerViewQues.setHasFixedSize(true);
        holder.recyclerViewQues.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        holder.recyclerViewQues.setAdapter(questionsAdapter);

        holder.recyclerViewLec.setHasFixedSize(true);
        holder.recyclerViewLec.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        holder.recyclerViewLec.setAdapter(lecturesAdapter);

        if (!MyApplication.getBoolData(PreferenceKeys.TOPIC_DETAIL_ADAPTER.getKey()) && position == 0 && isShowTapTarget) {
//            TapTargetSequence targetSequence = new TapTargetSequence((Activity) context).targets(
//                    TapTarget.forView(holder.show_lec, context.getString(R.string.first_ai_based),
//                                    context.getString(R.string.ask_q))
//                            .outerCircleColor(R.color.arivihan_primary_color)
//                            .outerCircleAlpha(0.96f)
//                            .targetCircleColor(R.color.navy_blue)
//                            .titleTextSize(18)
//                            .titleTextColor(R.color.white)
//                            .descriptionTextSize(15)
//                            .titleTypeface(Typeface.DEFAULT_BOLD)
//                            .descriptionTextColor(R.color.white)
//                            .textColor(R.color.white)
//                            .textTypeface(Typeface.SANS_SERIF)
//                            .dimColor(R.color.black)
//                            .drawShadow(true)
//                            .cancelable(false)
//                            .tintTarget(true)
//                            .transparentTarget(true)
//                            .targetRadius(60),
//                    TapTarget.forView(holder.show_ques, context.getString(R.string.lined_up),
//                                    context.getString(R.string.detailed_explanation))
//                            .outerCircleColor(R.color.arivihan_primary_color)
//                            .outerCircleAlpha(0.96f)
//                            .targetCircleColor(R.color.navy_blue)
//                            .titleTextSize(18)
//                            .titleTextColor(R.color.white)
//                            .descriptionTextSize(15)
//                            .titleTypeface(Typeface.DEFAULT_BOLD)
//                            .descriptionTextColor(R.color.white)
//                            .textColor(R.color.white)
//                            .textTypeface(Typeface.SANS_SERIF)
//                            .dimColor(R.color.black)
//                            .drawShadow(true)
//                            .cancelable(false)
//                            .tintTarget(true)
//                            .transparentTarget(true)
//                            .targetRadius(80)).listener(new TapTargetSequence.Listener() {
//                @Override
//                public void onSequenceFinish() {
//                    MyApplication.saveData(PreferenceKeys.TOPIC_DETAIL_ADAPTER.getKey(), true);
//                    holder.recyclerViewQues.setVisibility(View.GONE);
//                    holder.recyclerViewLec.setVisibility(View.VISIBLE);
//                    holder.show_lec.setTextSize(16);
//                    holder.show_ques.setTextSize(15);
//                    holder.show_note.setTextSize(15);
//                    holder.show_book.setTextSize(15);
//                    holder.book.setBackgroundColor(Color.WHITE);
//                    holder.note.setBackgroundColor(Color.WHITE);
//                    holder.ques.setBackgroundColor(Color.WHITE);
//                    holder.lect.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.option_background_selected));
//                    onTopicClick.onFinish();
//                }
//
//                @Override
//                public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
//                    holder.recyclerViewQues.setVisibility(View.VISIBLE);
//                    holder.recyclerViewLec.setVisibility(View.GONE);
//                    holder.show_lec.setTextSize(15);
//                    holder.show_note.setTextSize(15);
//                    holder.show_ques.setTextSize(16);
//                    holder.show_book.setTextSize(15);
//                    holder.book.setBackgroundColor(Color.WHITE);
//                    holder.note.setBackgroundColor(Color.WHITE);
//                    holder.lect.setBackgroundColor(Color.WHITE);
//                    holder.ques.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.option_background_selected));
//
//                }
//
//                @Override
//                public void onSequenceCanceled(TapTarget lastTarget) {
//                }
//            });


            boolean hideTapTarget = ((Activity) context).getIntent().getBooleanExtra("hideTapTarget", false);
            Log.i("ARIVIHANLOG", "RESPONSE" + hideTapTarget);
            if (hideTapTarget) {
                //targetSequence.cancel();
            } else {
                //targetSequence.start();
            }
        }


        if (MyApplication.getIntData(chapterName + PreferenceKeys.TYPE.getKey()) == 1) {
            holder.recyclerViewQues.setVisibility(View.VISIBLE);
            holder.recyclerViewLec.setVisibility(View.GONE);
            holder.show_lec.setTextSize(15);
            holder.show_note.setTextSize(15);
            holder.show_ques.setTextSize(16);
            holder.show_book.setTextSize(15);
            holder.book.setBackgroundColor(Color.WHITE);
            holder.note.setBackgroundColor(Color.WHITE);
            holder.lect.setBackgroundColor(Color.WHITE);
            //   holder.ques.setBackgroundColor(Color.parseColor("#E4E3EC"));
            holder.ques.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.option_background_selected));
        }

        holder.show_ques.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {
                MyApplication.saveData(chapterName + PreferenceKeys.TYPE.getKey(), 1);
                holder.recyclerViewQues.setVisibility(View.VISIBLE);
                holder.recyclerViewLec.setVisibility(View.GONE);
                holder.show_lec.setTextSize(15);
                holder.show_note.setTextSize(15);
                holder.show_ques.setTextSize(16);
                holder.show_book.setTextSize(15);
                holder.book.setBackgroundColor(Color.WHITE);
                holder.note.setBackgroundColor(Color.WHITE);
                holder.lect.setBackgroundColor(Color.WHITE);
                holder.ques.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.option_background_selected));
            }
        });
        holder.show_lec.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {
                MyApplication.saveData(chapterName + PreferenceKeys.TYPE.getKey(), 0);
                holder.recyclerViewQues.setVisibility(View.GONE);
                holder.recyclerViewLec.setVisibility(View.VISIBLE);
                holder.show_lec.setTextSize(16);
                holder.show_ques.setTextSize(15);
                holder.show_note.setTextSize(15);
                holder.show_book.setTextSize(15);
                holder.book.setBackgroundColor(Color.WHITE);
                holder.note.setBackgroundColor(Color.WHITE);
                holder.ques.setBackgroundColor(Color.WHITE);
                //        holder.lect.setBackgroundColor(ContextCompat.getColor(context, R.color.locked_background_color));
                holder.lect.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.option_background_selected));
            }
        });

        if (tm.getLocked()) {
            holder.lock.setVisibility(View.VISIBLE);
            holder.progress_text.setVisibility(View.GONE);
            holder.progress.setVisibility(View.GONE);
        } else {
            holder.lock.setVisibility(View.GONE);
            holder.progress.setVisibility(View.VISIBLE);
            holder.progress_text.setVisibility(View.VISIBLE);
        }

        if (tm.getExpandable() == true && !tm.getLocked()) {
            holder.frame1.setBackgroundColor(ContextCompat.getColor(context, R.color.locked_background_color));
            holder.unlockPercentage.setBackgroundColor(ContextCompat.getColor(context, R.color.locked_background_color));
            holder.frame2.setVisibility(View.VISIBLE);
            holder.unlockPercentage.setBackgroundColor(Color.WHITE);
            holder.Expandable_Layout.setVisibility(View.VISIBLE);
        } else {
            holder.frame1.setBackgroundColor(Color.WHITE);
            holder.frame2.setVisibility(View.GONE);
            holder.Expandable_Layout.setVisibility(View.GONE);
        }


        holder.relative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!tm.getLocked()) {
                    MyApplication.saveData(chapterName, position);
                    tm.setExpandable(!tm.getExpandable());
                    for (int i = 0; i < topicModels.size(); i++) {
                        if (i != position) {
                            topicModels.get(i).setExpandable(false);
                        }
                    }
                    notifyDataSetChanged();
                } else {
                    onTopicClick.ontopicClick(position);
                }
            }
        });

        if (tm.getExpandable() == true) {
            holder.frame1.setBackgroundColor(ContextCompat.getColor(context, R.color.locked_background_color));
            holder.unlockPercentage.setBackgroundColor(ContextCompat.getColor(context, R.color.locked_background_color));
        } else {
            holder.unlockPercentage.setBackgroundColor(Color.WHITE);
            holder.frame1.setBackgroundColor(Color.WHITE);
        }


    }


    @Override
    public int getItemCount() {
        return topicModels.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private final Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            List<TopicModel> filteredlist = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredlist.addAll(topicModelsFull);
            } else {
                String pattern = constraint.toString().toLowerCase().trim();
                for (TopicModel cp : topicModelsFull) {
                    String chap = cp.getTitle();
                    if (chap.toLowerCase().contains(pattern))
                        filteredlist.add(cp);
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredlist;
            results.count = filteredlist.size();
            return results;

        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            topicModels.clear();
            if (filterResults == null || filterResults.values == null) {
                notifyDataSetChanged();
                return;
            }
            topicModels.addAll((List) filterResults.values);
            notifyDataSetChanged();
        }
    };


    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView title, question, progress_text, show_ques, show_lec, show_note, show_book;
        RecyclerView recyclerViewQues;
        RecyclerView recyclerViewLec;
        LinearLayout ques, lect, note, book;
        ProgressBar progress;
        RelativeLayout lock_image, unlockPercentage;
        FrameLayout frame1, frame2;
        LinearLayout Expandable_Layout;
        LinearLayout relative;
        CardView lock;
        ImageView badge;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            badge = itemView.findViewById(R.id.badge);

            title = itemView.findViewById(R.id.title);
            question = itemView.findViewById(R.id.questions);
            show_lec = itemView.findViewById(R.id.show_lectures);
            unlockPercentage = itemView.findViewById(R.id.unlock_frame_percentage);
            show_ques = itemView.findViewById(R.id.show_question);
            show_book = itemView.findViewById(R.id.show_bookmark);
            progress_text = itemView.findViewById(R.id.progress_text);
            recyclerViewLec = itemView.findViewById(R.id.lecture_recyclerview);
            recyclerViewQues = itemView.findViewById(R.id.question_recyclerview);
            ques = itemView.findViewById(R.id.show_question_line);
            lect = itemView.findViewById(R.id.show_lectures_line);
            book = itemView.findViewById(R.id.show_bookmark_line);
            progress = itemView.findViewById(R.id.progress);
            lock = itemView.findViewById(R.id.lock_ner_version);
            show_note = itemView.findViewById(R.id.show_notes);
            note = itemView.findViewById(R.id.show_notes_line);
            frame1 = itemView.findViewById(R.id.frame1);
            frame2 = itemView.findViewById(R.id.frame2);
            lock_image = itemView.findViewById(R.id.lock_image_frame);
            Expandable_Layout = itemView.findViewById(R.id.expandable_layout);
            relative = itemView.findViewById(R.id.relativeLayout);

        }

    }

    public interface OnTopicClick {
        void ontopicClick(int position);

        void onFinish();
    }




}
