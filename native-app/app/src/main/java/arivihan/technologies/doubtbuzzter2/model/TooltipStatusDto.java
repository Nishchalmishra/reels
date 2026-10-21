package arivihan.technologies.doubtbuzzter2.model;

import lombok.Data;

@Data
public class TooltipStatusDto {
    int stepCount;
    int totalSteps;
    int fragment=-1;
    int currentFragment=-1;
    boolean isActive;
    boolean finished;
    TooltipType tooltipType;
}
