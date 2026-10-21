package arivihan.technologies.doubtbuzzter2.fragments;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_STREAM_LIST;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.SUBJECT_GROUP_LIST;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.enums.event.ActionEventDirection;
import arivihan.technologies.doubtbuzzter2.enums.event.ActionEventName;
import arivihan.technologies.doubtbuzzter2.enums.event.ActionEventScreen;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceMethods;
import arivihan.technologies.doubtbuzzter2.model.generic.ApiWrappedResponse;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.StreamsAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.model.OnBoardingSelectionModel;
import arivihan.technologies.doubtbuzzter2.model.batch.stream.StreamModel;
import arivihan.technologies.doubtbuzzter2.volley.EventApiHelper;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StreamsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StreamsFragment extends Fragment implements StreamsAdapter.OnSelectionClickListener {

    private static final String TAG = StreamsFragment.class.getSimpleName();
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_NAME = "userName";
    private static final String ARG_PHONE = "phone";
    private static final String ARG_NAVIGATION_DIRECTION = "navigationDirection";
    TextView step_text_1;

    private View view;
    private RecyclerView class_selection_rv;
    CardView card_layout;
    StreamsAdapter streamsAdapter;
    ArrayList<StreamModel> subjectGroupModels = new ArrayList<>();
    Gson gson = new Gson();
    SubjectGroupClickListener subjectGroupClickListener;
    ShimmerFrameLayout shimmer_layout;
    LinearLayout content_layout;
    LottieAnimationView animation;
    NestedScrollView nsv_scroll_view;

    private NetworkStatusLiveData networkStatusLiveData;
    private Boolean lastKnownState = null;
    private boolean isApiTriggered;
    private int dataLoaded = 0; // assuming 0 = not loaded
    String userName = "";
    String phone = "";
    private String navigationDirection = ActionEventDirection.FORWARD.getValue();

    public StreamsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        subjectGroupClickListener = (SubjectGroupClickListener) context;
    }

    public static StreamsFragment newInstance(String userName, String phone, ArrayList<StreamModel> subjectGroupModels,
                                              String navigationDirection) {
        Log.d(PreferenceKeys.CLASS_NAME.getKey(), "newInstance: " + subjectGroupModels);
        StreamsFragment fragment = new StreamsFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_PARAM1, subjectGroupModels);
        //args.putString(ARG_PARAM2, param2);
        args.putString(ARG_NAME, userName);
        args.putString(ARG_PHONE, phone);
        args.putString(ARG_NAVIGATION_DIRECTION, navigationDirection);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            subjectGroupModels.clear();
            subjectGroupModels.addAll(getArguments().getParcelableArrayList(ARG_PARAM1));
            userName = getArguments().getString(ARG_NAME, "");
            phone = getArguments().getString(ARG_PHONE, "");
            navigationDirection = getArguments().getString(ARG_NAVIGATION_DIRECTION,
                    ActionEventDirection.FORWARD.getValue());

            if (!subjectGroupModels.isEmpty()) {
                checkScrollable();
            }
