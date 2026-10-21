package arivihan.technologies.doubtbuzzter2.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodoDateSelectionModel implements Serializable {
    private int date;
    private String month;
    private String id;
    private String day;
    private String dayText;
    private String dayCount;
}
