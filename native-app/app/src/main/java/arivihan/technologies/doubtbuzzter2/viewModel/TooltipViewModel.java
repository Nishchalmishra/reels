package arivihan.technologies.doubtbuzzter2.viewModel;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.helper.TooltipHelper;
import arivihan.technologies.doubtbuzzter2.model.TooltipStatusDto;
import arivihan.technologies.doubtbuzzter2.model.TooltipType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TooltipViewModel extends AndroidViewModel {

    private MutableLiveData<TooltipStatusDto> clickTooltipStatus = new MutableLiveData<>();
    private MutableLiveData<TooltipStatusDto> optionTooltipStatus = new MutableLiveData<>();
    private MutableLiveData<TooltipStatusDto> submitTooltipStatus = new MutableLiveData<>();
    private MutableLiveData<TooltipStatusDto> uploadTooltipStatus = new MutableLiveData<>();
    private MutableLiveData<TooltipStatusDto> reviewTooltipStatus = new MutableLiveData<>();
    private int currentFragment = -1;

    public TooltipViewModel(@NonNull Application application) {
        super(application);
    }

    public void updateTooltipViewModel(){
        clickTooltipStatus.setValue(new TooltipStatusDto());
        uploadTooltipStatus.setValue(new TooltipStatusDto());
        reviewTooltipStatus.setValue(new TooltipStatusDto());
        submitTooltipStatus.setValue(new TooltipStatusDto());
        optionTooltipStatus.setValue(new TooltipStatusDto());
    }

    public void updateFragment(int f, Context context) {
        currentFragment=f+1;
        TooltipStatusDto tooltipStatusDto = new TooltipStatusDto();
        if (TooltipHelper.getInstance().countNotCompleted(TooltipType.CLICK_HERE,context) ||
                TooltipHelper.getInstance().countNotCompleted(TooltipType.OPTION,context)) {
            if (clickTooltipStatus.getValue() != null) {
                tooltipStatusDto = clickTooltipStatus.getValue();
            }
            tooltipStatusDto.setCurrentFragment(currentFragment);
            clickTooltipStatus.setValue(tooltipStatusDto);
        } else {
            clickTooltipStatus.setValue(null);
        }

        if (TooltipHelper.getInstance().countNotCompleted(TooltipType.OPTION,context)) {
            if (optionTooltipStatus.getValue() != null) {
                tooltipStatusDto = optionTooltipStatus.getValue();
            }
            tooltipStatusDto.setCurrentFragment(currentFragment);
            optionTooltipStatus.setValue(tooltipStatusDto);
        } else {
            optionTooltipStatus.setValue(null);
        }

        if (TooltipHelper.getInstance().countNotCompleted(TooltipType.UPLOAD,context)) {
            if (uploadTooltipStatus.getValue() != null) {
                tooltipStatusDto = uploadTooltipStatus.getValue();
            }
            tooltipStatusDto.setCurrentFragment(currentFragment);
            uploadTooltipStatus.setValue(tooltipStatusDto);
        } else {
            uploadTooltipStatus.setValue(null);
        }
        if (TooltipHelper.getInstance().countNotCompleted(TooltipType.REVIEW,context)) {
            if (reviewTooltipStatus.getValue() != null) {
                tooltipStatusDto = reviewTooltipStatus.getValue();
            }
            tooltipStatusDto.setCurrentFragment(currentFragment);
            reviewTooltipStatus.setValue(tooltipStatusDto);
        } else {
            reviewTooltipStatus.setValue(null);
        }

        if (TooltipHelper.getInstance().countNotCompleted(TooltipType.SUBMIT,context)) {
            if (submitTooltipStatus.getValue() != null) {
                tooltipStatusDto = submitTooltipStatus.getValue();
            }
            tooltipStatusDto.setCurrentFragment(currentFragment);
            submitTooltipStatus.setValue(tooltipStatusDto);
        } else {
            submitTooltipStatus.setValue(null);
        }
    }

    public void resetUploadTooltip(Context context){
       TooltipStatusDto tooltipStatusDto = uploadTooltipStatus.getValue();
       if(TooltipHelper.getInstance().countNotCompleted(TooltipType.UPLOAD, context) &&
               tooltipStatusDto!=null && !tooltipStatusDto.isFinished() && tooltipStatusDto.isActive()) {
           TooltipHelper.getInstance().dismiss();
           tooltipStatusDto = new TooltipStatusDto();
           tooltipStatusDto.setCurrentFragment(currentFragment);
           uploadTooltipStatus.setValue(tooltipStatusDto);
       }
    }

    public void resetReviewTooltip(Context context){
       TooltipStatusDto tooltipStatusDto = reviewTooltipStatus.getValue();
       if(TooltipHelper.getInstance().countNotCompleted(TooltipType.REVIEW, context) &&
               tooltipStatusDto!=null && !tooltipStatusDto.isFinished() && tooltipStatusDto.isActive()) {
           TooltipHelper.getInstance().dismiss();
           tooltipStatusDto = new TooltipStatusDto();
           tooltipStatusDto.setCurrentFragment(currentFragment);
           reviewTooltipStatus.setValue(tooltipStatusDto);
       }
    }
}
