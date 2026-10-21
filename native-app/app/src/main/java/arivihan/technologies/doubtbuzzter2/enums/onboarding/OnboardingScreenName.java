package arivihan.technologies.doubtbuzzter2.enums.onboarding;

import androidx.annotation.Nullable;

/**
 * Identifies each screen in the onboarding flow.
 * Used by the user-state API for resume flow persistence.
 */
public enum OnboardingScreenName {
    EXAM_SCREEN,
    LANGUAGE_SCREEN,
    GRADE_LEVEL_SCREEN,
    STREAM_SCREEN,
    NAME_SCREEN,
    NEET_GRADE_SCREEN,
    NEET_BOARD_SCREEN,
    NEET_COMING_SOON_SCREEN;

    @Nullable
    public static OnboardingScreenName fromKey(String key) {
        if (key == null) return null;
        try {
            return valueOf(key.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Maps an {@link OnboardingStep} (used by the step-based navigation in
     * OnboardingActivityNew) to the corresponding API screen name.
     * Returns null for steps that don't have a direct API mapping.
     */
    @Nullable
    public static OnboardingScreenName fromStep(@Nullable OnboardingStep step) {
        if (step == null) return null;
        switch (step) {
            case NAME:    return NAME_SCREEN;
            case MEDIUM:  return LANGUAGE_SCREEN;
            case GRADE:   return GRADE_LEVEL_SCREEN;
            case STREAM:  return STREAM_SCREEN;
            case BOARD:   return NEET_BOARD_SCREEN;
            default:      return null;
        }
    }

    /**
     * Maps this API screen name back to the {@link OnboardingStep} used by
     * OnboardingActivityNew's step-based navigation.
     * Returns null for screens handled outside OnboardingActivityNew
     * (e.g. EXAM_SCREEN, NEET_GRADE_SCREEN, NEET_COMING_SOON_SCREEN).
     */
    @Nullable
    public OnboardingStep toStep() {
        switch (this) {
            case NAME_SCREEN:        return OnboardingStep.NAME;
            case LANGUAGE_SCREEN:    return OnboardingStep.MEDIUM;
            case GRADE_LEVEL_SCREEN: return OnboardingStep.GRADE;
            case STREAM_SCREEN:      return OnboardingStep.STREAM;
            case NEET_BOARD_SCREEN:  return OnboardingStep.BOARD;
            default:                 return null;
        }
    }
}
