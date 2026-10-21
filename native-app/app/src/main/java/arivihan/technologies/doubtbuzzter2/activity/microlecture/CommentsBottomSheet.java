package arivihan.technologies.doubtbuzzter2.activity.microlecture;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.DELETE_FEEDBACK_COMMENT;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_FEEDBACK_COMMENTS;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_NESTED_FEEDBACK_COMMENTS;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.POST_FEEDBACK_COMMENT;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.microlecture.comment.CommentAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.microlecture.comment.DoubtCommentAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.listener.SnackbarMessageListener;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureDto;
import arivihan.technologies.doubtbuzzter2.model.asat.CallBackResponse;
import arivihan.technologies.doubtbuzzter2.model.microlecture.CommentConfigModel;
import arivihan.technologies.doubtbuzzter2.model.microlecture.CommentModel;
import arivihan.technologies.doubtbuzzter2.model.microlecture.CommentNestedReply;
import arivihan.technologies.doubtbuzzter2.model.microlecture.CommentResponse;
import arivihan.technologies.doubtbuzzter2.model.microlecture.MicrolectureMetadataModel;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import lombok.Setter;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class CommentsBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = CommentsBottomSheet.class.getSimpleName();
    private Runnable onDismissCallback;
    @Setter
    private SnackbarMessageListener snackbarMessageListener;

    // UI Variables
    private RecyclerView rv_comments;
    private static int VISIBLE_THRESHOLD = 3;
    private ImageView iv_close;
    private EditText et_comment;
    private ImageView iv_send;
    private LinearLayout ll_bottom_layout;
    private CardView cv_parent_layout;
    private ProgressBar pb_loading, pb_send;
    private TextView tv_no_comments;
    private RelativeLayout rl_comment_list;

    // Doubt comment UI
    private LinearLayout ll_doubt_list;
    private TextView tv_user_name, tv_initials;
    private CardView cv_profile;
    private ImageView iv_doubt_close, iv_doubt_back, iv_profile_img;
    private ProgressBar pb_doubt_loading;
    private TextView tv_no_comments_doubt;
    private RecyclerView rv_doubt_comments;

    // Data variables
    private CommentAdapter adapter;
    private CommentsViewModel viewModel;
    protected Gson gson = new Gson();
    private boolean isLoading = false;
    @Setter
    private int targetHeight = ViewGroup.LayoutParams.MATCH_PARENT;
    private List<String> colorList;
    @Setter
    private MicroLectureDto microLectureDto;
    @Setter
    private MicrolectureMetadataModel microlectureMetadataModel;
    @Setter
    private String chapterId;
    private ArrayList<CommentModel> commentModels = new ArrayList<>();
    private boolean isUserCommented = false;
    private String userId;
    private CommentConfigModel commentConfigModel;
    private CommentModel editedCommentModel;
    private boolean userCommentAllowedInitially;

    public CommentsBottomSheet(MicroLectureDto microLectureDto, MicrolectureMetadataModel microlectureMetadataModel) {
        this.microLectureDto = microLectureDto;
        this.microlectureMetadataModel = microlectureMetadataModel;
    }

    public void setOnDismissListener(Runnable listener) {
        this.onDismissCallback = listener;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissCallback != null) {
            onDismissCallback.run(); // Re-enable scroll
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.cancelVolleyRequest();
        MyVolleySingleton.getInstance(MyApplication.getInstance()).getRequestQueue().cancelAll(TAG);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.FullScreenBottomSheetDialog);
        viewModel = new ViewModelProvider(requireActivity()).get(CommentsViewModel.class); // shared

        String str = MyApplication.getFirebaseRemoteConfig().getString("lecture_comment_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));
        Gson gson = new Gson();
        commentConfigModel = gson.fromJson(str, CommentConfigModel.class);

        VISIBLE_THRESHOLD = commentConfigModel.getCommentScrollVisibleThresold();

        Log.d(TAG, "Metadata: " + microlectureMetadataModel.toString());
        if (microLectureDto != null && microlectureMetadataModel != null) {
            userCommentAllowedInitially = microLectureDto.isVideoCompleted()
                    || microLectureDto.getVideoDetails().getResumePosition() > microlectureMetadataModel.getShow_like_dislike_comment_min_time_ms();
        }
        Log.d(TAG, "---> " + userCommentAllowedInitially);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        try {
            dialog.setOnShowListener(dialogInterface -> {
                BottomSheetDialog d = (BottomSheetDialog) dialogInterface;
                FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);

                if (bottomSheet != null) {
                    // Delay height update until bottom sheet has layout
                    bottomSheet.post(() -> {
                        ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
                        layoutParams.height = targetHeight;
                        bottomSheet.setLayoutParams(layoutParams);

                        BottomSheetBehavior<?> behavior = BottomSheetBehavior.from(bottomSheet);
                        behavior.setPeekHeight(targetHeight);
                        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    });
                }
            });

            dialog.getWindow().setDimAmount(0f);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return dialog;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_comment_bottom_sheet, container, false);
        cv_parent_layout = view.findViewById(R.id.cv_parent_layout);
        iv_close = view.findViewById(R.id.iv_close);
        et_comment = view.findViewById(R.id.et_comment);
        iv_send = view.findViewById(R.id.iv_send);
        rv_comments = view.findViewById(R.id.rv_comments);
        ll_bottom_layout = view.findViewById(R.id.ll_bottom_layout);
        pb_loading = view.findViewById(R.id.pb_loading);
        pb_send = view.findViewById(R.id.pb_send);
        tv_no_comments = view.findViewById(R.id.tv_no_comments);

        rl_comment_list = view.findViewById(R.id.rl_comment_list);
        ll_doubt_list = view.findViewById(R.id.ll_doubt_list);
        iv_doubt_close = view.findViewById(R.id.iv_doubt_close);
        iv_doubt_back = view.findViewById(R.id.iv_doubt_back);
        tv_user_name = view.findViewById(R.id.tv_user_name);
        pb_doubt_loading = view.findViewById(R.id.pb_doubt_loading);
        tv_no_comments_doubt = view.findViewById(R.id.tv_no_comments_doubt);
        rv_doubt_comments = view.findViewById(R.id.rv_doubt_comments);

        cv_profile = view.findViewById(R.id.cv_profile);
        tv_initials = view.findViewById(R.id.tv_initials);
        iv_profile_img = view.findViewById(R.id.iv_profile_img);

        ll_bottom_layout.setVisibility(View.GONE);
        pb_loading.setVisibility(View.GONE);
        tv_no_comments.setVisibility(View.GONE);
        pb_send.setVisibility(View.GONE);

        pb_doubt_loading.setVisibility(View.GONE);
        tv_no_comments_doubt.setVisibility(View.GONE);

        tv_no_comments.setText(commentConfigModel.getNoCommentMessage());
        String jsonColors = MyApplication.getFirebaseRemoteConfig().getString("lecture_comment_colors");
        Gson gson = new Gson();
        Type listType = new TypeToken<List<String>>() {
        }.getType();
        colorList = gson.fromJson(jsonColors, listType);

        viewModel.setViewModelData(microLectureDto.getId(), commentConfigModel.getCommentPageSize());
        userId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
        iv_send.setOnClickListener(v -> {
            if (!et_comment.getText().toString().trim().isEmpty()) {
                if (et_comment.getText().toString().trim().length() <= commentConfigModel.getMaxCharLimit()) {
                    sendComment(et_comment.getText().toString().trim());
                } else {
                    Toast.makeText(requireContext(), commentConfigModel.getMaxLimitExceedMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireContext(), getString(R.string.apna_feedback_likhein), Toast.LENGTH_SHORT).show();
            }
        });

        rv_comments.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CommentAdapter(requireContext(), userId, colorList, commentModels,
                new CommentAdapter.Callback() {
                    @Override
                    public void onEdit(CommentModel comment, int position) {
                        showEditDialog(requireContext(), new CommentEditListener() {
                            @Override
                            public void onEdit() {
                                ll_bottom_layout.setVisibility(View.VISIBLE);
                                et_comment.setText(comment.getComment());
                                editedCommentModel = comment;
                            }

                            @Override
                            public void onDelete() {
                                if (editedCommentModel != null && editedCommentModel.getId().equals(comment.getId()))
                                    editedCommentModel = null;
                                deleteComment(comment);
                            }

                        });
                    }

                    @Override
                    public void onRetryNewComment(CommentModel comment) {

                    }

                    @Override
                    public void onNestedReplyReadMoreClick(CommentModel commentModel, CommentNestedReply commentNestedReply, int color) {
                        commentModel.setBgColor(color);
                        switchBetweenCommentAndDoubt(true, commentModel);
                    }
                });

        rv_comments.setAdapter(adapter);

        // Observe for comment changes
        viewModel.comments.observe(getViewLifecycleOwner(), commentList -> {
            if (commentList != null) {
                adapter.addComments(new ArrayList<>(commentList));
                checkIfUserCommented(commentList, false);
            }
        });

        // Observer for loading while fetching from api
        viewModel.loading.observe(getViewLifecycleOwner(), isLoading -> {
            pb_loading.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE);
        });

        // Observer for showing toast messages
        viewModel.toastMsg.observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        // Observer for loading while fetching from api
        viewModel.isLoadedOnce.observe(getViewLifecycleOwner(), isLoadedOnce -> {
//            if(Boolean.TRUE.equals(isLoadedOnce) && !(Objects.requireNonNull(viewModel.comments.getValue()).isEmpty())){
//            if(Boolean.TRUE.equals(isLoadedOnce)){
//                checkForUserComment(userId, commentModels);
//            }
        });

        viewModel.hasUserComment.observe(getViewLifecycleOwner(), hasComment -> {
            ll_bottom_layout.setVisibility(hasComment ? View.GONE : View.VISIBLE);
        });

