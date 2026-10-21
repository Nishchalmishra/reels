package arivihan.technologies.doubtbuzzter2.customView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;

import arivihan.technologies.doubtbuzzter2.R;

public class SavBottomSheetDialog extends BottomSheetDialogFragment {

    public static final String TAG = SavBottomSheetDialog.class.getSimpleName();
    TextView bottomSheetTitle,button_one_text, button_two_text;
    LottieAnimationView bottomSheetAnimationView;
    TextView bottomSheetSubtitle;
    int index = 0;
    CardView closeBottomSheet;
    Handler handler;
    Runnable runnable;
    boolean error;
    boolean backendError;
    boolean textExtracted;
    boolean textNotFound;
    String finalText;
    String textList;
    ArrayList<String> textArrayList = new ArrayList<>();
    LinearLayout bottomLayout;
    CardView openGallery;
    CardView clickAgain, button_one, button_two;
    BottomSheetListener bottomSheetListener;
    ImageView close;
    Handler extractionHandler;
    Runnable extractionRunnable;

    public SavBottomSheetDialog(BottomSheetListener bottomSheetListener) {
        this.bottomSheetListener = bottomSheetListener;
    }

    public SavBottomSheetDialog() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.BottomSheetDialog);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        removeCallback();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sav_bottom_sheet, container, false);
        bottomSheetTitle = view.findViewById(R.id.title);
        button_one_text = view.findViewById(R.id.btn1_text);
        button_two_text = view.findViewById(R.id.btn2_text);
        button_one = view.findViewById(R.id.btn1);
        button_two = view.findViewById(R.id.btn2);
        bottomSheetAnimationView = view.findViewById(R.id.animation);
        bottomSheetSubtitle = view.findViewById(R.id.subtitle);
        bottomLayout = view.findViewById(R.id.bottom_layout);
        close = view.findViewById(R.id.close_btn);
        close.setOnClickListener(v -> {
            if (bottomSheetListener != null) {
                bottomSheetListener.onCloseClick();
            }
        });
        openGallery = view.findViewById(R.id.btn1);
        openGallery.setOnClickListener(v -> {
                Log.d(TAG, "onCreateView clicked : ");
            if (bottomSheetListener != null) {
                bottomSheetListener.onGalleryClick(textNotFound);
            }
        });
        clickAgain = view.findViewById(R.id.btn2);
        clickAgain.setOnClickListener(v -> {
            Log.d(TAG, "onCreateView: ");
            if (bottomSheetListener != null) {
                Log.d(TAG, "onCreateView clickedinside: ");
                bottomSheetListener.onPictureClick(textNotFound);
            }
        });
        closeBottomSheet = view.findViewById(R.id.close_dialog_card);
        closeBottomSheet.setOnClickListener(v -> {
            if (isAdded()) {
                this.dismissAllowingStateLoss();
            }
        });

        error = getArguments().getBoolean("error");
        backendError = getArguments().getBoolean("backendError");
        textExtracted = getArguments().getBoolean("textExtracted");
        finalText = getArguments().getString("finalText");
        textList = getArguments().getString("textList");
        textArrayList = textList != null ? new ArrayList<>(Arrays.asList(textList.split("::"))) : new ArrayList<>();
        Log.d("BackEndError", "onCreateView:  error" + error + " " + "backendError" + backendError);
        handleBottomSheet(error, textExtracted, finalText, view);
        return view;
    }

    private void handleBottomSheet(boolean networkError, boolean textExtracted, String finalText, View view) {
        ArrayList<String> imageExtractionTextList = new ArrayList<>();
        index = 0;
        if (backendError) {
            Log.d("BackEndError", "handleBottomSheet backend Error: ");
            textNotFound = false;
            close.setVisibility(View.GONE);
            removeCallback();

            bottomSheetTitle.setText(getString(R.string.Oops_Something_went_wrong));
            bottomSheetSubtitle.setText(R.string.backend_error_message);
            bottomSheetAnimationView.setAnimation(R.raw.oops_pop);
            bottomLayout.setVisibility(View.VISIBLE);
            button_one_text.setText(R.string.try_again_later);
            button_two_text.setText(getResources().getString(R.string.contact_us));

            button_one.setOnClickListener(v -> {
                removeCallback();
                if (isAdded()) {
                    this.dismissAllowingStateLoss();
                }
            });
            button_two.setOnClickListener(v -> {
                removeCallback();
                if (isAdded()) {
                    this.dismissAllowingStateLoss();
                }
                showContactUsSheet();
            });

        } else if (!networkError) {
            if (textExtracted && StringUtils.isNotEmpty(finalText)) {
                textNotFound = false;
                close.setVisibility(View.VISIBLE);
                if(textArrayList.isEmpty()) {
                    imageExtractionTextList.add(getResources().getString(R.string.text_extraction_3));
                    imageExtractionTextList.add(getResources().getString(R.string.text_extraction_6));
                    imageExtractionTextList.add(getResources().getString(R.string.text_extraction_4));
                    imageExtractionTextList.add(getResources().getString(R.string.text_extraction_5));
                    imageExtractionTextList.add(getResources().getString(R.string.text_extraction_7));
                    imageExtractionTextList.add(getResources().getString(R.string.text_extraction_8));
                    imageExtractionTextList.add(getResources().getString(R.string.text_extraction_9));
                } else {
                    imageExtractionTextList.addAll(textArrayList);
                }
                bottomSheetTitle.setText(getString(R.string.Checking_your_answer));
                bottomSheetAnimationView.setScaleX(0.8f);
                bottomSheetAnimationView.setScaleY(0.8f);
                bottomSheetAnimationView.setAnimation(R.raw.text_analysis);
                closeBottomSheet.setVisibility(View.GONE);
                bottomLayout.setVisibility(View.GONE);
                bottomSheetSubtitle.setText(imageExtractionTextList.get(index));
                startAnimation();
                handler = new Handler();
                runnable = () -> {
                    if (index < imageExtractionTextList.size() - 1) {
                        index++;
                    } else
                        index = 0;
                    bottomSheetSubtitle.setText(imageExtractionTextList.get(index));
                    startAnimation();
                    handler.postDelayed(runnable, 3000);
                };
                handler.postDelayed(runnable, 3000);
            } else if (textExtracted && StringUtils.isEmpty(finalText)) {
                close.setVisibility(View.GONE);
                bottomSheetAnimationView.setAnimation(R.raw.image_extraction_failed);
                closeBottomSheet.setVisibility(View.GONE);
                bottomSheetTitle.setText(getString(R.string.Text_not_found));
                bottomSheetSubtitle.setText(getString(R.string.Apki_image_mein_text_nhi_mila));
                bottomLayout.setVisibility(View.VISIBLE);
                textNotFound = true;
            } else {
                textNotFound = false;
                close.setVisibility(View.GONE);
                if(textArrayList.isEmpty()) {
                    imageExtractionTextList.add(getResources().getString(R.string.text_extraction_1));
                    imageExtractionTextList.add(getResources().getString(R.string.text_extraction_2));
                    imageExtractionTextList.add(getResources().getString(R.string.text_extraction_10));
                    imageExtractionTextList.add(getResources().getString(R.string.text_extraction_4));
                    imageExtractionTextList.add(getResources().getString(R.string.text_extraction_5));
                } else {
                    imageExtractionTextList.addAll(textArrayList);
                }
                bottomSheetAnimationView.setAnimation(R.raw.reattempt);
                closeBottomSheet.setVisibility(View.GONE);
                bottomLayout.setVisibility(View.GONE);
                bottomSheetSubtitle.setText(imageExtractionTextList.get(index));
                startAnimation();
                handler = new Handler();
                runnable = () -> {
                    if (index < imageExtractionTextList.size() - 1) {
                        index++;
                    } else
                        index = 0;
                    bottomSheetSubtitle.setText(imageExtractionTextList.get(index));
                    startAnimation();
                    handler.postDelayed(runnable, 3000);
                };
                handler.postDelayed(runnable, 3000);
                extractionHandler = new Handler();
                extractionRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (isAdded()) {
                            close.setVisibility(View.VISIBLE);
                        }
                    }
                };
                extractionHandler.postDelayed(extractionRunnable, 60000);
            }
        } else {
            textNotFound = false;
            close.setVisibility(View.GONE);
            closeBottomSheet.setVisibility(View.VISIBLE);
            removeCallback();

            bottomSheetTitle.setText(getString(R.string.Oops_Something_went_wrong));
            bottomSheetSubtitle.setText(getString(R.string.You_might_have_internet_issue));
            bottomSheetAnimationView.setAnimation(R.raw.internet_issue);
        }
    }

    public void removeCallback() {
        try {
            if (handler != null) {
                handler.removeCallbacksAndMessages(null);
            }
            if (extractionHandler != null) {
                extractionHandler.removeCallbacksAndMessages(null);
            }
        } catch (Exception e) {
            Log.e("SavBottomSheet", "Error removing callbacks", e);
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        dismissAllowingStateLoss();
        removeCallback();
        if (bottomSheetListener != null)
            bottomSheetListener.onDismiss();
    }

    private void startAnimation() {
        if (!isAdded()) return;
        final Animation fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_from_left);
        bottomSheetSubtitle.startAnimation(fadeIn);
    }

    public interface BottomSheetListener {
        void onGalleryClick(boolean textNotFound);

        void onPictureClick(boolean textNotFound);

        void onCloseClick();

        default void onDismiss() {
        }
    }
    private void showContactUsSheet() {
        if (!isAdded() || getActivity() == null) return;
        dismissAllowingStateLoss();
        ContactUsBottomSheetFragment contactSheet = new ContactUsBottomSheetFragment();
        contactSheet.show(
                requireActivity().getSupportFragmentManager(),
                "contact_bottom_sheet"
        );
    }


}