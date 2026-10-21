package arivihan.technologies.doubtbuzzter2.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class GameSyllabus implements Serializable {
    private List<ChaptersInfo> chapters;
}
