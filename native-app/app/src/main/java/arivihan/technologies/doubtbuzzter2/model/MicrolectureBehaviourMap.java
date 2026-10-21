package arivihan.technologies.doubtbuzzter2.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class MicrolectureBehaviourMap implements Serializable {

    private String id;
    private Integer videoStartPosition;
    private Integer videoEndPosition;
    private Integer seekStartPosition;
    private Integer seekEndPosition;
    private Integer callbackPosition;

}
