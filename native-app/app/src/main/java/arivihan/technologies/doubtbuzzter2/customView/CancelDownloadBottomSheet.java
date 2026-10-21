package arivihan.technologies.doubtbuzzter2.customView;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.MicrolectureListActivity;

public class CancelDownloadBottomSheet extends BottomSheetDialogFragment {

    TextView download_progress_text;
    TextView micro_lecture_title;
    ImageView close;
    LinearLayout cancel_download;
    Context context;
    CancelDownloadBottomSheetListener bottomSheetListener;
    String microLectureTitle;
    int progress;
    String microLectureId;

    public CancelDownloadBottomSheet(Context context, CancelDownloadBottomSheetListener bottomSheetListener,
                                       String microLectureTitle, int progress,String microLectureId) {
        this.bottomSheetListener = bottomSheetListener;
        this.context = context;
        this.microLectureTitle = microLectureTitle;
        this.progress = progress;
        this.microLectureId=microLectureId;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.BottomSheetDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cancel_download_layout, container, false);
        download_progress_text = view.findViewById(R.id.download_progress_text);
        micro_lecture_title = view.findViewById(R.id.micro_lecture_title);
        close = view.findViewById(R.id.close);
        cancel_download = view.findViewById(R.id.cancel_download);
        micro_lecture_title.setText(microLectureTitle);
        updateProgress(progress);
        close.setOnClickListener(view1 -> bottomSheetListener.onCloseClick());
        cancel_download.setOnClickListener(view12 -> {
            bottomSheetListener.onCancelClick(microLectureId);
        });
        return view;
    }

    public void updateProgress(int progress){
        download_progress_text.setText(progress+"%");
    }

    public interface CancelDownloadBottomSheetListener {
        void onCancelClick(String microLectrueId);
        void onCloseClick();
    }


}