//            setRecyclerView();
            //mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        networkStatusLiveData = new NetworkStatusLiveData(requireContext());

        networkStatusLiveData.observe(getViewLifecycleOwner(), isOnline -> {

            boolean online = Boolean.TRUE.equals(isOnline);

            if (lastKnownState == null || online != lastKnownState) {
                lastKnownState = online;

                if (online) {

                    Log.d("NETWORK_STATUS", "Connected");

                    if (CollectionUtils.isEmpty(subjectGroupModels)) {
                        showShimmer(true);
                        getSubjectGroupData();
                    }
                } else {

                    Log.d("NETWORK_STATUS", "Disconnected");


                    CommonMethods.showNegativeSnackbar(
                            view,
                            getString(R.string.NoInternetConnection)
                    );
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_subject_group_layout, container, false);
        nsv_scroll_view = view.findViewById(R.id.nsv_scroll_view);
        shimmer_layout = view.findViewById(R.id.shimmer_layout);
        content_layout = view.findViewById(R.id.content_layout);
        step_text_1 = view.findViewById(R.id.step_text_1);
        setFragmentText();
        class_selection_rv = view.findViewById(R.id.class_selection_rv);
        card_layout = view.findViewById(R.id.card_layout);
        animation = view.findViewById(R.id.animation);

        animation.setVisibility(View.GONE);
        initializeSubjectSelectionRecycler();




        if (CollectionUtils.isEmpty(subjectGroupModels)) {
            showShimmer(true);
            getSubjectGroupData();
        } else {
            // Data already present
            showShimmer(false);
            animation.setVisibility(View.VISIBLE);
        }

//
//        if (CollectionUtils.isEmpty(subjectGroupModels)) {
//            shimmer_layout.setVisibility(View.VISIBLE);
//            shimmer_layout.startShimmer();
//            content_layout.setVisibility(View.GONE);
//            getSubjectGroupData();
//        } else {
//            shimmer_layout.setVisibility(View.GONE);
//            shimmer_layout.stopShimmer();
//            content_layout.setVisibility(View.VISIBLE);
//        }

        boolean scroll_view_listener_disabled = MyApplication.getFirebaseRemoteConfig().getBoolean("onboarding_course_selection_scroll_listener_disabled");

        if (!scroll_view_listener_disabled) {
            nsv_scroll_view.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
                try {
                    int scrollRange = nsv_scroll_view.getChildAt(0).getHeight() - nsv_scroll_view.getHeight();

                    if (scrollRange > 0) {
                        animation.setVisibility(View.GONE);
                    } else {
                        animation.setVisibility(View.VISIBLE);
                    }
                } catch (Exception ignored) {
                }
            });
        }
        startAnimation();



        return view;
    }

    private void checkScrollable() {
        try {
            nsv_scroll_view.post(() -> checkScrollability());
        } catch (Exception ignored) {
        }
    }

    private void checkScrollability() {
        if (nsv_scroll_view.getChildCount() > 0) {
            View child = nsv_scroll_view.getChildAt(0);

            int scrollRange = child.getHeight() - nsv_scroll_view.getHeight();

            if (scrollRange > 0) {
                animation.setVisibility(View.GONE);
            } else {
                animation.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setRecyclerView() {
        try {
            int scrollRange = nsv_scroll_view.getChildAt(0).getHeight() - nsv_scroll_view.getHeight();

            if (scrollRange > 0) {
                animation.setVisibility(View.GONE);
            } else {
                animation.setVisibility(View.VISIBLE);
            }
        } catch (Exception ignored) {
        }
    }

    private void showShimmer(boolean show) {
        if (show) {
            shimmer_layout.setVisibility(View.VISIBLE);
            shimmer_layout.startShimmer();
            content_layout.setVisibility(View.GONE);
        } else {
            shimmer_layout.stopShimmer();
            shimmer_layout.setVisibility(View.GONE);
            content_layout.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("SetTextI18n")
    private void setFragmentText() {
        step_text_1.setText(MyApplication.getFragmentFormattedText(requireContext(), "StreamsFragment"));
    }

    private void initializeSubjectSelectionRecycler() {
        try {
            streamsAdapter = new StreamsAdapter(requireActivity(), subjectGroupModels, this);
            class_selection_rv.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false));
            class_selection_rv.setAdapter(streamsAdapter);
        } catch (Exception ignored) {
        }
    }

    private void startAnimation() {
        final Animation fadeInFromRight = AnimationUtils.loadAnimation(requireContext(),
                R.anim.slide_in_from_right_switch);
        step_text_1.startAnimation(fadeInFromRight);
        card_layout.startAnimation(fadeInFromRight);

        OnBoardingSelectionModel sel = PreferenceMethods.getSelectedOnboardingModel();
        EventApiHelper.sendActionEvent(
                ActionEventName.STREAM_SCREEN_VIEWED.getValue(),
                (sel!=null && sel.getId()!=null)? sel.getId() : "",
                phone,
                navigationDirection,
                ActionEventScreen.STREAM_SCREEN.getValue());
    }

    private void getSubjectGroupData() {
        if(isApiTriggered) {
            return;
        }
        OnBoardingSelectionModel onBoardingSelectionModel = PreferenceMethods.getSelectedOnboardingModel();

        Map<String, Object> map = new HashMap<>();
        map.put("examCode", onBoardingSelectionModel != null ? onBoardingSelectionModel.getExamCode() : "");
        map.put("gradeCode", onBoardingSelectionModel != null ? onBoardingSelectionModel.getGradeCode() : "");

        Log.d(TAG, "---> Stream Request: "+map.toString());
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(AV_STREAM_LIST, map), null,
                response -> {
                    try {
                        Log.d("dataRequired", "getSubjectGroupData: " + response);
                        TypeToken<ApiWrappedResponse<List<StreamModel>>> token =
                                new TypeToken<ApiWrappedResponse<List<StreamModel>>>() {};
                        ApiWrappedResponse<List<StreamModel>> apiResponse =
                                gson.fromJson(response.toString(), token.getType());
                        Log.d(TAG, "Stream Response: "+apiResponse.toString());
                        if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                            subjectGroupModels.clear();
                            subjectGroupModels.addAll(apiResponse.getData());
                            applyDefaultStreamSelection(subjectGroupModels);
                            streamsAdapter.notifyDataSetChanged();
                            checkScrollable();
                            shimmer_layout.setVisibility(View.GONE);
                            shimmer_layout.stopShimmer();
                            content_layout.setVisibility(View.VISIBLE);
                            animation.setVisibility(View.VISIBLE);
                        }
                        isApiTriggered = false;
                    } catch (Exception e) {
                        Log.e("StreamsFragment", "getSubjectGroupData: failed to parse response", e);
                    }
                }, new VolleyGlobalErrorListener(getActivity()) {
            @Override
            public void postError(VolleyError error) {
                isApiTriggered = false;
            }
        });
        MyVolleySingleton.getInstance(getActivity()).addToRequestQueue(myJsonObjectRequest2);


    }

    private void applyDefaultStreamSelection(ArrayList<StreamModel> streams) {
        if (streams == null || streams.isEmpty()) return;

        OnBoardingSelectionModel sel = PreferenceMethods.getSelectedOnboardingModel();
        String resumedStreamCode = sel != null ? sel.getStreamCode() : null;
        if (StringUtils.isEmpty(resumedStreamCode)) return;

        for (int i = 0; i < streams.size(); i++) {
            StreamModel stream = streams.get(i);
            if (resumedStreamCode.equalsIgnoreCase(stream.getCode())) {
                stream.setSelected(true);
                subjectGroupClickListener.onSubjectGroupClicked(
                        stream, streams, false, "");
                return;
            }
        }
    }

    private boolean isAnyStreamSelected() {
        for (StreamModel model : subjectGroupModels) {
            if (model.getSelected()) return true;
        }
        return false;
    }


    @Override
    public void onSelectionChange(StreamModel subjectGroupModel, int pos) {
        for (int i = 0; i < subjectGroupModels.size(); i++) {
            subjectGroupModels.get(i).setSelected(i == pos);
        }
        streamsAdapter.notifyDataSetChanged();
        subjectGroupClickListener.onSubjectGroupClicked(subjectGroupModel, subjectGroupModels, true, "");
    }

    public interface SubjectGroupClickListener {
        void onSubjectGroupClicked(StreamModel subjectGroup, ArrayList<StreamModel> subjectGroupModels, boolean clicked, String subjectGroupLanguage);
    }
}