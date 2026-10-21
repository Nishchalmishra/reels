package arivihan.technologies.doubtbuzzter2.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class ChaptersInfo implements Serializable {
    private String name;
    private String id;
    private boolean completed;
    private List<NameAndIdDto> microlectures;
}
