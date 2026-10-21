package arivihan.technologies.doubtbuzzter2.converters;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.model.MicroLectureQuestionAnsResponseDto;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureQuestionsAnsDto;
import arivihan.technologies.doubtbuzzter2.model.TestSeriesQuestionDto;

public class Converters {

    private static final Gson gson = new Gson();
    @TypeConverter
    public static String fromQuestionsAnsList(List<MicroLectureQuestionsAnsDto> list) {
        Gson gson = new Gson();
        return gson.toJson(list);
    }

    @TypeConverter
    public static List<MicroLectureQuestionsAnsDto> toQuestionsAnsList(String json) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<MicroLectureQuestionsAnsDto>>() {}.getType();
        return gson.fromJson(json, type);
    }

    @TypeConverter
    public static String fromQuestionsAnsResponseList(List<MicroLectureQuestionAnsResponseDto> list) {
        Gson gson = new Gson();
        return gson.toJson(list);
    }

    @TypeConverter
    public static List<MicroLectureQuestionAnsResponseDto> toQuestionsAnsResponseList(String json) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<MicroLectureQuestionAnsResponseDto>>() {}.getType();
        return gson.fromJson(json, type);
    }

    @TypeConverter
    public static String fromQuestionList(List<TestSeriesQuestionDto> list) {
        return gson.toJson(list);
    }

    @TypeConverter
    public static List<TestSeriesQuestionDto> toQuestionList(String value) {
        if (value == null) return new ArrayList<>();
        Type type = new TypeToken<List<TestSeriesQuestionDto>>() {}.getType();
        return gson.fromJson(value, type);
    }
}
