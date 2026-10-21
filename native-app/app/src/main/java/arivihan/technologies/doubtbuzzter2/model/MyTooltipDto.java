package arivihan.technologies.doubtbuzzter2.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MyTooltipDto {
    String text;
    int alignment;
    boolean autoDismiss;
    String titleText;
    boolean showNext;
    String nextText;
    boolean isAboveAnchor;
    boolean showSkip;
    boolean hideArrow;
    boolean isRightAnchor;
    int stepCount;
    int totalSteps;
    TooltipType tooltipType;
    long millis;
    int fragmentNumber;
    int currentFragment;
    boolean isTest;
    int yMargin;
}
