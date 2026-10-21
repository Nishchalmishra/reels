package arivihan.technologies.doubtbuzzter2.model.profile;

import com.google.gson.annotations.SerializedName;

import arivihan.technologies.doubtbuzzter2.model.batch.user.UserDetailsResponse;

import java.io.Serializable;
import java.util.List;

public class UpdateAcademicDetailsData implements Serializable {

    @SerializedName("userDto")
    private UserDetailsResponse userDto;

    @SerializedName("isNewUser")
    private boolean isNewUser;

    @SerializedName("batches")
    private List<AcademicBatchResponse> batches;

    public UserDetailsResponse getUserDto() {
        return userDto;
    }

    public boolean isNewUser() {
        return isNewUser;
    }

    public List<AcademicBatchResponse> getBatches() {
        return batches;
    }

    public void setUserDto(UserDetailsResponse userDto)
    {
        this.userDto = userDto;
    }

    public void setNewUser(boolean newUser)
    {
        isNewUser = newUser;
    }

    public void setBatches(List<AcademicBatchResponse> batches)
    {
        this.batches = batches;
    }
}
