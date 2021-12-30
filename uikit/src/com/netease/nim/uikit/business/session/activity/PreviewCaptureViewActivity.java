package com.netease.nim.uikit.business.session.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.ToastHelper;
import com.netease.nim.uikit.common.activity.UI;

import java.io.File;

public class PreviewCaptureViewActivity extends UI implements SurfaceHolder.Callback {

    private SurfaceHolder surfaceHolder;

    public static void previewImage(Activity activity, String path, int requestCode) {
        Intent intent = new Intent();
        intent.setClass(activity, PreviewCaptureViewActivity.class);
        intent.putExtra("path", path);
        intent.putExtra("isImage", true);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void previewVideo(Activity activity, String path, int requestCode) {
        Intent intent = new Intent();
        intent.setClass(activity, PreviewCaptureViewActivity.class);
        intent.putExtra("path", path);
        intent.putExtra("isVideo", true);
        activity.startActivityForResult(intent, requestCode);
    }

    private View backBtn;
    private View okBtn;
    private ImageView imageView;

    private SurfaceView surfaceView;

    private MediaPlayer mediaPlayer;

    private boolean isSurfaceCreated = false;

    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preo_capture_view_act);

        surfaceView = findView(R.id.videoView);
        imageView = findView(R.id.imageView);
        backBtn = findViewById(R.id.backBtn);
        okBtn = findViewById(R.id.okBtn);

        path = getIntent().getStringExtra("path");

        if (getIntent().getBooleanExtra("isImage", false)) {
            imageView.setVisibility(View.VISIBLE);
            surfaceView.setVisibility(View.GONE);
            Glide
                    .with(this)
                    .load(new File(path))
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(imageView);
        } else {
            imageView.setVisibility(View.GONE);
            surfaceView.setVisibility(View.VISIBLE);

            mediaPlayer = new MediaPlayer();
            mediaPlayer.reset();

            surfaceHolder = surfaceView.getHolder();
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            surfaceHolder.addCallback(this);
        }

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK, getIntent());
                finish();
            }
        });

    }

    private void initVideoSize() {
        if (mediaPlayer == null) {
            return;
        }
        // 视频宽高
        int width = mediaPlayer.getVideoWidth();
        int height = mediaPlayer.getVideoHeight();

        if (width <= 0 || height <= 0) {
            return;
        }

        // 屏幕宽高
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;

        int videoRatio = width / height;
        int screenRatio = screenWidth / screenHeight;

        if (screenRatio > videoRatio) {
            int newHeight = screenHeight;
            int newWidth = screenHeight * width / height;
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    newWidth, newHeight);
            int margin = (screenWidth - newWidth) / 2;
            layoutParams.setMargins(margin, 0, margin, 0);
            surfaceView.setLayoutParams(layoutParams);
        } else {
            int newWidth = screenWidth;
            int newHeight = screenWidth * height / width;
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    newWidth, newHeight);
            int margin = (screenHeight - newHeight) / 2;
            layoutParams.setMargins(0, margin, 0, margin);
            surfaceView.setLayoutParams(layoutParams);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null) {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMediaPlayer();
    }

    private void stopMediaPlayer() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void play() {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(path);
            mediaPlayer.setDisplay(surfaceHolder);
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
            initVideoSize();

        } catch (Exception e) {
            ToastHelper.showToast(this, R.string.look_video_fail_try_again);
            e.printStackTrace();
            return;
        }
    }

    /**
     * ***************************** SurfaceHolder Callback **************************************
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!isSurfaceCreated) {
            isSurfaceCreated = true;
            play();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isSurfaceCreated = false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initVideoSize();// 屏幕旋转后，改变视频显示布局
    }
}
