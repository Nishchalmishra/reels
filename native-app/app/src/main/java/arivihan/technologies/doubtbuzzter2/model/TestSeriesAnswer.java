package arivihan.technologies.doubtbuzzter2.model;

/*
 * File: tTestSeriesAnswer Copyright 9/1/22 3:25 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 9/1/22 3:25 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestSeriesAnswer implements Serializable {
    private String testSeriesId;
    private String subject;
    private long timeTaken;
    private Long actualTimeTaken;
    private String userId;
    private boolean board;
    private boolean combined;
    private List<AnswerInfoList> answerInfoList;
}
