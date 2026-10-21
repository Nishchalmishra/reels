package arivihan.technologies.doubtbuzzter2.model;

/*
 * File: ArivihanLanguageEnum Copyright 9/6/22 4:30 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 9/6/22 4:30 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

public enum ArivihanLanguageEnum {
    HINDI("hi"),
    ENGLISH("en");

    String code;

    ArivihanLanguageEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static ArivihanLanguageEnum fromString(String value) {
        return getEnumByValue(value);
    }

    public static String fromStringToCode(String value) {
        ArivihanLanguageEnum lang = getEnumByValue(value);
        return lang == null ? null : lang.getCode();
    }

    private static ArivihanLanguageEnum getEnumByValue(String value) {
        if (value == null || value.isEmpty()) return null;

        String trimmed = value.trim();

        for (ArivihanLanguageEnum lang : ArivihanLanguageEnum.values()) {
            if (lang.name().equalsIgnoreCase(trimmed)) {
                return lang;
            }
            if (lang.code.equalsIgnoreCase(trimmed)) {
                return lang;
            }
        }

        return null;
    }

}

