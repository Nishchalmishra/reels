package arivihan.technologies.doubtbuzzter2.model;

import java.util.List;

import lombok.Data;

@Data
public class ImageTextExtractionResponse {
    private String message;
    private boolean textExtracted;
    private String code;
    private List<String> urls;
    private String requestId;
    private String sessionId;
}
