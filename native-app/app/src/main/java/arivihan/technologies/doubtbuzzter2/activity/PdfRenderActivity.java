package arivihan.technologies.doubtbuzzter2.activity;

/*
 * File: PdfRenderActivity Copyright 18/08/25, 9:58 am Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 18/08/25, 9:58 am
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import arivihan.technologies.doubtbuzzter2.R;

public class PdfRenderActivity extends AppCompatActivity {
    private ImageView pdfImageView;
    private PdfRenderer pdfRenderer;
    private PdfRenderer.Page currentPage;
    private ParcelFileDescriptor parcelFileDescriptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_render);

        pdfImageView = findViewById(R.id.pdfImageView);

        try {
            openRenderer();
            showPage(0); // Show first page
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openRenderer() throws IOException {
        // Copy PDF from assets to cache so it can be opened with ParcelFileDescriptor
        File file = new File(getCacheDir(), "sample.pdf");
        if (!file.exists()) {
            InputStream asset = getAssets().open("sample.pdf");
            FileOutputStream output = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int size;
            while ((size = asset.read(buffer)) != -1) {
                output.write(buffer, 0, size);
            }
            asset.close();
            output.close();
        }

        parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        pdfRenderer = new PdfRenderer(parcelFileDescriptor);
    }

    private void showPage(int index) {
        if (pdfRenderer.getPageCount() <= index) return;

        // Close current page if open
        if (currentPage != null) {
            currentPage.close();
        }

        currentPage = pdfRenderer.openPage(index);

        // Create bitmap for rendering
        Bitmap bitmap = Bitmap.createBitmap(
                currentPage.getWidth(),
                currentPage.getHeight(),
                Bitmap.Config.ARGB_8888
        );

        // Render page into bitmap
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

        // Show bitmap in ImageView
        pdfImageView.setImageBitmap(bitmap);
    }

    @Override
    protected void onDestroy() {
        try {
            if (currentPage != null) currentPage.close();
            if (pdfRenderer != null) pdfRenderer.close();
            if (parcelFileDescriptor != null) parcelFileDescriptor.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
