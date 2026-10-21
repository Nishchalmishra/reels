package arivihan.technologies.doubtbuzzter2.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WebChatMessageDto {
    String responseId;
    String botResponse;
    List<OptionResponse> optionResponse;
    DoubtChatConstants.DoubtResponseType responseType;
    DoubtChatConstants.DoubtRequestType requestType;
    String userQuery;
    String time;
    Boolean seen;
    String doubtId;
    boolean needFeedback;
    boolean chatQuotaExhausted;
    boolean clickable;
    boolean waitingForResponse;
    boolean markSeen;
    boolean showAvatar;
    boolean stepWiseSolution;

}
