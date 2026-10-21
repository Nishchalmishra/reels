package arivihan.technologies.doubtbuzzter2.model;

import lombok.Getter;

@Getter
public enum SubjectEnum {
    PHYSICS("Physics","Physics"),
    CHEMISTRY("Chemistry","Chemistry"),
    BIOLOGY("Biology","Biology"),
    MATHEMATICS("Mathematics","Math"),
    ENGLISH("English","English"),
    HINDI("Hindi","Hindi");
    private final String value;
    private final String alternateValue;
    SubjectEnum(String value,String alternateValue) {
        this.value = value;
        this.alternateValue=alternateValue;
    }

    public static SubjectEnum fromString(String text) {
        for (SubjectEnum subject : SubjectEnum.values()) {
            if (subject.value.equalsIgnoreCase(text) || subject.alternateValue.equalsIgnoreCase(text)) {
                return subject;
            }
        }

        return PHYSICS;
    }
}
