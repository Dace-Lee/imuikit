package com.netease.nim.uikit.business.session.module.input;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import androidx.fragment.app.FragmentActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.UIKitOptions;
import com.netease.nim.uikit.api.model.session.SessionCustomization;
import com.netease.nim.uikit.business.ait.AitTextChangeListener;
import com.netease.nim.uikit.business.session.actions.BaseAction;
import com.netease.nim.uikit.business.session.emoji.EmoticonPickerView;
import com.netease.nim.uikit.business.session.emoji.IEmoticonSelectedListener;
import com.netease.nim.uikit.business.session.emoji.MoonUtil;
import com.netease.nim.uikit.business.session.module.Container;
import com.netease.nim.uikit.common.ToastHelper;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.util.string.StringUtil;
import com.netease.nim.uikit.impl.NimUIKitImpl;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.media.record.AudioRecorder;
import com.netease.nimlib.sdk.media.record.IAudioRecordCallback;
import com.netease.nimlib.sdk.media.record.RecordType;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.msg.model.CustomNotificationConfig;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.util.List;

import io.reactivex.functions.Consumer;

/**
 * ????????????????????????????????????
 * Created by hzxuwen on 2015/6/16.
 */
public class InputPanel implements IEmoticonSelectedListener, IAudioRecordCallback, AitTextChangeListener {

    private static final String TAG = "MsgSendLayout";

    private static final int SHOW_LAYOUT_DELAY = 200;

    protected Container container;

    public Container getContainer() {
        return container;
    }

    protected View view;
    protected Handler uiHandler;

    protected View actionPanelBottomLayout; // ????????????
    protected LinearLayout messageActivityBottomLayout;

    protected LinearLayout LayoutSelectTag;
    protected LinearLayout layoutSelectPurchase;
    protected LinearLayout layoutSelectOrder;
    protected LinearLayout layoutSelectRefundOrder;

    protected EditText messageEditText;// ?????????????????????
    protected Button audioRecordBtn; // ????????????
    protected View audioAnimLayout; // ??????????????????
    protected FrameLayout textAudioSwitchLayout; // ?????????????????????????????????
    protected View switchToTextButtonInInputBar;// ????????????????????????
    protected View switchToAudioButtonInInputBar;// ????????????????????????
    protected View moreFuntionButtonInInputBar;// ????????????????????????
    protected View sendMessageButtonInInputBar;// ??????????????????
    protected View emojiButtonInInputBar;// ??????????????????
    protected View messageInputBar;

    private SessionCustomization customization;

    // ??????
    protected EmoticonPickerView emoticonPickerView;  // ??????????????????

    // ??????
    protected AudioRecorder audioMessageHelper;
    private Chronometer time;
    private TextView timerTip;
    private LinearLayout timerTipContainer;
    private boolean started = false;
    private boolean cancelled = false;
    private boolean touched = false; // ????????????
    private boolean isKeyboardShowed = true; // ??????????????????

    // state
    private boolean actionPanelBottomLayoutHasSetup = false;
    /**
     * ?????????????????????????????????
     */
    private boolean isTextAudioSwitchShow = true;

    // adapter
    private List<BaseAction> actions;

    // data
    private long typingTime = 0;

    private boolean isRobotSession;

    private TextWatcher aitTextWatcher;

    private View messageActivityLayout;

    public View attach_custom_msg_layout;
    public ImageView img_attach_custom_icon;
    public TextView txt_attach_custom_title;
    public TextView txt_attach_custom_sub_title;
    public TextView txt_attach_custom_sub_title_2;
    public Button btn_attach_custom_send;

    public InputPanel(Container container, View view, List<BaseAction> actions, boolean isTextAudioSwitchShow) {
        this.container = container;
        this.view = view;
        this.actions = actions;
        this.uiHandler = new Handler();
        this.isTextAudioSwitchShow = isTextAudioSwitchShow;
        init();
    }

    public InputPanel(Container container, View view, List<BaseAction> actions) {
        this(container, view, actions, true);
    }

