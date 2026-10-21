package arivihan.technologies.doubtbuzzter2.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class StyledTextModel implements Serializable {
    public String text;
    public String font;   // name of font in res/font
    public String color;  // e.g., "#000000"
    public String style;  // "normal", "bold", "italic"
    public int size;      // size in sp
    public boolean usePixel;
    public boolean useDp;
    public boolean underline;
    public boolean strikethrough; // NEW

    public StyledTextModel(String text, String font, String color, String style, int size, boolean usePixel, boolean useDp, boolean underline, boolean strikethrough) {
        this.text = text;
        this.font = font;
        this.color = color;
        this.style = style;
        this.size = size;
        this.usePixel = usePixel;
        this.useDp = useDp;
        this.underline = underline;
        this.strikethrough = strikethrough; // NEW
    }
}