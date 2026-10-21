package arivihan.technologies.doubtbuzzter2.helper;

import android.app.Dialog;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import arivihan.technologies.doubtbuzzter2.R;

public class GlobalDialogs {

    public static void fullScreenImageViewDialog(Context context, String imageUrl) {

        Dialog solutionDialog = new Dialog(context);
        solutionDialog.setContentView(R.layout.dialog_fullscreen_mock_test_question_image_view);
        solutionDialog.getWindow().setBackgroundDrawable(context.getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        solutionDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        solutionDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        solutionDialog.setCancelable(false);

        ImageView imgView = solutionDialog.findViewById(R.id.fullscreen_image);
        Glide.with(context).load(imageUrl).into(imgView);

        solutionDialog.findViewById(R.id.close_btn).setOnClickListener(v -> solutionDialog.dismiss());

        solutionDialog.show();

    }
}
