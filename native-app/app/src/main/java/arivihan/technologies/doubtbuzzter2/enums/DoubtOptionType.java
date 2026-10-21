package arivihan.technologies.doubtbuzzter2.enums;

import arivihan.technologies.doubtbuzzter2.R;

public enum DoubtOptionType {
    QUESTION_PREDICTOR("Question Predictor", R.drawable.heap_snapshot_large),
    TIPS_TRICKS("Tips & Tricks", R.drawable.cognition),
    DETAILED_EXPLANATION("Detailed Explanation", R.drawable.dictionary);

    private final String displayName;
    private final int iconResId;

    DoubtOptionType(String displayName, int iconResId) {
        this.displayName = displayName;
        this.iconResId = iconResId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getIconResId() {
        return iconResId;
    }
}