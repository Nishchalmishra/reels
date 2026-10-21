package arivihan.technologies.doubtbuzzter2.model;


import android.content.Context;
import android.util.Log;

import arivihan.technologies.doubtbuzzter2.R;
import lombok.Getter;

public enum OnboardingSubjectEnum {
    PHYSICS("Physics",  R.drawable.atom, R.drawable.atom),
    CHEMISTRY("Chemistry",  R.drawable.enzyme, R.drawable.atom),
    BIOLOGY("Biology", R.drawable.dna, R.drawable.dna_blue),
    MATHEMATICS("Mathematics", R.drawable.math_symbol, R.drawable.math_symbols_blue),
    DEFAULT("DEFAULT", R.drawable.deafault_subject, R.drawable.deafault_subject_teal),
    HINDI("Hindi", R.drawable.hindi_default, R.drawable.hindi_default),
    ENGLISH("English", R.drawable.english_default, R.drawable.english_default);

    @Getter
    private final String code;
    private final int iconResId;
    private final int iconBlueResId;

    OnboardingSubjectEnum(String code, int iconResId, int iconBlueResId) {
        this.code = code;
        this.iconResId = iconResId;
        this.iconBlueResId = iconBlueResId;
    }

    public int getIconResId(boolean isSelected) {
        return isSelected ? iconResId : iconBlueResId;
    }

    public String getBoardCode() {
        return code;
    }


    public static OnboardingSubjectEnum fromLocalizedName(String name, Context context) {
        for (OnboardingSubjectEnum subject : OnboardingSubjectEnum.values()) {
            if (subject.getBoardCode().equalsIgnoreCase(name.trim())) {
                return subject;
            }
        }
        return DEFAULT;
    }
}
