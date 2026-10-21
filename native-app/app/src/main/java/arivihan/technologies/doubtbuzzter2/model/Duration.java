package arivihan.technologies.doubtbuzzter2.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Duration {

    private String id;
    private int value;
    private String durationUnit;
    private String durationApplicable;
    private String createdAt;
    private String modifiedAt;
    private String discount;

}
