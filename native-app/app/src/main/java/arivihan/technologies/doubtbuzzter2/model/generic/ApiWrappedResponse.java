package arivihan.technologies.doubtbuzzter2.model.generic;

import com.google.gson.annotations.SerializedName;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApiWrappedResponse<T> {

    @SerializedName("message")
    private String message;

    @SerializedName("code")
    private int code;

    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private T data;
    // Explicit getters

    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }


    // Explicit setters

    public void setMessage(String message) {
        this.message = message;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setData(T data) {
        this.data = data;
    }
}