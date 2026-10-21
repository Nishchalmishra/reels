package arivihan.technologies.doubtbuzzter2.model.remote_config;

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
public class TestSeriesConfig implements Serializable {
    @SerializedName("testAttemptExceedMsg")
    private String testAttemptExceedMsg;

    @SerializedName("errorRetryCount")
    private int errorRetryCount;

    @SerializedName("testSyncingProgressMsg")
    private String testSyncingProgressMsg;

    @SerializedName("singleAttemptDialogVisibility")
    private boolean singleAttemptDialogVisibility;

    @SerializedName("testInterruptionDialogVisibility")
    private boolean testInterruptionDialogVisibility;

    @SerializedName("exitAllowedCount")
    private int exitAllowedCount;

    @SerializedName("syncingLastOfflineTestMsg")
    private String syncingLastOfflineTestMsg;

}
