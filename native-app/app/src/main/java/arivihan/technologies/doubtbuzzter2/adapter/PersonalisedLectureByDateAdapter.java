package arivihan.technologies.doubtbuzzter2.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.utils.EdgeToEdgeHelper;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;

import arivihan.technologies.doubtbuzzter2.activity.QuestionActivity;
import arivihan.technologies.doubtbuzzter2.activity.VideoPlayerActivity;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.listener.ChapterDetailActivityListener;
import arivihan.technologies.doubtbuzzter2.model.LectureContentType;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureContentDto;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureInfoDto;
import arivihan.technologies.doubtbuzzter2.model.TopicModel;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class PersonalisedLectureByDateAdapter extends RecyclerView.Adapter<PersonalisedLectureByDateAdapter.MyPersonalisedLectureViewHolder> implements QuestionsAdapter.OnQuestionClick, PersonaliseLectureTopicDetailAdapter.OnTopicClick, ChapterDetailActivityListener {

    private Map<String, ArrayList<MicroLectureInfoDto>> microlectureList;
    private Context context;
    ArrayList<TopicModel> topics = new ArrayList<>();


    public PersonalisedLectureByDateAdapter(Map<String, ArrayList<MicroLectureInfoDto>> lecturelist) {
        this.microlectureList = lecturelist;
    }

    @NonNull
    @Override
    public MyPersonalisedLectureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.adapter_personalised_lecture_by_date, parent, false);
        return new MyPersonalisedLectureViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyPersonalisedLectureViewHolder holder, int position) {
        Object[] keys = microlectureList.keySet().toArray();
        holder.generatedDate.setText(keys[position].toString());


        topics.clear();
//        ArrayList<TopicModel> topics = new ArrayList<>();
        for (MicroLectureInfoDto infoDto : microlectureList.get(keys[position].toString())) {
            TopicModel tm = new TopicModel();
            tm.setTitle(infoDto.getTitle());
            tm.setNoOfLecture(infoDto.getMicroLectureContentDtoList().size() + "");
            tm.setExpandable(true);

            ArrayList<MicroLectureContentDto> lectureContentDtos = new ArrayList<>();

            ArrayList<MicroLectureContentDto> questionsDtos = new ArrayList<>();
            for (MicroLectureContentDto microlectureContentDto : infoDto.getMicroLectureContentDtoList()) {
                if (microlectureContentDto.getLectureContentType().getType().equalsIgnoreCase(LectureContentType.VIDEO.getType())) {
                    lectureContentDtos.add(microlectureContentDto);
                } else if (microlectureContentDto.getLectureContentType().getType().equalsIgnoreCase(LectureContentType.NUMERICAL_QUESTION.getType())
                        || microlectureContentDto.getLectureContentType().getType().equalsIgnoreCase(LectureContentType.CONCEPTUAL_QUESTION.getType())) {

                    microlectureContentDto.setHeader_title(microlectureContentDto.getTitle());
                    questionsDtos.add(microlectureContentDto);
                }
            }
            tm.setLecturesDtos(lectureContentDtos);
            tm.setQuestionsDtos(questionsDtos);
            tm.setNoOfQuestions(questionsDtos.size() + "");
            tm.setProgress_value(infoDto.getProgress() + "");

            tm.setLocked(infoDto.isLocked());
            tm.setIsNotWatched(infoDto.getIsNotWatched());
            tm.setLockMessage(infoDto.getLockMessage());

            topics.add(tm);

            Log.i("LECTURE_LIST_SIZE", "SIZE :: " + topics.size());

            if (infoDto.getSummary() == null || infoDto.getSummary().isEmpty()) {
                holder.openInfoBtn.setVisibility(View.GONE);
            }
        }
        PersonaliseLectureTopicDetailAdapter topicsAdapter = new PersonaliseLectureTopicDetailAdapter(topics, holder.itemView.getContext(), this, "Date", this, false);
        holder.lecturesRecyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext(), LinearLayoutManager.VERTICAL, false));
        holder.lecturesRecyclerView.setAdapter(topicsAdapter);


        holder.openInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(holder.itemView.getContext());
                dialog.setContentView(R.layout.dialog_personalised_lecture_info);
                dialog.getWindow().setBackgroundDrawable(holder.itemView.getContext().getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                EdgeToEdgeHelper.applyToDialog(dialog);
                dialog.setCancelable(true);
                dialog.show();

                ImageView closeDialogBtn = dialog.findViewById(R.id.close_dialog_btn);
                closeDialogBtn.setOnClickListener((t) -> {
                    dialog.dismiss();
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return microlectureList.size();
    }

    @Override
    public void onquestionclick(int position, String id) {
        Log.i("PERSONINDEX", "fas " + position);
        Log.i("PERSONINDEX", "fas " + id);


//        RelativeLayout ParentLayout = find(R.id.ParentRelativeLayout);

        MyApplication.saveData(PreferenceKeys.PERSONALISED_TYPE.getKey(), 1);
        ArrayList<String> optionList = new ArrayList<>();
//        int topicPo;
        int questionPosition = 0;
        for (int i = 0; i < topics.size(); i++) {
            if (topics.get(i).getTitle().equals(id))
                questionPosition = i;
            break;
        }

        Log.d("PERSONINDEX", position + "");
        TopicModel topicModel = topics.get(questionPosition);
        MicroLectureContentDto MLCD = topicModel.getQuestionsDtos().get(position);
        Intent intent = new Intent(context, QuestionActivity.class);
        String p = (position + 1) + "";
        if (MLCD.getQuestion() == null) {

//            CommonMethods.showNegativeSnackbar(ParentLayout, context.getString(R.string.NoValidQuestionFound));
        } else {
            intent.putExtra(context.getResources().getString(R.string.passed_obj), MLCD);
            intent.putExtra(PreferenceKeys.POSITION.getKey(), p);
            intent.putExtra("Title", id);
            intent.putExtra("Chapter", "Personalised");
            intent.putExtra("Id", id);
            intent.putExtra("Question", MLCD.getQuestion().trim());
            if (MLCD.getAnswerEx() == null) {
                intent.putExtra("Explanation", "");
            } else {
                intent.putExtra("Explanation", MLCD.getAnswerEx().trim());
            }
            for (int i = 0; i < MLCD.getOptions().size(); i++) {
                optionList.add(MLCD.getOptions().get(i).trim());
            }
            intent.putStringArrayListExtra("Options", optionList);
            if (MLCD.getContentUrl() != null)
                intent.putExtra("ImageQuestion", MLCD.getContentUrl());
            else
                intent.putExtra("ImageQuestion", "");
            if (MLCD.getAnswer() == null) {
//                CommonMethods.showNegativeSnackbar(ParentLayout, context.getString(R.string.NoValidQuestionFound) + MLCD.getAnswer());
            } else
                intent.putExtra("Answer", MLCD.getAnswer().trim());

            String url = MLCD.getAnswerSolImage();
            if (url != null)
                intent.putExtra("AnswerImage", url);
            else
                intent.putExtra("AnswerImage", "");
            // for further questions---
            intent.putExtra("Subject", id);
            intent.putExtra("Chapter", PreferenceKeys.PERSONALISED_TYPE.getKey());

            if (MLCD.getQuestion() == null || optionList.size() == 0) {
//                CommonMethods.showNegativeSnackbar(ParentLayout, context.getString(R.string.NoValidQuestionFound));
            } else {
                context.startActivity(intent);
            }
        }

    }

    @Override
    public void ontopicClick(int position) {
        Log.i("lectureposition", "fas " + position);
    }

    @Override
    public void onFinish() {

    }

    @Override
    public void goToActivity(MicroLectureContentDto microLectureContentDto, int position) {
        switchToVideo(microLectureContentDto, position);
    }

    public void switchToVideo(MicroLectureContentDto microLectureContentDto, int position) {
//        MyApplication.saveData(PreferenceKeys.HOME_DASHBOARD_DATA_RELOAD.getKey(), true);
//        MyApplication.saveData(PreferenceKeys.MICROLECTURE_DATA_RELOAD.getKey(), true);
        @OptIn(markerClass = UnstableApi.class) Intent intent = new Intent(context, VideoPlayerActivity.class);
        intent.putExtra("position", position);
        intent.putExtra("chapterId", microLectureContentDto.getChapterId());

        intent.putExtra(context.getResources().getString(R.string.passed_obj), microLectureContentDto);
        if (microLectureContentDto.getResumePosition() != microLectureContentDto.getDefaultStartTime())
            intent.putExtra("shouldNotResume", false);
        else
            intent.putExtra("shouldNotResume", true);

        intent.putExtra("position", position);
        if (microLectureContentDto.getResumePosition() != microLectureContentDto.getDefaultStartTime())
            intent.putExtra("shouldNotResume", false);
        else
            intent.putExtra("shouldNotResume", true);
        intent.putExtra("resumePosition", microLectureContentDto.getResumePosition());
        intent.putExtra(context.getString(R.string.selected_chpter_id), microLectureContentDto.getId());
        CommonMethods.updateUnwatched(microLectureContentDto.getId());

        context.startActivity(intent);
    }

    protected static class MyPersonalisedLectureViewHolder extends RecyclerView.ViewHolder {

        private RecyclerView lecturesRecyclerView;
        //        private CardView tooltipCardView;
        private ImageView openInfoBtn;
        private TextView generatedDate;

        public MyPersonalisedLectureViewHolder(@NonNull View itemView) {
            super(itemView);
            lecturesRecyclerView = itemView.findViewById(R.id.microlectures);
//            tooltipCardView = itemView.findViewById(R.id.info_dialog_tooltip);
            openInfoBtn = itemView.findViewById(R.id.open_info_btn);
            generatedDate = itemView.findViewById(R.id.generated_date);
        }
    }
}
