package arivihan.technologies.doubtbuzzter2.model.asat;

import java.io.Serializable;

import lombok.Data;

@Data
public class CallBackResponse implements Serializable {
    private boolean success;
    private String message;
}
