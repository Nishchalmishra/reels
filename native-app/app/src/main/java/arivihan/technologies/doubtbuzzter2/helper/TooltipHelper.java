package arivihan.technologies.doubtbuzzter2.helper;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.view.View;
import android.widget.ScrollView;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.customView.HowToVideoView;
import arivihan.technologies.doubtbuzzter2.customView.MyToolTip;
import arivihan.technologies.doubtbuzzter2.model.MyTooltipDto;
import arivihan.technologies.doubtbuzzter2.model.TooltipStatusDto;
import arivihan.technologies.doubtbuzzter2.model.TooltipType;

public class TooltipHelper {

    private static TooltipHelper tooltipHelper;

    private TooltipHelper() {
    }

    public static TooltipHelper getInstance() {
        if (tooltipHelper == null) {
            tooltipHelper = new TooltipHelper();
        }
        return tooltipHelper;
    }

    private Handler tooltipHandler;
    public  MyToolTip myToolTip;
    private int visitCount;
    private MyTooltipDto myTooltipDto;
    private boolean isTest;
    private Context context;

    public void handleTooltip(ScrollView scrollView, View view, MyTooltipDto myTooltipDto,
                              MyToolTip.ToolTipClickListener toolTipClickListener, Context context) {
        dismiss();
        this.myTooltipDto = myTooltipDto;
        this.isTest = myTooltipDto.isTest();
        this.context = context;
        visitCount = MyApplication.getIntData(getKey(myTooltipDto.getTooltipType(), context));
        handleAccordingToType(myTooltipDto, view, scrollView, toolTipClickListener);
    }

    public boolean countNotCompleted(TooltipType tooltipType,Context context) {
        visitCount = MyApplication.getIntData(getKey(tooltipType, context));
        if (visitCount < 0)
            visitCount = 1;
        return visitCount <= 2;
    }

    private void handleAccordingToType(MyTooltipDto myTooltipDto, View view, ScrollView scrollView,
                                       MyToolTip.ToolTipClickListener toolTipClickListener) {

        switch (myTooltipDto.getTooltipType()) {
            case CLICK_HERE,OPTION -> {
                if (isViewFullyVisibleInScrollView(view, scrollView)) {
                    myTooltipDto.setStepCount(1);
                    myTooltipDto.setTotalSteps(1);
                    startHandler(myTooltipDto, view, toolTipClickListener);
                }
            }
            case UPLOAD -> {
                int stepCount = 0;
                TooltipStatusDto tooltipStatusDto = MyApplication.getInstance().getTooltipViewModel().getUploadTooltipStatus().getValue();
                if(tooltipStatusDto!=null)
                 stepCount = tooltipStatusDto.getStepCount();
                if (stepCount == myTooltipDto.getStepCount() || stepCount==0) {
                    if (isViewFullyVisibleInScrollView(view, scrollView) || myTooltipDto.getStepCount() == myTooltipDto.getTotalSteps()) {
                        myTooltipDto.setText(createToolTipMessageAfterUpload(myTooltipDto.getStepCount(), myTooltipDto.getTotalSteps(), context));
                        myTooltipDto.setTitleText(createToolTipTitleAfterUpload(myTooltipDto.getStepCount(), context));
                        startHandler(myTooltipDto, view, toolTipClickListener);
                    }
                }
            }
            case REVIEW -> {
                if (myTooltipDto.getStepCount() == 5 || isViewFullyVisibleInScrollView(view, scrollView)) {
                    MyApplication.getInstance().getTooltipViewModel().getReviewTooltipStatus().getValue().setStepCount(myTooltipDto.getStepCount());
                    if (visitCount == 2)
                        myTooltipDto.setShowSkip(true);
                    myTooltipDto.setText(createToolTipMessageReviewMode(myTooltipDto.getStepCount(), context));
                    myTooltipDto.setTitleText(createToolTipTitleReviewMode(myTooltipDto.getStepCount(), context));
                    if (myTooltipDto.getStepCount() == 5) {
                        myTooltipDto.setNextText(context.getString(R.string.Begin));
                        myTooltipDto.setShowSkip(false);
                    }
                    startHandler(myTooltipDto, view, toolTipClickListener);
                }
            }

            case SUBMIT -> {
                myTooltipDto.setStepCount(1);
                myTooltipDto.setTotalSteps(1);
                startHandler(myTooltipDto, view, toolTipClickListener);
            }
        }
    }

    private void startHandler(MyTooltipDto myTooltipDto, View view, MyToolTip.ToolTipClickListener toolTipClickListener) {
        tooltipHandler = new Handler();
        Runnable tooltipRunnable = () -> {
            showTooltip(view, myTooltipDto, toolTipClickListener);
            updateStatus();
            tooltipHandler.removeCallbacksAndMessages(null);
        };
        tooltipHandler.postDelayed(tooltipRunnable, 200);
    }

    private void updateStatus() {
        switch (myTooltipDto.getTooltipType()) {
            case OPTION -> {
                if(isTest){
                    MyApplication.getInstance().getTooltipViewModel().getOptionTooltipStatus().setValue(getTooltipStatusDto());
                    finish(context, TooltipType.OPTION);
                }else {
                    MyApplication.getInstance().getTooltipViewModel().getClickTooltipStatus().setValue(getTooltipStatusDto());
                    finish(context, TooltipType.OPTION);
                }
            }
            case CLICK_HERE -> {
                MyApplication.getInstance().getTooltipViewModel().getClickTooltipStatus().setValue(getTooltipStatusDto());
                finish(context,TooltipType.CLICK_HERE);
            }
            case UPLOAD -> MyApplication.getInstance().getTooltipViewModel().getUploadTooltipStatus().setValue(getTooltipStatusDto());
            case REVIEW -> MyApplication.getInstance().getTooltipViewModel().getReviewTooltipStatus().setValue(getTooltipStatusDto());
            case SUBMIT -> {
                MyApplication.getInstance().getTooltipViewModel().getSubmitTooltipStatus().setValue(getTooltipStatusDto());
                finish(context,TooltipType.SUBMIT);
            }
        }
    }

