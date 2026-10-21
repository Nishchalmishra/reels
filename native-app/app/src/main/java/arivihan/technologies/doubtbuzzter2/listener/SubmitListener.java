package arivihan.technologies.doubtbuzzter2.listener;

import arivihan.technologies.doubtbuzzter2.model.FactoryShipQnADto;

public interface SubmitListener {

    void onSubmit(int pos, FactoryShipQnADto factoryShipQnADto, boolean correct);
    void openNext();
    void reportQuestion();
    void onViewSolutionClick();
    default void onReattempt(int pos){};
    default void onBottomSheetOpen(){};
    default void onBottomSheetClose(){};
}
