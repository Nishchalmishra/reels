package arivihan.technologies.doubtbuzzter2.helper.offline;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.TEST_SERIES_ASYNC_v2;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.TEST_SERIES_v2;

import android.content.Context;
import android.content.Intent;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.MockTestAnalysisActivity;
import arivihan.technologies.doubtbuzzter2.activity.asat.ASATActivity;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.model.UserMockAnswerDto;
import arivihan.technologies.doubtbuzzter2.model.offline.OfflineTestSeriesModel;
import arivihan.technologies.doubtbuzzter2.model.offline.OfflineTestTypes;
import arivihan.technologies.doubtbuzzter2.model.remote_config.TestSeriesConfig;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class OfflineTestManager {
    private static final String TAG = OfflineTestManager.class.getSimpleName();
    private static final String OFFLINE_TEST_KEY = "offline_test_series_map";
    private static int errorRetryCount = 3;
    private static final Gson gson = new Gson();

    private static OfflineTestManager offlineTestManager;
    public OfflineTestManager(){
        try {
            String firebaseConfigJsonStr = MyApplication.getFirebaseRemoteConfig()
                    .getString("testseries_config_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));

            Gson gson = new Gson();
            TestSeriesConfig testSeriesConfig = gson.fromJson(firebaseConfigJsonStr, TestSeriesConfig.class);

            errorRetryCount = testSeriesConfig.getErrorRetryCount();
        } catch (Exception ignored) {}
    }

    public static OfflineTestManager getInstance() {
        if(offlineTestManager != null)
            return offlineTestManager;

        return new OfflineTestManager();
    }
    // Create or Update
    public static void saveOrUpdateModel(OfflineTestSeriesModel model) {
        Map<String, OfflineTestSeriesModel> map = getModelMap();
        map.put(model.getId(), model); // Upsert by ID
        saveModelMap(map);
    }

    // Read all
    public static List<OfflineTestSeriesModel> getAllModels() {
        return new ArrayList<>(getModelMap().values());
    }

    // Read one
    public static OfflineTestSeriesModel getModelById(String id) {
        return getModelMap().get(id);
    }

    // Delete one
    public static void deleteModelById(String id) {
        Map<String, OfflineTestSeriesModel> map = getModelMap();
        if (map.containsKey(id)) {
            map.remove(id);
            saveModelMap(map);
        }
    }

    // Delete all
    public static void clearAll() {
        MyApplication.saveData(OFFLINE_TEST_KEY, "");
    }

    // Internal methods
    public static List<OfflineTestSeriesModel> getModelsByTestType(OfflineTestTypes offlineTestType) {
        List<OfflineTestSeriesModel> list = new ArrayList<>();
        for(OfflineTestSeriesModel model : getAllModels()) {
            if(model.getTestType().equals(offlineTestType)) {
                list.add(model);
            }
        }

        return list;
    }

    private static Map<String, OfflineTestSeriesModel> getModelMap() {
        String json = MyApplication.getStringData(OFFLINE_TEST_KEY);
        if (json == null || json.isEmpty()) return new HashMap<>();
        Type type = new TypeToken<Map<String, OfflineTestSeriesModel>>() {
        }.getType();
        try {
            return gson.fromJson(json, type);
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    private static void saveModelMap(Map<String, OfflineTestSeriesModel> map) {
        String json = gson.toJson(map);
        MyApplication.saveData(OFFLINE_TEST_KEY, json);
    }

    public void syncAllOfflineTests(Context context) {
        for (OfflineTestSeriesModel offlineTestSeriesModel : getAllModels()) {
            syncOfflineTest(context, offlineTestSeriesModel, null);
        }
    }

    public void syncOfflineTest(Context context, OfflineTestSeriesModel offlineTestSeriesModel, OfflineTestSubmissionListener listener) {
        if(offlineTestSeriesModel.isBoard()) {
            submitTestSeriesBoard(context, offlineTestSeriesModel, listener);
        }
        else {
            submitTestSeriesNonBoard(context, offlineTestSeriesModel, listener);
        }
    }

    //APIs
    public void submitTestSeriesBoard(Context context, OfflineTestSeriesModel offlineTestSeriesModel, OfflineTestSubmissionListener listener) {
        try {
            if (CommonMethods.isOnline(context)) {
                if(listener!=null) {
                    listener.onStart(true, null);
                }
                MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.POST,
                        TEST_SERIES_ASYNC_v2, new JSONObject(gson.toJson(offlineTestSeriesModel.getUserMockAnswerDto())),
                        response -> {

                            Map<String, String> params = new HashMap<>();
                            params.put("testSeriesId", offlineTestSeriesModel.getTestSeriesId());
                            params.put("testSeriesName", offlineTestSeriesModel.getTestSeresName());
                            params.put(PreferenceKeys.TIME_TAKEN.getKey(), String.valueOf(offlineTestSeriesModel.getUserMockAnswerDto().getT()));
                            params.put("submission", "offline-sync");
                            MyApplication.logFirebaseBundleCreation(FirebaseEvents.Test_series_attempt.getActionName(), params);
                            deleteModelById(offlineTestSeriesModel.getId());
                            if(listener!=null) {
                                listener.onSuccess(true, response);
                            }
                        }, new VolleyGlobalErrorListener(context) {
                    @Override
                    public void postError(VolleyError error) {
                        if (offlineTestSeriesModel.getErrorRetryCount() < errorRetryCount) {
                            offlineTestSeriesModel.setErrorRetryCount(offlineTestSeriesModel.getErrorRetryCount() + 1);
                            offlineTestSeriesModel.setLastErrorMsg(error != null ? error.getMessage() : "");
                            saveOrUpdateModel(offlineTestSeriesModel);
                        } else {
                            deleteModelById(offlineTestSeriesModel.getId());
                        }
                        if(listener!=null) {
                            listener.onError(true, error);
                        }
                    }
                });
                MyVolleySingleton.getInstance(context).addToRequestQueue(myJsonObjectRequest2);
            }
        } catch (JSONException ignored) {
        }
    }

    public void submitTestSeriesNonBoard(Context context, OfflineTestSeriesModel offlineTestSeriesModel, OfflineTestSubmissionListener listener) {
        try {
            if (CommonMethods.isOnline(context)) {
                if(listener!=null) {
                    listener.onStart(true, null);
                }
                MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.POST,
                        TEST_SERIES_v2, new JSONObject(gson.toJson(offlineTestSeriesModel.getUserMockAnswerDto())),
                        response -> {
                            UserMockAnswerDto userMockAnswerDto = offlineTestSeriesModel.getUserMockAnswerDto();

                            Map<String, String> params = new HashMap<>();
                            params.put("testSeriesId", offlineTestSeriesModel.getTestSeriesId());
                            params.put("testSeriesName", offlineTestSeriesModel.getTestSeresName());
                            params.put(PreferenceKeys.TIME_TAKEN.getKey(), String.valueOf(offlineTestSeriesModel.getUserMockAnswerDto().getT()));
                            MyApplication.logFirebaseBundleCreation(FirebaseEvents.Test_series_submit.getActionName(), params);
                            if (userMockAnswerDto.getAsatId() != null || offlineTestSeriesModel.getTestType().equals(OfflineTestTypes.ASAT)) {
                                MyApplication.logFirebaseBundleCreation(FirebaseEvents.asat_test_submit.getActionName(), params);
                            }
                            deleteModelById(offlineTestSeriesModel.getId());
                            if(listener!=null) {
                                listener.onSuccess(true, response);
                            }
                        }, new VolleyGlobalErrorListener(context) {
                    @Override
                    public void postError(VolleyError error) {
                        if (offlineTestSeriesModel.getErrorRetryCount() < errorRetryCount) {
                            offlineTestSeriesModel.setErrorRetryCount(offlineTestSeriesModel.getErrorRetryCount() + 1);
                            offlineTestSeriesModel.setLastErrorMsg(error != null ? error.getMessage() : "");
                            saveOrUpdateModel(offlineTestSeriesModel);
                        } else {
                            deleteModelById(offlineTestSeriesModel.getId());
                        }
                        if(listener!=null) {
                            listener.onError(true, error);
                        }
                    }
                });
                MyVolleySingleton.getInstance(context).addToRequestQueue(myJsonObjectRequest2);
            }
        } catch (JSONException ignored) {
        }

    }

    public interface OfflineTestSubmissionListener {
        void onStart(boolean success, Object object);
        void onSuccess(boolean success, Object object);
        void onError(boolean success, Object object);
    }
}
