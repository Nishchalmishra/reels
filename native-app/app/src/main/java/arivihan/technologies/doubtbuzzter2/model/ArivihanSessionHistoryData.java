package arivihan.technologies.doubtbuzzter2.model;

import java.util.List;

import lombok.Data;

/**
 * Response of {@code /secure/doubt/list/v3} — a page of {@link ArivihanSession}
 * records plus the filter options and pagination cursor.
 */
@Data
public class ArivihanSessionHistoryData {
    List<ArivihanSession> list;
    List<DoubtFilters> filterModelList;
    String lastId;
}