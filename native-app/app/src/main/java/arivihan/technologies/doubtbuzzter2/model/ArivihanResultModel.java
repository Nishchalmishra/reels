package arivihan.technologies.doubtbuzzter2.model;


import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArivihanResultModel {

    public String message;
    public String link;

}
