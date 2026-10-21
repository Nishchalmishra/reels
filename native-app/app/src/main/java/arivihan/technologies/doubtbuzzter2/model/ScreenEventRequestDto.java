package arivihan.technologies.doubtbuzzter2.model;

import androidx.annotation.Nullable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * File: ScreenEventRequestDto Copyright Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created by: Divyansh Sharma
 * Version: 1.0
 * Project Name: Arivihan
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScreenEventRequestDto {
    private String eventId;
    private String eventType;
    private String userId;
    private String eventDate;
    private long timestamp;

    // Only present for SCREEN_VISITED events
    @Nullable private String screenName;
    @Nullable private String contentType;
    @Nullable private Long eventTime;
}