package arivihan.technologies.doubtbuzzter2.model;/*
 * File: VideoQualityDto Copyright 11/01/24, 4:19 pm Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 11/01/24, 4:19 pm
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VideoQualityDto implements Serializable {
    private boolean selected;
    private String quality;
    private String videoUrl;
    private String size;

}
