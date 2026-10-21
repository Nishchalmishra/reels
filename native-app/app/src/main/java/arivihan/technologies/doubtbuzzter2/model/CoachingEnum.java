package arivihan.technologies.doubtbuzzter2.model;

/*
 * File: CoachingEnum Copyright 31/08/25, 7:11 pm Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 31/08/25, 7:11 pm
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import arivihan.technologies.doubtbuzzter2.R;
import lombok.Getter;

public enum CoachingEnum {
    COACHING("1", R.drawable.coaching_white, R.drawable.coaching_blue, R.string.coaching_jaata_hu),
    NO_COACHING("2",R.drawable.no_coaching_white, R.drawable.no_coaching_blue, R.string.coaching_nahi_jaata_hu),
    DEFAULT("0",R.drawable.coaching_white, R.drawable.coaching_blue, R.string.coaching_jaata_hu);

    @Getter
    private final String coachingId;
    @Getter
    private final int coachingIconResId;
    @Getter
    private final int coachingIconBlueResId;
    @Getter
    private final int stringResource;

    CoachingEnum(String coachingId, int coachingIconResId, int coachingIconBlueResId, int stringResource) {
        this.coachingId = coachingId;
        this.coachingIconResId = coachingIconResId;
        this.coachingIconBlueResId = coachingIconBlueResId;
        this.stringResource = stringResource;
    }

    public int getIconResId(boolean isSelected) {
        return isSelected ? coachingIconResId : coachingIconBlueResId;
    }

    public String getExamCode() {
        return coachingId;
    }


    public static CoachingEnum fromLocalizedName(String name) {
        for (CoachingEnum exam : CoachingEnum.values()) {
            if (exam.getExamCode().equalsIgnoreCase(name.trim())) {
                return exam;
            }
        }
        return DEFAULT;
    }
}
