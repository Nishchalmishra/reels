package arivihan.technologies.doubtbuzzter2.model.asat;

import java.io.Serializable;

import lombok.Data;

@Data
public class UploadMarksheetResponse implements Serializable {
    private String message;
    private boolean success;
}
