package arivihan.technologies.doubtbuzzter2.viewModel;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_CAROUSEL;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_SUBJECTS_V2;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_COMPETITIVE_HOME_SCREEN_CAROUSEL;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_PERSONALIZED_PATH;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_TEST_SERIES_CARDS;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_PERSONALIZED_PATH_V2;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.HOME_SCREEN_BADGE_PROGRESS;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.enums.batch.carousel.BatchCarouselScreenType;
import arivihan.technologies.doubtbuzzter2.model.CarouselDto;
import arivihan.technologies.doubtbuzzter2.model.SubjectModel;
import arivihan.technologies.doubtbuzzter2.model.carousel.CarouselResponse;
import arivihan.technologies.doubtbuzzter2.model.generic.ApiWrappedResponse;
import arivihan.technologies.doubtbuzzter2.model.GetAllSubjectProgressAnalyticsModel;
import arivihan.technologies.doubtbuzzter2.model.HighlistModel;
import arivihan.technologies.doubtbuzzter2.model.HomeScreenCacheData;
import arivihan.technologies.doubtbuzzter2.model.StudentProgressDto;
import arivihan.technologies.doubtbuzzter2.model.subject.SubjectListWrapperResponse;
import arivihan.technologies.doubtbuzzter2.model.subject.SubjectResponse;
import arivihan.technologies.doubtbuzzter2.model.batch.test_series.TestSeriesCardResponse;
import arivihan.technologies.doubtbuzzter2.model.ToppersHomeScreenModel;
import arivihan.technologies.doubtbuzzter2.model.competitive.HomeScreenCardResponse;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import lombok.Getter;
import lombok.Setter;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;


@Getter
@Setter
public class HomeScreenViewModel extends AndroidViewModel {
    private static String TAG = HomeScreenViewModel.class.getSimpleName();
    Gson gson = new Gson();
    private MutableLiveData<List<CarouselResponse>> carouselData = new MutableLiveData<>();
    private MutableLiveData<List<SubjectResponse>> subjectList = new MutableLiveData<>();
    private MutableLiveData<Boolean> optionalSubjectAvailable = new MutableLiveData<>(false);
    private MutableLiveData<TestSeriesCardResponse> testSeriesList = new MutableLiveData<>();
    private MutableLiveData<List<SubjectResponse>> competitivePersonalizedPath = new MutableLiveData<>();

    /**
     * true = shimmer visible (first-ever fetch in progress), false = shimmer gone
     */
    private MutableLiveData<Boolean> testSeriesLoading = new MutableLiveData<>();
    /**
     * Set to true after the first API attempt completes (success, empty, or error).
     */
    private boolean testSeriesApiEverCompleted = false;
    private MutableLiveData<StudentProgressDto> progress = new MutableLiveData<>();
    private List<HighlistModel> highlightClassList;
    private List<ToppersHomeScreenModel> toppersDataLive;

    private GetAllSubjectProgressAnalyticsModel analyticsFragmentTestSeriesData;
    private List<SubjectResponse> analyticsFragmentSubscribedSubjectsData;
    private GetAllSubjectProgressAnalyticsModel analyticsFragmentSubjectCompletionData;

    private MutableLiveData<List<HomeScreenCardResponse>> featureSectionModelMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> batchCodeRefreshTrigger = new MutableLiveData<>();

    public void triggerBatchCodeRefresh() {
        batchCodeRefreshTrigger.postValue(true);
    }


    public HomeScreenViewModel(@NonNull Application application) {
        super(application);
    }


    public void getSubscribedSubjectData(boolean refresh) {
        Log.d("getSubscribedSubjectData", "getSubscribedSubjectData " + refresh);
        if (!refresh && subjectList.getValue() == null) {
            List<SubjectResponse> localData = MyApplication.getSubjectModelList(HomeScreenCacheData.AV_SUBJECTS_V2.name());
            if (localData != null) {
                subjectList.setValue(localData);
                Log.i("getSubscribedSubjectData", "Loaded subjects from local storage");
            }
            boolean cachedOptional = MyApplication.getBoolData(PreferenceKeys.OPTIONAL_SUBJECT_AVAILABLE.getKey());
            optionalSubjectAvailable.setValue(cachedOptional);
        }
//        getSubscribedSubjectForChat(subjectList, refresh);
//        if (MyApplication.isCompetitiveProfile()) {
//            getSubscribedSubjectNonBoard(HomeScreenCacheData.AV_SUBJECTS_V2.name(), subjectList, refresh);
//        } else {
        getSubscribedSubject(HomeScreenCacheData.AV_SUBJECTS_V2.name(), subjectList, refresh);
//        }
    }

