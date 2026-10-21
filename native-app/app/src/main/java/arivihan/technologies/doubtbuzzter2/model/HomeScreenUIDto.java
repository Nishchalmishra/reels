package arivihan.technologies.doubtbuzzter2.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public class HomeScreenUIDto implements Serializable {

    private String homeScreenTitleIcon;
    private String homeScreenTitleText;
    private String homeScreenHeaderBackgroundImageUrl;
    private String homeScreenFooterImageUrl;

}
