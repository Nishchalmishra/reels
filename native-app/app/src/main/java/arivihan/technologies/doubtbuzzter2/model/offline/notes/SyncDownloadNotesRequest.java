package arivihan.technologies.doubtbuzzter2.model.offline.notes;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SyncDownloadNotesRequest implements Serializable {
    private Integer id;
    private String type;
    private String url;
    private String subjectName;
    private boolean locked;
}
