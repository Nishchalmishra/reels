package arivihan.technologies.doubtbuzzter2.model;

import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class SearchResultResponse {
    private String group; // video/text
    private String type; // lecture in focus / section
    private String title; // title
    private Integer pos; // position
    private boolean strmr; // strimmer
    private String screen;
    private ArrayList<SearchResult> data; //data
    private boolean last;

    @Override
    public String toString() {
        return "SearchResultResponse{" +
                "group='" + group + '\'' +
                ", type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", pos=" + pos +
                ", strmr=" + strmr +
                ", screen='" + screen + '\'' +
                ", last=" + last +
                ", data=" + data +
                '}';
    }
}
