package arivihan.technologies.doubtbuzzter2.listener.dialog;

public interface DialogPositiveNegativeOptionClickListener {
    void onPositiveButtonClick(boolean status, String feedback , Object object);
    void onNegativeButtonClick(boolean status, String feedback , Object object);
}
