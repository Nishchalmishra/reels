package arivihan.technologies.doubtbuzzter2.fragments.competitive.learn;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.ACTION_EVENT;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.COMPETITIVE_LEARN_ADAPTIVE_PRACTISE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.COMPETITIVE_LEARN_CONTINUE_LEARNING;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.COMPETITIVE_LEARN_DPP;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.COMPETITIVE_LEARN_ONE_SHOT_LECTURE;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.client.Firebase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.TestSeriesAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.competitive.continue_learning.CompetitiveContinueLearningAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.competitive.subject.CompetitiveSubjectChapterAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.CommonNavigator;
import arivihan.technologies.doubtbuzzter2.enums.AnalyticsEvent;
import arivihan.technologies.doubtbuzzter2.enums.test.TestSeriesType;
import arivihan.technologies.doubtbuzzter2.helper.ScreenAnalyticsHelper;
import arivihan.technologies.doubtbuzzter2.utils.EdgeToEdgeHelper;
import arivihan.technologies.doubtbuzzter2.model.ApiResponseDto;
import arivihan.technologies.doubtbuzzter2.model.generic.ApiWrappedResponse;
import arivihan.technologies.doubtbuzzter2.model.subject.SubjectResponse;
import arivihan.technologies.doubtbuzzter2.model.batch.test_series.TestSeriesCard;
import arivihan.technologies.doubtbuzzter2.model.batch.test_series.TestSeriesCardResponse;
import arivihan.technologies.doubtbuzzter2.model.competitive.adaptive.CompetitiveAdaptivePractiseDataDto;
import arivihan.technologies.doubtbuzzter2.model.competitive.continue_learning.ContinueLearningDataDto;
import arivihan.technologies.doubtbuzzter2.model.competitive.continue_learning.ContinueLearningDto;
import arivihan.technologies.doubtbuzzter2.model.competitive.dpp.DppLearnScreenDataDto;
import arivihan.technologies.doubtbuzzter2.model.competitive.lecture.OneShotLectureDataDto;
import arivihan.technologies.doubtbuzzter2.enums.competitive.CompetitiveChapterType;
import arivihan.technologies.doubtbuzzter2.viewModel.HomeScreenViewModel;
import arivihan.technologies.doubtbuzzter2.model.competitive.ChapterListTabConfig;
import arivihan.technologies.doubtbuzzter2.model.competitive.topic.CompetitiveTopicDto;
import arivihan.technologies.doubtbuzzter2.model.competitive.topic.CompetitiveTopicListDto;
import arivihan.technologies.doubtbuzzter2.model.remote_config.TodoMessageConfig;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;


