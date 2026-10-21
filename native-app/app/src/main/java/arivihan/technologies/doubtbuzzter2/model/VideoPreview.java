package arivihan.technologies.doubtbuzzter2.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoPreview implements Serializable {

    private String thumbnail;
    private String url;
    private long startPos;
    private long endPos;

}
