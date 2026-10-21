package arivihan.technologies.doubtbuzzter2.model.microlecture;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Data
public class CommentConfigModel {
    private int commentPageSize;
    private int commentScrollVisibleThresold;
    private int maxCharLimit;
    private String maxLimitExceedMessage;
    private String noCommentMessage;
    private String commentAddedMessage;
    private String commentEditedMessage;
    private String commentDeleted;

}
