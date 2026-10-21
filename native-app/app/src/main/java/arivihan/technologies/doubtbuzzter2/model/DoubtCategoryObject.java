package arivihan.technologies.doubtbuzzter2.model;

import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class DoubtCategoryObject {
    String title;
    ArrayList<WebChatRecord> webChatRecords;
}
