package arivihan.technologies.doubtbuzzter2.enums.onboarding;

public enum OnboardingStep {
    NAME,
    MEDIUM,
    BOARD,
    COACHING,
    GRADE,
    STREAM;

    /**
     * Returns the {@link OnboardingStep} matching the given server/remote-config key,
     * or {@code null} if no match is found (case-insensitive).
     */
    public static OnboardingStep fromKey(String key) {
        if (key == null) return null;
        try {
            return valueOf(key.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
