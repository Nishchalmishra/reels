package arivihan.technologies.doubtbuzzter2.model.microlecture;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Data
public class CommentResponse implements Serializable {
    private List<CommentModel> results;
    private String evaluatedKey;
}
