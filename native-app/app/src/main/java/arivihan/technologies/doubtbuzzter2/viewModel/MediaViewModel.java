package arivihan.technologies.doubtbuzzter2.viewModel;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.DELETE_YOUR_DOWNLOADED_LECTURE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_SYNC_DOWNLOADED_LECTURE_DATA;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.model.DownloadMedia;
import arivihan.technologies.doubtbuzzter2.model.MediaEntity;
import arivihan.technologies.doubtbuzzter2.repository.MediaRepository;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonArrayRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MediaViewModel extends AndroidViewModel {

    private final MediaRepository repository;

    public MediaViewModel(@NonNull Application application) {
        super(application);
        repository = new MediaRepository(application);
    }

    public LiveData<List<MediaEntity>> getAllMedia(String userId) {
        return repository.getAllMedia(userId);
    }

    public LiveData<List<DownloadMedia>> getAllDownloadedMedia(String userId, String downloadStatus) {
        return repository.getAllDownloadedMedia(userId, downloadStatus);
    }

    public void updateByLockedStatusByLectureId(String lectureId, boolean locked) {
        repository.updateByLockedStatusByLectureId(lectureId, locked);
    }

    public void insert(MediaEntity mediaEntity) {
        repository.insert(mediaEntity);
    }

    public void delete(String microLectureId) {

        repository.delete(microLectureId);
    }

    public void deleteThenInsert(MediaEntity mediaEntity) {
        repository.deleteThenInsert(mediaEntity);
    }

    public void deleteByUrl(String url) {
        repository.deleteByUrl(url);
    }

    public LiveData<MediaEntity> getMediaByLectureId(String microLectureId) {
        return repository.getMediaByLectureId(microLectureId);
    }

    public MediaEntity getMediaById(String id) {
        return repository.getMediaById(id);
    }

    public void updateById(String id, String status) {
        repository.updateById(id, status);
    }


    public void clearUserData(String userId) {
        repository.clearUserData(userId);
    }

    public void updateProgress(int progress, String id) {
        repository.updateProgress(progress, id);
    }

    public void deleteMicroLectureEntry(String microLectureId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("microLectureId", microLectureId);
            MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.DELETE,
                    CommonMethods.getUrlParam(DELETE_YOUR_DOWNLOADED_LECTURE, params), null,
                    response -> {
                    }, new VolleyGlobalErrorListener(getApplication()) {
                @Override
                public void postError(VolleyError error) {
                }
            });
            MyVolleySingleton.getInstance(getApplication()).addToRequestQueue(myJsonObjectRequest2);
        } catch (Exception e) {

        }
    }


}
