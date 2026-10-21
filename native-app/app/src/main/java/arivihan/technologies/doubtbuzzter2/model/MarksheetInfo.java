package arivihan.technologies.doubtbuzzter2.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarksheetInfo implements Serializable {
    boolean uploaded;
    String roll;
    String percent;
    String url;

    @Override
    public String toString() {
        return "MarksheetInfo{" +
                "uploaded=" + uploaded +
                ", roll='" + roll + '\'' +
                ", percent='" + percent + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
