package arivihan.technologies.doubtbuzzter2.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UiTimetableData {
    private String title;
    private String view;
    private String startDate;
    private String endDate;
    private List<UiTimetableDay> days;
}