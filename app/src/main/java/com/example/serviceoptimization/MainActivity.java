package com.example.serviceoptimization;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.solver.widgets.ConstraintAnchor;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
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
import java.util.Calendar;
import java.util.Random;

import weka.classifiers.bayes.NaiveBayesUpdateable;
import weka.classifiers.lazy.IBk;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ConverterUtils;

public class MainActivity extends AppCompatActivity {
    private static final int TASK_PDF = 1;
    private static final int TASK_FRAMES = 2;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private Intent batteryStatus;
    private ConnectivityManager connectivityManager;

    private Agent agent;

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

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryStatus = this.registerReceiver(null, ifilter);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        //this.agent = new ReinforcementAgent();
        //this.agent = new KnnAgent(3);
        this.agent = new NaiveBayesAgent();
    }

    public void onBtnPdfClick(View v) {
        /*
        try {
            String directoryPath = android.os.Environment.getExternalStorageDirectory().toString();
            ArffLoader loader = new ArffLoader();
            loader.setFile(new File(directoryPath + "/example.arff"));
            Instances structure = loader.getStructure();
            structure.setClassIndex(structure.numAttributes() - 1);

            IBk ibk = new IBk();
            ibk.buildClassifier(structure);
            Instance current;
            while ((current = loader.getNextInstance(structure)) != null)
                ibk.updateClassifier(current);

            // output generated model
            System.out.println(ibk);
            Instance newInstance = new Data(getCurrentState(), getCurrentState()).toWekaInstance();
            newInstance.setDataset(structure);
            System.out.println(ibk.classifyInstance(newInstance));
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
        */
        State state = getCurrentState();
        state.setTaskNumber(TASK_PDF);
        if(agent.shouldOffload(state)) {
            state.setToOffload(true);
            new JpgToPdfTask(state).execute();
        } else {
            state.setToOffload(false);
            new SimulatedTask(state).execute();
        }
    }

    public void onBtnFramesClick(View v) {
        new TestTask(getCurrentState()).execute();
    }

    public State getCurrentState() {
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        float batteryPct = level * 100 / (float)scale;
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        Calendar calendar = Calendar.getInstance();

        return new State()
                .setBatteryLevel(batteryPct)
                .setCharging(isCharging)
                .setNetworkInfo(info)
                .setTime(calendar);
    }

    public void saveData() {
        FastVector atts = Data.getAttributes();
        Instances dataset = new Instances("MyRelation", atts, 0);

        Data exampleData = new Data(getCurrentState(), getCurrentState());

        String directoryPath = android.os.Environment.getExternalStorageDirectory().toString();
        String outputFilename = directoryPath + "/example.arff";

        dataset.add(exampleData.toWekaInstance());

        try {
            ConverterUtils.DataSink.write(outputFilename, dataset);
        }
        catch (Exception e) {
            System.err.println("Failed to save data to: " + outputFilename);
            e.printStackTrace();
        }
    }

    public void saveData(State before, State after) {
        String directoryPath = android.os.Environment.getExternalStorageDirectory().toString();
        ArffLoader loader = new ArffLoader();
        String filename = directoryPath + "/example.arff";

        try {
            loader.setFile(new File(filename));
            Instances dataset = loader.getStructure();

            Data data = new Data(before, after);

            dataset.add(data.toWekaInstance());

            ConverterUtils.DataSink.write(filename, dataset);
        }
        catch (Exception e) {
            System.err.println("Failed to save data to: " + filename);
            e.printStackTrace();
        }
    }

    public static boolean isConnectionFast(int type, int subType) {
        if (type == ConnectivityManager.TYPE_WIFI) {
            return true;
        } else if (type == ConnectivityManager.TYPE_MOBILE) {
            switch (subType) {
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    return false; // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    return true; // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    return true; // ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return false; // ~ 100 kbps
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    return true; // ~ 2-14 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    return true; // ~ 700-1700 kbps
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    return true; // ~ 1-23 Mbps
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    return true; // ~ 400-7000 kbps
                /*
                 * Above API level 7, make sure to set android:targetSdkVersion
                 * to appropriate level to use these
                 */
                case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
                    return true; // ~ 1-2 Mbps
                case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
                    return true; // ~ 5 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
                    return true; // ~ 10-20 Mbps
                case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
                    return false; // ~25 kbps
                case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
                    return true; // ~ 10+ Mbps
                // Unknown
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    private class TrainClassifier extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                String directoryPath = android.os.Environment.getExternalStorageDirectory().toString();
                ArffLoader loader = new ArffLoader();
                loader.setFile(new File(directoryPath + "/example.arff"));
                Instances structure = loader.getStructure();
                structure.setClassIndex(structure.numAttributes() - 1);

                // train NaiveBayes
                NaiveBayesUpdateable nb = new NaiveBayesUpdateable();
                nb.buildClassifier(structure);
                Instance current;
                while ((current = loader.getNextInstance(structure)) != null)
                    nb.updateClassifier(current);

                // output generated model
                System.out.println(nb);
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
            return null;
        }
    }

    private class JpgToPdfTask extends AsyncTask<Void, Void, State> {
        State before;

        JpgToPdfTask(State state) {
            this.before = state;
        }

        @Override
        protected State doInBackground(Void... voids) {
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
                return null;
            }

            document.close();
            return MainActivity.this.getCurrentState();
        }

        @Override
        protected void onPostExecute(State result) {
            agent.updateKnowledge(new Data(before, result));
            Toast.makeText(MainActivity.this, "not offloaded, execution time (millis): " +  (result.getStartTimeMillis() - before.getStartTimeMillis()), Toast.LENGTH_SHORT).show();
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

    private class TestTask extends AsyncTask<Void, Void, State> {
        State before;

        TestTask(State state) {
            this.before = state;
        }

        @Override
        protected State doInBackground(Void... voids) {
            try {
                Thread.sleep(new Random().nextInt(3000) + 1000);
            } catch (InterruptedException e) {
                Log.e("error", e.getLocalizedMessage());
                return null;
            }
            return MainActivity.this.getCurrentState();
        }

        @Override
        protected void onPostExecute(State result) {
            saveData(before, result);
            Toast.makeText(MainActivity.this, R.string.task_done, Toast.LENGTH_SHORT).show();
        }
    }

    private class SimulatedTask extends AsyncTask<Void, Void, State> {
        State before;

        /*
        private boolean isNetworkConnected;
        private int taskNumber;
        private int connectionType;
        private int connectionSubType;
        private int hour;
        private int day;
        private int month;
        */

        SimulatedTask(State state) {
            this.before = state;
        }

        @Override
        protected State doInBackground(Void... voids) {
            try {
                if(before.getTaskNumber() == TASK_PDF) {
                    long time = 65 + new Random().nextInt(20); //half of mean execution time on phone
                    double timeMultiplier = 1;
                    if(before.getDay() == 4 || before.getDay() == 5 || before.getDay() == 6)
                        timeMultiplier = 1.5;
                    if(isConnectionFast(before.getConnectionType(), before.getConnectionSubType())) {
                        time += 50 * timeMultiplier;
                    } else {
                        time += 100 * timeMultiplier;
                    }
                    Thread.sleep(time);
                }
            } catch (InterruptedException e) {
                Log.e("error", e.getLocalizedMessage());
                return null;
            }
            return MainActivity.this.getCurrentState();
        }

        @Override
        protected void onPostExecute(State result) {
            //saveData(before, result);
            agent.updateKnowledge(new Data(before, result));
            Toast.makeText(MainActivity.this, "offloaded, execution time (millis): " +  (result.getStartTimeMillis() - before.getStartTimeMillis()), Toast.LENGTH_SHORT).show();
        }
    }
}
