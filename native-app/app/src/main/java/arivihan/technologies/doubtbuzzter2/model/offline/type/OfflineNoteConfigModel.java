package arivihan.technologies.doubtbuzzter2.model.offline.type;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfflineNoteConfigModel implements Serializable {

    @SerializedName("categories")
    private Map<String, SubjectColorModel> categories;

    @SerializedName("note_types")
    private Map<String, NoteTypeColorModel> noteTypes;
}
