package arivihan.technologies.doubtbuzzter2.model;

import java.io.Serializable;

import arivihan.technologies.doubtbuzzter2.enums.ActionType;
import lombok.Data;

@Data
public class BottomSheetActionButton implements Serializable {

    public enum ButtonType {
        PRIMARY,
        SECONDARY
    }
    private ButtonType buttonType;
    private String buttonText;
    private String className;
    private String activityParams;
    private ActionType actionType;
    private String dialogBannerText;
    private Long dialogTimeout;
    private String dialogBannerImage;
    private String bgColorCode;
    private String textColorCode;



//    private String buttonText;
//    private String className;
//    private String activityParams;
//    private String actionType;
//    private String dialogBannerImage;
//    private Long dialogTimeout;
//    private String dialogBannerText;
//    private String bgColorCode;
//    private String textColorCode;
}
