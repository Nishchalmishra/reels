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
public class DoubtBotResponse {
    String chatSesssionId;
    String botResponse;
    String time;
    boolean needFeedback;
    List<OptionResponse> optionResponse;
    DoubtChatConstants.DoubtResponseType responseType;
    String responseId;
    boolean chatQuotaExhausted;
    boolean rasaOption;
    boolean giveOption;
    Boolean seen;
    String doubtId;
    Boolean thumbsUp;
    boolean showBotAvatar;
    boolean waitingForResponse;
    boolean markSeen;
    boolean stepWiseSolution;
}
