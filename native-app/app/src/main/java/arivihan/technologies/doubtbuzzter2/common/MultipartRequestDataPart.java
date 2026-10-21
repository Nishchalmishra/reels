package arivihan.technologies.doubtbuzzter2.common;

import java.io.UnsupportedEncodingException;

public class MultipartRequestDataPart {

    private String fileName;
    private byte[] content;
    private String type;

    public MultipartRequestDataPart(String fileName, byte[] content, String type) {
        this.fileName = fileName;
        this.content = content;
        this.type = type;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getContent() {
        return content;
    }

    public String getType() {
        return type;
    }

    public String getBase64() {
        try {
            return new String(content, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }
}
