package arivihan.technologies.doubtbuzzter2.model;


import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TestSeriesModel implements Serializable {

    public String id;
    public String name;
    public String color;
    public String iconUrl;
    public int count;



}
