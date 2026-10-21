package arivihan.technologies.doubtbuzzter2.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ToDoResponse {
    String message;
    int code;
    RoutineSyllabus data;
}
