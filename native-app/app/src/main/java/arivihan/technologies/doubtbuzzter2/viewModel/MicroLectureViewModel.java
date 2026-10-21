package arivihan.technologies.doubtbuzzter2.viewModel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureEntity;
import arivihan.technologies.doubtbuzzter2.repository.MicroLectureRepository;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MicroLectureViewModel extends AndroidViewModel {
    private final MicroLectureRepository repository;

    public MicroLectureViewModel(@NonNull Application application) {
        super(application);
        repository = new MicroLectureRepository(application);
    }

    public void insert(MicroLectureEntity microLecture) {
        repository.insert(microLecture);
    }

    public LiveData<MicroLectureEntity> getSelectionById(String chapterId) {
        return repository.getSelectionById(chapterId);
    }

    public void clearTable(String userId) {
        repository.clearTable(userId);
    }

    public void updateSelection(String selectedMicroLectureId, boolean isLectureSelected,
                                boolean isPracticeSelected, String chapterId, String lectureContentType) {
        Log.d("roomdb", "updateSelection: "+isPracticeSelected+"_"+isLectureSelected);
        repository.updateSelection(selectedMicroLectureId, isLectureSelected, isPracticeSelected, chapterId,lectureContentType);
    }
}
