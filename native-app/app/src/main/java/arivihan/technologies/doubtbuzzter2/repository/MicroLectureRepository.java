package arivihan.technologies.doubtbuzzter2.repository;
import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.concurrent.Executors;
import arivihan.technologies.doubtbuzzter2.database.dao.MicroLectureDao;
import arivihan.technologies.doubtbuzzter2.database.AppDatabase;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureEntity;
import lombok.Getter;

@Getter
public class MicroLectureRepository {
    private final MicroLectureDao microLectureDao;

    public MicroLectureRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        microLectureDao = database.microLectureDao();
    }

    public void insert(MicroLectureEntity microLecture) {
        Executors.newSingleThreadExecutor().execute(() -> microLectureDao.insert(microLecture));
    }

    public LiveData<MicroLectureEntity> getSelectionById(String chapterId) {
        return microLectureDao.getSelectionById(chapterId);
    }

    public void clearTable(String userId) {
        microLectureDao.clearTable(userId);
    }

    public void updateSelection(String selectedMicroLectureId, boolean isLectureSelected,
                                boolean isPracticeSelected, String chapterId, String lectureContentType) {
        Executors.newSingleThreadExecutor().execute(() ->
                microLectureDao.updateSelection(selectedMicroLectureId, isLectureSelected, isPracticeSelected,
                        chapterId, lectureContentType));

    }
}
