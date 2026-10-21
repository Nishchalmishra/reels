package arivihan.technologies.doubtbuzzter2.model;


import java.util.List;

import arivihan.technologies.doubtbuzzter2.enums.BottomSheetCategory;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class BottomSheetContent {
    private String id;

    //IMAGE,VIDEO
    private String type;
    private String url;
    private String title;
    private String subtitle;
    private String thumbnail;
    private String btnText;
    private Integer from;
    private Integer to;
    private String classId;
    private String courseId;
    private String language;
    private boolean isForSubscribedUser;
    private String className;
    private String activityParams;
    private BottomSheetCategory category;
    private boolean forLiveClass;
    private boolean showPopup;
    //newfieldAdded
    private List<BottomSheetActionButton> buttons;
}
