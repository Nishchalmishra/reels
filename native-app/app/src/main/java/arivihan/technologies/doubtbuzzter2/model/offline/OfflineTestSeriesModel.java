package arivihan.technologies.doubtbuzzter2.model.offline;

import java.io.Serializable;

import arivihan.technologies.doubtbuzzter2.model.UserMockAnswerDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfflineTestSeriesModel implements Serializable {
    private String id;
    private String testId;
    private String testSeriesId;
    private String testSeresName;
    private boolean board;
    private boolean singleAttempt;
    private UserMockAnswerDto userMockAnswerDto;
    private OfflineTestSubmissionStatus status;
    private OfflineTestTypes testType;
    private String createdAt;
    private String updatedAt;
    private int errorRetryCount;
    private String lastErrorMsg;
}
