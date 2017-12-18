package com.android.recorddemo.ui;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.recorddemo.R;
import com.android.recorddemo.app.BaseActivity;

import java.io.File;
import java.io.IOException;

public class MainActivity extends BaseActivity {

    private static final int REQUEST_CODE_DO_RECORD = 1001;

    private ImageView ivDoRecord;
    private RelativeLayout rlRecord;
    private TextView tvTime;
    private ImageView ivDelete;

    private String mAudioPath;
    private MediaPlayer mPlayer;

    private View.OnClickListener mClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_do_record:
                    startActivityForResult(new Intent(MainActivity.this, RecordActivity.class), REQUEST_CODE_DO_RECORD);
                    break;
                case R.id.rl_record:
                    doPlay();
                    break;
                case R.id.iv_delete:
                    doDelete();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();
        initView();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_DO_RECORD) {
            if (resultCode == RESULT_OK && data != null) {
                mAudioPath = data.getStringExtra(RecordActivity.KEY_RETURN_DATA);
                try {
                    if (mPlayer == null) {
                        mPlayer = new MediaPlayer();
                        mPlayer.setLooping(false);
                    }
                    mPlayer.setDataSource(mAudioPath);
                    mPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                final String time = "录音总时长" + (int) (mPlayer.getDuration() / 1000f) + "秒";
                tvTime.setText(time);
                rlRecord.setVisibility(View.VISIBLE);

                mPlayer.release();
                mPlayer = null;
            }
        }
    }

    private void initData() {

    }

    private void initView() {
        ivDoRecord = (ImageView) findViewById(R.id.iv_do_record);
        rlRecord = (RelativeLayout) findViewById(R.id.rl_record);
        tvTime = (TextView) findViewById(R.id.tv_time);
        ivDelete = (ImageView) findViewById(R.id.iv_delete);

        ivDoRecord.setOnClickListener(mClick);
        rlRecord.setOnClickListener(mClick);
        ivDelete.setOnClickListener(mClick);
    }

    private void doPlay() {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
            mPlayer.setLooping(false);
        }

        if (mPlayer.isPlaying()) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        } else {
            try {
                mPlayer.reset();
                mPlayer.setDataSource(mAudioPath);
                mPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mPlayer.start();
        }
    }

    private void doDelete() {
        final File file = new File(mAudioPath);
        if (file.exists()) {
            file.delete();
        }
        mAudioPath = "";
        rlRecord.setVisibility(View.GONE);
    }
}
