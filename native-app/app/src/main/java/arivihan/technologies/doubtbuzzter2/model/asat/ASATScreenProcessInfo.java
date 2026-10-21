package arivihan.technologies.doubtbuzzter2.model.asat;

import java.io.Serializable;
import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.model.ToppersHomeScreenModel;
import arivihan.technologies.doubtbuzzter2.model.UserAsatInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ASATScreenProcessInfo implements Serializable {

    String id;
    String classId;
    String courseId;
    String language;
    String thumbnail;
    String videoUrl;
    String resultVideoUrl;
    String text1;
    String text2;
    String text3;
    String text4;
    String scholarShipText;
    // id of test
    String testId;
    // JEE, NEET, Board
    String testGroupId;
    boolean hideAsat;
    String testTitle;
    ArrayList<ToppersHomeScreenModel> toppersList;
    String rules;
    String syllabus;
    ArrayList<ASATStepsInfo> asatStepsInfo;
    UserAsatInfo userAsatInfo;
}

//public class ASATScreenscholarshipDto implements Serializable {
//    String id;
//    String classId;
//    String courseId;
//    String language;
//    String thumbnail;
//    String videoUrl;
//    String text1;
//    String text2;
//    String text3;
//    String text4;
//    String scholarShipText;
//    String rulesAndRegulation;
//    String syllabusInfo;
//    //id of test
//    String testId;
//    //Jee, nee, board
//    String testGroupId;
//    String testTitle;
//    ArrayList<ToppersModel> toppersList;
//    String rules;
//    String syllabus;
//    ArrayList<ASATStepsInfo> asatStepsInfo;
//    UserAsatInfo userAsatInfo;
//}
