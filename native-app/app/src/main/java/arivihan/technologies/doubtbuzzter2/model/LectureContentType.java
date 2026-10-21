package arivihan.technologies.doubtbuzzter2.model;

/*
 * File: LectureContentType Copyright 14/2/21 12:32 AM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 14/2/21 12:32 AM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

public enum LectureContentType {
    VIDEO("video"), PDF("pdf"), NUMERICAL_QUESTION("Numerical Question"), CONCEPTUAL_QUESTION("Conceptual Question"),
    NOTES("Notes"), LONG_ANSWERS("Long Answers"), SHORT_ANSWERS("Short Answers"), VERY_SHORT_ANSWERS("Very Short Answers"),
    TRUE_FALSE("True False"), MATCH_THE_COLUMN("Match The Column"), FILL_IN_THE_BLANKS("Fill In The Blanks"),
    ONE_WORD_ANSWER("One Word Answer"), MULTIPLE_CHOICE_QUESTION("Multiple Choice Question"),ONE_LINER("One Liner"),ESSAY("Essay");

    private final String type;

    LectureContentType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static LectureContentType getLectureContentType(String s) {

        for (LectureContentType value : LectureContentType.values()) {
            if (value.name().equalsIgnoreCase(s)) {
                return value;
            }
        }
        return LectureContentType.MULTIPLE_CHOICE_QUESTION;
    }
}
