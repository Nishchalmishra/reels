package arivihan.technologies.doubtbuzzter2.common;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.google.android.material.card.MaterialCardView;
import arivihan.technologies.doubtbuzzter2.R;

public class PlayStoreDialog extends Dialog {

    public PlayStoreDialog(Context context) {
        super(context);
        setContentView(R.layout.no_pdf_viewer); // Ensure this layout exists
        getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        setCancelable(false);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        initializeViews();
    }

    private void initializeViews() {
        MaterialCardView playStoreLayout = findViewById(R.id.playStoreLayout);
        playStoreLayout.setOnClickListener(view -> {
            dismiss();
            Intent playStoreIntent = new Intent(Intent.ACTION_VIEW);
            playStoreIntent.setData(Uri.parse("market://details?id=com.adobe.reader"));

            if (playStoreIntent.resolveActivity(getContext().getPackageManager()) != null) {
                getContext().startActivity(playStoreIntent);
            } else {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                browserIntent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.adobe.reader"));
                if (browserIntent.resolveActivity(getContext().getPackageManager()) != null) {
                    getContext().startActivity(browserIntent);
                }
            }
        });

        ImageView cancelPopUp = findViewById(R.id.cancel_pop_up);
        cancelPopUp.setOnClickListener(v -> dismiss());
    }
    public void showPlayStoreDialog() {
        if (!isShowing()) {
            show();
        }
    }
}
