package arivihan.technologies.doubtbuzzter2.model.competitive.topic;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PyqsInformationDto implements Serializable {
    private String cardBgColor;
    private String title;
    private String titleTextColor;
}
