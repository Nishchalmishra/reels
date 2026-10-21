package arivihan.technologies.doubtbuzzter2.model;

import lombok.Data;

@Data
public class DoubtChatLimitationCheckDto {
    private boolean subscriptionStatus;
    private int chatSessionsCount;
    private int sessionsLimitPerUser;
    private int messageLimitPerSession;
    private int messageLimitPerSessionForSubscribedUser;

}
