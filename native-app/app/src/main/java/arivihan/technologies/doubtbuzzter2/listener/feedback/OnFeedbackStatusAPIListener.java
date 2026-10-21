package arivihan.technologies.doubtbuzzter2.listener.feedback;

import arivihan.technologies.doubtbuzzter2.model.feedback.AppFeedbackStatusModel;

public interface OnFeedbackStatusAPIListener {
    void onSuccess(boolean status, AppFeedbackStatusModel appFeedbackStatusModel);
    void onException(boolean status, AppFeedbackStatusModel appFeedbackStatusModel);
    void onError(boolean status, AppFeedbackStatusModel appFeedbackStatusModel);
}
