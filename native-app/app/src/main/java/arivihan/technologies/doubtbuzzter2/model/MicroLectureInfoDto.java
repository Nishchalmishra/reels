package arivihan.technologies.doubtbuzzter2.model;

/*
 * File: MicroLectureInfoDto Copyright 14/2/21 12:31 AM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 14/2/21 12:31 AM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */


import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MicroLectureInfoDto implements Serializable {
    private String summary;
    private String id;
    private String title;
    private boolean personalized;
    private String message;
    private String code;
    private boolean locked;
    private boolean expandable;
    private int progress;
    private List<MicroLectureContentDto> microLectureContentDtoList;
    private String lockMessage;
    private Boolean isNotWatched;

}
