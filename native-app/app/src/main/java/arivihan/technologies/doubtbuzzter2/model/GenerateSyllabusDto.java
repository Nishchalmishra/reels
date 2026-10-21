package arivihan.technologies.doubtbuzzter2.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GenerateSyllabusDto {
    private Map<String, List<UserSyllabusOrder>> datas;
    private String examDate;
    private String startDate;
}