    public void onPause() {
        // ????????????
        if (audioMessageHelper != null) {
            onEndAudioRecord(true);
        }
    }

    public void onDestroy() {
        // release
        if (audioMessageHelper != null) {
            audioMessageHelper.destroyAudioRecorder();
        }
    }

    public boolean collapse(boolean immediately) {
        boolean respond = (emoticonPickerView != null && emoticonPickerView.getVisibility() == View.VISIBLE
                || actionPanelBottomLayout != null && actionPanelBottomLayout.getVisibility() == View.VISIBLE);

        hideAllInputLayout(immediately);

        return respond;
    }

    public void addAitTextWatcher(TextWatcher watcher) {
        aitTextWatcher = watcher;
    }

    private void init() {
        initViews();
        initInputBarListener();
        initTextEdit();
        initAudioRecordButton();
        restoreText(false);

        for (int i = 0; i < actions.size(); ++i) {
            actions.get(i).setIndex(i);
            actions.get(i).setContainer(container);
        }
    }

    public void setCustomization(SessionCustomization customization) {
        this.customization = customization;
        if (customization != null) {
            emoticonPickerView.setWithSticker(customization.withSticker);
        }
    }

    public void reload(Container container, SessionCustomization customization) {
        this.container = container;
        setCustomization(customization);
    }

