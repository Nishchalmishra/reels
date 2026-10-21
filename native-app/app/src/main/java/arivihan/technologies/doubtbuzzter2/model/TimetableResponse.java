//package arivihan.technologies.doubtbuzzter2.dto;
//
//import com.google.gson.annotations.SerializedName;
//
///**
// * Maps the outermost API envelope:
// * { "success": true, "data": {...}, "message": "...", "code": 200 }
// */
//public class TimetableResponse {
//
//    @SerializedName("success")
//    private boolean success;
//
//    @SerializedName("data")
//    private TimetableData data;
//
//    @SerializedName("message")
//    private String message;
//
//    @SerializedName("code")
//    private int code;
//
//    public boolean       isSuccess() { return success; }
//    public TimetableData getData()   { return data;    }
//    public String        getMessage(){ return message; }
//    public int           getCode()   { return code;    }
//}