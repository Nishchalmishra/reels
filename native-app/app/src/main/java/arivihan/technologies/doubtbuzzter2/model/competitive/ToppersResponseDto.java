package arivihan.technologies.doubtbuzzter2.model.competitive;

import java.io.Serializable;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.model.ToppersHomeScreenModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToppersResponseDto implements Serializable {
    private String title;
    private String subtitle;
    private List<ToppersHomeScreenModel> toppers;
}
