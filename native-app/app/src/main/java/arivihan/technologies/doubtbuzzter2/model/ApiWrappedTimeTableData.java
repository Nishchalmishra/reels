package arivihan.technologies.doubtbuzzter2.model;

import com.google.gson.annotations.SerializedName;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class ApiWrappedTimeTableData {

    @SerializedName("message")
    private String message;

    @SerializedName("code")
    private int code;

    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private UiTimetableData data;
}
