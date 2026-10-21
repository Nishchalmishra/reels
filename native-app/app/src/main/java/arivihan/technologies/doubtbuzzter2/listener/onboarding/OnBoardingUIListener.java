package arivihan.technologies.doubtbuzzter2.listener.onboarding;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

public interface OnBoardingUIListener {
    void setGradientVisibility(boolean isVisible);
    void saveWelcomeFragmentBundle(Bundle bundle);
}
