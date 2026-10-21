package arivihan.technologies.doubtbuzzter2.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import arivihan.technologies.doubtbuzzter2.model.db_entity.TestSeriesCacheEntity;

@Dao
public interface TestSeriesCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TestSeriesCacheEntity entity);

    @Query("SELECT * FROM test_series_cache WHERE testId = :id")
    TestSeriesCacheEntity getByTestId(String id);

    @Query("DELETE FROM test_series_cache WHERE testId = :id")
    void deleteById(String id);

    @Query("DELETE FROM test_series_cache")
    void clearAll();

}

