package arivihan.technologies.doubtbuzzter2.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoubtUserRequest {
    boolean firstDoubt;
    String userQuery;
    DoubtChatConstants.DoubtRequestType requestType;
    String selectedSubjectName;
    String chatSessionId;
    boolean giveOption;
    String doubtImageUrl;
    boolean mockTestDoubt;
    String question;
    String answer;

    @Override
    public String toString() {
        return "DoubtUserRequest{" +
                "firstDoubt=" + firstDoubt +
                ", userQuery='" + userQuery + '\'' +
                ", requestType=" + requestType +
                ", selectedSubjectName='" + selectedSubjectName + '\'' +
                ", chatSessionId='" + chatSessionId + '\'' +
                ", giveOption=" + giveOption + '\'' +
                ", isMockTestDoubt=" + mockTestDoubt + '\'' +
                ", question=" + question + '\'' +
                ", answer=" + answer + '\'' +
                '}';
    }
}
