package arivihan.technologies.doubtbuzzter2.model.remote_config.pdf_circle;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PdfCircleConfigLanguageModel implements Serializable {

    @SerializedName("pencilExpandedText")
    private String pencilExpandedText;

    @SerializedName("pdfWrongCircleMessage")
    private String pdfWrongCircleMessage;

    @SerializedName("pdfCircleOnBackMessage")
    private String pdfCircleOnBackMessage;

    @SerializedName("pdfCircleLimitExceedMessage")
    private String pdfCircleLimitExceedMessage;

}
