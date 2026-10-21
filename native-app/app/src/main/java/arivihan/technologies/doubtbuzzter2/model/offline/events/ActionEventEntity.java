package arivihan.technologies.doubtbuzzter2.model.offline.events;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lombok.Data;

/*
 * File: ActionEventEntity Copyright 18/04/26, 12:50 pm Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 18/04/26, 12:50 pm
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

@Entity(tableName = "action_events_table")
@Data
public class ActionEventEntity {

    @PrimaryKey
    @NonNull
    public String id;

    public String event;
    public String examSelected;
    public String phoneNumber;
    public boolean registered;
    public long timestamp;
    public String status; // PENDING, SENT, FAILED
    public int responseCode;
    public String responseBody;

    public ActionEventEntity() {
    }

    public ActionEventEntity(@NonNull String id, String event, String examSelected, String phoneNumber, boolean registered, long timestamp, String status) {
        this.id = id;
        this.event = event;
        this.examSelected = examSelected;
        this.phoneNumber = phoneNumber;
        this.registered = registered;
        this.timestamp = timestamp;
        this.status = status;
    }

    // Getters and setters (generated)
    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getExamSelected() {
        return examSelected;
    }

    public void setExamSelected(String examSelected) {
        this.examSelected = examSelected;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }
}
