package arivihan.technologies.doubtbuzzter2.model;


import android.annotation.SuppressLint;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoubtModel {
    @JsonIgnore
    FastDateFormat dateFormat = FastDateFormat.getInstance("yyyyMMdd_HHmmss", TimeZone.getTimeZone("IST"));
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
    private int DoubtPoints;
    private String imageUrl;
    private String AskedAt;
    private boolean unwatched;
    private Long askedOn;
    private boolean isPseudoDoubt;
    String askedBy;
    String microlectureId;
    private boolean chatHistoryExists;


    public long getAskedOn() {
        try {
            if (askedOn == null && StringUtils.isNotEmpty(AskedAt)) {
                Date parse = dateFormat.parse(AskedAt);
                if (parse != null) {
                    return parse.getTime();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }


}
