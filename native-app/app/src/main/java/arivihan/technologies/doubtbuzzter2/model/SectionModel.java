package arivihan.technologies.doubtbuzzter2.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class SectionModel {
    String title;
    String color;
    int userScore;
    String subtitle;
    String type;
    String key;
}
