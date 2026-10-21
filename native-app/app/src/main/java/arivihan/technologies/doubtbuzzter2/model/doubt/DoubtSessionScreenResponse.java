package arivihan.technologies.doubtbuzzter2.model.doubt;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoubtSessionScreenResponse implements Serializable {
    private List<DoubtTabsModel> doubtTypes;
    private List<DoubtSessionHistory> list;
    private String lastId;
}


