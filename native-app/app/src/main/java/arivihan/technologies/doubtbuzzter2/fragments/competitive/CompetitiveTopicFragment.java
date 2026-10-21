package arivihan.technologies.doubtbuzzter2.fragments.competitive;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.COMPETITIVE_PRACTISE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_SAMPLE_PAPER_CONTENTS;

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
import com.google.android.gms.common.util.CollectionUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.SamplePapersActivity;
import arivihan.technologies.doubtbuzzter2.adapter.competitive.topic.CompetitiveTopicListCardAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.model.SamplePaperContent;
import arivihan.technologies.doubtbuzzter2.model.SamplePaperModel;
import arivihan.technologies.doubtbuzzter2.model.competitive.topic.CompetitiveTopicDto;
import arivihan.technologies.doubtbuzzter2.model.competitive.topic.CompetitiveTopicListDto;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;

public class CompetitiveTopicFragment extends Fragment implements CompetitiveTopicListCardAdapter.OnCompetitiveTopicClickListener {

    private static final String TAG = CompetitiveTopicFragment.class.getSimpleName();

    // UI Variable
    private FrameLayout fl_parent_layout;
    private RecyclerView rv_topics;
    private CompetitiveTopicListCardAdapter adapter;
    private LinearLayout ll_no_data;
    private ShimmerFrameLayout shimmer;
    private TextView tv_no_result_title, tv_no_result_subtitle;

    // Data Variable
    private String id;

    public CompetitiveTopicFragment() {

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("id", id);
    }

    public static CompetitiveTopicFragment newInstance(String id) {
        CompetitiveTopicFragment fragment = new CompetitiveTopicFragment();
        Bundle args = new Bundle();
        args.putString("id", id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            try {
                id = getArguments().getString("id");
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_competitive_topic, container, false);
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
        getTopicBySubjectId(id);
    }

    private void getTopicBySubjectId(String subjectId) {
        shimmer.startShimmer();
        shimmer.setVisibility(View.VISIBLE);
        Map<String, Object> map = new HashMap<>();
        map.put("id", subjectId);
        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(COMPETITIVE_PRACTISE, map), null,
                response -> {
                    try {
                        Log.d(TAG, "Response: " + response.toString());
                        Gson gson = new Gson();
                        TypeToken<CompetitiveTopicDto> token = new TypeToken<CompetitiveTopicDto>() {
                        };
                        CompetitiveTopicListDto content = gson.fromJson(response.toString(), token.getType());
                        shimmer.stopShimmer();
                        shimmer.setVisibility(View.GONE);
                        if (content.getList() != null && !content.getList().isEmpty()) {

                            adapter = new CompetitiveTopicListCardAdapter(content.getList(), this, requireContext());
                            rv_topics.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                            rv_topics.setAdapter(adapter);

                            rv_topics.setVisibility(View.VISIBLE);
                            ll_no_data.setVisibility(View.GONE);
                        } else {
                            rv_topics.setVisibility(View.GONE);
                            ll_no_data.setVisibility(View.VISIBLE);
                        }
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
    public void onTopicClick(int position, CompetitiveTopicDto competitiveTopicDto) {
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}