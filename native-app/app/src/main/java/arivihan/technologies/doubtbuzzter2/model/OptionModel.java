package arivihan.technologies.doubtbuzzter2.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptionModel {

    public String option;
    public String submit;
    public int correctOption;
    public int selectedOption;

    public String optionType; // Either TEXT / IMAGE / NULL

}
