package arivihan.technologies.doubtbuzzter2.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.model.player.MiniVideoPlayerConfig;
import lombok.Data;


@Data
public class ToDoStateUser implements Serializable {
    private String userId;
    private String classIdCourseIdLanguage;
    private int stepNumber;
    private ArrayList<ToDoSubjectSelectionModel> subjectModelList;
    private String startDate;
    private String endDate;
    private String examDate;
    private ArrayList<ArrayList<UserSyllabusOrder>> list;
    private Map<String, MiniVideoPlayerConfig> miniVideoList;
}
