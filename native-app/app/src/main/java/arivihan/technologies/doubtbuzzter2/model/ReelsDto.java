package arivihan.technologies.doubtbuzzter2.model;

import java.io.Serializable;
import java.util.Set;

import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * File: ReelsDto Copyright 29/5/21 10:41 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 29/5/21 10:41 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReelsDto implements Serializable {
    private String id;
    private String contentUrl;
    private ReelsType reelsType;
    private String name;
    private String actualName;
    private long actualLikes;
    private long actualViews;
    private String likes;
    private String views;
    private Set<String> likesBy;
    private boolean liked;
    private boolean hideAction;
    private String aspectRatio;

    public String getViews() {
        return CommonMethods.format(actualViews);
    }

    public String getLikes() {
        return CommonMethods.format(actualLikes);
    }

    @Override
    public String toString() {
        return "ReelsDto{" +
                "contentUrl='" + contentUrl + '\'' +
                ", reelsType=" + reelsType +
                ", name='" + name + '\'' +
                ", actualName='" + actualName + '\'' +
                ", actualLikes=" + actualLikes +
                ", actualViews=" + actualViews +
                '}';
    }
}
