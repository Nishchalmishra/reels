package arivihan.technologies.doubtbuzzter2.model.offline.type;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteTypeColorModel implements Serializable {
    private LanguageNameModel name;
    private String colorHex;
}
