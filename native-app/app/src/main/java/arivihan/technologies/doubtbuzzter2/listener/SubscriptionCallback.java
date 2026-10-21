package arivihan.technologies.doubtbuzzter2.listener;

import arivihan.technologies.doubtbuzzter2.model.SubscriptionCheckDto;
    public interface SubscriptionCallback {
        void onSuccess(SubscriptionCheckDto dto);
        void onError(String errorMessage);
    }
