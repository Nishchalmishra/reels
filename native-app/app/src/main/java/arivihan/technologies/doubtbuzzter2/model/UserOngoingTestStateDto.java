package arivihan.technologies.doubtbuzzter2.model;

import android.os.Parcelable;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserOngoingTestStateDto implements Serializable {
    private int lastSelectedTab;
    HashMap<String, String> map;
}
