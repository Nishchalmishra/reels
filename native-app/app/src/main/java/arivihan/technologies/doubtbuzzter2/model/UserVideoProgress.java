package arivihan.technologies.doubtbuzzter2.model;

import java.io.Serializable;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserVideoProgress implements Serializable {
    private String userId;
    private String chapterId;
    private Map<String, Long> videoProgress;
}
