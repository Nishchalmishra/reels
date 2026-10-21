package arivihan.technologies.doubtbuzzter2.model;


import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoryModel {

    String game;
    String gameId;
    int points;
    String duration;
    String date;
    GameSyllabus gameSyllabus;
    List<List<String>> instructions;

}
