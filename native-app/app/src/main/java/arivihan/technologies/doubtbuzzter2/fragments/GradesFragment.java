package arivihan.technologies.doubtbuzzter2.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.card.MaterialCardView;
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
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.GradeAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.constants.UrlRoutes;
import arivihan.technologies.doubtbuzzter2.enums.event.ActionEventDirection;
import arivihan.technologies.doubtbuzzter2.enums.event.ActionEventName;
import arivihan.technologies.doubtbuzzter2.enums.event.ActionEventScreen;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceMethods;
import arivihan.technologies.doubtbuzzter2.model.generic.ApiWrappedResponse;
import arivihan.technologies.doubtbuzzter2.model.OnBoardingSelectionModel;
import arivihan.technologies.doubtbuzzter2.model.batch.grade.GradeModel;
import arivihan.technologies.doubtbuzzter2.volley.EventApiHelper;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.helper.StyledTextBuilder;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GradesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GradesFragment extends Fragment implements GradeAdapter.OnSelectionClickListener {

    public static final String TAG = GradesFragment.class.getSimpleName();
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_NAME = "userName";
    private static final String ARG_PHONE = "phone";
    private static final String ARG_NAVIGATION_DIRECTION = "navigationDirection";
    TextView step_text_1;
    private RecyclerView class_selection_rv;
    CardView card_layout;
    GradeAdapter gradeAdapter;
    private BoardFragment.BoardClickListener boardClickListener;
    ArrayList<GradeModel> gradeList = new ArrayList<>();
    Gson gson = new Gson();
    GradeClickListener gradeClickListener;
    ShimmerFrameLayout shimmer_layout;
    ConstraintLayout content_layout;
    String userName = "";
    String phone = "";
    private String navigationDirection = ActionEventDirection.FORWARD.getValue();
    private GradeModel previouslySelectedItem = null;

    private NetworkStatusLiveData networkStatusLiveData;
    private Boolean lastKnownNetworkState = null;
    private boolean apiCallFailed = false;

    public GradesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        gradeClickListener = (GradeClickListener) context;
    }

    public static GradesFragment newInstance(String userName, String phone, ArrayList<GradeModel> classList,
                                             String navigationDirection) {
        Log.d(PreferenceKeys.CLASS_NAME.getKey(), "newInstance: " + classList);
        GradesFragment fragment = new GradesFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_PARAM1, classList);
        args.putString(ARG_NAME, userName);
        args.putString(ARG_PHONE, phone);
        args.putString(ARG_NAVIGATION_DIRECTION, navigationDirection);
        //args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            gradeList.addAll(getArguments().getParcelableArrayList(ARG_PARAM1));
            userName = getArguments().getString(ARG_NAME, "");
            phone = getArguments().getString(ARG_PHONE, "");
            navigationDirection = getArguments().getString(ARG_NAVIGATION_DIRECTION,
                    ActionEventDirection.FORWARD.getValue());
            //mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_class, container, false);
        shimmer_layout = view.findViewById(R.id.shimmer_layout);
        content_layout = view.findViewById(R.id.content_layout);
        step_text_1 = view.findViewById(R.id.step_text_1);
        setFragmentText();
        class_selection_rv = view.findViewById(R.id.class_selection_rv);
        card_layout = view.findViewById(R.id.card_layout);
        initializeSubjectSelectionRecycler();
        if (CollectionUtils.isEmpty(gradeList)) {
            shimmer_layout.setVisibility(View.VISIBLE);
            shimmer_layout.startShimmer();
            content_layout.setVisibility(View.GONE);
            setClassData();
        } else {
            shimmer_layout.setVisibility(View.GONE);
            shimmer_layout.stopShimmer();
            content_layout.setVisibility(View.VISIBLE);
            gradeClickListener.nextCardEnabled(GradesFragment.class.getSimpleName());
        }
        startAnimation();

        Map<String, String> eventParams = new HashMap<>();
        eventParams.put("phone", phone!=null ? phone : "");
        eventParams.put("name", userName!=null ? userName : "");
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.onboarding_name_screen_view.getActionName(), eventParams);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        networkStatusLiveData = new NetworkStatusLiveData(requireContext(), false);
        networkStatusLiveData.observe(getViewLifecycleOwner(), isOnline -> {
            boolean online = Boolean.TRUE.equals(isOnline);
            if (lastKnownNetworkState == null || online != lastKnownNetworkState) {
                lastKnownNetworkState = online;
                if (online && apiCallFailed && CollectionUtils.isEmpty(gradeList)) {
                    apiCallFailed = false;
                    shimmer_layout.setVisibility(View.VISIBLE);
                    shimmer_layout.startShimmer();
                    content_layout.setVisibility(View.GONE);
                    setClassData();
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void setFragmentText() {
        step_text_1.setText(MyApplication.getFragmentFormattedText(requireContext(), "GradesFragment"));
    }

    private void initializeSubjectSelectionRecycler() {
        try {
            gradeAdapter = new GradeAdapter(requireActivity(), gradeList, this);
            class_selection_rv.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false));
            class_selection_rv.setAdapter(gradeAdapter);
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
                ActionEventName.CLASS_SCREEN_VIEWED.getValue(),
                (sel!=null && sel.getId()!=null)? sel.getId() : "",
                phone,
                navigationDirection,
                ActionEventScreen.CLASS_SCREEN.getValue()
        );
    }

    private void setClassData() {
        OnBoardingSelectionModel onBoardingSelectionModel = PreferenceMethods.getSelectedOnboardingModel();
        Map<String, Object> map = new HashMap<>();
        map.put("examCode", onBoardingSelectionModel != null ? onBoardingSelectionModel.getExamCode() : "");
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(UrlRoutes.AV_GRADE_LIST, map), null,
                response -> {
                    try {
                        TypeToken<ApiWrappedResponse<List<GradeModel>>> token =
                                new TypeToken<ApiWrappedResponse<List<GradeModel>>>() {};
                        ApiWrappedResponse<List<GradeModel>> apiResponse =
                                gson.fromJson(response.toString(), token.getType());
                        Log.d(TAG, "Grades Response: "+apiResponse.toString());
                        if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                            gradeList.addAll(apiResponse.getData());
                            updateByDefaultSelection(gradeList);
                            gradeAdapter.notifyDataSetChanged();
                            shimmer_layout.setVisibility(View.GONE);
                            shimmer_layout.stopShimmer();
                            content_layout.setVisibility(View.VISIBLE);
                            gradeClickListener.nextCardEnabled(GradesFragment.TAG);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "setClassData: failed to parse response", e);
                    }
                }, new VolleyGlobalErrorListener(getActivity()) {
            @Override
            public void postError(VolleyError error) {
                if (!isAdded()) return;
                apiCallFailed = true;
                String msg = !MyApplication.isNetworkAvailable()
                        ? getString(R.string.internet_failure_error)
                        : getString(R.string.server_error);
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
        MyVolleySingleton.getInstance(getActivity()).addToRequestQueue(myJsonObjectRequest2);
    }

    private void updateByDefaultSelection(ArrayList<GradeModel> gradeModels) {
        if (gradeModels == null || gradeModels.isEmpty()) return;

        // Priority 1: resumed gradeCode from selection model (resume flow)
        OnBoardingSelectionModel sel = PreferenceMethods.getSelectedOnboardingModel();
        String resumedGradeCode = sel != null ? sel.getGradeCode() : null;

        // Priority 2: remote config default
        String defaultGradeCode = MyApplication.getFirebaseRemoteConfig()
                .getString("selected_grade_v1");

        String targetCode = StringUtils.isNotEmpty(resumedGradeCode)
                ? resumedGradeCode : defaultGradeCode;

        if (StringUtils.isEmpty(targetCode)) return;

        for (GradeModel gradeModel : gradeModels) {
            if (targetCode.equalsIgnoreCase(gradeModel.getGradeCode())) {
                gradeModel.setSelected(true);
                gradeClickListener.onGradeClick(gradeModel, false);
                gradeClickListener.saveGradeData(gradeModels);
                return;
            }
            gradeModel.setSelected(false);
        }
    }


    @Override
    public void onSelectionChange(GradeModel gradeModel, int pos) {
//        for (int i = 0; i < gradeList.size(); i++) {
//            gradeList.get(i).setSelected(i == pos);
//        }

        previouslySelectedItem = null;
        for (GradeModel item : gradeList) {
            if (item.getSelected()) {
                previouslySelectedItem = item;
                break;
            }
        }

        for (int i = 0; i < gradeList.size(); i++) {
            gradeList.get(i).setSelected(i == pos);
        }
        gradeAdapter.notifyDataSetChanged();

        if (gradeModel.isGradeConfirmationPopup()) {
            showReappearPopup(gradeModel);
            return;
        }

        gradeClickListener.onGradeClick(gradeModel, true);
        gradeClickListener.saveGradeData(gradeList);
    }

    private void showReappearPopup(GradeModel vernacularItem) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.popup_reappear_confirmation);
        dialog.setCancelable(false);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        TextView tvTitle = dialog.findViewById(R.id.tv_logout_title);
        TextView tvBody  = dialog.findViewById(R.id.tv_logout_body);
        ImageView close=dialog.findViewById(R.id.iv_close_pop_up);

        String remoteJson = MyApplication.getFirebaseRemoteConfig()
                .getString("confirmation_pop_up_text");

        try {
            JSONObject object = new JSONObject(remoteJson);
            String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());

            if (!object.has(lang)) {
                lang = "en";
            }

            JSONObject langObject = object.optJSONObject(lang);

            String title = langObject.optString("title");
            Object subTitleObj = langObject.opt("subtitle");

            Log.d(TAG, "showReappearPopup: --title " + title + " ---subtitle--- " + subTitleObj);

            if (StringUtils.isNotEmpty(title)) {
                tvTitle.setText(
                        StyledTextBuilder.fromJson(requireContext(), title)
                );
            }

            if (subTitleObj instanceof JSONArray) {

                JSONArray subTitleArray = (JSONArray) subTitleObj;

                tvBody.setText(
                        StyledTextBuilder.fromJson(requireContext(), subTitleArray.toString())
                );

            } else if (subTitleObj instanceof String) {

                String subTitle = (String) subTitleObj;

                if (StringUtils.isNotEmpty(subTitle)) {
                    tvBody.setText(
                            StyledTextBuilder.fromJson(requireContext(), subTitle)
                    );
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            tvTitle.setText("Confirm Selection");
            tvBody.setText("Something went wrong");
        }

        MaterialCardView cvYes = dialog.findViewById(R.id.cv_yes);
        MaterialCardView cvNo  = dialog.findViewById(R.id.cv_no);

        cvYes.setOnClickListener(v -> {
            dialog.dismiss();
            gradeClickListener.onGradeClick(vernacularItem, true);
            gradeClickListener.saveGradeData(gradeList);
        });

        cvNo.setOnClickListener(v -> {
            dialog.dismiss();
            restorePreviousSelection();
        });

        close.setOnClickListener(v -> {
            dialog.dismiss();
            restorePreviousSelection();
        });
        close.setOnClickListener(v -> {
            dialog.dismiss();
            for (GradeModel item : gradeList) {
                item.setSelected(false);
            }
            gradeAdapter.notifyDataSetChanged();
        });

        dialog.show();
    }

    private void restorePreviousSelection() {
        for (GradeModel item : gradeList) {
            item.setSelected(previouslySelectedItem != null
                    && item.getId().equals(previouslySelectedItem.getId()));
        }
        gradeAdapter.notifyDataSetChanged();
    }

    public interface GradeClickListener {
        void saveGradeData(ArrayList<GradeModel> classList);

        void onGradeClick(GradeModel gradeModel, boolean clicked);

        void nextCardEnabled(String simpleName);
    }
}