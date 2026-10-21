package arivihan.technologies.doubtbuzzter2.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import arivihan.technologies.doubtbuzzter2.R;

public class ZoomImageActivity extends AppCompatActivity {

    String Url = "";
    ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoom_image);

        Url = getIntent().getStringExtra("Url");
        Log.i("URL","   "+Url);

        image = findViewById(R.id.full_image);

        Glide.with(this).load(Url).into(image);
    }
}