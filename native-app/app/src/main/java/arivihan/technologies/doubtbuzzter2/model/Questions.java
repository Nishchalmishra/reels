package arivihan.technologies.doubtbuzzter2.model;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Questions implements Serializable {

    private String title;
    private String type;
    private String status;
    private boolean lock;
    private boolean skip; // skip define weather we should skip lectures notes in continuity
    private String pdfUrl;
    private boolean highlightText ;
    private Set<String> questionIds;
    @NonNull
    @Override
    public String toString() {
        return "Questions{" +
                "title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", skip='" + skip + '\'' +
                ", pdfUrl='" + pdfUrl + '\'' +
                ", highlightText =" + highlightText +
                '}';
    }

}
