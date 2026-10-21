package arivihan.technologies.doubtbuzzter2.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter

public class NameAndIdDto implements Serializable {
    private String name;
    private String examName;
    private String id;
    private boolean completed;
    private boolean selected;
    private String examIconUrl;
    private Boolean additional;
    private boolean dailyRoutineExists;
    private boolean examDateFilled;


    public NameAndIdDto() {

    }

    public NameAndIdDto(String name, String id) {
        this.name = name;
        this.id = id;
        this.completed = completed;
    }

    @Override
    public String toString() {
        return "NameAndIdDto{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", completed=" + completed +
                ", selected=" + selected +
                ", examIconUrl='" + examIconUrl + '\'' +
                ", additional=" + additional +
                ", dailyRoutineExists=" + dailyRoutineExists +
                ", examDateFilled=" + examDateFilled +
                '}';
    }
}
