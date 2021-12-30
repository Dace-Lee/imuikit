package com.netease.nim.uikit.business.session.actions;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.Settings;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.business.session.constant.RequestCode;
import com.netease.nim.uikit.business.session.helper.VideoMessageHelper;
import com.netease.nim.uikit.business.uinfo.ImUserRespCache;
import com.netease.nim.uikit.business.uinfo.ImUserRespDTO;
import com.netease.nimlib.sdk.chatroom.ChatRoomMessageBuilder;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;

import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

/**
 * Created by hzxuwen on 2015/6/12.
 */
public class VideoAction extends BaseAction {

    // 视频
    protected transient VideoMessageHelper videoMessageHelper;

    public VideoAction() {
        super(R.drawable.img_c_more_64_2, R.string.input_panel_take);

    }

    @SuppressLint("CheckResult")
    @Override
    public void onClick() {
        RxPermissions r = new RxPermissions(getActivity());
        r.request(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.RECORD_AUDIO
        ).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(final Boolean accept) {
                if (accept) {
                    boolean canLongClickTakeVideo = false;
                    ImUserRespDTO dto = ImUserRespCache.instance.get(getContainer().account);
                    canLongClickTakeVideo = dto != null && !dto.getDataMasking();
                    /// 非脱敏用户可以发视频

                    videoHelper().showVideoSource(makeRequestCode(RequestCode.GET_LOCAL_VIDEO), makeRequestCode(RequestCode.CAPTURE_VIDEO), canLongClickTakeVideo);
                } else {
                    dialogAppSettings("请前往设置允许打开相机相关权限");
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                dialogAppSettings("请前往设置允许打开相机相关权限");
            }
        }, new Action() {
            @Override
            public void run() {

            }
        });
    }

    /**
     * ********************** 视频 *******************************
     */
    private void initVideoMessageHelper() {
        videoMessageHelper = new VideoMessageHelper(getActivity(), new VideoMessageHelper.VideoMessageHelperListener() {

            @Override
            public void onVideoPicked(File file, String md5) {
                MediaPlayer mediaPlayer = getVideoMediaPlayer(file);
                long duration = mediaPlayer == null ? 0 : mediaPlayer.getDuration();
                int height = mediaPlayer == null ? 0 : mediaPlayer.getVideoHeight();
                int width = mediaPlayer == null ? 0 : mediaPlayer.getVideoWidth();
                IMMessage message = MessageBuilder.createVideoMessage(getAccount(), getSessionType(), file, duration, width, height, md5);
                sendMessage(message);
            }

            @Override
            public void onPicturePicked(File file, String md5) {
                IMMessage message;
                if (getContainer() != null && getContainer().sessionType == SessionTypeEnum.ChatRoom) {
                    message = ChatRoomMessageBuilder.createChatRoomImageMessage(getAccount(), file, file.getName());
                } else {
                    message = MessageBuilder.createImageMessage(getAccount(), getSessionType(), file, file.getName());
                }
                sendMessage(message);
            }
        });
    }

    /**
     * 获取视频mediaPlayer
     *
     * @param file 视频文件
     * @return mediaPlayer
     */
    private MediaPlayer getVideoMediaPlayer(File file) {
        try {
            return MediaPlayer.create(getActivity(), Uri.fromFile(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == videoHelper().getLocalRequestCode()) {
            videoHelper().onGetLocalVideoResult(data);

        } else if (requestCode == videoHelper().getCaptureRequestCode()) {
            videoHelper().onCaptureVideoResult(data);
        }
    }

    private VideoMessageHelper videoHelper() {
        if (videoMessageHelper == null) {
            initVideoMessageHelper();
        }
        return videoMessageHelper;
    }

    /**
     * 弹框提示app权限设置
     *
     * @param msg
     */
    private void dialogAppSettings(String msg) {
        new AlertDialog.Builder(getActivity()).setMessage(msg != null ? msg : "请前往设置允许打开相机相关权限")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        // 去应用信息
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        intent.setData(Uri.parse("package:com.ddcar"));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        getActivity().startActivity(intent);
                    }
                }).show();
    }
}
