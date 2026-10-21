package arivihan.technologies.doubtbuzzter2.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.android.gms.common.util.CollectionUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FactoryShipQnADto implements Parcelable {
    private String code;
    private String questionId;
    private String microLectureCode;
    private String chapterCode;
    private String videoThumbnail;
    private String questionLevel;
    private String type;
    private String question;
    private String imageQuestion;
    private String optionType;
    private List<String> options;
    private String answer;
    private String answerSolImage;
    private String answerEx;
    private String answerImage;
    private String videoUrl;
    private String answerVideo;
    private long defaultStartTime;
    private long videoCompletionStartPos;
    private long videoCompletionEndPos;
    private int pos;
    private String wordLimit;
    private String marks;
    private String language;
    private String title;
    private boolean completed;
    private List<String> answers;
    private String userAnswer;
    private boolean userAnswerCorrect;
    private boolean reattempt;
    private boolean review;
    private boolean hideViewSolution;
    private String marksObtained;
    private String userSolution;
    private String analysis;
    private boolean savType;
    private boolean clearAttempt;
    public boolean wordLimitApplicable;
    private String year;
    private String imageExplanationLatex;// For Year Tag - e.g 2025

    protected FactoryShipQnADto(Parcel in) {
        code = in.readString();
        questionId = in.readString();
        microLectureCode = in.readString();
        videoThumbnail = in.readString();
        questionLevel = in.readString();
        type = in.readString();
        question = in.readString();
        imageQuestion = in.readString();
        optionType = in.readString();
        options = in.createStringArrayList();
        answer = in.readString();
        answerSolImage = in.readString();
        answerEx = in.readString();
        answerImage = in.readString();
        videoUrl = in.readString();
        answerVideo = in.readString();
        defaultStartTime = in.readLong();
        videoCompletionStartPos = in.readLong();
        videoCompletionEndPos = in.readLong();
        pos = in.readInt();
        wordLimit = in.readString();
        marks = in.readString();
        language = in.readString();
        title = in.readString();
        completed = in.readByte() != 0;
        userAnswer = in.readString();
        userAnswerCorrect = in.readByte() != 0;
        answers = in.createStringArrayList();
        marksObtained = in.readString();
        userSolution = in.readString();
        reattempt = in.readByte() != 0;
        review = in.readByte() != 0;
        clearAttempt = in.readByte() != 0;
        analysis = in.readString();
        savType = in.readByte() != 0;
        wordLimitApplicable = in.readByte() != 0;
        year = in.readString();
        imageExplanationLatex=in.readString();
    }

    public static final Creator<FactoryShipQnADto> CREATOR = new Creator<FactoryShipQnADto>() {
        @Override
        public FactoryShipQnADto createFromParcel(Parcel in) {
            return new FactoryShipQnADto(in);
        }

        @Override
        public FactoryShipQnADto[] newArray(int size) {
            return new FactoryShipQnADto[size];
        }
    };

    public String getAnswer() {
        if (answer != null) return answer.trim();
        return null;
    }

    public List<String> getOptions() {
        if (!CollectionUtils.isEmpty(options)) {
            List<String> filteredOptions = new ArrayList<>();
            for (String t : options) {
                if (!"NaN".equalsIgnoreCase(t)) {
                    filteredOptions.add(t);
                }
            }
            options = filteredOptions;
        }
        return options;
    }
    public List<String> getAnswers() {
        if (!CollectionUtils.isEmpty(answers)) {
            List<String> filteredAnswers = new ArrayList<>();
            for (String t : answers) {
                if (!"NaN".equalsIgnoreCase(t)) {
                    filteredAnswers.add(t);
                }
            }
            answers = filteredAnswers;
        }
        return answers;
    }

    public String getQuestion() {
        if ("NaN".equals(question)) return "";
        else return question;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(code);
        dest.writeString(questionId);
        dest.writeString(microLectureCode);
        dest.writeString(videoThumbnail);
        dest.writeString(questionLevel);
        dest.writeString(type);
        dest.writeString(question);
        dest.writeString(imageQuestion);
        dest.writeString(optionType);
        dest.writeStringList(options);
        dest.writeString(answer);
        dest.writeString(answerSolImage);
        dest.writeString(answerEx);
        dest.writeString(answerImage);
        dest.writeString(videoUrl);
        dest.writeString(answerVideo);
        dest.writeLong(defaultStartTime);
        dest.writeLong(videoCompletionStartPos);
        dest.writeLong(videoCompletionEndPos);
        dest.writeInt(pos);
        dest.writeString(wordLimit);
        dest.writeString(marks);
        dest.writeString(language);
        dest.writeString(title);
        dest.writeByte((byte) (completed ? 1 : 0));
        dest.writeString(userAnswer);
        dest.writeByte((byte) (userAnswerCorrect ? 1:0));
        dest.writeStringList(answers);
        dest.writeString(marksObtained);
        dest.writeString(userSolution);
        dest.writeByte((byte) (reattempt ? 1 : 0));
        dest.writeByte((byte) (review ? 1 : 0));
        dest.writeByte((byte) (clearAttempt ? 1 : 0));
        dest.writeString(analysis);
        dest.writeByte((byte) (savType ? 1 : 0));
        dest.writeByte((byte) (wordLimitApplicable ? 1 : 0));
        dest.writeString(year);
        dest.writeString(imageExplanationLatex);
    }

    @Override
    public String toString() {
        return "FactoryShipQnADto{" +
                "code='" + code + '\'' +
                ", marksObtained='" + marksObtained + '\'' +
                ", marks='" + marks + '\'' +
                ", questionId='" + questionId + '\'' +
                ", microLectureCode='" + microLectureCode + '\'' +
                ", videoThumbnail='" + videoThumbnail + '\'' +
                ", questionLevel='" + questionLevel + '\'' +
                ", type='" + type + '\'' +
                ", wordLimit='" + wordLimit + '\'' +
                ", wordLimitApplicable='" + wordLimitApplicable + '\'' +
                ", question='" + question + '\'' +
                ", imageExplanationLatex='" + imageExplanationLatex + '\'' +
                '}';
    }
}
