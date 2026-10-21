package arivihan.technologies.doubtbuzzter2.model;


import arivihan.technologies.doubtbuzzter2.R;
import lombok.Getter;

public enum ExamEnum {
    JEE("1", R.drawable.jee_white, R.drawable.jee_teal),
    NEET("3", R.drawable.neet_white, R.drawable.neet_teal),
    DEFAULT("0", R.drawable.exam_default, R.drawable.exam_default_teal);

    @Getter
    private final String examId;
    @Getter
    private final int iconResId;
    private final int iconBlueResId;

    ExamEnum(String examId, int iconResId, int iconBlueResId) {
        this.examId = examId;
        this.iconResId = iconResId;
        this.iconBlueResId = iconBlueResId;
    }

    public int getIconResId(boolean isSelected) {
        return isSelected ? iconResId : iconBlueResId;
    }

    public String getExamCode() {
        return examId;
    }


    public static ExamEnum fromLocalizedName(String name) {
        for (ExamEnum exam : ExamEnum.values()) {
            if (exam.getExamCode().equalsIgnoreCase(name.trim())) {
                return exam;
            }
        }
        return DEFAULT;
    }
}
