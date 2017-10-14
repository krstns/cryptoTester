package es.devhero.cryptotester;

import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

//import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "CRYPTO";
    private static final String TARGET_BASE_PATH = Environment.getExternalStorageDirectory() + "/CRYPTOTEST/";


    private ProgressBar progressBar;
    private TextView textViewResultDecryptionLargeFiles;
    private TextView textViewResultEncryptionLargeFiles;
    private TextView textViewResultDecryptionSmallFiles;
    private TextView textViewResultEncryptionSmallFiles;
    private TextView textViewResultDecryptionLargeFilesExternal;
    private TextView textViewResultEncryptionLargeFilesExternal;
    private TextView textViewResultDecryptionSmallFilesExternal;
    private TextView textViewResultEncryptionSmallFilesExternal;
    private TextView textViewStatus;
    private Button button;

    private int largeTestCount = 5;
    private int smallTestCount = 5;
    private long startTime;

    private long decryptLargeFilesSize;
    private long encryptLargeFilesSize;
    private long decryptSmallFilesSize;
    private long encryptSmallFilesSize;

    private int currentTask = 0;

//    FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        progressBar.setIndeterminate(false);

        textViewResultDecryptionLargeFiles = (TextView) findViewById(R.id.textViewResultDecryptionLargeFiles);
        textViewResultDecryptionLargeFiles.setVisibility(View.GONE);
        textViewResultEncryptionLargeFiles = (TextView) findViewById(R.id.textViewResultEncryptionLargeFiles);
        textViewResultEncryptionLargeFiles.setVisibility(View.GONE);
        textViewResultDecryptionSmallFiles = (TextView) findViewById(R.id.textViewResultDecryptionSmallFiles);
        textViewResultDecryptionSmallFiles.setVisibility(View.GONE);
        textViewResultEncryptionSmallFiles = (TextView) findViewById(R.id.textViewResultEncryptionSmallFiles);
        textViewResultEncryptionSmallFiles.setVisibility(View.GONE);
        textViewResultDecryptionLargeFilesExternal = (TextView) findViewById(R.id.textViewResultDecryptionLargeFilesExternal);
        textViewResultDecryptionLargeFilesExternal.setVisibility(View.GONE);
        textViewResultEncryptionLargeFilesExternal = (TextView) findViewById(R.id.textViewResultEncryptionLargeFilesExternal);
        textViewResultEncryptionLargeFilesExternal.setVisibility(View.GONE);
        textViewResultDecryptionSmallFilesExternal = (TextView) findViewById(R.id.textViewResultDecryptionSmallFilesExternal);
        textViewResultDecryptionSmallFilesExternal.setVisibility(View.GONE);
        textViewResultEncryptionSmallFilesExternal = (TextView) findViewById(R.id.textViewResultEncryptionSmallFilesExternal);
        textViewResultEncryptionSmallFilesExternal.setVisibility(View.GONE);

        textViewStatus = (TextView) findViewById(R.id.textViewStatus);
        textViewStatus.setVisibility(View.GONE);

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isStoragePermissionGranted()) {
                    prepareTest();
                }
            }
        });

