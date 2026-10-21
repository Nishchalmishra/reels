package arivihan.technologies.doubtbuzzter2.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult implements Serializable {
    private static final long serialVersionUID = -1547612378868495717L;
    private String id; // all ids - separated by ##
    private String t; //title
    private String st; //subTitle
    private String des; // description
    private String lang; // language
    private String type; //type
    private boolean board;
    private boolean doubt;
    private String turl; // thumbnailUrl
    private String curl; // Content url
    private String ctype; //content Type
    private Long videoStartPos;
    private Long videoEndPos;
    private boolean strmr;
    private String mid;

    @Override
    public String toString() {
        return "SearchResult{" +
                "id='" + id + '\'' +
                ", t='" + t + '\'' +
                ", st='" + st + '\'' +
                ", des='" + des + '\'' +
                ", lang='" + lang + '\'' +
                ", type='" + type + '\'' +
                ", board=" + board +
                ", curl='" + curl + '\'' +
                ", tUrl='" + turl + '\'' +
                ", ctype='" + ctype + '\'' +
                ", videoStartPos=" + videoStartPos +
                ", videoEndPos=" + videoEndPos +
                ", strmr=" + strmr +
                '}';
    }
}
