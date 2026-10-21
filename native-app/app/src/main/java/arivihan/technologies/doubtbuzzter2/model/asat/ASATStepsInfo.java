package arivihan.technologies.doubtbuzzter2.model.asat;


import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ASATStepsInfo implements Serializable {
    int stepNumber;
    String stepText1;
    String stepText2;
    boolean done;
    String icon;
}
