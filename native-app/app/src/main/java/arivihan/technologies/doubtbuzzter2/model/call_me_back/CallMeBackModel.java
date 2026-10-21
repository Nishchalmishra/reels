package arivihan.technologies.doubtbuzzter2.model.call_me_back;

import java.io.Serializable;

import arivihan.technologies.doubtbuzzter2.model.SnackBarMessageModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CallMeBackModel implements Serializable {
    private String identifier;
    private SnackBarMessageModel snackBarMessage;
}
