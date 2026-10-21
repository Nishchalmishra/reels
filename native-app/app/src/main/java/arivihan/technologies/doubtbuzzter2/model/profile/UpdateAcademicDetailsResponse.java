package arivihan.technologies.doubtbuzzter2.model.profile;

import com.google.gson.annotations.SerializedName;

import arivihan.technologies.doubtbuzzter2.model.batch.user.UserDetailsResponse;

import java.io.Serializable;
import java.util.List;

public class UpdateAcademicDetailsResponse implements Serializable {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private UpdateAcademicDetailsData data;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public UpdateAcademicDetailsData getData() {
        return data;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(
            UpdateAcademicDetailsData data
    ) {
        this.data = data;
    }
}