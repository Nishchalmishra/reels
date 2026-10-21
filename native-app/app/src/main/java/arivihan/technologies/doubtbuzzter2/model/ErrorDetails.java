package arivihan.technologies.doubtbuzzter2.model;

/*
 * File: ErrorDetails Copyright 13/1/22 1:27 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 13/1/22 1:27 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ErrorDetails {
    private Date date;
    private int status;
    private String message;
    private String details;

    @Override
    public String toString() {
        return "ErrorDetails{" +
                "date=" + date +
                ", status=" + status +
                ", message='" + message + '\'' +
                ", details='" + details + '\'' +
                '}';
    }
}