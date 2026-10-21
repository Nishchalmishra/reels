package arivihan.technologies.doubtbuzzter2.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoutineSyllabus implements Serializable {
    private Map<String, List<UserDailyRoutineDto>> allUserRoutine;
    private List<String> dates;
    private int currentDateIndex;
    private boolean inProgress;
    private boolean generated;
    private String nextWeekStartDate;
    private String prevWeekEndDate;
    private String routineStartDate;
    private boolean todayOrBeforeStartDate;
    private int selection;

    private List<TodoDateSelectionModel> dateCardsData;
    private String timeTableDownloadUrl;
    private String selectedDateId;
    private int completedTasksOfToday;
    private int totalTasksOfToday;

    private String titleText;
    private boolean syllabusCompleted;
}
