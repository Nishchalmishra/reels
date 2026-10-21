package arivihan.technologies.doubtbuzzter2.model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OptionResponse {
    private String title;
    private String iconUrl;
    private long startPosition;
    private long endPosition;
    private boolean somethingElseOption;
    private String contentUrl;
}
