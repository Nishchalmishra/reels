package arivihan.technologies.doubtbuzzter2.model;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReelsApiResponse {
    private int pageNo;
    private int pageSize;
    private int totalPages;
    private boolean isLastPage;
    private Integer examId;
    private Integer gradeId;
    private Integer streamId;
    private List<ReelsResponse> data;
}
