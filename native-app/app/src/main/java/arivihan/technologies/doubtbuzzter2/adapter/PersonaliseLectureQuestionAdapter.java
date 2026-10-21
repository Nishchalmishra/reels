package arivihan.technologies.doubtbuzzter2.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.QuestionActivity;
import arivihan.technologies.doubtbuzzter2.model.LectureContentType;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureContentDto;
import arivihan.technologies.doubtbuzzter2.model.TopicModel;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class PersonaliseLectureQuestionAdapter extends RecyclerView.Adapter<PersonaliseLectureQuestionAdapter.ViewHolder> {


    ArrayList<MicroLectureContentDto> questionModels;
    Context context;

    public PersonaliseLectureQuestionAdapter(ArrayList<MicroLectureContentDto> questionModels, Context context) {
        this.questionModels = questionModels;
        this.context = context;
    }

    @NonNull
    @Override
    public PersonaliseLectureQuestionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.question_ui, parent, false);
        return new PersonaliseLectureQuestionAdapter.ViewHolder(view);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull PersonaliseLectureQuestionAdapter.ViewHolder holder, int position) {
        MicroLectureContentDto qm = questionModels.get(position);

        if (position == questionModels.size() - 1)
            holder.line.setVisibility(View.GONE);

        holder.question.setText(qm.getTitle() + "");
        if (qm.isCompleted()) {
            holder.image_done.setVisibility(View.VISIBLE);
            holder.image_notdone.setVisibility(View.INVISIBLE);
            holder.detail.setVisibility(View.INVISIBLE);
        } else {
            holder.image_notdone.setVisibility(View.VISIBLE);
            holder.image_done.setVisibility(View.INVISIBLE);
        }

        holder.itemView.setOnClickListener(V -> {
            this.handleQuestionClick(position);
        });

    }

    @Override
    public int getItemCount() {
        return questionModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView question;
        ImageView image_done, image_notdone, detail;
        LinearLayout line;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            question = itemView.findViewById(R.id.question);
            image_done = itemView.findViewById(R.id.question_done);
            line = itemView.findViewById(R.id.lineEnd);
            image_notdone = itemView.findViewById(R.id.question_notdone);
            detail = itemView.findViewById(R.id.details);

        }


    }

    private void handleQuestionClick(int position) {
        MicroLectureContentDto MLCD = questionModels.get(position);
//        RelativeLayout ParentLayout = find(R.id.ParentRelativeLayout);

        MyApplication.saveData(PreferenceKeys.PERSONALISED_TYPE.getKey(), 1);
        ArrayList<String> optionList = new ArrayList<>();

        Intent intent = new Intent(context, QuestionActivity.class);
        String p = (position + 1) + "";
        if (MLCD.getQuestion() == null) {

//            CommonMethods.showNegativeSnackbar(ParentLayout, context.getString(R.string.NoValidQuestionFound));
        } else {
            Gson gson = new Gson();

            ArrayList<TopicModel> topicModels = new ArrayList<>();

            TopicModel tm = new TopicModel();
            tm.setTitle("Personalised Question");
            ArrayList<MicroLectureContentDto> questionsDtos = new ArrayList<>();

            for (MicroLectureContentDto microLectureContentDto : questionModels) {
                if (microLectureContentDto.getLectureContentType().getType().equalsIgnoreCase(LectureContentType.NUMERICAL_QUESTION.getType()) ||
                        microLectureContentDto.getLectureContentType().getType().equalsIgnoreCase(LectureContentType.CONCEPTUAL_QUESTION.getType())) {
                    microLectureContentDto.setHeader_title(context.getResources().getString(R.string.personalised_type));
                    questionsDtos.add(microLectureContentDto);
                }
            }
            tm.setNoOfQuestions(questionsDtos.size() + "");
            tm.setQuestionsDtos(questionsDtos);
            topicModels.add(tm);

            intent.putExtra("allQuestions", gson.toJson(topicModels));
            intent.putExtra(context.getResources().getString(R.string.passed_obj), MLCD);
            intent.putExtra(PreferenceKeys.POSITION.getKey(), p);
            intent.putExtra("Title", "Personalised Question");
            intent.putExtra("Chapter", "Personalised");
            intent.putExtra("Id", MLCD.getQuestion());
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
            intent.putExtra("Subject", MLCD.getQuestion());
            intent.putExtra("Chapter", PreferenceKeys.PERSONALISED_TYPE.getKey());

            if (MLCD.getQuestion() == null || optionList.size() == 0) {
//                CommonMethods.showNegativeSnackbar(ParentLayout, context.getString(R.string.NoValidQuestionFound));
            } else {
                context.startActivity(intent);
            }
        }

    }

}