    private TooltipStatusDto getTooltipStatusDto() {
        TooltipStatusDto tooltipStatusDto = new TooltipStatusDto();
        tooltipStatusDto.setTooltipType(myTooltipDto.getTooltipType());
        tooltipStatusDto.setStepCount(myTooltipDto.getStepCount());
        tooltipStatusDto.setTotalSteps(myTooltipDto.getTotalSteps());
        tooltipStatusDto.setActive(true);
        tooltipStatusDto.setFragment(myTooltipDto.getFragmentNumber());
        tooltipStatusDto.setCurrentFragment(MyApplication.getInstance().getTooltipViewModel().getCurrentFragment());
        return tooltipStatusDto;
    }

    private String createToolTipMessageAfterUpload(int stepCount, int totalSteps, Context context) {
        String messageKey = context.getString(R.string.Step) + "_" + stepCount + "_" + totalSteps;
        int resId = context.getResources().getIdentifier(messageKey, "string", context.getPackageName());
        if (resId != 0)
            return context.getString(resId);
        return null;
    }

    private String createToolTipTitleAfterUpload(int stepCount, Context context) {
        return context.getString(R.string.Step_) + stepCount;
    }


    private String createToolTipMessageReviewMode(int stepCount, Context context) {
        String messageKey = context.getString(R.string.Step) + "_" + stepCount;
        int resId = context.getResources().getIdentifier(messageKey, "string", context.getPackageName());
        if (resId != 0)
            return context.getString(resId);
        return null;
    }

    private String createToolTipTitleReviewMode(int stepCount, Context context) {
        String titleKey = context.getString(R.string.Step) + "_" + stepCount + "_" + 5;
        int resId = context.getResources().getIdentifier(titleKey, "string", context.getPackageName());
        if (resId != 0)
            return context.getString(resId);
        return null;
    }

    public boolean isViewFullyVisibleInScrollView(View view, ScrollView scrollView) {
        boolean contains = false;
        if (view == null || scrollView == null) {
            return false;
        }
        Rect scrollViewRect = new Rect();
        boolean scrollViewVisible = scrollView.getGlobalVisibleRect(scrollViewRect);
        if (!scrollViewVisible) {
            return contains;
        }
        Rect viewRect = new Rect();
        boolean viewVisible = view.getGlobalVisibleRect(viewRect);

        if (!viewVisible) {
            return contains;
        }
        contains = scrollViewRect.contains(viewRect);
        return contains;
    }

    public void showTooltip(View view, MyTooltipDto myTooltipDto, MyToolTip.ToolTipClickListener toolTipClickListener) {
        myToolTip = new MyToolTip(view, toolTipClickListener);
        myToolTip.showWithAnchorView(view, myTooltipDto, true);
    }

    public void onHowToRemove() {
        if(myToolTip!=null && myTooltipDto!=null) {
            myToolTip.checkForContentVisibility();
            myToolTip.checkForAutoDismiss(myTooltipDto.isAutoDismiss());
        }
    }


    public void finish(Context context, TooltipType tooltipType) {
        visitCount = MyApplication.getIntData(getKey(tooltipType, context));
        if(visitCount<0)
            visitCount=1;
        MyApplication.saveData(getKey(tooltipType, context), visitCount + 1);
    }


    private String getKey(TooltipType tooltipType, Context context) {
        String key = null;
        switch (tooltipType) {
            case OPTION -> {
                if(isTest) {
                    key = context.getString(R.string.OptionTooltipCountTest);
                }else{
                    key = context.getString(R.string.OptionTooltipCount);
                }
            }
            case CLICK_HERE -> {
                if(isTest) {
                    key = context.getString(R.string.ClickHereTooltipCountTest);
                }else{key = context.getString(R.string.ClickHereTooltipCount);}
            }
            case UPLOAD -> {
                if(isTest) {
                    key = context.getString(R.string.ThreeStepTooltipCountTest);
                }else{key = context.getString(R.string.ThreeStepTooltipCount);}
            }
            case REVIEW -> {
                if(isTest) {
                    key = context.getString(R.string.FiveStepTooltipCountTest);
                }else{key = context.getString(R.string.FiveStepTooltipCount);}
            }
            case SUBMIT -> {
                if(isTest) {
                    key = context.getString(R.string.SubmitTooltipCountTest);
                }else{ key = context.getString(R.string.SubmitTooltipCount);}
            }
        }
        return key;
    }

    public void dismiss() {
        if (tooltipHandler != null)
            tooltipHandler.removeCallbacksAndMessages(null);
        if (myToolTip != null)
            myToolTip.dismiss();
    }

    public boolean isTooltipCompleted(Context context, boolean isTest){
        this.isTest = isTest;
        return MyApplication.getIntData(getKey(TooltipType.OPTION,context))>2 &&
                MyApplication.getIntData(getKey(TooltipType.CLICK_HERE,context))>2 &&
                MyApplication.getIntData(getKey(TooltipType.UPLOAD,context))>2 &&
                MyApplication.getIntData(getKey(TooltipType.REVIEW,context))>2;
    }
}
