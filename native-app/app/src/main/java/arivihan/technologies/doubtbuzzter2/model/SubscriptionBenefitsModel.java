package arivihan.technologies.doubtbuzzter2.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionBenefitsModel implements Parcelable, Serializable {
    private String title;
    private String description;
    private String className;
    private Map<String, Object> activityParams;

    protected SubscriptionBenefitsModel(Parcel in) {
        title = in.readString();
        description = in.readString();
        className = in.readString();
        activityParams = new HashMap<>();
        in.readMap(activityParams,ClassLoader.getSystemClassLoader());
    }

    public static final Creator<SubscriptionBenefitsModel> CREATOR = new Creator<SubscriptionBenefitsModel>() {
        @Override
        public SubscriptionBenefitsModel createFromParcel(Parcel in) {
            return new SubscriptionBenefitsModel(in);
        }

        @Override
        public SubscriptionBenefitsModel[] newArray(int size) {
            return new SubscriptionBenefitsModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(description);
        parcel.writeString(className);
        parcel.writeMap(activityParams);
    }
}
