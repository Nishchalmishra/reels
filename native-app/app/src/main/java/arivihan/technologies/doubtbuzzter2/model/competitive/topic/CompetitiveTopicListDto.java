package arivihan.technologies.doubtbuzzter2.model.competitive.topic;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CompetitiveTopicListDto implements Serializable {
    private List<CompetitiveTopicDto> list;
}
