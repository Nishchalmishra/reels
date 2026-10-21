package arivihan.technologies.doubtbuzzter2.listener;

public interface ApiCallBackListener {
    void onSuccess(boolean status, String message, Object object);
    void onError(boolean status, String message, Object object);
}
