package arivihan.technologies.doubtbuzzter2.customView;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_ADD_USER_OPTIONAL_SUBJECTS;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_OPTIONAL_SUBJECTS;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.OptionalSubjectAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.helper.RemoteConfigUtils;
import arivihan.technologies.doubtbuzzter2.model.generic.ApiWrappedResponse;
import arivihan.technologies.doubtbuzzter2.model.subject.OptionalSubjectWrapperResponse;
import arivihan.technologies.doubtbuzzter2.model.subject.SubjectResponse;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;

public class OptionalSubjectBottomSheet extends DialogFragment {

    private static final String TAG = OptionalSubjectBottomSheet.class.getSimpleName();

    // Disabled bg: #1F26C6DA (12% alpha teal), text: #8026C6DA (50% alpha teal)
    // Enabled  bg: #26C6DA (full teal),          text: #FFFFFF
    private static final int COLOR_TEAL = 0xFF26C6DA;
    private static final int COLOR_TEAL_BG_DIS = 0x1F26C6DA;
    private static final int COLOR_TEAL_TEXT_DIS = 0x8026C6DA;
    private ShimmerFrameLayout shimmer;
    private RecyclerView recyclerView;
    private TextView tvTitle;
    private TextView tvEmpty;
    private CardView cvSubmit;
    private TextView tvSubmitLabel;
    private ProgressBar pbSubmit;
    private ImageView btnClose;

    private TextView tvSubTitle;
    private int selectionLimit = 1;
    private OptionalSubjectAdapter adapter;
    private final List<SubjectResponse> subjectList = new ArrayList<>();
    private final Gson gson = new Gson();

    private OnSubjectsAddedListener onSubjectsAddedListener;

    public interface OnSubjectsAddedListener {
        void onSubjectsAdded();
    }

    public void setOnSubjectsAddedListener(OnSubjectsAddedListener listener) {
        this.onSubjectsAddedListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext(), R.style.Theme_AppCompat_Light_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.hs_opt_sub_bottom_sheet_view.getActionName());
        return inflater.inflate(R.layout.dialog_optional_subjects, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            Window window = dialog.getWindow();
            window.setBackgroundDrawableResource(android.R.color.transparent);
            float density = getResources().getDisplayMetrics().density;
            int horizontalMarginPx = (int) (24 * density); // 24dp each side
            int width = getResources().getDisplayMetrics().widthPixels - 2 * horizontalMarginPx;
            window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        shimmer = view.findViewById(R.id.shimmer_optional_subjects);
        recyclerView = view.findViewById(R.id.rv_optional_subjects);
        tvTitle = view.findViewById(R.id.tv_title);
        tvSubTitle = view.findViewById(R.id.tv_sub_title);
        tvEmpty = view.findViewById(R.id.tv_empty);
        cvSubmit = view.findViewById(R.id.cv_submit_optional);
        tvSubmitLabel = view.findViewById(R.id.tv_submit_label);
        pbSubmit = view.findViewById(R.id.pb_submit);
        btnClose = view.findViewById(R.id.btn_close);

        applyRemoteConfigTexts();

        btnClose.setOnClickListener(v -> {

            Map<String, String> params = new HashMap<>();
            boolean hasSelection = adapter != null && adapter.hasSelection();
            params.put("selectionMade", String.valueOf(hasSelection));

            if (hasSelection && adapter.getSelectedIds().size() > 0) {
                params.put("selectedSubject", adapter.getSelectedIds().toString());
            } else {
                params.put("selectedSubject", "none");
            }
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.hs_opt_sub_bottom_sheet_close_btn_click.getActionName(), params);
            dismissAllowingStateLoss();
        });

        setSubmitEnabled(false);
        cvSubmit.setOnClickListener(v -> {
            if (cvSubmit.isClickable()) {
                if(CommonMethods.isOnline(requireContext())) {
                    submitSelectedSubjects();
                } else {
                    Toast.makeText(requireContext(), getString(R.string.error_no_internet_connection), Toast.LENGTH_SHORT).show();
                }
            }
        });

