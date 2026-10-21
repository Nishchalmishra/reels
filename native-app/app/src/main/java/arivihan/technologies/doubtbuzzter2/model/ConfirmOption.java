package arivihan.technologies.doubtbuzzter2.model;
/*
 * File: ConfirmOption Copyright 15/07/21, 11:40 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 15/07/21, 11:40 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmOption implements Serializable {
    private String id;
    private boolean correct;
    private String text;
    private Integer seekTo;
    private Integer trackerCompletionStartPos;
    private Integer trackerCompletionEndPos;
    private Integer fallbackPos;

    @Override
    public String toString() {
        return "ConfirmOption{" +
                "id='" + id + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
