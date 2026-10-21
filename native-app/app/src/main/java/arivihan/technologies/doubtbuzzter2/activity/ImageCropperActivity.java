package arivihan.technologies.doubtbuzzter2.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.ImagePagerAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.ImagePeekCropAdapter;
import arivihan.technologies.doubtbuzzter2.model.CropImageDto;

public class ImageCropperActivity extends BaseActivity implements ImagePeekCropAdapter.PeekListener {

    private ArrayList<CropImageDto> urls = new ArrayList<>();
    ImagePagerAdapter adapter;
    ImagePeekCropAdapter imagePeekCropAdapter;
    private int previousPosition = -1;
    private ViewPager viewPager;

    private ActivityResultLauncher<Intent> cropImageLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<PickVisualMediaRequest> pickImagesLauncher;

    private int croppedIndex;
    private int allowedImages;
    private int remainingImages;
    private String allowedErrorText;
    FloatingActionButton cropDone;
    String imageUri;
    private MaterialCardView question_view;
    private EditText question;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_cropper);

        allowedImages = getIntent().getIntExtra("allowedImages", 1);
        remainingImages = getIntent().getIntExtra("remainingImages", 0);
        allowedErrorText = getIntent().getStringExtra("errorText");

        viewPager = findViewById(R.id.viewPager);
        cropDone = findViewById(R.id.crop_done);
        question_view = findViewById(R.id.question_view);
        question = findViewById(R.id.question);

        cropDone.setOnClickListener(view -> {
            ArrayList<String> urlList = generateListOfFileUri(urls);
            Intent returnIntent = new Intent();
            returnIntent.putStringArrayListExtra("url_list", urlList);
            returnIntent.putExtra("question", question.getText().toString());
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        });

        adapter = new ImagePagerAdapter(getSupportFragmentManager(), urls);
        viewPager.setAdapter(adapter);

        cropImageLauncher();
        initializePickImagesLauncher();
        initializeGalleryLauncher();

        RecyclerView recyclerView = findViewById(R.id.peek_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        imagePeekCropAdapter = new ImagePeekCropAdapter(urls, this, this);
        recyclerView.setAdapter(imagePeekCropAdapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                for (CropImageDto dto : urls) dto.setSelected(false);
                urls.get(position).setSelected(true);
                previousPosition = position;
                adapter.notifyDataSetChanged();
                imagePeekCropAdapter.notifyDataSetChanged();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        findViewById(R.id.image_crop_btn).setOnClickListener(view -> {
            if (!urls.isEmpty()) {
                int currentItem = viewPager.getCurrentItem();
                startCropActivity(Uri.parse(urls.get(currentItem).getUrl()), currentItem);
            }
        });

        imageUri = getIntent().getStringExtra("imageUri");

        if (imageUri != null) handleDoubtFlow();
        else openGalleryCompat();
    }

    /**
     * Handle single image from doubt flow
     */
    private void handleDoubtFlow() {
        question_view.setVisibility(View.VISIBLE);
        urls.add(CropImageDto.builder().url(imageUri).build());
        if (!urls.isEmpty()) urls.get(0).setSelected(true);
        adapter.notifyDataSetChanged();
        imagePeekCropAdapter.notifyDataSetChanged();
    }

    /**
     * Generate list of file URI strings
     */
    private ArrayList<String> generateListOfFileUri(ArrayList<CropImageDto> urls) {
        ArrayList<String> uris = new ArrayList<>();
        for (CropImageDto url : urls) {
            Uri contentUri = Uri.parse(url.getUrl());
            File file = copyUriToFile(this, contentUri);
            if (file != null && file.exists()) uris.add("file://" + file.getAbsolutePath());
        }
        return uris;
    }

    @Override
    public void deletePeekImage(int pos) {
        urls.remove(pos);
        if (CollectionUtils.isEmpty(urls)) openGalleryCompat();
        if (pos < urls.size()) urls.get(pos).setSelected(true);
        adapter.notifyDataSetChanged();
        imagePeekCropAdapter.notifyDataSetChanged();
    }

    @Override
    public void changePosition(int position) {
        viewPager.setCurrentItem(position);
        if (previousPosition != -1 && previousPosition != position)
            urls.get(previousPosition).setSelected(false);
        urls.get(position).setSelected(true);
        previousPosition = position;
        adapter.notifyDataSetChanged();
        imagePeekCropAdapter.notifyDataSetChanged();
    }

    /**
     * Launch crop activity
     */
    private void startCropActivity(Uri imageUri, int index) {
        Intent cropIntent = CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                .getIntent(this);
        cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        cropImageLauncher.launch(cropIntent);
        croppedIndex = index;
    }

    /**
     * Crop launcher
     */
    @SuppressLint("NotifyDataSetChanged")
    private void cropImageLauncher() {
        cropImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (isFinishing() || isDestroyed()) return;
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data == null) return;
                        CropImage.ActivityResult cropResult = CropImage.getActivityResult(data);
                        if (cropResult != null) {
                            Uri croppedImageUri = cropResult.getUri();
                            if (croppedImageUri != null && croppedIndex >= 0 && croppedIndex < urls.size()) {
                                urls.get(croppedIndex).setUrl(String.valueOf(croppedImageUri));
                                adapter.notifyDataSetChanged();
                                imagePeekCropAdapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(this, getString(R.string.size_issue), Toast.LENGTH_LONG).show();
                            }
                        }
                    } else
                        Toast.makeText(this, getString(R.string.cropping_failed), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Initialize PickMultipleVisualMedia launcher for API 33+
     */
    private void initializePickImagesLauncher() {
        pickImagesLauncher = registerForActivityResult(
                new ActivityResultContracts.PickMultipleVisualMedia(),
                uris -> {
                    if (uris != null && !uris.isEmpty()) {
                        question_view.setVisibility(View.GONE);
                        int maxSelectable = allowedImages - remainingImages;
                        int count = Math.min(uris.size(), maxSelectable);

                        for (int i = 0; i < count; i++) {
                            urls.add(CropImageDto.builder().url(String.valueOf(uris.get(i))).build());
                        }

                        if (uris.size() > maxSelectable) {
                            Toast.makeText(this,
                                    allowedErrorText != null ?  String.format(allowedErrorText, maxSelectable) : String.format(getString(R.string.Oops_only_images_allowed), maxSelectable),
                                    Toast.LENGTH_LONG).show();
                        }

                        if (!urls.isEmpty()) urls.get(0).setSelected(true);
                        adapter.notifyDataSetChanged();
                        imagePeekCropAdapter.notifyDataSetChanged();

                        if (allowedImages == 1)
                            startCropActivity(Uri.parse(urls.get(0).getUrl()), 0);
                    } else {
                        if (urls.isEmpty()) finish();
                    }
                }
        );
    }

    /**
     * Initialize legacy gallery launcher for older APIs
     */
    private void initializeGalleryLauncher() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        question_view.setVisibility(View.GONE);

                        if (data.getData() != null) {
                            urls.add(CropImageDto.builder().url(String.valueOf(data.getData())).build());
                        } else if (data.getClipData() != null) {
                            ClipData clipData = data.getClipData();
                            int maxSelectable = allowedImages - remainingImages;
                            for (int i = 0; i < Math.min(clipData.getItemCount(), maxSelectable); i++) {
                                urls.add(CropImageDto.builder().url(String.valueOf(clipData.getItemAt(i).getUri())).build());
                            }
                            if (clipData.getItemCount() > maxSelectable) {
                                Toast.makeText(this,
                                        allowedErrorText != null ? String.format(allowedErrorText, maxSelectable) : String.format(getString(R.string.Oops_only_images_allowed), maxSelectable),
                                        Toast.LENGTH_LONG).show();
                            }
                        }

                        if (!urls.isEmpty()) urls.get(0).setSelected(true);
                        adapter.notifyDataSetChanged();
                        imagePeekCropAdapter.notifyDataSetChanged();

                        if (allowedImages == 1 && !urls.isEmpty())
                            startCropActivity(Uri.parse(urls.get(0).getUrl()), 0);
                    } else {
                        if (!isFinishing() && !isDestroyed()) finish();
                    }
                });
    }


    /**
     * Open gallery depending on Android version
     */
    private void openGalleryCompat() {
        if (allowedImages == 1) {
            openGalleryLegacy();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pickImagesLauncher.launch(
                        new PickVisualMediaRequest.Builder()
                                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                                .build()
                );
            } else {
                openGalleryLegacy();
            }
        }
    }


    /**
     * Legacy gallery fallback
     */
    private void openGalleryLegacy() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        if (imageUri == null) intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        galleryLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

    /**
     * Copy URI to file
     */
    public File copyUriToFile(Context context, Uri contentUri) {
        File file = null;
        try {
            String fileName = generateTempImageName();
            file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);
            InputStream inputStream = context.getContentResolver().openInputStream(contentUri);
            OutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) outputStream.write(buffer, 0, length);

            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * Generate unique temp image name
     */
    public static String generateTempImageName() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String uuid = UUID.randomUUID().toString();
        return "IMG_" + timeStamp + "_" + uuid + ".jpg";
    }
}
