package arivihan.technologies.doubtbuzzter2.listener;

import arivihan.technologies.doubtbuzzter2.adapter.MySpinnerType;

public interface SnackbarMessageListener {
    void onPositiveSnackMessage(String positiveMessage, int duration);
    void onNegativeSnackMessage(String positiveMessage, int duration);
}