    private void initViews() {
        messageActivityLayout = view.findViewById(R.id.messageActivityLayout);
        // input bar
        messageActivityBottomLayout = view.findViewById(R.id.messageActivityBottomLayout);
        LayoutSelectTag = view.findViewById(R.id.ll_select_tag);
        layoutSelectPurchase = view.findViewById(R.id.ll_select_purchase);
        layoutSelectOrder = view.findViewById(R.id.ll_select_order);
        layoutSelectRefundOrder = view.findViewById(R.id.ll_select_refund_order);

        messageInputBar = view.findViewById(R.id.textMessageLayout);
        switchToTextButtonInInputBar = view.findViewById(R.id.buttonTextMessage);
        switchToAudioButtonInInputBar = view.findViewById(R.id.buttonAudioMessage);
        moreFuntionButtonInInputBar = view.findViewById(R.id.buttonMoreFuntionInText);
        emojiButtonInInputBar = view.findViewById(R.id.emoji_button);
        sendMessageButtonInInputBar = view.findViewById(R.id.buttonSendMessage);
        messageEditText = view.findViewById(R.id.editTextMessage);

        // ??????
        audioRecordBtn = view.findViewById(R.id.audioRecord);
        audioAnimLayout = view.findViewById(R.id.layoutPlayAudio);
        time = view.findViewById(R.id.timer);
        timerTip = view.findViewById(R.id.timer_tip);
        timerTipContainer = view.findViewById(R.id.timer_tip_container);

        // ??????
        emoticonPickerView = view.findViewById(R.id.emoticon_picker_view);

        // ??????????????????
        switchToTextButtonInInputBar.setVisibility(View.GONE);
        switchToAudioButtonInInputBar.setVisibility(View.VISIBLE);

        // ??????????????????????????????
        textAudioSwitchLayout = view.findViewById(R.id.switchLayout);
        if (isTextAudioSwitchShow) {
            textAudioSwitchLayout.setVisibility(View.VISIBLE);
        } else {
            textAudioSwitchLayout.setVisibility(View.GONE);
        }

        /// ??????
        nim_bottom_layer = view.findViewById(R.id.nim_bottom_layer);
        nim_bottom_layer_bottom = view.findViewById(R.id.nim_bottom_layer_bottom);
        nim_bottom_layer_icon = view.findViewById(R.id.nim_bottom_layer_icon);
        niu_close_icon = view.findViewById(R.id.niu_close_icon);
        text_audio_amplitude_layer = view.findViewById(R.id.text_audio_amplitude_layer);
        text_audio_amplitude_layer_bottom_icon = view.findViewById(R.id.text_audio_amplitude_layer_bottom_icon);

        // ???????????????
        attach_custom_msg_layout = view.findViewById(R.id.attach_custom_msg_layout);
        img_attach_custom_icon = view.findViewById(R.id.img_attach_custom_icon);
        txt_attach_custom_title = view.findViewById(R.id.txt_attach_custom_title);
        txt_attach_custom_sub_title = view.findViewById(R.id.txt_attach_custom_sub_title);
        txt_attach_custom_sub_title_2 = view.findViewById(R.id.txt_attach_custom_sub_title_2);
        btn_attach_custom_send = view.findViewById(R.id.btn_attach_custom_send);
        attach_custom_msg_layout.setVisibility(View.GONE);

        messageActivityLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attach_custom_msg_layout.setVisibility(View.GONE);
                messageActivityLayout.setOnClickListener(null);
                messageActivityLayout.setClickable(false);
            }
        });
    }

    private ViewGroup text_audio_amplitude_layer;
    private ImageView niu_close_icon;
    private ImageView text_audio_amplitude_layer_bottom_icon;

    private void initInputBarListener() {
        switchToTextButtonInInputBar.setOnClickListener(clickListener);
        switchToAudioButtonInInputBar.setOnClickListener(clickListener);
        emojiButtonInInputBar.setOnClickListener(clickListener);
        sendMessageButtonInInputBar.setOnClickListener(clickListener);
        moreFuntionButtonInInputBar.setOnClickListener(clickListener);
    }

    private void initTextEdit() {
        messageEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        messageEditText.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    switchToTextLayout(true);
                }
                return false;
            }
        });

        messageEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                messageEditText.setHint("");
                checkSendButtonEnable(messageEditText);
            }
        });

        messageEditText.addTextChangedListener(new TextWatcher() {
            private int start;
            private int count;

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                this.start = start;
                this.count = count;
                if (aitTextWatcher != null) {
                    aitTextWatcher.onTextChanged(s, start, before, count);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (aitTextWatcher != null) {
                    aitTextWatcher.beforeTextChanged(s, start, count, after);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkSendButtonEnable(messageEditText);
                MoonUtil.replaceEmoticons(container.activity, s, start, count);

                int editEnd = messageEditText.getSelectionEnd();
                messageEditText.removeTextChangedListener(this);
                while (StringUtil.counterChars(s.toString()) > NimUIKitImpl.getOptions().maxInputTextLength && editEnd > 0) {
                    s.delete(editEnd - 1, editEnd);
                    editEnd--;
                }
                messageEditText.setSelection(editEnd);
                messageEditText.addTextChangedListener(this);

                if (aitTextWatcher != null) {
                    aitTextWatcher.afterTextChanged(s);
                }

                sendTypingCommand();
            }
        });
    }


    /**
     * ??????????????????????????????
     */
    private void sendTypingCommand() {
        if (container.account.equals(NimUIKit.getAccount())) {
            return;
        }

        if (container.sessionType == SessionTypeEnum.Team || container.sessionType == SessionTypeEnum.ChatRoom) {
            return;
        }

        if (System.currentTimeMillis() - typingTime > 5000L) {
            typingTime = System.currentTimeMillis();
            CustomNotification command = new CustomNotification();
            command.setSessionId(container.account);
            command.setSessionType(container.sessionType);
            CustomNotificationConfig config = new CustomNotificationConfig();
            config.enablePush = false;
            config.enableUnreadCount = false;
            command.setConfig(config);

            JSONObject json = new JSONObject();
            json.put("id", "1");
            command.setContent(json.toString());

            NIMClient.getService(MsgService.class).sendCustomNotification(command);
        }
    }

    /**
     * ************************* ?????????????????? *******************************
     */

    private View.OnClickListener clickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (v == switchToTextButtonInInputBar) {
                switchToTextLayout(true);// ???????????????????????????
            } else if (v == sendMessageButtonInInputBar) {
                //??????????????????
                if (sendTextListener == null) {
                    onTextMessageSendButtonPressed();
                } else {
                    sendTextListener.sendText(messageEditText.getText().toString());
                }
            } else if (v == switchToAudioButtonInInputBar) {
                switchToAudioLayout();
            } else if (v == moreFuntionButtonInInputBar) {
                toggleActionPanelLayout();
            } else if (v == emojiButtonInInputBar) {
                toggleEmojiLayout();
            }
        }
    };

    private FragmentActivity activity;

    public void setActivity(FragmentActivity activity) {
        this.activity = activity;
    }


    public interface SendTextListener {
        void sendText(String text);
    }

    private SendTextListener sendTextListener;

    public void setSendTextListener(SendTextListener listener) {
        sendTextListener = listener;
    }

    // ??????edittext??????????????????????????????
    private void switchToTextLayout(boolean needShowInput) {
        hideEmojiLayout();
        hideActionPanelLayout();

        audioRecordBtn.setVisibility(View.GONE);
        messageEditText.setVisibility(View.VISIBLE);
        switchToTextButtonInInputBar.setVisibility(View.GONE);
        switchToAudioButtonInInputBar.setVisibility(View.VISIBLE);

        messageInputBar.setVisibility(View.VISIBLE);

        if (needShowInput) {
            uiHandler.postDelayed(showTextRunnable, SHOW_LAYOUT_DELAY);
        } else {
            hideInputMethod();
        }
    }

    // ??????????????????
    public void onTextMessageSendButtonPressed() {
        String text = messageEditText.getText().toString();
        IMMessage textMessage = createTextMessage(text);

        if (container.proxy.sendMessage(textMessage)) {
            restoreText(true);
        }
    }

    protected IMMessage createTextMessage(String text) {
        return MessageBuilder.createTextMessage(container.account, container.sessionType, text);
    }

    // ??????????????????????????????????????????????????????
    @SuppressLint("CheckResult")
    private void switchToAudioLayout() {
        RxPermissions r = new RxPermissions(activity);
        r.request(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.RECORD_AUDIO
        ).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(final Boolean accept) {
                if (accept) {
                    messageEditText.setVisibility(View.GONE);
                    audioRecordBtn.setVisibility(View.VISIBLE);
                    hideInputMethod();
                    hideEmojiLayout();
                    hideActionPanelLayout();

                    switchToAudioButtonInInputBar.setVisibility(View.GONE);
                    switchToTextButtonInInputBar.setVisibility(View.VISIBLE);
                } else {
                    dialogAppSettings("????????????????????????????????????????????????");
                }
            }
        });
    }

    /**
     * ????????????app????????????
     *
     * @param msg
     */
    private void dialogAppSettings(String msg) {
        new AlertDialog.Builder(activity).setMessage(msg != null ? msg : "????????????????????????????????????????????????")
                .setNegativeButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("?????????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        // ???????????????
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        intent.setData(Uri.parse("package:com.ddcar"));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        activity.startActivity(intent);
                    }
                }).show();
    }

    // ?????????+??????????????????????????????????????????
    private void toggleActionPanelLayout() {
        if (actionPanelBottomLayout == null || actionPanelBottomLayout.getVisibility() == View.GONE) {
            showActionPanelLayout();
        } else {
            hideActionPanelLayout();
        }
    }

    // ????????????????????????????????????
    private void toggleEmojiLayout() {
        if (emoticonPickerView == null || emoticonPickerView.getVisibility() == View.GONE) {
            showEmojiLayout();
        } else {
            hideEmojiLayout();
        }
    }

    // ??????????????????
    private void hideEmojiLayout() {
        uiHandler.removeCallbacks(showEmojiRunnable);
        if (emoticonPickerView != null) {
            emoticonPickerView.setVisibility(View.GONE);
        }
    }

    // ??????????????????
    private void hideActionPanelLayout() {
        uiHandler.removeCallbacks(showMoreFuncRunnable);
        if (actionPanelBottomLayout != null) {
            actionPanelBottomLayout.setVisibility(View.GONE);
        }
    }

    // ??????????????????
    private void hideInputMethod() {
        isKeyboardShowed = false;
        uiHandler.removeCallbacks(showTextRunnable);
        InputMethodManager imm = (InputMethodManager) container.activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(messageEditText.getWindowToken(), 0);
        messageEditText.clearFocus();
    }

    // ??????????????????
    private void hideAudioLayout() {
        audioRecordBtn.setVisibility(View.GONE);
        messageEditText.setVisibility(View.VISIBLE);
        switchToTextButtonInInputBar.setVisibility(View.VISIBLE);
        switchToAudioButtonInInputBar.setVisibility(View.GONE);
    }

    // ??????????????????
    private void showEmojiLayout() {
        hideInputMethod();
        hideActionPanelLayout();
        hideAudioLayout();

        messageEditText.requestFocus();
        uiHandler.postDelayed(showEmojiRunnable, 200);
        emoticonPickerView.setVisibility(View.VISIBLE);
        emoticonPickerView.show(this);
        container.proxy.onInputPanelExpand();
    }

    // ?????????????????????
    private void addActionPanelLayout() {
        if (actionPanelBottomLayout == null) {
            View.inflate(container.activity, R.layout.nim_message_activity_actions_layout, messageActivityBottomLayout);
            actionPanelBottomLayout = view.findViewById(R.id.actionsLayout);
            actionPanelBottomLayoutHasSetup = false;
        }
        initActionPanelLayout();
    }

    // ??????????????????
    private void showInputMethod(EditText editTextMessage) {
        editTextMessage.requestFocus();
        //??????????????????,???????????????????????????????????????????????????
        if (!isKeyboardShowed) {
            editTextMessage.setSelection(editTextMessage.getText().length());
            isKeyboardShowed = true;
        }

        InputMethodManager imm = (InputMethodManager) container.activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editTextMessage, 0);

        container.proxy.onInputPanelExpand();
    }

    // ??????????????????
    private void showActionPanelLayout() {
        addActionPanelLayout();
        hideEmojiLayout();
        hideInputMethod();

        uiHandler.postDelayed(showMoreFuncRunnable, SHOW_LAYOUT_DELAY);
        container.proxy.onInputPanelExpand();
    }

    // ???????????????more layout????????????
    private void initActionPanelLayout() {
        if (actionPanelBottomLayoutHasSetup) {
            return;
        }
        //?????????????????????
        ActionsPanel.init(view, actions);
        actionPanelBottomLayoutHasSetup = true;
    }

    private Runnable showEmojiRunnable = new Runnable() {
        @Override
        public void run() {
            emoticonPickerView.setVisibility(View.VISIBLE);
        }
    };

    private Runnable showMoreFuncRunnable = new Runnable() {
        @Override
        public void run() {
            actionPanelBottomLayout.setVisibility(View.VISIBLE);
        }
    };

    private Runnable showTextRunnable = new Runnable() {
        @Override
        public void run() {
            showInputMethod(messageEditText);
        }
    };

    private void restoreText(boolean clearText) {
        if (clearText) {
            messageEditText.setText("");
        }

        checkSendButtonEnable(messageEditText);
    }

    /**
     * ?????????????????????
     *
     * @param editText
     */
    private void checkSendButtonEnable(EditText editText) {
        if (isRobotSession) {
            return;
        }
        String textMessage = editText.getText().toString();
        if (!TextUtils.isEmpty(StringUtil.removeBlanks(textMessage)) && editText.hasFocus()) {
            moreFuntionButtonInInputBar.setVisibility(View.GONE);
            sendMessageButtonInInputBar.setVisibility(View.VISIBLE);
        } else {
            sendMessageButtonInInputBar.setVisibility(View.GONE);
            moreFuntionButtonInInputBar.setVisibility(View.VISIBLE);
        }
    }

    /**
     * *************** IEmojiSelectedListener ***************
     */
    @Override
    public void onEmojiSelected(String key) {
        Editable mEditable = messageEditText.getText();
        if (key.equals("/DEL")) {
            messageEditText.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
        } else {
            int start = messageEditText.getSelectionStart();
            int end = messageEditText.getSelectionEnd();
            start = (start < 0 ? 0 : start);
            end = (start < 0 ? 0 : end);
            mEditable.replace(start, end, key);
        }
    }

    private Runnable hideAllInputLayoutRunnable;

    @Override
    public void onStickerSelected(String category, String item) {
        Log.i("InputPanel", "onStickerSelected, category =" + category + ", sticker =" + item);

        if (customization != null) {
            MsgAttachment attachment = customization.createStickerAttachment(category, item);
            IMMessage stickerMessage = MessageBuilder.createCustomMessage(container.account, container.sessionType, "????????????", attachment);
            container.proxy.sendMessage(stickerMessage);
        }
    }

    @Override
    public void onTextAdd(String content, int start, int length) {
        if (messageEditText.getVisibility() != View.VISIBLE ||
                (emoticonPickerView != null && emoticonPickerView.getVisibility() == View.VISIBLE)) {
            switchToTextLayout(true);
        } else {
            uiHandler.postDelayed(showTextRunnable, SHOW_LAYOUT_DELAY);
        }
        messageEditText.getEditableText().insert(start, content);
    }

    @Override
    public void onTextDelete(int start, int length) {
        if (messageEditText.getVisibility() != View.VISIBLE) {
            switchToTextLayout(true);
        } else {
            uiHandler.postDelayed(showTextRunnable, SHOW_LAYOUT_DELAY);
        }
        int end = start + length - 1;
        messageEditText.getEditableText().replace(start, end, "");
    }

    public int getEditSelectionStart() {
        return messageEditText.getSelectionStart();
    }


    /**
     * ????????????????????????
     */
    private void hideAllInputLayout(boolean immediately) {
        if (hideAllInputLayoutRunnable == null) {
            hideAllInputLayoutRunnable = new Runnable() {

                @Override
                public void run() {
                    hideInputMethod();
                    hideActionPanelLayout();
                    hideEmojiLayout();
                }
            };
        }
        long delay = immediately ? 0 : ViewConfiguration.getDoubleTapTimeout();
        uiHandler.postDelayed(hideAllInputLayoutRunnable, delay);
    }

    /**
     * ****************************** ?????? ***********************************
     */
    private void initAudioRecordButton() {
        audioRecordBtn.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    touched = true;
                    initAudioRecord();
                    onStartAudioRecord();
                } else if (event.getAction() == MotionEvent.ACTION_CANCEL
                        || event.getAction() == MotionEvent.ACTION_UP) {
                    touched = false;
                    onEndAudioRecord(isCancelled(nim_bottom_layer, event));
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    touched = true;
                    cancelAudioRecord(isCancelled(nim_bottom_layer, event));
                }

                return false;
            }
        });
    }

    private View nim_bottom_layer;

    // ????????????????????????
    private static boolean isCancelled(View view, MotionEvent event) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);

        if (event.getRawX() < location[0] || event.getRawX() > location[0] + view.getWidth()
                || event.getRawY() < location[1] - 40) {
            return true;
        }

        return false;
    }

    /**
     * ?????????AudioRecord
     */
    private void initAudioRecord() {
        if (audioMessageHelper == null) {
            UIKitOptions options = NimUIKitImpl.getOptions();
            audioMessageHelper = new AudioRecorder(container.activity, options.audioRecordType, options.audioRecordMaxTime, this);
        }
    }

    /**
     * ??????????????????
     */
    private void onStartAudioRecord() {
        container.activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        audioMessageHelper.startRecord();
        cancelled = false;
    }

    /**
     * ??????????????????
     *
     * @param cancel
     */
    private void onEndAudioRecord(boolean cancel) {
        started = false;
        container.activity.getWindow().setFlags(0, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        audioMessageHelper.completeRecord(cancel);
        audioRecordBtn.setText(R.string.record_audio_start);
        audioRecordBtn.setBackgroundResource(R.drawable.nim_message_input_edittext_box);
        stopAudioRecordAnim();
    }

    /**
     * ??????????????????
     *
     * @param cancel
     */
    private void cancelAudioRecord(boolean cancel) {
        // reject
        if (!started) {
            return;
        }
        // no change
        if (cancelled == cancel) {
            return;
        }

        cancelled = cancel;
        updateTimerTip(cancel);
    }

    private View nim_bottom_layer_bottom;
    private ImageView nim_bottom_layer_icon;

    /**
     * ????????????????????????????????????????????????????????????
     *
     * @param cancel
     */
    private void updateTimerTip(boolean cancel) {
        if (cancel) {
            timerTip.setText("?????? ??????");
            nim_bottom_layer_bottom.setBackgroundColor(Color.parseColor("#282828"));
            nim_bottom_layer.setBackgroundResource(R.drawable.shape_audio_record_foucs_bg_g);
            niu_close_icon.setImageResource(R.drawable.icon_close_recoding_g);
            text_audio_amplitude_layer.setBackgroundResource(R.drawable.shape_audio_record_orange_foucs_g);
            text_audio_amplitude_layer_bottom_icon.setImageResource(R.drawable.icon_recoding_cancel);
            nim_bottom_layer_icon.setImageResource(R.drawable.icon_voice_l_w);
//            timerTipContainer.setBackgroundResource(R.drawable.nim_cancel_record_red_bg);


        } else {
            timerTip.setText("????????????");
            nim_bottom_layer_bottom.setBackgroundColor(Color.parseColor("#D8D8D8"));
            nim_bottom_layer.setBackgroundResource(R.drawable.shape_audio_record_foucs_bg);
            niu_close_icon.setImageResource(R.drawable.icon_close_recoding);
            text_audio_amplitude_layer.setBackgroundResource(R.drawable.shape_audio_record_orange_foucs);
            text_audio_amplitude_layer_bottom_icon.setImageResource(R.drawable.icon_recoding);
            nim_bottom_layer_icon.setImageResource(R.drawable.icon_voice_l);
//            timerTipContainer.setBackgroundResource(0);
        }
    }

    private Handler handler = new Handler();

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            View view = text_audio_amplitude_layer.getChildAt(text_audio_amplitude_layer.getChildCount() - 1);
            text_audio_amplitude_layer.removeView(view);

            double minHeight = (8 * text_audio_amplitude_layer.getResources().getDisplayMetrics().density + 0.5f);
            double height = audioMessageHelper.getCurrentRecordMaxAmplitude() / 1250.0 * minHeight;
            if (height < minHeight) height = minHeight;

            ViewGroup.LayoutParams params = view.getLayoutParams();
            params.height = (int) height;
            view.setLayoutParams(params);
            text_audio_amplitude_layer.addView(view, 0);

            handler.postDelayed(this, 200);
        }
    };

    /**
     * ????????????????????????
     */
    private void playAudioRecordAnim() {
        audioAnimLayout.setVisibility(View.VISIBLE);
        time.setBase(SystemClock.elapsedRealtime());
        time.start();

        int minHeight = (int) (8 * text_audio_amplitude_layer.getResources().getDisplayMetrics().density + 0.5f);
        for (int i = 0; i < text_audio_amplitude_layer.getChildCount(); i++) {
            View v = text_audio_amplitude_layer.getChildAt(i);
            ViewGroup.LayoutParams params = v.getLayoutParams();
            params.height = minHeight;
            v.setLayoutParams(params);
        }

        handler.postDelayed(runnable, 200);

    }

    /**
     * ????????????????????????
     */
    private void stopAudioRecordAnim() {
        audioAnimLayout.setVisibility(View.GONE);
        time.stop();
        time.setBase(SystemClock.elapsedRealtime());

        handler.removeCallbacks(runnable);
    }

    // ??????????????????
    @Override
    public void onRecordReady() {

    }

    @Override
    public void onRecordStart(File audioFile, RecordType recordType) {
        started = true;
        if (!touched) {
            return;
        }

        audioRecordBtn.setText(R.string.record_audio_end);
        audioRecordBtn.setBackgroundResource(R.drawable.nim_message_input_edittext_box_pressed);

        updateTimerTip(false); // ???????????????????????????
        playAudioRecordAnim();
    }

    @Override
    public void onRecordSuccess(File audioFile, long audioLength, RecordType recordType) {
        IMMessage audioMessage = MessageBuilder.createAudioMessage(container.account, container.sessionType, audioFile, audioLength);
        container.proxy.sendMessage(audioMessage);
    }

    @Override
    public void onRecordFail() {
        if (started) {
            ToastHelper.showToast(container.activity, R.string.recording_error);
        }
    }

    @Override
    public void onRecordCancel() {

    }

    @Override
    public void onRecordReachedMaxTime(final int maxTime) {
        stopAudioRecordAnim();
        EasyAlertDialogHelper.createOkCancelDiolag(container.activity, "", container.activity.getString(R.string.recording_max_time), false, new EasyAlertDialogHelper.OnDialogActionListener() {
            @Override
            public void doCancelAction() {
            }

            @Override
            public void doOkAction() {
                audioMessageHelper.handleEndRecord(true, maxTime);
            }
        }).show();
    }

    public boolean isRecording() {
        return audioMessageHelper != null && audioMessageHelper.isRecording();
    }

    /**
     * ?????????????????????????????????data????????????null,????????????
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
//        int index = (requestCode << 16) >> 24;
//        if (index != 0) {
//            index--;
//            if (index < 0 | index >= actions.size()) {
//                LogUtil.d(TAG, "request code out of actions' range");
//                return;
//            }
//            BaseAction action = actions.get(index);
//            if (action != null) {
//                action.onActivityResult(requestCode & 0xff, resultCode, data);
//            }
//        }

        for (BaseAction action : actions) {
            action.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void switchRobotMode(boolean isRobot) {
        isRobotSession = isRobot;
        if (isRobot) {
            if (isTextAudioSwitchShow) {
                textAudioSwitchLayout.setVisibility(View.GONE);
            }
            emojiButtonInInputBar.setVisibility(View.GONE);
            sendMessageButtonInInputBar.setVisibility(View.VISIBLE);
            moreFuntionButtonInInputBar.setVisibility(View.GONE);
        } else {
            if (isTextAudioSwitchShow) {
                textAudioSwitchLayout.setVisibility(View.VISIBLE);
            }
            emojiButtonInInputBar.setVisibility(View.VISIBLE);
            sendMessageButtonInInputBar.setVisibility(View.GONE);
            moreFuntionButtonInInputBar.setVisibility(View.VISIBLE);
        }
    }

    /**
     * ??????????????????
     */
    public void hide() {
        messageActivityBottomLayout.setVisibility(View.GONE);
    }

    /**
     * ??????????????????
     */
    public void show() {
        messageActivityBottomLayout.setVisibility(View.VISIBLE);
    }

    /**
     * ??????????????? ??? ?????? ????????????
     */
    public void setSelectPurchaseAndOrderShow(boolean purchaseShow, boolean orderShow, boolean refundOrderShow) {
        layoutSelectPurchase.setVisibility(purchaseShow ? View.VISIBLE : View.GONE);
        layoutSelectOrder.setVisibility(orderShow ? View.VISIBLE : View.GONE);
        layoutSelectRefundOrder.setVisibility(refundOrderShow ? View.VISIBLE : View.GONE);
        changedShow();
    }

    /**
     * ??????????????? ??? ?????? ????????????
     */
    public void setSelectPurchaseAndOrderClickListener(View.OnClickListener purchaseListener, View.OnClickListener orderListener, View.OnClickListener refundOrderListener) {
        layoutSelectPurchase.setOnClickListener(purchaseListener);
        layoutSelectOrder.setOnClickListener(orderListener);
        layoutSelectRefundOrder.setOnClickListener(refundOrderListener);
    }

    private void changedShow() {
        int visibilityPurchase = layoutSelectPurchase.getVisibility();
        int visibilityOrder = layoutSelectOrder.getVisibility();

        if (visibilityPurchase == View.VISIBLE
                || visibilityOrder == View.VISIBLE
                || layoutSelectRefundOrder.getVisibility() == View.VISIBLE
        ) {
            LayoutSelectTag.setVisibility(View.VISIBLE);
        } else {
            LayoutSelectTag.setVisibility(View.GONE);
        }
    }
}
