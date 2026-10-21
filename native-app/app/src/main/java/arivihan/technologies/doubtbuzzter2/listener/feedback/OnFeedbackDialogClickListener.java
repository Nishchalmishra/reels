package arivihan.technologies.doubtbuzzter2.listener.feedback;

public interface OnFeedbackDialogClickListener {
    void onPrimaryButtonClick(boolean status, String feedback , Object object);
    void onSecondaryButtonClick(boolean status, String feedback , Object object);
    void onDismissed();
    void onCanceled(boolean status, String feedback , Object object);
}
