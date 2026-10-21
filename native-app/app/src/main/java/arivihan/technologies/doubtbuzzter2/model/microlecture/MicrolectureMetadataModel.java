package arivihan.technologies.doubtbuzzter2.model.microlecture;

import java.io.Serializable;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Data
public class MicrolectureMetadataModel implements Serializable {
    private long show_feedback_on_back_min_time_ms;
    private long show_like_dislike_comment_min_time_ms;
    private long preview_buffer_threshold_ms;
    private boolean show_feedback_on_back_pressed;
    private boolean show_like_dislike_for_small_video;
    private boolean show_ml_like_dislike;
    private boolean show_ml_comment;
    private boolean show_video_like_dislike;

    public MicrolectureMetadataModel(long show_feedback_on_back_min_time_ms, long show_like_dislike_comment_min_time_ms, boolean show_feedback_on_back_pressed) {
        this.show_feedback_on_back_min_time_ms = show_feedback_on_back_min_time_ms;
        this.show_like_dislike_comment_min_time_ms = show_like_dislike_comment_min_time_ms;
        this.show_feedback_on_back_pressed = show_feedback_on_back_pressed;
    }
}
