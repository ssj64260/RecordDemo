package com.android.recorddemo.ui;

import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.android.recorddemo.R;
import com.android.recorddemo.app.BaseActivity;
import com.android.recorddemo.config.Constants;
import com.android.recorddemo.utils.SDCardUtils;
import com.android.recorddemo.utils.ThreadPoolUtils;
import com.android.recorddemo.utils.ToastMaster;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 录音
 */

public class RecordActivity extends BaseActivity {

    public static final String KEY_RETURN_DATA = "key_return_data";
    private static final int MAX_TIME = 60;

    private TextView tvCancel;
    private TextView tvDoRecord;

    private int mTime;
    private MediaRecorder mMediaRecorder;
    private String mAudioPath;
    private File mAudioFile;

    private View.OnClickListener mClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_cancel:
                    onBackPressed();
                    break;
            }
        }
    };

    private View.OnTouchListener mTouch = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            final int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    doRecord();
                    break;
                case MotionEvent.ACTION_UP:
                    finishRecord();
                    break;
            }
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        initData();
        initView();

    }

    private void initData() {
        mAudioPath = SDCardUtils.getExternalFilesDir(this) + Constants.PATH_TEMP;
        final File audioDir = new File(mAudioPath);
        if (!audioDir.exists()) {
            audioDir.mkdir();
        }
    }

    private void initView() {
        tvCancel = (TextView) findViewById(R.id.tv_cancel);
        tvDoRecord = (TextView) findViewById(R.id.tv_do_record);

        tvCancel.setOnClickListener(mClick);
        tvDoRecord.setOnTouchListener(mTouch);
    }

    private void doRecord() {
        tvDoRecord.setText("松开 结束");
        tvDoRecord.setSelected(true);
        mAudioFile = new File(mAudioPath, System.currentTimeMillis() + ".m4a");

        if (mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
        }
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setOutputFile(mAudioFile.getAbsolutePath());

        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mMediaRecorder.start();

        ToastMaster.toast("开始录制");

        mTime = 0;
        ThreadPoolUtils.getInstache().scheduledRate(new Runnable() {
            @Override
            public void run() {
                mTime++;
                if (mTime >= MAX_TIME) {
                    finishRecord();
                }
            }
        }, 100, 1000, TimeUnit.MILLISECONDS);
    }

    private void finishRecord() {
        if (mMediaRecorder != null) {
            try {
                mMediaRecorder.stop();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }

            mMediaRecorder.release();
            mMediaRecorder = null;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvDoRecord.setText("按住 说话");
                    tvDoRecord.setSelected(false);

                    doReturnData();
                }
            });

            ThreadPoolUtils.getInstache().scheduledShutDown(0);
        }
    }

    private void doReturnData() {
        if (mAudioFile.exists()) {
            Intent intent = new Intent();
            intent.putExtra(KEY_RETURN_DATA, mAudioFile.getAbsolutePath());
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}
