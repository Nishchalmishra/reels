package arivihan.technologies.doubtbuzzter2.model.firebase.rdb;

import java.io.Serializable;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Data
public class TechnicalErrorPopUp implements Serializable {
    private long updatedAt;
    private String url;
}
