package arivihan.technologies.doubtbuzzter2.model;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.Data;

@Data
public class HalfYearlySection implements Serializable {
    private String title;
    private HalfYearlySectionEnum sectionType;
    private HalfYearlyPopupData halfYearlyPopupData;
    private Integer ongoingDay;
    private ArrayList<HalfYearlyItem> sectionItems;
}
