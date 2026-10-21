package arivihan.technologies.doubtbuzzter2.model.dialog;

import java.io.Serializable;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PopUpOptionsDto implements Serializable {
    private String title;
    private String subTitle;
    private String bodyText;
    private String primaryButtonName;
    private String secondaryButtonName;
    private String primaryButtonNavigationType;
    private String primaryButtonClassName;
    private Map<String, Object> primaryButtonActivityParams;
    private String secondaryButtonNavigationType;
    private String secondaryButtonClassName;
    private Map<String, Object> secondaryButtonActivityParams;
}
