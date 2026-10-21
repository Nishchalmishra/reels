package arivihan.technologies.doubtbuzzter2.model.doubt;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoubtTabsModel implements Serializable {
    private String type;
    private String title;
}
