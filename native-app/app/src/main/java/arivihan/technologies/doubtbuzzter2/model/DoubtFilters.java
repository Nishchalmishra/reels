package arivihan.technologies.doubtbuzzter2.model;

import androidx.annotation.NonNull;

import java.util.List;

import lombok.Data;

@Data
public class DoubtFilters implements Cloneable {
    String title;
    List<DoubtFilterModel> list;

    @NonNull
    @Override
    public DoubtFilters clone() {
        try {
            return (DoubtFilters) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
