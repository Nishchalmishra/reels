package arivihan.technologies.doubtbuzzter2.model.user.batch;


import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchDetail {
    private Integer batchId;
    private String batchName;
    private String batchCode;
    private String language;
    private Integer examId;
    private String examCode;
    private String examType;
    private String examName;
    private String enrollmentDate;
    private Boolean active;
    private Boolean subscribed;
}