    public void getPersonalizedPathData(boolean refresh) {
        Log.d("getPersonalizedPathData", "PersonalizedPathData:  " + refresh);
        if (!refresh && competitivePersonalizedPath.getValue() == null) {
            List<SubjectResponse> localData = MyApplication.getSubjectModelList(HomeScreenCacheData.AV_COMPETITIVE_PERSONALIZED_PATH.name());
            if (localData != null) {
                competitivePersonalizedPath.setValue(localData);
                Log.i("getPersonalizedPathData", "Loaded getPersonalizedPathData Subjects from local storage");
            }
        }
        getCompetitivePersonalizedPath(HomeScreenCacheData.AV_COMPETITIVE_PERSONALIZED_PATH.name(), competitivePersonalizedPath, refresh);
    }

    public void getCompetitivePersonalizedPath(String key, MutableLiveData<List<SubjectResponse>> mutableLiveData, boolean refresh) {
        if (!refresh && !CollectionUtils.isEmpty(mutableLiveData.getValue())) {
            mutableLiveData.postValue(mutableLiveData.getValue());
            return;
        }
        
        String batchId = MyApplication.getStringData(PreferenceKeys.BATCH_ID.getKey());
        Log.d(TAG, "Fetching Personalized Path for Batch ID: " + batchId);
        
        if (StringUtils.isEmpty(batchId)) {
            Log.e(TAG, "Batch ID is null or empty, cannot fetch personalized path");
            mutableLiveData.setValue(new ArrayList<>());
            return;
        }

        mutableLiveData.setValue(new ArrayList<>());
        try {
            final String requestingUserId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
            Map<String,Object> params = new HashMap<>();
            params.put("batchId", batchId);
            MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(
                    Request.Method.GET,
                    CommonMethods.getUrlParam(GET_PERSONALIZED_PATH_V2, params),
                    null,
                    response -> {
                        try {
                            Log.d(TAG, "GET_PERSONALIZED_PATH: Response: "+response);
                            JSONObject dataObject = response.getJSONObject("data");
                            Log.d("subjects", "getCompetitivePersonalizedPath: " + dataObject.toString());
                            JSONArray subjectArray = dataObject.getJSONArray("subjects");
                            TypeToken<List<SubjectModel>> token = new TypeToken<List<SubjectModel>>() {
                            };
                            List<SubjectModel> parsed = gson.fromJson(subjectArray.toString(), token.getType());
                            Log.d(TAG, "Competitive personalized path subjects: " + parsed.toString());

                            MyApplication.saveData("personalisedPathSubjectTitle",
                                    dataObject.optString("title", ""));

                            List<SubjectResponse> list = new ArrayList<>();
                            ArrayList<String> subjectsName = new ArrayList<>();
                            ArrayList<String> subjectsCode = new ArrayList<>();
                            for (SubjectModel model : parsed) {
                                SubjectResponse sr = new SubjectResponse();
                                sr.id = model.getId();
                                sr.subjectName = model.getSubjectName();
                                sr.iconUrl = model.getIconUrl();
                                sr.color = model.getColor();
                                sr.chapterCount = model.getChapterCount();
                                sr.setSubjectCode(model.getSubjectCode());
                                sr.setBackgroundColor(model.getBackgroundColor());
                                sr.setBackgroundImageUrl(model.getBackgroundImageUrl());
                                sr.setSubtitle(model.getSubtitle());
                                sr.setLearningPathId(model.getLearningPathId());
                                sr.setMiniVideoUrl(model.getMiniVideoUrl());
                                sr.setSubjectTitleColor(model.getSubjectTitleColor());
                                sr.setPersonalised(model.isPersonalised());
                                sr.setBatchPathId(model.getBatchPathId());
                                list.add(sr);
                                subjectsName.add(model.getSubjectName());
                                subjectsCode.add(model.getSubjectCode() != null ? model.getSubjectCode() : model.getSubjectName());
                            }

//                            MyApplication.saveSubjectList("subjectNameListChat", subjectsName);
//                            MyApplication.saveSubjectList("subjectCodeListChat", subjectsCode);
                            String currentUserId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
                            if (org.apache.commons.lang3.StringUtils.equals(requestingUserId, currentUserId)) {
                                MyApplication.saveSubjectModelList(key, list);
                            }

                            mutableLiveData.postValue(list);
                        } catch (Exception e) {
                            Log.e(TAG, "JSON parsing error in competitive personalized path", e);
                        }
                    },
                    error -> Log.e(TAG, "Competitive personalized path error: " + error.getMessage())
            );
            MyVolleySingleton.getInstance(getApplication()).addToRequestQueue(myJsonObjectRequest);
        } catch (Exception e) {
            Log.e(TAG, "Competitive personalized path request failed", e);
        }
    }

