package arivihan.technologies.doubtbuzzter2.model;

import java.util.List;

import lombok.Data;

@Data
public class DoubtChatHistoryData {
    List<WebChatRecord> list;
    List<DoubtFilters> filterModelList;
    String lastId;
}