        shimmer.startShimmer();
        loadOptionalSubjects();
    }

    private void applyRemoteConfigTexts() {
        String title = RemoteConfigUtils.getFeatureText("optional_subject_dialog_title");
        if (!title.isEmpty()) tvTitle.setText(title);
        else tvTitle.setText(R.string.apna_optional_subject_select_karein);

        String submit = RemoteConfigUtils.getFeatureText("optional_subject_dialog_submit");
        if (!submit.isEmpty()) tvSubmitLabel.setText(submit);
        else tvSubmitLabel.setText(R.string.submit);

        String empty = RemoteConfigUtils.getFeatureText("optional_subject_dialog_empty");
        if (!empty.isEmpty()) tvEmpty.setText(empty);
        else tvEmpty.setText(R.string.no_optional_subjects_available);

    }

    private void setSubmitEnabled(boolean enabled) {
        cvSubmit.setCardBackgroundColor(ContextCompat.getColor(requireContext(), enabled ? R.color.arivihan_secondary_color : R.color.teal_light));
        tvSubmitLabel.setTextColor(ContextCompat.getColor(requireContext(), enabled ? R.color.white : R.color.arivihan_secondary_color));
        cvSubmit.setClickable(enabled);
    }

    private void setSubmitting(boolean submitting) {
        pbSubmit.setVisibility(submitting ? View.VISIBLE : View.GONE);
        tvSubmitLabel.setVisibility(submitting ? View.INVISIBLE : View.VISIBLE);
        btnClose.setVisibility(submitting ? View.GONE : View.VISIBLE);
        setCancelable(!submitting);
        cvSubmit.setClickable(!submitting);
    }

    private void loadOptionalSubjects() {
        Map<String, Object> params = new HashMap<>();

        MyJsonObjectRequest request = new MyJsonObjectRequest(
                Request.Method.GET,
                CommonMethods.getUrlParam(AV_OPTIONAL_SUBJECTS, params),
                null,
                response -> {
                    if (!isAdded()) return;
                    shimmer.stopShimmer();
                    shimmer.setVisibility(View.GONE);
                    try {
                        TypeToken<ApiWrappedResponse<OptionalSubjectWrapperResponse>> token =
                                new TypeToken<ApiWrappedResponse<OptionalSubjectWrapperResponse>>() {
                                };
                        ApiWrappedResponse<OptionalSubjectWrapperResponse> apiResponse =
                                gson.fromJson(response.toString(), token.getType());

                        OptionalSubjectWrapperResponse wrapper = (apiResponse != null && apiResponse.isSuccess())
                                ? apiResponse.getData() : null;

                        List<SubjectResponse> list = (wrapper != null && wrapper.getSubjects() != null)
                                ? wrapper.getSubjects() : new ArrayList<>();

                        int limit = (wrapper != null) ? wrapper.getRemainingOptionalSubjectSlots() : 1;
                        if (limit <= 0) limit = 1;
                        selectionLimit = limit;

                        if (!list.isEmpty()) {
                            subjectList.addAll(list);


                            List<String> names = new ArrayList<>();

                            for (SubjectResponse subject : list) {
                                names.add(subject.getSubjectName());
                            }

                            Map<String, String> popupParams = new HashMap<>();
                            popupParams.put("subjectsShown", android.text.TextUtils.join(", ", names));
                            popupParams.put("subjectCount", String.valueOf(names.size()));

                            MyApplication.logFirebaseBundleCreation(FirebaseEvents.hs_opt_sub_bottom_sheet_popup_view.getActionName(), popupParams
                            );

                            String template = RemoteConfigUtils.getFeatureText("optional_subject_dialog_limit_exceeded");
                            if (template.isEmpty()) template = getString(R.string.optional_subject_selection_limit_exceeded);
                            tvSubTitle.setText(String.format(template, selectionLimit));
                            tvSubTitle.setVisibility(View.VISIBLE);


                            adapter = new OptionalSubjectAdapter(subjectList, requireContext(), limit);
                            adapter.setOnSelectionChangedListener(hasSelection -> {
                                if (!isAdded()) return;
                                setSubmitEnabled(hasSelection);
//                                tvSubTitle.setVisibility(View.GONE);
                            });
                            adapter.setOnSelectionLimitExceededListener(() -> {
                                if (!isAdded()) return;
//                                String template = RemoteConfigUtils.getFeatureText("optional_subject_dialog_limit_exceeded");
//                                if (template.isEmpty()) template = getString(R.string.optional_subject_selection_limit_exceeded);
//                                tvSubTitle.setText(String.format(template, selectionLimit));
//                                tvSubTitle.setVisibility(View.VISIBLE);
                            });
                            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                            recyclerView.setAdapter(adapter);

                            recyclerView.setVisibility(View.VISIBLE);
                            cvSubmit.setVisibility(View.VISIBLE);
                        } else {
                            tvEmpty.setVisibility(View.VISIBLE);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing optional subjects", e);
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                },
                new VolleyGlobalErrorListener(requireActivity().getApplication()) {
                    @Override
                    public void postError(VolleyError error) {
                        if (!isAdded()) return;
                        shimmer.stopShimmer();
                        shimmer.setVisibility(View.GONE);
                        tvEmpty.setVisibility(View.VISIBLE);
                        Log.e(TAG, "Error loading optional subjects: " + error.toString());
                    }
                }
        );

        MyVolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
    }

    private void submitSelectedSubjects() {
        if (adapter == null || !adapter.hasSelection()) return;

        setSubmitting(true);

        try {
            Map<String, Object> params = new HashMap<>();

            JSONObject body = new JSONObject();
            JSONArray idsArray = new JSONArray();
            for (String id : adapter.getSelectedIds()) {
                idsArray.put(id);
            }
            body.put("subjectIds", idsArray);

            MyJsonObjectRequest request = new MyJsonObjectRequest(
                    Request.Method.POST,
                    CommonMethods.getUrlParam(AV_ADD_USER_OPTIONAL_SUBJECTS, params),
                    body,
                    response -> {
                        if (!isAdded()) return;
                        dismissAllowingStateLoss();
                        if (onSubjectsAddedListener != null) {
                            onSubjectsAddedListener.onSubjectsAdded();
                            List<String> selectedNames = new ArrayList<>();

                            for (SubjectResponse subject : subjectList) {
                                if (adapter.getSelectedIds().contains(subject.getId())) {
                                    selectedNames.add(subject.getSubjectName());
                                }
                            }

                            Map<String, String> newparams = new HashMap<>();
                            newparams.put("selectedSubjects",android.text.TextUtils.join(", ", selectedNames));
                            newparams.put("selectionCount", String.valueOf(selectedNames.size()));
                            MyApplication.logFirebaseBundleCreation(FirebaseEvents.hs_opt_sub_bottom_sheet_sbt_btn_click.getActionName(),newparams);
                        }
                    },
                    new VolleyGlobalErrorListener(requireActivity().getApplication()) {
                        @Override
                        public void postError(VolleyError error) {
                            if (!isAdded()) return;
                            setSubmitting(false);
                            setSubmitEnabled(true);
                            Toast.makeText(requireContext(),
                                    getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Error submitting optional subjects: " + error.toString());
                        }
                    }
            );

            request.setTag(TAG);
            MyVolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
        } catch (Exception e) {
            setSubmitting(false);
            setSubmitEnabled(true);
            Log.e(TAG, "Error building submit request", e);
        }
    }
}
