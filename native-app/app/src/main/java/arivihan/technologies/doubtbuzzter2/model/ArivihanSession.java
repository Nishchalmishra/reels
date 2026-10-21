package arivihan.technologies.doubtbuzzter2.model;

import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import lombok.Data;

/**
 * A doubt chat session from the {@code arivihan-sessions} table
 * (response of {@code /secure/doubt/list/v3}).
 */
@Data
public class ArivihanSession {

    @SerializedName("chat_id")
    private String chatId;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("batch_code")
    private String batchCode;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    @SerializedName("first_message")
    private String firstMessage;

    private List<ArivihanSessionMessage> messages;

    @SerializedName("per_model_usage")
    private String perModelUsage;

    private String source;

    @SerializedName("student_name")
    private String studentName;

    private String subject;

    @SerializedName("subscription_type")
    private String subscriptionType;

    @SerializedName("token_usage")
    private ArivihanSessionTokenUsage tokenUsage;

    /**
     * Adapts this {@code arivihan-sessions} record to the legacy {@link WebChatRecord} shape used by
     * the previous-doubts list on the chat screens (which group by {@code historyCategory}).
     */
    public WebChatRecord toWebChatRecord() {
        WebChatRecord record = new WebChatRecord();
        record.setSessionId(chatId);
        record.setUserId(userId);
        record.setTitle(firstMessage);
        record.setSelectedSubjectId(subject);
        record.setSelectedSubject(subject);
        record.setDoubtChatQuestionType("TEXT");
        Date date = parseTimestamp(updatedAt != null ? updatedAt : createdAt);
        record.setUpdatedAt(date);
        record.setCreatedAt(date);
        record.setHistoryCategory(categorize(date));
        return record;
    }

    /** Parses an ISO timestamp such as {@code 2026-06-07T10:25:40.763761} (ignoring sub-seconds). */
    private static Date parseTimestamp(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            String secondsPart = value.length() >= 19 ? value.substring(0, 19) : value;
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH).parse(secondsPart);
        } catch (ParseException e) {
            return null;
        }
    }

    /** Mirrors the backend's bucketing; the keys match the {@code Recent_Chats}/… string resources. */
    private static String categorize(Date date) {
        if (date == null) {
            return "Older_Chats";
        }
        long days = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - date.getTime());
        if (days < 1) {
            return "Recent_Chats";
        } else if (days < 7) {
            return "Last_Week";
        } else if (days < 30) {
            return "Last_Month";
        }
        return "Older_Chats";
    }
}