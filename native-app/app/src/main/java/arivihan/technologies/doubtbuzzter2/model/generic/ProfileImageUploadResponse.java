package arivihan.technologies.doubtbuzzter2.model.generic;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProfileImageUploadResponse {
    private boolean success;
    private String message;
    private int code;
    private String data;

//    @Data
//    @NoArgsConstructor
//    public static class ProfileImageData {
//        private String imageUrl;
//    }
}
