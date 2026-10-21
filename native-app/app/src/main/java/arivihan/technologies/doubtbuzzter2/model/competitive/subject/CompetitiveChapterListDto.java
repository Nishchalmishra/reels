package arivihan.technologies.doubtbuzzter2.model.competitive.subject;

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
public class CompetitiveChapterListDto implements Serializable {
    private List<CompetitiveChapterDto> list;
}
