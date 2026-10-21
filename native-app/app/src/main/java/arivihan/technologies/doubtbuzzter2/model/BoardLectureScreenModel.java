package arivihan.technologies.doubtbuzzter2.model;

import android.graphics.drawable.Drawable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardLectureScreenModel {
    Drawable icon;
    MicrolectureBoardContentInfoDto data;

}
