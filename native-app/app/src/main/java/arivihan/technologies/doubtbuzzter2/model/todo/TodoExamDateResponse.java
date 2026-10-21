package arivihan.technologies.doubtbuzzter2.model.todo;

import java.io.Serializable;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Data
public class TodoExamDateResponse implements Serializable {
    private String message;
    private int code;
    private boolean success;
    private TodoExamDate data;

}
