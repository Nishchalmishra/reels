package arivihan.technologies.doubtbuzzter2.model;

import lombok.Data;

@Data
public class ArivihanSessionMessage {
    private String content;
    private String metadata;
    private String role;
    private String timestamp;
}