package arivihan.technologies.doubtbuzzter2.model;


import com.fasterxml.jackson.annotation.JsonIgnore;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import java.util.Date;
import java.util.TimeZone;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnsolvedDoubtModel {

    private String doubtType;
    private String Text;
    private Boolean Approved;
    private String AskesBy;
    private String Chapter;
    private String Subject;
    private String PreferredFormat;
    private String Description;
    private String DoubtId;
    private String DoubtNumber;
    private Boolean Solved;
    private Boolean Taken;
    private String TakenBy;
    private String selectedSubject;
    private int DoubtPoints;
    private String imageUrl;
    private String AskedAt;
    private Long askedOn;
    private boolean isPseudoDoubt;
    String askedBy;
    String microlectureId;




}
