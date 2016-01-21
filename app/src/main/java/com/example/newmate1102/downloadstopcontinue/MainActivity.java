package com.example.newmate1102.downloadstopcontinue;

import android.app.Activity;
import android.os.*;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends Activity {
    private static final int PROCESSING = 1;
    private static final int FAILURE = -1;
    private static final int DOWNLOAD_PAUSE = 1;
    private static final int DOWNLOAD_START = 2;
    private static int DOWNLOAD_BUTTON_STATE = 0;
    private String path = "http://img.mpreader.com:80/2014/12/nqexn5kyd2i2egbya9fj.mpr";

    private TextView resultView;
    private Button downloadButton;
    private Button stopButton;
    private ProgressBar progressBar;
    private TextView speedText;

    private Handler handler = new UIHandler();

    private final class UIHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PROCESSING:
                    progressBar.setProgress(msg.getData().getInt("size"));
                    float num = (float) progressBar.getProgress() / (float) progressBar.getMax();
                    int result = (int) (num * 100);
                    resultView.setText(result + "%");
                    if (progressBar.getProgress() == progressBar.getMax()) {
                        Toast.makeText(getApplicationContext(), R.string.success, Toast.LENGTH_LONG).show();
                    }
                    break;
                case FAILURE:
                    Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        pathText = (EditText) findViewById(R.id.path);
        resultView = (TextView) findViewById(R.id.download_process_mb);
        speedText = (TextView)findViewById(R.id.download_process_speed);
        downloadButton = (Button) findViewById(R.id.downloadbutton);
        progressBar = (ProgressBar) findViewById(R.id.download_process);
        ButtonClickListener listener = new ButtonClickListener();
        downloadButton.setOnClickListener(listener);
    }

    private final class ButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.downloadbutton:
                    if(DOWNLOAD_BUTTON_STATE != DOWNLOAD_START ){
                        DOWNLOAD_BUTTON_STATE = DOWNLOAD_START;
                        downloadButton.setText("开始");
                        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                            //File savDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
                            File savDir = Environment.getExternalStorageDirectory();
                            download(path, savDir);
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.sdcarderror, Toast.LENGTH_LONG).show();
                        }
                    }else {
                        DOWNLOAD_BUTTON_STATE = DOWNLOAD_PAUSE;
                        downloadButton.setText("暂停");
                        exit();
                        Toast.makeText(getApplicationContext(), "Now thread is Stopping!!", Toast.LENGTH_LONG).show();
                        speedText.setText("0KB/S");
                    }

                    break;
                default:
                    break;
//                case R.id.stopbutton:
//                    exit();
//                    Toast.makeText(getApplicationContext(), "Now thread is Stopping!!", Toast.LENGTH_LONG).show();
//                    downloadButton.setEnabled(true);
//                    stopButton.setEnabled(false);
//                    speedText.setText("0KB/S");
//
//                    break;
            }
        }

        private DownloadTask task;

        private void exit() {
            if (task != null)
                task.exit();
        }

        private void download(String path, File savDir) {
            task = new DownloadTask(path, savDir);
            new Thread(task).start();
        }

        private final class DownloadTask implements Runnable {
            private String path;
            private File saveDir;
            private FileDownloader loader;

            public DownloadTask(String path, File saveDir) {
                this.path = path;
                this.saveDir = saveDir;
            }

            public void exit() {
                if (loader != null)
                    loader.exit();
            }

            DownloadProgressListener downloadProgressListener = new DownloadProgressListener() {
                @Override
                public void onDownloadSize(int size) {
                    Message msg = new Message();
                    msg.what = PROCESSING;
                    msg.getData().putInt("size", size);
                    handler.sendMessage(msg);
                }
            };

            public void run() {
                try {
                    loader = new FileDownloader(getApplicationContext(), path, saveDir, 3);
                    progressBar.setMax(loader.getFileSize());
                    loader.download(downloadProgressListener);
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.sendMessage(handler.obtainMessage(FAILURE));
                }
            }
        }
    }
}
