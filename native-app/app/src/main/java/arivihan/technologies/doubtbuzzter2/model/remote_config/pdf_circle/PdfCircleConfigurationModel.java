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
public class PdfCircleConfigurationModel implements Serializable {
    @SerializedName("pdfPencilVisible")
    private boolean pdfPencilVisible;

    @SerializedName("pdfExpandedShowCounter")
    private int pdfExpandedShowCounter;

    @SerializedName("isColorGradientVisible")
    private boolean isColorGradientVisible;

    @SerializedName("pdfCircleCompressQuality")
    private int pdfCircleCompressQuality;

    @SerializedName("isTealTouchGradientVisible")
    private boolean isTealTouchGradientVisible;

    @SerializedName("isPdfCircleAvailableForOffline")
    private boolean isPdfCircleAvailableForOffline;

    @SerializedName("pdfClosedCircleThreshold")
    private double pdfClosedCircleThreshold;

    @SerializedName("pdfClosedCircleAreaThreshold")
    private double pdfClosedCircleAreaThreshold;

    @SerializedName("pdfClosedPathLength")
    private double pdfClosedPathLength;

    @SerializedName("doubtChatUrl")
    private String doubtChatUrl;

    @SerializedName("mlApiUrl")
    private String mlApiUrl;

    @SerializedName("webViewUrl")
    private String webViewUrl;

    @SerializedName("mlApiUrlStage")
    private String mlApiUrlStage;

    @SerializedName("webViewUrlStage")
    private String webViewUrlStage;

    @SerializedName("mlApiUrlProd")
    private String mlApiUrlProd;

    @SerializedName("webViewUrlProd")
    private String webViewUrlProd;

    @SerializedName("allowedPhoneForImageSavedGallery")
    private String allowedPhoneForImageSavedGallery;

    @SerializedName("webViewJsFunctionName")
    private String webViewJsFunctionName;

}
