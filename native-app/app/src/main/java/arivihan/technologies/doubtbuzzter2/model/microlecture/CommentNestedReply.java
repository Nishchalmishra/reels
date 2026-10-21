package arivihan.technologies.doubtbuzzter2.model.microlecture;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CommentNestedReply implements Serializable {
    private String messageText;
    private String replyId;
    private String timeStamp;
    private String userId;
    private String username;
    private boolean systemReply;

    private boolean isExpanded; // for UI Use-case
}