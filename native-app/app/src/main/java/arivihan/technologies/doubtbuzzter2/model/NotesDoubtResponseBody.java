package arivihan.technologies.doubtbuzzter2.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class NotesDoubtResponseBody implements Serializable {
    private String title;
    private String response;
    private String pageContext;
    private String subject;
    private String chapterName;
    private String language;
    private String request_id;
    private long processing_time_ms;
    private String trace_id;
    private String screenshot_url;
    private String userQuery;
    private String ocr_text;
    private String userId;
}

