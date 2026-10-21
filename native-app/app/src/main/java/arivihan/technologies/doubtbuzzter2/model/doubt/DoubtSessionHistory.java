package arivihan.technologies.doubtbuzzter2.model.doubt;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoubtSessionHistory implements Serializable {
    private String chatId;
    private String userId;
    private String subject;
    private String batchCode;
    private String firstMessage;
    private String createdAt;
    private String updatedAt;
}
