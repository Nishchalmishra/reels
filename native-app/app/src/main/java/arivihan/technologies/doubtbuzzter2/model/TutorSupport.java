package arivihan.technologies.doubtbuzzter2.model;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class TutorSupport {
    private String id;
    private String sessionId;
    private String userId;
    private String doubtId;
    private String chatRequest;
    private String chatResponse;
    private Date createdAt;
    private Date updatedAt;
}
