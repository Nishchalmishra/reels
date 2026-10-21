package arivihan.technologies.doubtbuzzter2.model;

import arivihan.technologies.doubtbuzzter2.enums.DoubtOptionType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DoubtOption {
    private DoubtOptionType doubtOptionType;
    private String title;
}
