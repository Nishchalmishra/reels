package arivihan.technologies.doubtbuzzter2.customView;/*
 * File: MyToolTip Copyright 23/10/24, 10:41 am Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 23/10/24, 10:41 am
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.lang.ref.WeakReference;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.MyTooltipDto;
import arivihan.technologies.doubtbuzzter2.model.TooltipType;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class MyToolTip {
    private static final long DEFAULT_POPUP_DISPLAY_TIME = 4000;
    private WeakReference<View> anchorViewRef;
    private Context context;
    private PopupContentView popupContent;
    private PopupWindow popupWindow;
    private Style style = Style.BLACK;
    private long nuxDisplayTime = DEFAULT_POPUP_DISPLAY_TIME;
    private ToolTipClickListener toolTipClickListener;

    public enum Style {
        BLUE,
        BLACK
    }

    public MyToolTip(View anchor,ToolTipClickListener toolTipClickListener) {
        this.anchorViewRef = new WeakReference<>(anchor);
        this.context = anchor.getContext();
        this.toolTipClickListener=toolTipClickListener;
    }

    public MyToolTip(View anchor) {
        this.anchorViewRef = new WeakReference<>(anchor);
        this.context = anchor.getContext();
    }

    public void showWithAnchorView(View view, MyTooltipDto myTooltipDto, boolean showTooltip) {
        if(showTooltip) {
            view.post(() -> {
                try {
                    int[] location = new int[2];
                    view.getLocationOnScreen(location);
                    show(location[0], location[1], myTooltipDto);
                } catch (Exception e) {
                    Log.d("showWithAnchorView: ", e.getMessage());
                }
            });
        }
    }

    public void show(int xOffset, int yOffset, MyTooltipDto myTooltipDto) {
        if (anchorViewRef.get() != null) {

            popupContent = new PopupContentView(context);
            popupContent.bodyFrame.setOnClickListener(view -> {
                if(toolTipClickListener!=null && !myTooltipDto.isShowNext())
                  toolTipClickListener.onToolTipClick();
            });


            if(StringUtils.isNotEmpty(myTooltipDto.getTitleText())) {
                TextView title = popupContent.findViewById(R.id.title);
                title.setText(myTooltipDto.getTitleText());
                title.setVisibility(View.VISIBLE);
                if(StringUtils.isNotEmpty(myTooltipDto.getText())) {
                    TextView body = popupContent.findViewById(R.id.text_body);
                    body.setText(myTooltipDto.getText());
                    body.setVisibility(View.VISIBLE);
                }
            }else if(StringUtils.isNotEmpty(myTooltipDto.getText())) {
                TextView body = popupContent.findViewById(R.id.text_body_2);
                body.setText(myTooltipDto.getText());
                body.setVisibility(View.VISIBLE);
            }

            if(myTooltipDto.isShowNext()) {
                TextView next = popupContent.findViewById(R.id.next);
                if(StringUtils.isNotEmpty(myTooltipDto.getNextText()))
                    next.setText(myTooltipDto.getNextText());
                next.setVisibility(View.VISIBLE);
                next.setOnClickListener(view -> {
                    if(toolTipClickListener!=null)
                      toolTipClickListener.onToolTipNextClick();
                });
            }

            if(myTooltipDto.isShowSkip()) {
                TextView skip = popupContent.findViewById(R.id.skip);
                skip.setVisibility(View.VISIBLE);
                skip.setOnClickListener(view -> {
                    if(toolTipClickListener!=null)
                     toolTipClickListener.onToolTipSkipClick();
                });
            }

            // Set style (blue or black)
//            if (style == Style.BLUE) {
//                popupContent.bodyFrame.setBackgroundResource(R.drawable.com_facebook_tooltip_blue_background);
//                popupContent.bottomArrow.setImageResource(R.drawable.tip_down);
//                popupContent.topArrow.setImageResource(R.drawable.tip_up);
//            } else {
                //popupContent.bodyFrame.setBackgroundResource(R.drawable.com_facebook_tooltip_black_background);
                popupContent.bottomArrow.setImageResource(R.drawable.tip_down);
                popupContent.topArrow.setImageResource(R.drawable.tip_up);
            //}

            // Measure content size
            popupContent.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int contentWidth = popupContent.getMeasuredWidth();
            int contentHeight = popupContent.getMeasuredHeight();

            if(myTooltipDto.getAlignment()==RelativeLayout.ALIGN_PARENT_START)
                xOffset = xOffset  - anchorViewRef.get().getWidth() - 120 ;
            else if(myTooltipDto.getAlignment()==RelativeLayout.ALIGN_END)
                xOffset = xOffset + (anchorViewRef.get().getWidth() / 2) - contentWidth ;
            else if(myTooltipDto.getAlignment()==RelativeLayout.ALIGN_START)
                xOffset = xOffset + (anchorViewRef.get().getWidth() / 2) + contentWidth;
            else
                xOffset = xOffset + (anchorViewRef.get().getWidth() / 2) - (contentWidth / 2);

            // Get anchor location
            int[] anchorLocation = new int[2];
            Log.d("AnchorYLocation", "pos " + anchorLocation[0] + "pos "+ anchorLocation[1]);
            anchorViewRef.get().getLocationOnScreen(anchorLocation);

            // Create and display PopupWindow, keeping yOffset as is
            popupWindow = new PopupWindow(popupContent, contentWidth, contentHeight);

            int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
            if(myTooltipDto.isRightAnchor()){
                yOffset = yOffset - 40;
            }else if (!myTooltipDto.isAboveAnchor()) {
                // Show below the anchor
                yOffset = yOffset + anchorViewRef.get().getHeight()-myTooltipDto.getYMargin(); // Position just below the anchor
            } else {
                // Show above the anchor

                yOffset = yOffset - contentHeight + 15; // Position just above the anchor
            }
            checkForContentVisibility();
            popupWindow.showAtLocation(anchorViewRef.get(), Gravity.NO_GRAVITY, xOffset, yOffset);  // Use the centered xOffset

            startAnimation();

            if(myTooltipDto.isHideArrow())
              popupContent.hideArrow();
            else
              updateArrows(myTooltipDto.getAlignment(),myTooltipDto.isAboveAnchor(),myTooltipDto.isRightAnchor());

            checkForAutoDismiss(myTooltipDto.isAutoDismiss());

            popupWindow.setTouchable(true);
            popupContent.setOnClickListener(v -> dismiss());
        }
    }

    private void startAnimation(){
        // Add bounce animation
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                0.7f, 1.0f, 0.7f, 1.0f,  // From 70% of size to 100% size
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(1000);  // Duration of bounce effect (500ms)
        scaleAnimation.setInterpolator(new BounceInterpolator());  // Bounce effect
        if (popupContent != null) {
            popupContent.startAnimation(scaleAnimation);
        } else {
            Log.e("MyToolTip", "popupContent is null. Animation skipped.");
        }
    }

    public void checkForContentVisibility(){
        if (popupContent == null) {
            Log.e("MyToolTip", "popupContent is null in checkForContentVisibility()");
            return;
        }
        if (MyApplication.getBoolData(PreferenceKeys.HIDE_TOOLTIP_FOR_HOW_TO.getKey())) {
            popupContent.setVisibility(View.GONE);
        } else {
            startAnimation();
            popupContent.setVisibility(View.VISIBLE);
        }
    }


    public void checkForAutoDismiss(boolean isAutoDismiss){
        try {
            if(popupContent!=null) {
                if (MyApplication.getBoolData(PreferenceKeys.HIDE_TOOLTIP_FOR_HOW_TO.getKey())) {
                    popupContent.setVisibility(View.GONE);
                } else if (isAutoDismiss && nuxDisplayTime > 0) {
                    popupContent.postDelayed(this::dismiss, nuxDisplayTime);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateArrows(int alignment,boolean isAboveAnchor, boolean isRightAnchor) {
        if (popupWindow != null && popupWindow.isShowing()) {
            if(isRightAnchor){
                popupContent.showRightArrow(alignment);
            }else if (isAboveAnchor) {
                popupContent.showBottomArrow(alignment);
            } else {
                popupContent.showTopArrow(alignment);
            }
        }
    }

    public void dismiss() {
        if (popupWindow != null) {
            //toolTipClickListener.onToolTipDismiss();
            popupWindow.dismiss();
        }
    }

    private class PopupContentView extends FrameLayout {
        ImageView rightArrow;
        ImageView topArrow;
        ImageView bottomArrow;
        View bodyFrame;

        public PopupContentView(Context context) {
            super(context);
            LayoutInflater.from(context).inflate(R.layout.custom_tool_tip, this);
            rightArrow = findViewById(R.id.right_pointer);
            topArrow = findViewById(R.id.top_pointer);
            bottomArrow = findViewById(R.id.bottom_pointer);
            bodyFrame = findViewById(R.id.body_frame);
        }

        void showRightArrow(int alignment) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) rightArrow.getLayoutParams();
            params.addRule(alignment, bodyFrame.getId());
            rightArrow.setLayoutParams(params);
            rightArrow.setVisibility(VISIBLE);
            topArrow.setVisibility(INVISIBLE);
            bottomArrow.setVisibility(INVISIBLE);
        }

        void showTopArrow(int alignment) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) topArrow.getLayoutParams();
            params.addRule(alignment, bodyFrame.getId());
            topArrow.setLayoutParams(params);
            topArrow.setVisibility(VISIBLE);
            bottomArrow.setVisibility(GONE);
        }

        void showBottomArrow(int alignment) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) bottomArrow.getLayoutParams();
            params.addRule(alignment,bodyFrame.getId());
            bottomArrow.setLayoutParams(params);
            topArrow.setVisibility(GONE);
            bottomArrow.setVisibility(VISIBLE);
        }

        void hideArrow() {
            topArrow.setVisibility(GONE);
            bottomArrow.setVisibility(GONE);
        }
    }

    public interface ToolTipClickListener{
         void onToolTipClick();
         void onToolTipNextClick();
         void onToolTipSkipClick();
         default void onToolTipDismiss(){};
    }
}
