package arivihan.technologies.doubtbuzzter2.fragments.competitive.subject;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_COMPETITIVE_ADAPTIVE_CHAPTER_LIST;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.competitive.subject.CompetitiveChapterListCardAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.CommonNavigator;
import arivihan.technologies.doubtbuzzter2.enums.competitive.AdaptivePath;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.listener.competitive.RefreshActivityListener;
import arivihan.technologies.doubtbuzzter2.model.competitive.subject.CompetitiveChapterDto;
import arivihan.technologies.doubtbuzzter2.model.competitive.subject.TopicWiseChapterListResponse;
import arivihan.technologies.doubtbuzzter2.model.generic.ApiWrappedResponse;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;


public class CompetitiveSubjectChaptersFragment extends Fragment implements CompetitiveChapterListCardAdapter.OnCompetitiveChapterClickListener {

    private static final String TAG = CompetitiveSubjectChaptersFragment.class.getSimpleName();

    // UI Variable
    private FrameLayout fl_parent_layout;
    private RecyclerView rv_topics;
    private CompetitiveChapterListCardAdapter adapter;
    private LinearLayout ll_no_data;
    private ShimmerFrameLayout shimmer;
    private TextView tv_no_result_title, tv_no_result_subtitle;

    // Data Variable
    private String id;
    private String name;
    private List<CompetitiveChapterDto> chapterList;
    private String queryText = "";
    private List<String> filterOptions;
    private RefreshActivityListener refreshActivityListener;

    public CompetitiveSubjectChaptersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);


        try {
            if (getActivity() instanceof RefreshActivityListener) {
                refreshActivityListener = (RefreshActivityListener) getActivity();
            }
        } catch (Exception exception) {
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("id", id);
        outState.putString(PreferenceKeys.NAME.getKey(), name);
//        outState.putSerializable("chapterList", (Serializable) chapterList);
    }

    public static CompetitiveSubjectChaptersFragment newInstance(String id, String name, List<CompetitiveChapterDto> chapterList) {
        CompetitiveSubjectChaptersFragment fragment = new CompetitiveSubjectChaptersFragment();
        Bundle args = new Bundle();
        args.putString("id", id);
        args.putString(PreferenceKeys.NAME.getKey(), name);
//        args.putSerializable("chapterList", (Serializable) chapterList);
        fragment.setArguments(args);
        return fragment;
    }

    public static CompetitiveSubjectChaptersFragment newInstance(String id, String name) {
        CompetitiveSubjectChaptersFragment fragment = new CompetitiveSubjectChaptersFragment();
        Bundle args = new Bundle();
        args.putString("id", id);
        args.putString(PreferenceKeys.NAME.getKey(), name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            try {
                id = getArguments().getString("id");
//                chapterList = (List<NameAndIdDto>) getArguments().getSerializable("chapterList");
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_competitive_subject_chapters, container, false);
        Log.d(TAG, "Id: " + (StringUtils.isNotEmpty(id) ? id : ""));
        initViews(view);
        return view;
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
        MyVolleySingleton.getInstance(requireContext()).getRequestQueue().cancelAll(TAG);
        super.onDestroy();
    }

    private void initViews(View view) {
        fl_parent_layout = view.findViewById(R.id.fl_parent_layout);
        rv_topics = view.findViewById(R.id.rv_topics);
        ll_no_data = view.findViewById(R.id.ll_no_data);
        shimmer = view.findViewById(R.id.shimmer);
        tv_no_result_title = view.findViewById(R.id.tv_no_result_title);
        tv_no_result_subtitle = view.findViewById(R.id.tv_no_result_subtitle);

        rv_topics.setVisibility(View.GONE);
        ll_no_data.setVisibility(View.GONE);
        shimmer.setVisibility(View.GONE);

        if (chapterList == null || chapterList.isEmpty()) {
            getChaptersBySubjectId(id);
        } else {
            setRecyclerView(chapterList);
        }
    }

    public void setFilter(String filter, List<String> filterOptions) {
        try {
            this.queryText = filter;
            this.filterOptions = filterOptions;
            if(adapter!=null) {
                adapter.setFilterPriorityList(filterOptions);
                adapter.filter.filter(filter);
//                Log.d(TAG, "Fragment - Options size: "+filterOptions.size());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setRecyclerView(List<CompetitiveChapterDto> list) {
        String selectedChapterId = MyApplication.getStringData(PreferenceKeys.ADAPTIVE_SELECTED_CHAPTER_ID.getKey());
        adapter = new CompetitiveChapterListCardAdapter(requireContext(), list, new ArrayList<>(), this, selectedChapterId);
        rv_topics.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rv_topics.setAdapter(adapter);
//        adapter.filter.filter(queryText);
        rv_topics.setVisibility(View.VISIBLE);
        ll_no_data.setVisibility(View.GONE);

        setFilter(queryText, filterOptions);
    }

    private void getChaptersBySubjectId(String subjectId) {
        shimmer.startShimmer();
        shimmer.setVisibility(View.VISIBLE);
        Map<String, Object> map = new HashMap<>();
        map.put("subjectId", subjectId);
        Log.d(TAG, "Request: " + map.toString());
        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_COMPETITIVE_ADAPTIVE_CHAPTER_LIST, map), null,
                response -> {
                    try {
                        Log.d(TAG, "Response: " + response.toString());
                        Gson gson = new Gson();
                        TypeToken<ApiWrappedResponse<TopicWiseChapterListResponse>> token = new TypeToken<ApiWrappedResponse<TopicWiseChapterListResponse>>() {
                        };
                        ApiWrappedResponse<TopicWiseChapterListResponse> apiWrappedResponse = gson.fromJson(response.toString(), token.getType());
                        TopicWiseChapterListResponse content = apiWrappedResponse.getData();
                        shimmer.stopShimmer();
                        shimmer.setVisibility(View.GONE);
                        setRecyclerView(content.getChapters());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }, new VolleyGlobalErrorListener(requireContext()) {
            @Override
            public void postError(VolleyError error) {
                shimmer.stopShimmer();
                shimmer.setVisibility(View.GONE);
                CommonMethods.showNegativeSnackbar(fl_parent_layout, error.getMessage());
            }
        });
        myJsonObjectRequest.setTag(TAG);
        MyVolleySingleton.getInstance(requireContext()).addToRequestQueue(myJsonObjectRequest);
    }

    @Override
    public void onChapterClick(int position, CompetitiveChapterDto competitiveChapterDto) {
        try {
            if (!MyApplication.isNetworkAvailable()) {
                CommonMethods.showNegativeSnackbar(fl_parent_layout, getString(R.string.NoInternetConnection));
                return;
            }

            MyApplication.saveData(PreferenceKeys.ADAPTIVE_SELECTED_CHAPTER_ID.getKey(), competitiveChapterDto.getChapterId());
            String selectedTopicId = MyApplication.getStringData(PreferenceKeys.ADAPTIVE_SELECTED_TOPIC_ID.getKey());
            String url = CommonMethods.getPersonalizedPathUrl() + AdaptivePath.TOPIC_WISE.path();
            Map<String, Object> params = new HashMap<>();
            params.put("chapterId", competitiveChapterDto.getChapterId());
            params.put("chapterCode", competitiveChapterDto.getChapterCode());
            params.put("chapterName", competitiveChapterDto.getTitle());
            params.put("subject", id);
            params.put("topicId", selectedTopicId);
            CommonNavigator.navigateToWebViewByUrlParams(requireContext(), url, params,
                    true, true, false, false, false, false, null, true, true, false, false, null,TAG,null);

            Map<String, String> eventParams = new HashMap<>();
            params.put("chapterId", competitiveChapterDto.getChapterId());
            params.put("chapterName", competitiveChapterDto.getTitle());
            params.put("subjectName", name);
            params.put("subjectId", id);
            params.put("selectedTopicId", selectedTopicId);
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.clicked_adaptive_chapter.getActionName(), eventParams);

            if(refreshActivityListener !=null) {
                Log.d(TAG, "Refresh Page : true");
                refreshActivityListener.refreshPage(true);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onChapterFilter(List<CompetitiveChapterDto> list) {
        if(ll_no_data!=null) {
            ll_no_data.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }
}