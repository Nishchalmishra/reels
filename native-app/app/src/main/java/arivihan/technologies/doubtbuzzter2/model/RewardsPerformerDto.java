package arivihan.technologies.doubtbuzzter2.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RewardsPerformerDto {

    private String studentName;
    private String studentImageUrl;
    private String videoUrl;
    private String thumbnailUrl;
    private String prizeName;
    private int rank;
}