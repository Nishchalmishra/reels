package arivihan.technologies.doubtbuzzter2.model;


import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public class GameLevelDto {

     private String id;
     private String name;
     private Boolean locked;
     private int pointToWin;

}