//        viewModel.setPositiveIfTrueFound(!userCommentAllowedInitially, true);

        // Load comments only once
        if (Boolean.FALSE.equals(viewModel.isLoadedOnce.getValue())) {
            viewModel.loadMoreComments();
        }

        adapter.addComments(viewModel.getComments());
//        commentModels.addAll(viewModel.getComments());

        rv_comments.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                if (dy <= 0) return;

                RecyclerView.LayoutManager layoutManager = rv.getLayoutManager();
                if (layoutManager instanceof LinearLayoutManager) {
                    LinearLayoutManager lm = (LinearLayoutManager) layoutManager;
                    int totalItemCount = lm.getItemCount();
                    int lastVisibleItem = lm.findLastVisibleItemPosition();

                    if (lastVisibleItem == RecyclerView.NO_POSITION) return;

                    if (totalItemCount <= (lastVisibleItem + VISIBLE_THRESHOLD)) {
                        if (Boolean.FALSE.equals(viewModel.loading.getValue()) &&
                                viewModel.hasMoreData()) {
                            viewModel.loadMoreComments();
                        }
                    }

                    Log.d("TAG", "totalItemCount: " + totalItemCount + ", lastVisibleItem: " + lastVisibleItem + ", VISIBLE_THRESHOLD: " + VISIBLE_THRESHOLD + ", " + Boolean.FALSE.equals(viewModel.loading.getValue()) + ", " + viewModel.hasMoreData());
                }
            }
        });

        iv_close.setOnClickListener(imageView -> {
            dismiss();
        });

        iv_doubt_back.setOnClickListener(imageView -> {
            onDoubtBack();
        });

        iv_doubt_close.setOnClickListener(imageView -> {
            dismiss();
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        View bottomSheet = getDialog().findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior<?> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setSkipCollapsed(true);
        }
    }

    private void onDoubtBack() {
        switchBetweenCommentAndDoubt(false, null);
    }

    private void switchBetweenCommentAndDoubt(boolean isDoubt, CommentModel commentModel) {
        if (isDoubt && commentModel != null) {
            setDoubtComment(commentModel);
            rl_comment_list.setVisibility(View.GONE);
            ll_doubt_list.setVisibility(View.VISIBLE);
        } else {
            rl_comment_list.setVisibility(View.VISIBLE);
            ll_doubt_list.setVisibility(View.GONE);
        }
    }

    private int getRandomColor() {
        if (colorList == null || colorList.isEmpty()) {
            return ContextCompat.getColor(requireContext(), R.color.arivihan_secondary_color);
        }
        int index = new Random().nextInt(colorList.size());
        return Color.parseColor(colorList.get(index));
    }

    private void setDoubtComment(CommentModel commentModel) {
        if (commentModel == null)
            return;

        tv_user_name.setText(String.format(Locale.US, (StringUtils.isEmpty(commentModel.getUserName()) ? commentModel.getUserShortName() : commentModel.getUserName().replaceAll("\\s+", ""))));
        tv_initials.setText(commentModel.getUserShortName());

        int bgColor = commentModel.getBgColor();
        if(bgColor==0)
            bgColor = getRandomColor();
        cv_profile.setCardBackgroundColor(bgColor);

        Glide.with(requireContext()).clear(iv_profile_img);
        if (!StringUtils.isEmpty(commentModel.getUserPic())) {
            Glide.with(requireContext())
                    .load(commentModel.getUserPic())
                    .error(R.drawable.white_logo)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                    Target<Drawable> target, boolean isFirstResource) {
                            tv_initials.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model,
                                                       Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(iv_profile_img);
        }

        rv_doubt_comments.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_doubt_comments.setAdapter(null);
        rv_doubt_comments.setVisibility(View.GONE);
        getNestedComments(commentModel.getUserId(), microLectureDto.getId());
    }

    private void getNestedComments(String _userId, String microlectureId) {
        try {
            Map<String, Object> requestQueryMap = new HashMap<>();
            requestQueryMap.put(PreferenceKeys.MICROLECTURE_ID.getKey(), microlectureId);
            requestQueryMap.put(PreferenceKeys.USER_ID.getKey(), _userId);
            Log.d(TAG, "Request: " + requestQueryMap.toString());
            pb_doubt_loading.setVisibility(View.VISIBLE);
            MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                    CommonMethods.getUrlParam(GET_NESTED_FEEDBACK_COMMENTS, requestQueryMap), null,
                    response -> {
                        try {
                            Log.d(TAG, "Response: " + response.toString());
                            TypeToken<CommentResponse> token = new TypeToken<CommentResponse>() {
                            };
                            CommentResponse commentResponse = gson.fromJson(response.toString(), token);
                            pb_doubt_loading.setVisibility(View.GONE);

                            List<CommentModel> commentModelList = commentResponse.getResults();
                            if (commentModelList != null && !commentModelList.isEmpty()) {
                                DoubtCommentAdapter doubtCommentAdapter = new DoubtCommentAdapter(requireContext(), (ArrayList<CommentModel>) commentModelList);
                                rv_doubt_comments.setAdapter(doubtCommentAdapter);

                                tv_no_comments_doubt.setVisibility(View.GONE);
                                rv_doubt_comments.setVisibility(View.VISIBLE);
                            } else {
                                tv_no_comments_doubt.setVisibility(View.VISIBLE);
                                rv_doubt_comments.setVisibility(View.GONE);
                            }

                        } catch (Exception ex) {
                            ex.printStackTrace();
                            pb_doubt_loading.setVisibility(View.GONE);
                            CommonMethods.showNegativeSnackbar(requireView().getRootView(), getString(R.string.something_went_wrong));
                        }
                    }, error -> {
                Log.d(TAG, "Error: " + error.toString());
                pb_doubt_loading.setVisibility(View.GONE);
                CommonMethods.showNegativeSnackbar(requireView().getRootView(), getString(R.string.something_went_wrong));
            });
            myJsonObjectRequest.setTag(TAG);
            MyVolleySingleton.getInstance(MyApplication.getInstance()).addToRequestQueue(myJsonObjectRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void checkForUserComment(String userId, List<CommentModel> list) {
        tv_no_comments.setVisibility((commentModels.isEmpty() && Boolean.TRUE.equals(viewModel.isLoadedOnce.getValue())) ? View.VISIBLE : View.GONE);
        if (isUserCommented)
            return;

        for (CommentModel commentModel : list) {
            if (commentModel != null && commentModel.getUserId() != null && commentModel.getUserId().equals(userId)) {
                isUserCommented = true;
                break;
            }
        }
        ll_bottom_layout.setVisibility(isUserCommented ? View.GONE : View.VISIBLE);
    }

    private void checkIfUserCommented(List<CommentModel> list, boolean isForceUpdate) {
        boolean isPositiveFound = false;

        for (CommentModel comment : list) {
            if (userId.equals(comment.getUserId())) {
                isPositiveFound = true;
                break;
            }
        }

        if (commentModels.isEmpty()) {
            isForceUpdate = true;
        }
        tv_no_comments.setVisibility((commentModels.isEmpty() && Boolean.TRUE.equals(viewModel.isLoadedOnce.getValue())) ? View.VISIBLE : View.GONE);
        viewModel.setPositiveIfTrueFound(isPositiveFound || !userCommentAllowedInitially, isForceUpdate);

    }

    private void sendComment(String message) {
        Context safeContext = getContext();
        View rootView = getView();

        if (CommonMethods.isOnline(requireContext())) {
            try {
                Map<String, Object> requestQueryMap = new HashMap<>();
                JSONObject object = new JSONObject();
                object.put("chapterId", chapterId);
                object.put(PreferenceKeys.MICROLECTURE_ID.getKey(), microLectureDto.getId());
                object.put("comment", message);
                if (editedCommentModel != null) {
                    object.put("id", editedCommentModel.getId());
                }
                ll_bottom_layout.setEnabled(false);
                iv_send.setVisibility(View.GONE);
                pb_send.setVisibility(View.VISIBLE);
                CommonMethods.hideKeyboard(requireContext(), et_comment);
                MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.POST,
                        CommonMethods.getUrlParam(POST_FEEDBACK_COMMENT, requestQueryMap), object,
                        response -> {
                            if (!isAdded()) return;
                            Log.d(TAG, "Response: " + response.toString());
                            TypeToken<CommentModel> token = new TypeToken<CommentModel>() {
                            };
                            CommentModel commentModel = gson.fromJson(response.toString(), token);
                            if (commentModel != null) {
                                et_comment.setText("");
                                if (editedCommentModel != null) {
                                    // For edit
                                    adapter.updateComment(commentModel);
                                } else {
                                    // for new
                                    adapter.addCommentAtTop(commentModel);
                                }
                                rv_comments.smoothScrollToPosition(0);
                                ll_bottom_layout.setVisibility(View.GONE);
//                            checkForUserComment(userId, commentModels);
                                checkIfUserCommented(new ArrayList<>(List.of(commentModel)), false);
                                if (commentConfigModel != null && !StringUtils.isEmpty(commentConfigModel.getCommentAddedMessage()) && !StringUtils.isEmpty(commentConfigModel.getCommentEditedMessage())) {
                                    CommonMethods.showPositiveSnackbar(requireView().getRootView(), editedCommentModel != null ? commentConfigModel.getCommentEditedMessage() : commentConfigModel.getCommentAddedMessage(), 3000);
                                }
                                editedCommentModel = null;
                            } else {
                                Toast.makeText(safeContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                            }
                            ll_bottom_layout.setEnabled(true);
                            iv_send.setVisibility(View.VISIBLE);
                            pb_send.setVisibility(View.GONE);
                        }, error -> {
                    if (!isAdded()) return;
                    Log.d(TAG, "Error: " + error.toString());
                    if (safeContext != null) {
                        Toast.makeText(safeContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }
                    ll_bottom_layout.setEnabled(true);
                    iv_send.setVisibility(View.VISIBLE);
                    pb_send.setVisibility(View.GONE);
                });
                myJsonObjectRequest.setTag(TAG);
                MyVolleySingleton.getInstance(MyApplication.getInstance()).addToRequestQueue(myJsonObjectRequest);

                // Firebase event
                Map<String, String> params = new HashMap<>();
                params.put("message", message);
                params.put(PreferenceKeys.MICROLECTURE_ID.getKey(), microLectureDto.getId());
                params.put("chapterId", chapterId);
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.ml_send_or_edit_comment.name(), params);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            CommonMethods.showNegativeSnackbar(requireView().getRootView(), getString(R.string.NoInternetConnection));
        }
    }

//    private void editComment(String newMessage, CommentModel commentModel) {
//        Map<String, Object> requestQueryMap = new HashMap<>();
//
//        ll_bottom_layout.setEnabled(false);
//        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
//                CommonMethods.getUrlParam(EDIT_FEEDBACK_COMMENT, requestQueryMap), null,
//                response -> {
//                    Log.d(TAG, "Response: "+response.toString());
//                    TypeToken<CallBackResponse> token = new TypeToken<CallBackResponse>() {
//                    };
//                    CallBackResponse callBackResponse = gson.fromJson(response.toString(), token);
//                    if(callBackResponse.isSuccess()){
//                        rv_comments.smoothScrollToPosition(0);
//                        commentModel.setComment(newMessage);
//                        adapter.updateComment(commentModel);
//                        rv_comments.smoothScrollToPosition(0);
//                        ll_bottom_layout.setVisibility(View.GONE);
//                    }
//                    ll_bottom_layout.setEnabled(true);
//                }, error->{
//            Log.d(TAG, "Error: "+error.toString());
//            Toast.makeText(requireContext(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
//            ll_bottom_layout.setEnabled(true);
//        });
//        myJsonObjectRequest.setTag(TAG);
//        MyVolleySingleton.getInstance(MyApplication.getInstance()).addToRequestQueue(myJsonObjectRequest);
//    }

    private void deleteComment(CommentModel commentModel) {
        if (CommonMethods.isOnline(requireContext())) {
            try {
                Map<String, Object> requestQueryMap = new HashMap<>();
                requestQueryMap.put("chapterId", chapterId);
                requestQueryMap.put(PreferenceKeys.MICROLECTURE_ID.getKey(), microLectureDto.getId());
                requestQueryMap.put("id", commentModel.getId());
                commentModel.setLoading(true);
                adapter.updateComment(commentModel);
                MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.DELETE,
                        CommonMethods.getUrlParam(DELETE_FEEDBACK_COMMENT, requestQueryMap), null,
                        response -> {
                            Log.d(TAG, "Response: " + response.toString());
                            TypeToken<CallBackResponse> token = new TypeToken<CallBackResponse>() {
                            };
                            CallBackResponse callBackResponse = gson.fromJson(response.toString(), token);
                            commentModel.setLoading(false);
                            adapter.updateComment(commentModel);
                            if (callBackResponse.isSuccess()) {
                                adapter.removeComment(commentModel);
//                        rv_comments.smoothScrollToPosition(0);
                                ll_bottom_layout.setVisibility(View.VISIBLE);
//                        checkForUserComment(userId, commentModels);
                                checkIfUserCommented(new ArrayList<>(), true);
                                if (commentConfigModel != null && !StringUtils.isEmpty(commentConfigModel.getCommentDeleted())) {
                                    CommonMethods.showPositiveSnackbar(requireView().getRootView(), commentConfigModel.getCommentDeleted(), 3000);
                                }
                            }
                        }, error -> {
                    Log.d(TAG, "Error: " + error.toString());
                    Toast.makeText(requireContext(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                });
                myJsonObjectRequest.setTag(TAG);
                MyVolleySingleton.getInstance(MyApplication.getInstance()).addToRequestQueue(myJsonObjectRequest);

                // Firebase event
                Map<String, String> params = new HashMap<>();
                params.put("message", commentModel.getComment());
                params.put(PreferenceKeys.MICROLECTURE_ID.getKey(), microLectureDto.getId());
                params.put("chapterId", chapterId);
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.ml_delete_comment.name(), params);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            CommonMethods.showNegativeSnackbar(requireView().getRootView(), getString(R.string.NoInternetConnection));
        }
    }

    private void showEditDialog(Context context, CommentEditListener listener) {
        Dialog dialog = new Dialog(context);
        try {
            dialog.setContentView(R.layout.dialog_comment_edit);
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            Objects.requireNonNull(dialog.getWindow()).getAttributes().windowAnimations = R.style.BottomDialogAnimation;
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);

            try {
                Window window = dialog.getWindow();
                WindowManager.LayoutParams params = window.getAttributes();
                params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                window.setAttributes(params);

                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            } catch (Exception ex) {
                ex.printStackTrace();
                Log.d(TAG, "Error: " + ex.getMessage());
            }

            LinearLayout ll_delete = dialog.findViewById(R.id.ll_delete);
            LinearLayout ll_edit = dialog.findViewById(R.id.ll_edit);
            ImageView iv_close = dialog.findViewById(R.id.iv_close);

            ll_delete.setOnClickListener(v -> {
                dialog.dismiss();
                listener.onDelete();
            });

            ll_edit.setOnClickListener(v -> {
                dialog.dismiss();
                listener.onEdit();
            });

            iv_close.setOnClickListener(v -> {
                dialog.dismiss();
            });

            dialog.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.d("GlobalVariable", "Error : " + ex.getMessage());
        }
    }

    public interface CommentEditListener {
        void onEdit();

        void onDelete();
    }

    public static String getTimeAgo(long pastMillis) {
        long now = System.currentTimeMillis();
        long diff = now - pastMillis;

        if (diff < 0) return "just now";

        long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        long days = TimeUnit.MILLISECONDS.toDays(diff);

        if (hours < 24) {
            return hours + " hr" + (hours == 1 ? "" : "s") + " ago";
        } else if (days < 7) {
            return days + " day" + (days == 1 ? "" : "s") + " ago";
        } else if (days < 30) {
            long weeks = days / 7;
            return weeks + " wk" + (weeks == 1 ? "" : "s") + " ago";
        } else if (days < 365) {
            long months = days / 30;
            return months + " mo" + (months == 1 ? "" : "s") + " ago";
        } else {
            long years = days / 365;
            return years + " yr" + (years == 1 ? "" : "s") + " ago";
        }
    }

}