public class CompetitiveLearnFragment extends Fragment implements
        CompetitiveSubjectChapterAdapter.OnSubjectClickListener {

    public static final String TAG = CompetitiveLearnFragment.class.getSimpleName();

    // UI Variable
    private FrameLayout fl_parent_layout;
    private LinearLayout ll_continue_learning, ll_one_shot_lecture, ll_adaptive_practise, ll_test_series;
    private RelativeLayout rl_continue_learning_data;
    private LinearLayout ll_one_shot_lecture_data, ll_adaptive_practise_data, ll_test_series_data;
    private TextView tv_cl_title, tv_sl_title, tv_live_title, tv_dpp_title, tv_ap_title, tv_ts_title;
    private RecyclerView rv_continue_learning, rv_one_shot_lecture, rv_live_lecture, rv_dpp, rv_test_series;
    private ShimmerFrameLayout shimmer_continue_learning, shimmer_one_shot_lecture, shimmer_adaptive_practise, shimmer_test_series;
    private CardView cv_adaptive_practise;
    private ImageView iv_adaptive_practise;

    // Data Variable
    private CompetitiveAdaptivePractiseDataDto competitiveAdaptivePractiseDataDto;
    private List<ChapterListTabConfig> tabConfigs = new ArrayList<>();
    private HomeScreenViewModel homeScreenViewModel;

    public CompetitiveLearnFragment() {
        // Required empty public constructor
    }

    public static CompetitiveLearnFragment newInstance() {
        CompetitiveLearnFragment fragment = new CompetitiveLearnFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        View view = inflater.inflate(R.layout.fragment_competitive_learn, container, false);
        ScreenAnalyticsHelper.sendScreenVisitedEvent(AnalyticsEvent.LEARN_SCREEN.name(), AnalyticsEvent.LEARN_SCREEN.name());
        initViews(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EdgeToEdgeHelper.padForSystemBars(view.findViewById(R.id.linear_layout_scrollView));
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroyCalled: ");
        super.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStopCalled");

        if (isAdded()) {
            MyVolleySingleton.getInstance(
                    requireActivity().getApplication()
            ).getRequestQueue().cancelAll(TAG);
        }
    }


    private void initViews(View view) {
        fl_parent_layout = view.findViewById(R.id.fl_parent_layout);
        ll_continue_learning = view.findViewById(R.id.ll_continue_learning);
        ll_one_shot_lecture = view.findViewById(R.id.ll_one_shot_lecture);
        ll_adaptive_practise = view.findViewById(R.id.ll_adaptive_practise);
        ll_test_series = view.findViewById(R.id.ll_test_series);
        rl_continue_learning_data = view.findViewById(R.id.rl_continue_learning_data);
        ll_one_shot_lecture_data = view.findViewById(R.id.ll_one_shot_lecture_data);
        ll_adaptive_practise_data = view.findViewById(R.id.ll_adaptive_practise_data);
        ll_test_series_data = view.findViewById(R.id.ll_test_series_data);
        tv_cl_title = view.findViewById(R.id.tv_cl_title);
        tv_sl_title = view.findViewById(R.id.tv_sl_title);
        tv_live_title = view.findViewById(R.id.tv_live_title);
        tv_dpp_title = view.findViewById(R.id.tv_dpp_title);
        tv_ap_title = view.findViewById(R.id.tv_ap_title);
        tv_ts_title = view.findViewById(R.id.tv_ts_title);
        rv_continue_learning = view.findViewById(R.id.rv_continue_learning);
        rv_one_shot_lecture = view.findViewById(R.id.rv_one_shot_lecture);
        rv_live_lecture = view.findViewById(R.id.rv_live_lecture);
        rv_dpp = view.findViewById(R.id.rv_dpp);
        rv_test_series = view.findViewById(R.id.rv_test_series);
        shimmer_continue_learning = view.findViewById(R.id.shimmer_continue_learning);
        shimmer_one_shot_lecture = view.findViewById(R.id.shimmer_one_shot_lecture);
        shimmer_adaptive_practise = view.findViewById(R.id.shimmer_adaptive_practise);
        shimmer_test_series = view.findViewById(R.id.shimmer_test_series);
        cv_adaptive_practise = view.findViewById(R.id.cv_adaptive_practise);
        iv_adaptive_practise = view.findViewById(R.id.iv_adaptive_practise);

        ll_adaptive_practise_data.setVisibility(View.GONE);
        ll_test_series_data.setVisibility(View.GONE);
        ll_one_shot_lecture_data.setVisibility(View.GONE);
        rl_continue_learning_data.setVisibility(View.GONE);

        shimmer_adaptive_practise.setVisibility(View.GONE);
        shimmer_test_series.setVisibility(View.GONE);
        shimmer_one_shot_lecture.setVisibility(View.GONE);
        shimmer_continue_learning.setVisibility(View.GONE);

        cv_adaptive_practise.setOnClickListener(v -> {
            if (competitiveAdaptivePractiseDataDto != null
                    && StringUtils.isNotEmpty(competitiveAdaptivePractiseDataDto.getClassName())) {
                MyApplication.openActivity(requireContext(),
                        competitiveAdaptivePractiseDataDto.getClassName(),
                        competitiveAdaptivePractiseDataDto.getActivityParams());
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.clicked_adaptive_practice_section.getActionName());
            }
        });

        try {
            homeScreenViewModel = ((MyApplication) requireActivity().getApplication()).getHomeScreenViewModel();
        } catch (Exception ex) {
            Log.e(TAG, "initViews: failed to get HomeScreenViewModel", ex);
        }

        setAdaptiveCardHeight();
        getContinueLearningData();
//        getAdaptivePractiseData();
        getOneShotLectureData();
        getDppData();
        setupTestSeriesSection();

    }

    private void setAdaptiveCardHeight() {
        try {
            cv_adaptive_practise.post(() -> {
                int width = cv_adaptive_practise.getWidth();
                int height = (int) (width * 13f / 29f);

                ViewGroup.LayoutParams params = cv_adaptive_practise.getLayoutParams();
                params.height = height;
                cv_adaptive_practise.setLayoutParams(params);
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void getContinueLearningData() {
        Log.d(TAG, "getContinueLearningData: ");
        ll_continue_learning.setVisibility(View.VISIBLE);
        rl_continue_learning_data.setVisibility(View.GONE);
        shimmer_continue_learning.startShimmer();
        shimmer_continue_learning.setVisibility(View.VISIBLE);
        Map<String, Object> map = new HashMap<>();
        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(COMPETITIVE_LEARN_CONTINUE_LEARNING, map), null,
                response -> {
                    try {
                        shimmer_continue_learning.stopShimmer();
                        shimmer_continue_learning.setVisibility(View.GONE);
                        Log.d(TAG, "Continue Learning Response: " + response.toString());
                        Gson gson = new Gson();
                        TypeToken<ApiResponseDto<ContinueLearningDataDto>> token = new TypeToken<ApiResponseDto<ContinueLearningDataDto>>() {
                        };
                        ApiResponseDto<ContinueLearningDataDto> content = gson.fromJson(response.toString(), token.getType());
                        if (content.isSuccess() && content.getData() != null) {
                            ContinueLearningDataDto continueLearningDataDto = content.getData();
                            if (continueLearningDataDto != null && continueLearningDataDto.getItems() != null && !continueLearningDataDto.getItems().isEmpty()) {
                                if (StringUtils.isNotEmpty(continueLearningDataDto.getTitle())) {
                                    tv_cl_title.setText(continueLearningDataDto.getTitle());
                                    tv_cl_title.setVisibility(View.VISIBLE);
                                } else {
                                    tv_cl_title.setVisibility(View.GONE);
                                }
                                List<ContinueLearningDto> list = continueLearningDataDto.getItems();
                                CompetitiveContinueLearningAdapter adapter = new CompetitiveContinueLearningAdapter(requireContext(), list, null);
                                rv_continue_learning.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                                rv_continue_learning.setAdapter(adapter);
                                rv_continue_learning.setVisibility(View.VISIBLE);
                                rl_continue_learning_data.setVisibility(View.VISIBLE);
                            } else {
                                rv_continue_learning.setVisibility(View.GONE);
                                ll_continue_learning.setVisibility(View.GONE);
                            }
                        } else {

                        }
                    } catch (Exception ex) {
                        Log.e(TAG, "getContinueLearningData: ",ex );
            if (isAdded()) {
                ll_continue_learning.setVisibility(View.GONE);
                CommonMethods.showNegativeSnackbar(fl_parent_layout, getString(R.string.something_went_wrong));
            }
        }
    }, new VolleyGlobalErrorListener(requireContext()) {
        @Override
        public void postError(com.android.volley.VolleyError error) {
            if (!isAdded()) return;
            Log.d(TAG, "postError: ");
            shimmer_continue_learning.stopShimmer();
            shimmer_continue_learning.setVisibility(View.GONE);
            ll_continue_learning.setVisibility(View.GONE);
            CommonMethods.showNegativeSnackbar(fl_parent_layout, error.getMessage());
        }
    });

        myJsonObjectRequest.setTag(TAG);
        MyVolleySingleton.getInstance(requireContext()).addToRequestQueue(myJsonObjectRequest);
    }

    private void getOneShotLectureData() {
        Log.d(TAG, "getOneShotLectureData: ");
        ll_one_shot_lecture.setVisibility(View.VISIBLE);
        ll_one_shot_lecture_data.setVisibility(View.GONE);
        shimmer_one_shot_lecture.startShimmer();
        shimmer_one_shot_lecture.setVisibility(View.VISIBLE);

        ll_adaptive_practise.setVisibility(View.VISIBLE);
        ll_adaptive_practise_data.setVisibility(View.GONE);
        shimmer_adaptive_practise.startShimmer();
        shimmer_adaptive_practise.setVisibility(View.VISIBLE);

        Map<String, Object> map = new HashMap<>();
        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(COMPETITIVE_LEARN_ONE_SHOT_LECTURE, map), null,
                response -> {
                    try {
                        shimmer_one_shot_lecture.stopShimmer();
                        shimmer_one_shot_lecture.setVisibility(View.GONE);

                        shimmer_adaptive_practise.stopShimmer();
                        shimmer_adaptive_practise.setVisibility(View.GONE);
                        setAdaptivePathData();
                        Log.d(TAG, "Response: " + response.toString());
                        Gson gson = new Gson();
                        TypeToken<ApiWrappedResponse<OneShotLectureDataDto>> token = new TypeToken<ApiWrappedResponse<OneShotLectureDataDto>>() {
                        };
                        ApiWrappedResponse<OneShotLectureDataDto> content = gson.fromJson(response.toString(), token.getType());

                        if (content.isSuccess() && content.getData() != null) {
                            OneShotLectureDataDto oneShotLectureDataDto = content.getData();
                            List<SubjectResponse> subjects = oneShotLectureDataDto.getSubjects();
                            loadTabConfigs();
                            bindOneShotSections(subjects, oneShotLectureDataDto.getTitle());
                        }
                    } catch (Exception ex) {
                        Log.d(TAG, "getOneShotLectureData catch: ");
                        ll_one_shot_lecture.setVisibility(View.GONE);
                        ll_adaptive_practise.setVisibility(View.GONE);
                        CommonMethods.showNegativeSnackbar(fl_parent_layout, getString(R.string.something_went_wrong));
                        ex.printStackTrace();
                    }
                }, new VolleyGlobalErrorListener(requireContext()) {
            @Override
            public void postError(VolleyError error) {
                Log.d(TAG, "postError: ");
                shimmer_one_shot_lecture.stopShimmer();
                shimmer_one_shot_lecture.setVisibility(View.GONE);
                ll_one_shot_lecture.setVisibility(View.GONE);

                shimmer_adaptive_practise.stopShimmer();
                shimmer_adaptive_practise.setVisibility(View.GONE);
                ll_adaptive_practise.setVisibility(View.GONE);

                CommonMethods.showNegativeSnackbar(fl_parent_layout, error.getMessage());
            }
        });
        myJsonObjectRequest.setTag(TAG);
        MyVolleySingleton.getInstance(requireContext()).addToRequestQueue(myJsonObjectRequest);
    }

    private void loadTabConfigs() {
        try {
            String batchCode = MyApplication.getStringData(PreferenceKeys.BATCH_CODE.getKey());
            String json = MyApplication.getFirebaseRemoteConfig().getString("chapter_list_by_type_config");
            Type mapType = new TypeToken<Map<String, List<ChapterListTabConfig>>>() {}.getType();
            Map<String, List<ChapterListTabConfig>> configMap = new Gson().fromJson(json, mapType);
            if (configMap != null && configMap.containsKey(batchCode)) {
                tabConfigs = configMap.get(batchCode);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void bindOneShotSections(List<SubjectResponse> subjects, String apiTitle) {
        // Identify which tab types are configured for this batch
        ChapterListTabConfig liveTab = null;
        ChapterListTabConfig oneShotTab = null;
        for (ChapterListTabConfig cfg : tabConfigs) {
            if (CompetitiveChapterType.LIVE.getType().equalsIgnoreCase(cfg.getType()) && liveTab == null) {
                liveTab = cfg;
            } else if (!CompetitiveChapterType.LIVE.getType().equalsIgnoreCase(cfg.getType()) && oneShotTab == null) {
                oneShotTab = cfg;
            }
        }

        boolean hasSubjects = subjects != null && !subjects.isEmpty();

        // Live section
        if (liveTab != null && hasSubjects) {
//            tv_live_title.setText(liveTab.getName());
            tv_live_title.setVisibility(View.VISIBLE);
            rv_live_lecture.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            rv_live_lecture.setAdapter(new CompetitiveSubjectChapterAdapter(subjects, liveTab.getType(), true, this));
            rv_live_lecture.setVisibility(View.VISIBLE);

            ll_one_shot_lecture.setVisibility(View.VISIBLE);
            ll_one_shot_lecture_data.setVisibility(View.VISIBLE);
        } else {
            tv_live_title.setVisibility(View.GONE);
            rv_live_lecture.setVisibility(View.GONE);
        }

//        Log.d(TAG, "---> One shot section fallback: "+hasSubjects+" | Live : "+liveTab.toString()+" | Oneshot : "+oneShotTab.toString());

        // One-shot section
        if (oneShotTab != null && hasSubjects) {
            String sectionTitle = StringUtils.isNotEmpty(apiTitle) ? apiTitle : oneShotTab.getName();
            tv_sl_title.setText(sectionTitle);
            tv_sl_title.setVisibility(View.VISIBLE);
            rv_one_shot_lecture.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            rv_one_shot_lecture.setAdapter(new CompetitiveSubjectChapterAdapter(subjects, oneShotTab.getType(), false, this));
            rv_one_shot_lecture.setVisibility(View.VISIBLE);

            ll_one_shot_lecture_data.setVisibility(View.VISIBLE);
            ll_one_shot_lecture.setVisibility(View.VISIBLE);
        } else {
            tv_sl_title.setVisibility(View.GONE);
            rv_one_shot_lecture.setVisibility(View.GONE);
//            ll_one_shot_lecture.setVisibility(View.GONE);
        }

        if(tabConfigs.isEmpty()) {
            ll_one_shot_lecture.setVisibility(View.GONE);
        }
    }

    private void setAdaptivePathData() {
        try {
            String batchCode = MyApplication.getStringData(PreferenceKeys.BATCH_CODE.getKey());
            String json = MyApplication.getFirebaseRemoteConfig().getString("competitive_adaptive_path_data_v2");
            Type type = new TypeToken<Map<String, CompetitiveAdaptivePractiseDataDto>>() {}.getType();
            Map<String, CompetitiveAdaptivePractiseDataDto> adaptivePathMap = new Gson().fromJson(json, type);

            if (adaptivePathMap != null && adaptivePathMap.containsKey(batchCode)) {
                competitiveAdaptivePractiseDataDto = adaptivePathMap.get(batchCode);
            }

            if (competitiveAdaptivePractiseDataDto != null
                    && competitiveAdaptivePractiseDataDto.getTitle() != null
                    && competitiveAdaptivePractiseDataDto.getImageUrl() != null) {
                tv_ap_title.setText(competitiveAdaptivePractiseDataDto.getTitle());
                Glide.with(requireContext())
                        .load(competitiveAdaptivePractiseDataDto.getImageUrl())
                        .error(R.drawable.white_logo)
                        .into(iv_adaptive_practise);
                ll_adaptive_practise_data.setVisibility(View.VISIBLE);
                setAdaptiveCardHeight();
            } else {
                rv_one_shot_lecture.setVisibility(View.GONE);
                ll_adaptive_practise.setVisibility(View.GONE);
            }
        } catch (Exception ex) {
            ll_adaptive_practise.setVisibility(View.GONE);
            ex.printStackTrace();
        }
    }

    private void getAdaptivePractiseData() {
        ll_adaptive_practise.setVisibility(View.VISIBLE);
        ll_adaptive_practise_data.setVisibility(View.GONE);
        shimmer_adaptive_practise.startShimmer();
        shimmer_adaptive_practise.setVisibility(View.VISIBLE);
        Map<String, Object> map = new HashMap<>();
        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(COMPETITIVE_LEARN_ADAPTIVE_PRACTISE, map), null,
                response -> {
                    try {
                        shimmer_adaptive_practise.stopShimmer();
                        shimmer_adaptive_practise.setVisibility(View.GONE);
                        Log.d(TAG, "Response: " + response.toString());
                        Gson gson = new Gson();
                        TypeToken<ApiResponseDto<CompetitiveAdaptivePractiseDataDto>> token = new TypeToken<ApiResponseDto<CompetitiveAdaptivePractiseDataDto>>() {
                        };
                        ApiResponseDto<CompetitiveAdaptivePractiseDataDto> content = gson.fromJson(response.toString(), token.getType());

                        if (content.isSuccess() && content.getData() != null) {
                            competitiveAdaptivePractiseDataDto = content.getData();

                        }

                    } catch (Exception ex) {
                        ll_adaptive_practise.setVisibility(View.GONE);
                        CommonMethods.showNegativeSnackbar(fl_parent_layout, getString(R.string.something_went_wrong));
                        ex.printStackTrace();
                    }
                }, new VolleyGlobalErrorListener(requireContext()) {
            @Override
            public void postError(VolleyError error) {
                shimmer_adaptive_practise.stopShimmer();
                shimmer_adaptive_practise.setVisibility(View.GONE);
                ll_adaptive_practise.setVisibility(View.GONE);
                CommonMethods.showNegativeSnackbar(fl_parent_layout, error.getMessage());
            }
        });
        myJsonObjectRequest.setTag(TAG);
        MyVolleySingleton.getInstance(requireContext()).addToRequestQueue(myJsonObjectRequest);
    }

    private static final String SECTION_TYPE_DPP = "DPP";

    private void getDppData() {
        Map<String, Object> map = new HashMap<>();
        MyJsonObjectRequest request = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(COMPETITIVE_LEARN_DPP, map), null,
                response -> {
                    try {
                        if (!isAdded()) return;
                        Log.d(TAG, "getDppData response: " + response.toString());
                        Gson gson = new Gson();
                        TypeToken<ApiWrappedResponse<DppLearnScreenDataDto>> token =
                                new TypeToken<ApiWrappedResponse<DppLearnScreenDataDto>>() {};
                        ApiWrappedResponse<DppLearnScreenDataDto> content =
                                gson.fromJson(response.toString(), token.getType());
                        if (content.isSuccess() && content.getData() != null) {
                            DppLearnScreenDataDto dto = content.getData();
                            List<SubjectResponse> subjects = dto.getSubjects();
                            if (subjects != null && !subjects.isEmpty()) {
                                String title = StringUtils.isNotEmpty(dto.getTitle())
                                        ? dto.getTitle() : "DPP";
                                tv_dpp_title.setText(title);
                                tv_dpp_title.setVisibility(View.VISIBLE);
                                rv_dpp.setLayoutManager(new LinearLayoutManager(
                                        getContext(), LinearLayoutManager.HORIZONTAL, false));
                                rv_dpp.setAdapter(new CompetitiveSubjectChapterAdapter(
                                        subjects, SECTION_TYPE_DPP, false, this));
                                rv_dpp.setVisibility(View.VISIBLE);
                                ll_one_shot_lecture.setVisibility(View.VISIBLE);
                                ll_one_shot_lecture_data.setVisibility(View.VISIBLE);
                            } else {
                                tv_dpp_title.setVisibility(View.GONE);
                                rv_dpp.setVisibility(View.GONE);
                            }
                        } else {
                            tv_dpp_title.setVisibility(View.GONE);
                            rv_dpp.setVisibility(View.GONE);
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, "getDppData: ", ex);
                        if (isAdded()) {
                            tv_dpp_title.setVisibility(View.GONE);
                            rv_dpp.setVisibility(View.GONE);
                        }
                    }
                }, new VolleyGlobalErrorListener(requireContext()) {
            @Override
            public void postError(VolleyError error) {
                if (!isAdded()) return;
                Log.d(TAG, "getDppData postError: ");
                tv_dpp_title.setVisibility(View.GONE);
                rv_dpp.setVisibility(View.GONE);
            }
        });
        request.setTag(TAG);
        MyVolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
    }

    private void setupTestSeriesSection() {
        if (homeScreenViewModel == null) {
            Log.e(TAG, "setupTestSeriesSection: homeScreenViewModel is null, hiding section");
            ll_test_series.setVisibility(View.GONE);
            return;
        }

        ll_test_series.setVisibility(View.VISIBLE);
        ll_test_series_data.setVisibility(View.GONE);

        // Only show shimmer on the very first load; if the API already ran, data will
        // re-emit immediately via the observer and we avoid a shimmer flash.
        if (!homeScreenViewModel.isTestSeriesApiEverCompleted()) {
            shimmer_test_series.setVisibility(View.VISIBLE);
            shimmer_test_series.startShimmer();
        }

        // Loading state — drives shimmer while API is in-flight.
        homeScreenViewModel.getTestSeriesLoading().observe(getViewLifecycleOwner(), loading -> {
            if (!isAdded()) return;
            if (Boolean.TRUE.equals(loading)) {
                shimmer_test_series.setVisibility(View.VISIBLE);
                shimmer_test_series.startShimmer();
            } else {
                shimmer_test_series.stopShimmer();
                shimmer_test_series.setVisibility(View.GONE);
            }
        });

        // Data — renders the list whenever the ViewModel posts a new value.
        homeScreenViewModel.getTestSeriesList().observe(getViewLifecycleOwner(), this::renderTestSeriesSection);

        // Trigger fetch; ViewModel handles in-memory cache + persistent cache internally.
        homeScreenViewModel.getTestSeriesData(false);
    }

    private void renderTestSeriesSection(TestSeriesCardResponse response) {
        if (!isAdded()) return;
        try {
            shimmer_test_series.stopShimmer();
            shimmer_test_series.setVisibility(View.GONE);

            List<TestSeriesCard> list = (response != null && response.getTestSeries() != null)
                    ? response.getTestSeries() : null;

            if (list == null || list.isEmpty()) {
                ll_test_series.setVisibility(View.GONE);
                return;
            }

            String title = response.getTitle();
            if (StringUtils.isNotEmpty(title)) {
                tv_ts_title.setText(title);
                tv_ts_title.setVisibility(View.VISIBLE);
            } else {
                tv_ts_title.setVisibility(View.GONE);
            }

            TestSeriesAdapter adapter = new TestSeriesAdapter(list, requireContext(),
                    new TestSeriesAdapter.TestSeriesSetClickListener() {
                        @Override
                        public void onTestSeriesSetClick(TestSeriesCard card) {
                            if (!isAdded()) return;
                            CommonNavigator.navigateToTestSeriesListFromCard(requireContext(), card, TestSeriesType.adaptive.name(), TAG);
                        }

                        @Override
                        public void calculateCount(TestSeriesCard card) {
                            // Not required in this fragment
                        }
                    });

            rv_test_series.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            rv_test_series.setAdapter(adapter);
            rv_test_series.setVisibility(View.VISIBLE);
            ll_test_series_data.setVisibility(View.VISIBLE);
        } catch (Exception ex) {
            Log.e(TAG, "renderTestSeriesSection: ", ex);
            if (isAdded()) ll_test_series.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSubjectClick(SubjectResponse subject, String sectionType) {
        try {
            if (SECTION_TYPE_DPP.equals(sectionType)) {
                CommonNavigator.navigateToDppSubjectActivity(requireContext(), subject.getId(), subject.getSubjectName());
            } else {
                CommonNavigator.navigateToChapterListByTypeActivity(requireContext(), getString(R.string.lectures), sectionType, subject.getId(), TAG);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
