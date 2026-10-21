package arivihan.technologies.doubtbuzzter2.model.microlecture;

import java.io.Serializable;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Data
public class FeedbackOption implements Serializable {
    private String icon;
    private String message;
    private boolean isSelected; // Default false, used for storing selected option locally
}
