package arivihan.technologies.doubtbuzzter2.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReelsApiResult {
    private int size;
    private String lastId;
    private boolean success;
    private List<ReelInfoDto> data;

    @Override
    public String toString() {
        return "ReelsApiResult{" +
                "size=" + size +
                ", lastId='" + lastId + '\'' +
                ", success=" + success +
                ", data=" + data +
                '}';
    }
}
