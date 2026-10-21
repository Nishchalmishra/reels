package arivihan.technologies.doubtbuzzter2.model;


import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UiTimetableDay {
    private int dayNumber;
    private String dayLabel;
    private String date;
    public boolean isToday;
    private List<UiSession> sessions;
}