package arivihan.technologies.doubtbuzzter2.model.microlecture;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Data
public class CommentModel implements Serializable {
    private String id;
    private String userId;
    private String microlectureId;
    private String userName;
    private String userShortName;
    private String userPic;
    private String comment;
    private String sentiment;
    private long timestamp;
    private String timeAgo;
    private boolean userComment;
    private boolean isLoading; //for internal use
    private int bgColor; // for internal use

    private List<CommentNestedReply> nestedReply;
}
