package arivihan.technologies.doubtbuzzter2.model.db_entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.model.TestSeriesQuestionDto;
import lombok.Builder;
import lombok.Data;

@Entity(tableName = "test_series_cache")
@Data
@Builder(access = lombok.AccessLevel.PUBLIC)
public class TestSeriesCacheEntity {

    @PrimaryKey
    @NonNull
    public String testId;

    public String testSeriesAnalytics; // Stringify JSON

    public List<TestSeriesQuestionDto> questions; // Stringify JSON via TypeConverter
}
