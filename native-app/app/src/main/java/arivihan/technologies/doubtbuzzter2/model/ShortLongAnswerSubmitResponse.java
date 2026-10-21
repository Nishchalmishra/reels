package arivihan.technologies.doubtbuzzter2.model;

import androidx.annotation.NonNull;

import lombok.Data;

@Data
public class ShortLongAnswerSubmitResponse {
    private boolean success;
    private String explanation;
    private String marks;
    private String totalMarks;
    private String analysis;

    @NonNull
    @Override
    public String toString() {
        return "ShortLongAnswerSubmitResponse{" +
                "success=" + success +
                ", explanation='" + explanation + '\'' +
                ", marks='" + marks + '\'' +
                ", totalMarks='" + totalMarks + '\'' +
                '}';
    }
}
