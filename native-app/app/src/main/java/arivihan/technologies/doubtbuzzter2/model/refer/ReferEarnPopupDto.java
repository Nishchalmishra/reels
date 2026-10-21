package arivihan.technologies.doubtbuzzter2.model.refer;

import com.google.firebase.database.PropertyName;

import java.util.List;

/**
 * DTO for Firebase Realtime Database node: users/refer-earn-popup/{userId}/
 */
public class ReferEarnPopupDto {

    @PropertyName("title")
    public String title;

    @PropertyName("subtitle")
    public String subtitle;

    @PropertyName("subtitleItems")
    public List<String> subtitleItems;

    @PropertyName("imageUrl")
    public String imageUrl;

    @PropertyName("buttons")
    public List<PopupButton> buttons;

    public ReferEarnPopupDto() {
        // Required empty constructor for Firebase
    }

    public static class PopupButton {

        @PropertyName("buttonText")
        public String buttonText;

        @PropertyName("actionType")
        public String actionType; // OPEN_ACTIVITY, DISMISS, OPEN_URL

        @PropertyName("className")
        public String className;

        @PropertyName("activityParams")
        public String activityParams;

//        @PropertyName("bgColorCode")
//        public String bgColorCode;
//
//        @PropertyName("textColorCode")
//        public String textColorCode;

        public PopupButton() {
            // Required empty constructor for Firebase
        }
    }
}
