package arivihan.technologies.doubtbuzzter2.model.remote_config;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodoMessageConfig implements Serializable {
    @SerializedName("todo_creation_visibility")
    private boolean todoCreationVisibility;

    @SerializedName("todo_restriction_message_header")
    private String todoRestrictionMessageHeader;

    @SerializedName("todo_restriction_message_body")
    private String todoRestrictionMessageBody;

    @SerializedName("todo_empty_message")
    private String todoEmptyMessage;
}
