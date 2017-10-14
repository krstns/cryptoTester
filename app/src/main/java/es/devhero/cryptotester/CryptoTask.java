package es.devhero.cryptotester;

import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

//import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CryptoTask extends AsyncTask<String, Integer, Long> {

    Button button;
    ProgressBar progressBar;
    TextView textViewStatus;
    TextView textViewResult;
    MainActivity activity;
    int statusString;
    int resultString;
    CryptoTaskListener listener;

    boolean isDecrypting;
    boolean isExternal;
    int testCount;
    String analyticsTag;

    long startTime;
    long fileSize;

    public CryptoTask(MainActivity activity, boolean isDecrypting, boolean isExternal, Button button, ProgressBar progressBar, TextView textViewStatus, TextView textViewResult, int testCount, int statusString, int resultString, String analyticsTag, CryptoTaskListener listener) {
        this.activity = activity;
        this.isDecrypting = isDecrypting;
        this.isExternal = isExternal;
        this.button = button;
        this.progressBar = progressBar;
        this.textViewStatus = textViewStatus;
        this.testCount = testCount;
        this.textViewResult = textViewResult;
        this.statusString = statusString;
        this.resultString = resultString;
        this.analyticsTag = analyticsTag;
        this.listener = listener;
    }

    // Runs in UI before background thread is called
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        button.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);

        textViewStatus.setText(statusString);
        textViewStatus.setVisibility(View.VISIBLE);

        startTime = System.currentTimeMillis();
    }

    // This is run in a background thread
    @Override
    protected Long doInBackground(String... params) {

        try {

            byte[] fileData;

            for (int i = 0; i < testCount; i++) {
                publishProgress(i);

                if (isExternal) {

                    fileData = readFromExternal(params[0]);
                } else {
                    fileData = readFromAssets(params[0]);
                }

                if (isDecrypting) {
                    AESEncryptor.decrypt(fileData, "#eXduw:^A[a(f@cc9+*?aG*eMV3%)rTT".getBytes(), "~xr4hUw`feMQ_6Cc".getBytes());
                } else {
                    AESEncryptor.encrypt(fileData, "#eXduw:^A[a(f@cc9+*?aG*eMV3%)rTT".getBytes(), "~xr4hUw`feMQ_6Cc".getBytes());
                }
                fileData = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return 0L;
        }

        return 0L;
    }

    private byte[] readFromAssets(String path) throws IOException {
        InputStream is = activity.getAssets().open(path);
        int size = is.available();
        fileSize += size;
        byte[] fileData = new byte[size];
        is.read(fileData);
        is.close();
        return fileData;
    }

    private byte[] readFromExternal(String path) throws IOException {
        File file = new File(path);
        if (file.exists()) {
            try {
                int size = (int) file.length();
                fileSize += size;
                byte[] bytes = new byte[size];

                BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
                buf.read(bytes, 0, bytes.length);
                buf.close();

                return bytes;

            } catch (Exception e) {
                throw new AssertionError("Could not decrypt keys json file");
            }
        }
        throw new IOException("File does not exist");
    }

    // This is called from background thread but runs in UI
    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        progressBar.setProgress((int) ((values[0] / (float) testCount) * 100));
        // Do things like update the progress bar
    }

    // This runs in UI when background thread finishes
    @Override
    protected void onPostExecute(Long result) {
        long processTime = (System.currentTimeMillis() - startTime);
        super.onPostExecute(result);
        button.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        textViewResult.setVisibility(View.VISIBLE);
        textViewStatus.setVisibility(View.GONE);
        textViewResult.setText(activity.getString(resultString) + " " + processTime + " speed: " + ((int) (fileSize / (processTime / 60.0)) + "B/s"));

//        Bundle bundle = new Bundle();
//        bundle.putLong(FirebaseAnalytics.Param.SCORE, processTime);
//        bundle.putLong(FirebaseAnalytics.Param.LEVEL, fileSize);
//        bundle.putString(FirebaseAnalytics.Param.CHARACTER, analyticsTag);
//        activity.mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.POST_SCORE, bundle);

        listener.taskFinished();
    }
}

interface CryptoTaskListener {
    void taskFinished();
}