package arivihan.technologies.doubtbuzzter2.model.pdf_circle;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PDFCircleUsage implements Serializable {
    private int usageCount;
    private int dailyLimit;
}