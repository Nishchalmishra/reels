package arivihan.technologies.doubtbuzzter2.model.batch.user;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.model.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDetailsResponse {

    private UserDto userDto;
    private Boolean isNewUser;
    private List<BatchDetail> batches;
    private boolean boardSwitch;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BatchDetail {
        private Integer batchId;
        private String batchName;
        private String batchDisplayName;
        private String batchCode;
        private String language;
        private Integer examId;
        private String examCode;
        private String examType;
        private String examName;
        private Integer gradeId;
        private String gradeName;
        private String gradeCode;
        private Integer yearId;
        private String yearCode;
        private String yearName;
        private Integer streamId;
        private String streamName;
        private String streamCode;
        private String enrollmentDate;
        private Boolean active;
        private Boolean subscribed;



        public Boolean getActive() {
            return active;
        }

        public void setActive(Boolean active) {
            this.active = active;
        }

        public Integer getExamId() {
            return examId;
        }

        public void setExamId(Integer examId) {
            this.examId = examId;
        }

        public String getGradeCode() {
            return gradeCode;
        }

        public void setGradeCode(String gradeCode) {
            this.gradeCode = gradeCode;
        }

        public String getStreamCode() {
            return streamCode;
        }

        public void setStreamCode(String streamCode) {
            this.streamCode = streamCode;
        }

        public String getExamCode() {
            return examCode;
        }

        public void setExamCode(String examCode) {
            this.examCode = examCode;
        }
    }
    public List<BatchDetail> getBatches() {
        return batches;
    }

    public void setBatches(List<BatchDetail> batches) {
        this.batches = batches;
    }
}
