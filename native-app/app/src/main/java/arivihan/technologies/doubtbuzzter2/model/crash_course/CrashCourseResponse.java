package arivihan.technologies.doubtbuzzter2.model.crash_course;

public class CrashCourseResponse {
    private TodayClassResponse todayClasses;
    private PreExamPreparationResponse preExamPreparation;

    public CrashCourseResponse() {}

    public TodayClassResponse getTodayClasses() { return todayClasses; }
    public void setTodayClasses(TodayClassResponse todayClasses) { this.todayClasses = todayClasses; }

    public PreExamPreparationResponse getPreExamPreparation() { return preExamPreparation; }
    public void setPreExamPreparation(PreExamPreparationResponse preExamPreparation) { this.preExamPreparation = preExamPreparation; }
}
