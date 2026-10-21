package arivihan.technologies.doubtbuzzter2.model;

import android.content.Intent;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.model.call_me_back.CallMeBackModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NameInputDto implements Serializable {
    private String title;
    private String posterUrl;
    private String screen;
    private String nextScreenClass;
    private Map<String, Object> extras;
    private String navigationType;

}
