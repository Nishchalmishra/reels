package arivihan.technologies.doubtbuzzter2.model;/*
 * File: MicrolectureBoardContentInfoDto Copyright 09/12/23, 1:44 AM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 09/12/23, 1:44 AM
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
public class MicrolectureBoardContentInfoDto implements Serializable {
    private String title;
    private LectureContentType type;
    private int progress;
    private boolean expanded;
    private List<MicroLectureContentDto> data;
}
