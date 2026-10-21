package arivihan.technologies.doubtbuzzter2.model;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoDetails implements Serializable {

    private int progress;
    private String status;
    private String url;
    private long startPos;
    private long endPos;
    private int resumePosition;
    private boolean resume;
    private List<LanguageAndVideoUrlDto> languageAndVideoUrlDtos;
    private List<VideoQualityDto> qualityDtos;

    @NonNull
    @Override
    public String toString() {
        return "VideoDetails{" +
                "progress=" + progress +
                ", status='" + status + '\'' +
                ", url='" + url + '\'' +
                ", startPos=" + startPos +
                ", endPos=" + endPos +
                ", resumePosition=" + resumePosition +
                ", resume=" + resume +
                '}';
    }
}
