package arivihan.technologies.doubtbuzzter2.model;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class ArivihanSessionTokenUsage {
    @SerializedName("cached_tokens")
    private Integer cachedTokens;
    @SerializedName("completion_tokens")
    private Integer completionTokens;
    @SerializedName("fresh_tokens")
    private Integer freshTokens;
    @SerializedName("prompt_tokens")
    private Integer promptTokens;
    @SerializedName("total_tokens")
    private Integer totalTokens;
}