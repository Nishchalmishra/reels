package arivihan.technologies.doubtbuzzter2.activity.microlecture;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_FEEDBACK_COMMENTS;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.model.microlecture.CommentModel;
import arivihan.technologies.doubtbuzzter2.model.microlecture.CommentResponse;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class CommentsViewModel extends ViewModel {
    private static final String TAG = CommentsViewModel.class.getSimpleName();
    private final MutableLiveData<List<CommentModel>> _comments = new MutableLiveData<>();//new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<CommentModel>> comments = _comments;
    private final MutableLiveData<Boolean> _loading = new MutableLiveData<>(false);
    public LiveData<Boolean> loading = _loading;
    private final MutableLiveData<Boolean> _isLoadedOnce = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoadedOnce = _isLoadedOnce;
    private final MutableLiveData<Boolean> _hasUserComment = new MutableLiveData<>(true);
    public LiveData<Boolean> hasUserComment = _hasUserComment;
    private final MutableLiveData<String> _toastMsg = new MutableLiveData<>("");
    public LiveData<String> toastMsg = _toastMsg;


    protected Gson gson = new Gson();
    private int page = 0;
    private int pageSize = 10;
    private String microLectureId;
    private CommentResponse commentResponse;
    private boolean hasMoreData = true;
    public CommentsViewModel() {
    }

    public void setViewModelData(String microLectureId, int pageSize) {
        this.microLectureId = microLectureId;
        this.pageSize = pageSize;
    }
    public void loadMoreComments() {
        if (Boolean.TRUE.equals(_loading.getValue()) || !hasMoreData) return;

        _loading.setValue(true);

//        if (Boolean.TRUE.equals(_isLoadedOnce.getValue())) {
//            _loading.setValue(false);
//            return;
//        }

        try {
            Map<String, Object> requestQueryMap = new HashMap<>();
            requestQueryMap.put(PreferenceKeys.MICROLECTURE_ID.getKey(), microLectureId);
            requestQueryMap.put("pageSize", pageSize);
            requestQueryMap.put("pageNumber", page);
            if (commentResponse != null && commentResponse.getEvaluatedKey() != null) {
                requestQueryMap.put("lastEvaluatedKey", URLEncoder.encode(commentResponse.getEvaluatedKey(), "UTF-8"));
            }
            Log.d(TAG, "Request: " + requestQueryMap.toString());
            MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                    CommonMethods.getUrlParam(GET_FEEDBACK_COMMENTS, requestQueryMap), null,
                    response -> {
                        Log.d(TAG, "Response: " + response.toString());
                        TypeToken<CommentResponse> token = new TypeToken<CommentResponse>() {
                        };
                        commentResponse = gson.fromJson(response.toString(), token);
                        _isLoadedOnce.setValue(true);
                        _comments.postValue(commentResponse.getResults());
                        page++;
                        _loading.setValue(false);
                        if (StringUtils.isEmpty(commentResponse.getEvaluatedKey()) || commentResponse.getResults() == null || commentResponse.getResults().isEmpty()) {
                            hasMoreData = false;
                        }

                        Log.d(TAG, "--> hasMoreData: " + hasMoreData);
                    }, error -> {
                Log.d(TAG, "Error: " + error.toString());
                _loading.setValue(false);
                hasMoreData = false;
                _toastMsg.setValue(MyApplication.getInstance().getString(R.string.something_went_wrong));
            });
            myJsonObjectRequest.setTag(TAG);
            MyVolleySingleton.getInstance(MyApplication.getInstance()).addToRequestQueue(myJsonObjectRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setPositiveIfTrueFound(boolean isTrue, boolean isForceUpdate) {
        if(isForceUpdate) {
            _hasUserComment.setValue(isTrue);
            return;
        }
        if (Boolean.TRUE.equals(_hasUserComment.getValue())) return;
        if (isTrue) _hasUserComment.setValue(true);
    }

    public List<CommentModel> getComments() {
        return comments.getValue();
    }

    public boolean hasMoreData() {
        return hasMoreData;
    }
    public void cancelVolleyRequest() {
        MyVolleySingleton.getInstance(MyApplication.getInstance()).getRequestQueue().cancelAll(TAG);
    }

    public void reset() {
        Log.d(TAG, "Resetting CommentsViewModel state");

        _comments.setValue(new ArrayList<>());
        _loading.setValue(false);
        _isLoadedOnce.setValue(false);
        _toastMsg.setValue("");

        page = 0;
        pageSize = 10;
        microLectureId = null;
        commentResponse = null;
        hasMoreData = true;
    }
}