//        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
//        try {
//            Bundle bundle = new Bundle();
//            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, getCPUInfo());
//            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "cpu");
//            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

    private void prepareTest() {
        button.setVisibility(View.GONE);
        textViewStatus.setText(R.string.preparingForTest);

        File cryptoTestDirectory = new File(TARGET_BASE_PATH);
        if (!(cryptoTestDirectory.exists() && cryptoTestDirectory.isDirectory())) {
            cryptoTestDirectory.delete();
            if (!cryptoTestDirectory.mkdirs()) {
                Toast.makeText(this, "Cannot create external directory!", Toast.LENGTH_LONG).show();
            }
        }

        copyFileOrDir("");

        runTest();
    }



    private void copyFileOrDir(String path) {
        AssetManager assetManager = this.getAssets();
        String assets[] = null;
        try {
            Log.i("tag", "copyFileOrDir() " + path);
            assets = assetManager.list(path);
            if (assets.length == 0) {
                copyFile(path);
            } else {
                String fullPath = TARGET_BASE_PATH + path;
                Log.i("tag", "path=" + fullPath);
                File dir = new File(fullPath);
                if (!dir.exists() && !path.startsWith("images") && !path.startsWith("sounds") && !path.startsWith("webkit"))
                    if (!dir.mkdirs())
                        Log.i("tag", "could not create dir " + fullPath);
                for (int i = 0; i < assets.length; ++i) {
                    String p;
                    if (path.equals(""))
                        p = "";
                    else
                        p = path + "/";

                    if (!path.startsWith("images") && !path.startsWith("sounds") && !path.startsWith("webkit"))
                        copyFileOrDir(p + assets[i]);
                }
            }
        } catch (IOException ex) {
            Log.e("tag", "I/O Exception", ex);
        }
    }

    private void copyFile(String filename) {
        AssetManager assetManager = this.getAssets();

        InputStream in = null;
        OutputStream out = null;
        String newFileName = null;
        try {
            Log.i("tag", "copyFile() " + filename);
            in = assetManager.open(filename);
            if (filename.endsWith(".jpg")) // extension was added to avoid compression on APK file
                newFileName = TARGET_BASE_PATH + filename.substring(0, filename.length() - 4);
            else
                newFileName = TARGET_BASE_PATH + filename;
            out = new FileOutputStream(newFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (Exception e) {
            Log.e("tag", "Exception in copyFile() of " + newFileName);
            Log.e("tag", "Exception in copyFile() " + e.toString());
        }

    }

    private void runTest() {
        textViewResultDecryptionLargeFiles.setVisibility(View.GONE);
        textViewResultEncryptionLargeFiles.setVisibility(View.GONE);
        textViewResultDecryptionSmallFiles.setVisibility(View.GONE);
        textViewResultEncryptionSmallFiles.setVisibility(View.GONE);

        nextTask();
    }

    private void nextTask() {
        CryptoTask task;

        switch (currentTask) {
            case 0:
                task = new CryptoTask(this, true, false, button, progressBar, textViewStatus, textViewResultDecryptionLargeFiles, largeTestCount, R.string.statusDecryptionLargeFiles, R.string.statusDecryptionLargeFiles, "decrypt_large", new CryptoTaskListener() {
                @Override
                    public void taskFinished() {
                        nextTask();

                    }
                });
                task.execute("encrypted/10MB.tst");
                break;
            case 1:
                task = new CryptoTask(this, false, false, button, progressBar, textViewStatus, textViewResultEncryptionLargeFiles, largeTestCount, R.string.statusEncryptionLargeFiles, R.string.statusEncryptionLargeFiles, "encrypt_large", new CryptoTaskListener() {
                @Override
                    public void taskFinished() {
                        nextTask();

                    }
                });
                task.execute("decrypted/10MB.tst");
                break;
            case 2:
                task = new CryptoTask(this, true, false, button, progressBar, textViewStatus, textViewResultDecryptionSmallFiles, smallTestCount, R.string.statusDecryptionSmallFiles, R.string.statusDecryptionSmallFiles, "decrypt_small", new CryptoTaskListener() {
                @Override
                    public void taskFinished() {
                        nextTask();

                    }
                });
                task.execute("encrypted/200KB.tst");
                break;
            case 3:
                task = new CryptoTask(this, false, false, button, progressBar, textViewStatus, textViewResultEncryptionSmallFiles, smallTestCount, R.string.statusEncryptionSmallFiles, R.string.statusEncryptionSmallFiles, "encrypt_small", new CryptoTaskListener() {
                @Override
                    public void taskFinished() {
                        nextTask();

                    }
                });
                task.execute("decrypted/200KB.tst");
                break;
            case 4:
                task = new CryptoTask(this, true, true, button, progressBar, textViewStatus, textViewResultDecryptionLargeFilesExternal, largeTestCount, R.string.statusDecryptionLargeFilesExternal, R.string.statusDecryptionLargeFilesExternal, "decrypt_large_external", new CryptoTaskListener() {
                @Override
                    public void taskFinished() {
                        nextTask();

                    }
                });
                task.execute(TARGET_BASE_PATH + "encrypted/10MB.tst");
                break;
            case 5:
                task = new CryptoTask(this, false, true, button, progressBar, textViewStatus, textViewResultEncryptionLargeFilesExternal, largeTestCount, R.string.statusEncryptionLargeFilesExternal, R.string.statusEncryptionLargeFilesExternal, "encrypt_large_external", new CryptoTaskListener() {
                @Override
                    public void taskFinished() {
                        nextTask();

                    }
                });
                task.execute(TARGET_BASE_PATH + "decrypted/10MB.tst");
                break;
            case 6:
                task = new CryptoTask(this, true, true, button, progressBar, textViewStatus, textViewResultDecryptionSmallFilesExternal, smallTestCount, R.string.statusDecryptionSmallFilesExternal, R.string.statusDecryptionSmallFilesExternal, "decrypt_small_external", new CryptoTaskListener() {
                @Override
                    public void taskFinished() {
                        nextTask();

                    }
                });
                task.execute(TARGET_BASE_PATH + "encrypted/200KB.tst");
                break;
            case 7:
                task = new CryptoTask(this, false, true, button, progressBar, textViewStatus, textViewResultEncryptionSmallFilesExternal, smallTestCount, R.string.statusEncryptionSmallFilesExternal, R.string.statusEncryptionSmallFilesExternal, "encrypt_small_external", new CryptoTaskListener() {
                    @Override
                    public void taskFinished() {
                        nextTask();

                    }
                });
                task.execute(TARGET_BASE_PATH + "decrypted/200KB.tst");
                break;
            default:
                testDone();
                break;
        }
        currentTask++;
    }

    private void testDone() {
        File cryptoTestDirectory = new File(TARGET_BASE_PATH);
        String[] children = cryptoTestDirectory.list();
        for (int i = 0; i < children.length; i++)
        {
            new File(cryptoTestDirectory, children[i]).delete();
        }
        cryptoTestDirectory.delete();
        Log.d(TAG, "Done");
    }



    public String getCPUInfo() throws IOException {

        String line = null;
        StringBuilder stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");

        try (BufferedReader reader = new BufferedReader(new FileReader("/proc/cpuinfo"))) {
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }

            return stringBuilder.toString();
        }

    }

}