    public void getSubscribedSubject(String key, MutableLiveData<List<SubjectResponse>> mutableLiveData, boolean refresh) {

        if (!refresh && !CollectionUtils.isEmpty(mutableLiveData.getValue())) {
            mutableLiveData.postValue(mutableLiveData.getValue());
            return;
        }

        mutableLiveData.setValue(new ArrayList<>());

        final String requestingUserId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
        Map<String, Object> map = new HashMap<>();

        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(AV_SUBJECTS_V2, map), null,
                response -> {
                    Log.d(TAG, "---> Subject Response: " + response);
                    TypeToken<ApiWrappedResponse<SubjectListWrapperResponse>> token =
                            new TypeToken<ApiWrappedResponse<SubjectListWrapperResponse>>() {
                            };
                    ApiWrappedResponse<SubjectListWrapperResponse> apiResponse =
                            gson.fromJson(response.toString(), token.getType());

                    SubjectListWrapperResponse wrapper = (apiResponse != null && apiResponse.isSuccess())
                            ? apiResponse.getData() : null;

                    List<SubjectResponse> list = (wrapper != null && wrapper.getSubjects() != null)
                            ? wrapper.getSubjects() : new ArrayList<>();
                    boolean optional = wrapper != null && wrapper.isCanAddOptionalSubject();

                    Log.e("HOMEVIEWMODEL", list.toString());

                    ArrayList<String> subjectsName = new ArrayList<>();
                    ArrayList<String> subjectsCode = new ArrayList<>();

                    for (SubjectResponse model : list) {
                        subjectsName.add(model.getSubjectName());
                        subjectsCode.add(model.getSubjectName());
                    }

                    String currentUserId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
                    if (org.apache.commons.lang3.StringUtils.equals(requestingUserId, currentUserId)) {
                        MyApplication.saveSubjectList("subjectNameList", subjectsName);
                        MyApplication.saveSubjectList("subjectCodeList", subjectsCode);
                        MyApplication.saveSubjectModelList(key, list);
                        MyApplication.saveData(PreferenceKeys.OPTIONAL_SUBJECT_AVAILABLE.getKey(), optional);
                        optionalSubjectAvailable.postValue(optional);
                    }
                    mutableLiveData.postValue(list);
                }, new VolleyGlobalErrorListener(getApplication()) {
            @Override
            public void postError(VolleyError error) {
            }
        });
        MyVolleySingleton.getInstance(getApplication()).addToRequestQueue(myJsonObjectRequest2);
    }

    public void getTestSeriesData(boolean refresh) {
        Log.d("getTestSeriesData", "getTestSeriesData " + refresh);
        TestSeriesCardResponse cachedTestSeries = MyApplication.getTestSeriesDataFromLocal(HomeScreenCacheData.AV_TEST_SERIES.name());
        if (!refresh && cachedTestSeries != null) {
            Log.i("getTestSeriesData", "Loaded test series from local cache.");
            testSeriesList.setValue(cachedTestSeries);
        }
        Log.d("getTestSeriesData", "Fetching test series from API.");
        getTestSeries(testSeriesList, refresh);
    }

    public void getTestSeries(MutableLiveData<TestSeriesCardResponse> mutableLiveData, boolean refresh) {

        if (!refresh && mutableLiveData.getValue() != null) {
            // Cache already in memory — no shimmer, just re-emit
            testSeriesApiEverCompleted = true;
            testSeriesLoading.postValue(false);
            mutableLiveData.postValue(mutableLiveData.getValue());
            return;
        }

        // Only show shimmer on the very first API call (e.g. after login/onboarding)
        if (!testSeriesApiEverCompleted) {
            testSeriesLoading.postValue(true);
        }

        Map<String, Object> map = new HashMap<>();
        MyJsonObjectRequest request = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(AV_TEST_SERIES_CARDS, map), null,
                response -> {
                    Log.d(TAG, "Test Series Cards Response: " + response);
                    TypeToken<ApiWrappedResponse<TestSeriesCardResponse>> token = new TypeToken<ApiWrappedResponse<TestSeriesCardResponse>>() {
                    };
                    ApiWrappedResponse<TestSeriesCardResponse> apiResponse = gson.fromJson(response.toString(), token.getType());
                    TestSeriesCardResponse data = (apiResponse != null && apiResponse.isSuccess()) ? apiResponse.getData() : null;
                    testSeriesApiEverCompleted = true;
                    testSeriesLoading.postValue(false);
                    if (data != null) {
                        mutableLiveData.postValue(data);
                        MyApplication.saveTestSeriesData(HomeScreenCacheData.AV_TEST_SERIES.name(), data);
                        Log.d("HOMEVIEWMODEL", data.toString());
                    } else {
                        // Empty response — post null so the observer fires and hides the card
                        mutableLiveData.postValue(null);
                    }
                }, new VolleyGlobalErrorListener(getApplication()) {
            @Override
            public void postError(VolleyError error) {
                // Error — stop shimmer and hide card; don't leave shimmer spinning forever
                testSeriesApiEverCompleted = true;
                testSeriesLoading.postValue(false);
                mutableLiveData.postValue(null);
            }
        });
        MyVolleySingleton.getInstance(getApplication()).addToRequestQueue(request);
    }

    public void getCarouselData(boolean refresh) {

        Log.d("CarouselDebug", "getCarouselData called | refresh = " + refresh);

        Log.d("CarouselDebug2", "carouselData memory empty = "
                + CollectionUtils.isEmpty(carouselData.getValue()));

        if (CollectionUtils.isEmpty(carouselData.getValue()) || refresh) {
            if (!refresh) {
//                String cacheKey = MyApplication.isCompetitiveProfile()
//                        ? HomeScreenCacheData.AV_COMPETITIVE_SCREEN_CAROUSEL_V2.name()
//                        : HomeScreenCacheData.AV_HOME_SCREEN_CAROUSEL_V2.name();
                String cacheKey = HomeScreenCacheData.AV_HOME_SCREEN_CAROUSEL_V2.name();
                List<CarouselResponse> data = MyApplication.getCarouselDataFromLocal(cacheKey);
                if (data != null) {
                    Log.d("CarouselDebug", "Loaded carousel from local cache");
                    carouselData.setValue(data);
                } else {
                    Log.d("CarouselDebug", "No local cache found");
                }
            }

            Log.d("CarouselDebug", "Calling Carousel API");
            callCarouselApi();
        } else {
            Log.d("CarouselDebug", "Using existing carousel data in memory");
        }
    }

    public void callCarouselApi() {
        Log.d("CarouselDebug", "callCarouselApi called");
//        if (MyApplication.isCompetitiveProfile()) {
//            callCompetitiveCarouselApi();
//        } else {
        fetchCarouselData(carouselData, BatchCarouselScreenType.HOME_SCREEN.name());
//        }
    }

    public void fetchCarouselData(MutableLiveData<List<CarouselResponse>> mutableLiveData,
                                  String screenType) {

        Log.d(TAG, "fetchCarouselData called | screenType = " + screenType);

        Map<String, Object> map = new HashMap<>();
        map.put("screenType", screenType);

        String url = CommonMethods.getUrlParam(AV_CAROUSEL, map);
        Log.d(TAG, "Request URL: " + map.toString());

        MyJsonObjectRequest jsonObjectRequest = new MyJsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {

                    Log.d(TAG, "Carousel API response received:" + response);

                    TypeToken<ApiWrappedResponse<List<CarouselResponse>>> token =
                            new TypeToken<ApiWrappedResponse<List<CarouselResponse>>>() {
                            };
                    ApiWrappedResponse<List<CarouselResponse>> apiResponse =
                            gson.fromJson(response.toString(), token.getType());

                    List<CarouselResponse> data = (apiResponse != null && apiResponse.isSuccess())
                            ? apiResponse.getData() : null;

                    Log.d("CarouselDebug", "Parsed carousel size: " + (data != null ? data.size() : 0));

                    if (data != null) {
                        mutableLiveData.setValue(data);
                        MyApplication.saveCarouselData(HomeScreenCacheData.AV_HOME_SCREEN_CAROUSEL_V2.name(), data);
                    }
                    Log.d(TAG, "fetchCarouselData: " + data);
                }, new VolleyGlobalErrorListener(this.getApplication().getApplicationContext()) {
            @Override
            public void postError(VolleyError error) {
                Log.e("CarouselDebug", "Carousel API error: " + error.toString());
            }
        });

        jsonObjectRequest.setTag(TAG);

        Log.d("CarouselDebug", "Adding request to queue");

        MyVolleySingleton
                .getInstance(this.getApplication().getApplicationContext())
                .addToRequestQueue(jsonObjectRequest);
    }

    public void refreshHomeScreenData(boolean refresh) {
        getSubscribedSubjectData(refresh);
        getCarouselData(refresh);
        getTestSeriesData(refresh);
        if(MyApplication.isCompetitiveProfile()) {
            getPersonalizedPathData(refresh);
        }
        MyApplication.saveData(PreferenceKeys.REFRESH.getKey(), false);
    }

    public void switchHomeScreenData() {
        getSubscribedSubjectData(false);
        getTestSeriesData(false);
        getCarouselData(true);
        getStudentProgressData(false);
    }

    public void removeDataModel() {
        for (HomeScreenCacheData cacheData : HomeScreenCacheData.values()) {
            MyApplication.removeString(cacheData.name());
            Log.i("clearAllCacheData", "Removed: " + cacheData.name());
        }
        MyVolleySingleton.getInstance(this.getApplication().getApplicationContext()).getRequestQueue().cancelAll(TAG);

        MyApplication.removeString(PreferenceKeys.OPTIONAL_SUBJECT_AVAILABLE.getKey());
        MyApplication.removeString(PreferenceKeys.SWITCHABLE_COURSE_INFO.getKey());
        MyApplication.removeString(PreferenceKeys.NEET_TOGGLE_STATE.getKey());
        MyApplication.removeString(PreferenceKeys.PERSONALISED_PATH_SUBJECT_TITLE.getKey());
        MyApplication.removeString(PreferenceKeys.SHOW_PRE_EXAM_LAYOUT_PREFIX.getKey() +
                MyApplication.getStringData(PreferenceKeys.SELECTED_COURSE_ID_LOCAL.getKey()) + "_" +
                MyApplication.getStringData(PreferenceKeys.MY_LANG_CAMEL.getKey()));
        carouselData = new MutableLiveData<>();
        subjectList = new MutableLiveData<>();
        competitivePersonalizedPath = new MutableLiveData<>();
//        chatSubjectList = new MutableLiveData<>();
        testSeriesList = new MutableLiveData<>();
        testSeriesLoading = new MutableLiveData<>();
        testSeriesApiEverCompleted = false;
        progress = new MutableLiveData<>();
        optionalSubjectAvailable = new MutableLiveData<>(false);

    }

    public void clearCarouselData() {
        if (carouselData != null && carouselData.getValue() != null)
            carouselData.getValue().clear();
        else
            carouselData = new MutableLiveData<>();
    }

    public void getStudentProgressData(boolean refresh) {
        getStudentProgress(progress, refresh);
    }

    private void getStudentProgress(MutableLiveData<StudentProgressDto> mutableLiveData, boolean refresh) {
        if (!refresh && mutableLiveData.getValue() != null) {
            mutableLiveData.postValue(mutableLiveData.getValue());
            return;
        }

        Map<String, Object> params = new HashMap<>();
        MyJsonObjectRequest jsonArrayRequest = new MyJsonObjectRequest(
                Request.Method.GET,
                CommonMethods.getUrlParam(HOME_SCREEN_BADGE_PROGRESS, params),
                null,
                response -> handleStudentProgressResponse(response, mutableLiveData),
                new VolleyGlobalErrorListener(getApplication()) {
                    @Override
                    public void postError(VolleyError error) {
                        Log.e("CAROUSELERROR", error.toString());
                    }
                }
        );

        MyVolleySingleton.getInstance(getApplication()).addToRequestQueue(jsonArrayRequest);
    }

    private void handleStudentProgressResponse(JSONObject response, MutableLiveData<StudentProgressDto> targetProgress) {
        TypeToken<StudentProgressDto> token = new TypeToken<>() {
        };
        StudentProgressDto studentProgressDto = gson.fromJson(response.toString(), token.getType());

        targetProgress.postValue(studentProgressDto);
    }


}
