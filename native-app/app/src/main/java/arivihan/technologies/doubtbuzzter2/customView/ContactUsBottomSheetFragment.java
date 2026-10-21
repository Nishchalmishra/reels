package arivihan.technologies.doubtbuzzter2.customView;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;

public class ContactUsBottomSheetFragment extends BottomSheetDialogFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.BottomSheetDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_us, container, false);

        CardView instagram = view.findViewById(R.id.instagram);
        CardView whatsapp = view.findViewById(R.id.whatsapp);
        CardView gmail = view.findViewById(R.id.gmail);
        CardView youtube = view.findViewById(R.id.youtube);

        instagram.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(MyApplication.getInstagramUrl()));
                startActivity(intent);

                Bundle contactBundle = new Bundle();
                contactBundle.putString("type", "Instagram");
                MyApplication.logFirebaseEvent("click_profile_contact", contactBundle);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Unable to open Instagram", Toast.LENGTH_SHORT).show();
            }
            dismiss();
        });

        whatsapp.setOnClickListener(v -> {
            try {
                String url = "https://api.whatsapp.com/send?phone=" + MyApplication.getPhoneNumberWith91();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setPackage("com.whatsapp");
                intent.setData(Uri.parse(url));
                startActivity(intent);

                Bundle contactBundle = new Bundle();
                contactBundle.putString("type", "Whatsapp");
                MyApplication.logFirebaseEvent("click_profile_contact", contactBundle);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(requireContext(), R.string.whatsapp_not_found, Toast.LENGTH_SHORT).show();
            }
            dismiss();
        });

        gmail.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_SEND);
                String[] recipients = {"support@arivihan.com"};
                intent.putExtra(Intent.EXTRA_EMAIL, recipients);
                intent.setType("text/html");
                intent.setPackage("com.google.android.gm");
                startActivity(intent);

                Bundle contactBundle = new Bundle();
                contactBundle.putString("type", "Email");
                MyApplication.logFirebaseEvent("click_profile_contact", contactBundle);
            } catch (Exception e) {
                // Fallback to regular email intent
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:support@arivihan.com"));
                startActivity(Intent.createChooser(intent, "Send email"));
            }
            dismiss();
        });

        youtube.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(MyApplication.getYoutubeUrl()));
                startActivity(intent);

                Bundle contactBundle = new Bundle();
                contactBundle.putString("type", "Youtube");
                MyApplication.logFirebaseEvent("click_profile_contact", contactBundle);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Unable to open YouTube", Toast.LENGTH_SHORT).show();
            }
            dismiss();
        });

        return view;
    }
}