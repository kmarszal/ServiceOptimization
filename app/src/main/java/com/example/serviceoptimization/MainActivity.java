package com.example.serviceoptimization;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;

import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.AndroidUtil;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.Picture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private void verifyStoragePermissions() {
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions();
    }

    public void onBtnPdfClick(View v) {
        new JpgToPdfTask().execute();
    }

    public void onBtnFramesClick(View v) {
        new GetFramesTask().execute(30);
    }

    private class JpgToPdfTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            Document document = new Document();

            String directoryPath = android.os.Environment.getExternalStorageDirectory().toString();
            try {
                PdfWriter.getInstance(document, new FileOutputStream(directoryPath + "/example.pdf"));

                document.open();

                Image image = Image.getInstance(directoryPath + "/example.jpg");

                float scaler = ((document.getPageSize().getWidth() - document.leftMargin()
                        - document.rightMargin()) / image.getWidth()) * 100;

                image.scalePercent(scaler);
                image.setAlignment(Image.ALIGN_CENTER | Image.ALIGN_TOP);

                document.add(image);
            } catch (DocumentException | IOException e) {
                Log.e("error", e.getLocalizedMessage());
                return e.getLocalizedMessage();
            }

            document.close();
            return getString(R.string.task_done);
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
        }
    }

    private class GetFramesTask extends AsyncTask<Integer, Void, String> {
        @Override
        protected String doInBackground(Integer... frames) {
            String directoryPath = android.os.Environment.getExternalStorageDirectory().toString();
            SeekableByteChannel ch = null;
            try {
                File file = new File(directoryPath + "/example.mp4");
                ch = NIOUtils.readableChannel(file);
                FrameGrab grab = FrameGrab.createFrameGrab(ch);
                Picture picture;
                int frameNumber = 0;
                while (null != (picture = grab.getNativeFrame())) {
                    if(++frameNumber % frames[0] == 0) {
                        Bitmap bitmap = AndroidUtil.toBitmap(picture);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(String.format("%s/frame%d.png", directoryPath, frameNumber)));
                    }
                }
            } catch (JCodecException | IOException e) {
                Log.e("error", e.getLocalizedMessage());
                return e.getLocalizedMessage();
            } finally {
                NIOUtils.closeQuietly(ch);
            }
            return getString(R.string.task_done);
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
        }
    }
}
