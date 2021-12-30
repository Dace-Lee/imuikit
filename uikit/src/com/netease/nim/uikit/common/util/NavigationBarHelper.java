package com.netease.nim.uikit.common.util;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.nim.uikit.R;

/**
 * 标题栏帮助类
 *
 * @since 2015-3
 */
public class NavigationBarHelper {

    /**
     * 顶部标题栏
     */
    public ViewGroup mNavGroup;

    /**
     * 顶部标题栏左侧控制按钮所在视图
     */
    public ViewGroup mNavLeftGroup;

    /**
     * 顶部标题栏右侧控制按钮所在视图
     */
    public ViewGroup mNavRightGroup;

    /**
     * 顶部标题栏左侧控制按钮
     */
    public TextView mNavLeftTextControl;

    /**
     * 顶部标题栏右侧控制按钮
     */
    public ImageView mNavRightControl;
    public TextView mNavRightTextControl;
    public TextView mNavRightIconControl;
    /**
     * 顶部标题栏文本
     */
    public TextView mLabelTitle;

    /**
     * 当前Activity
     */
    private Activity mCurrentActivity;

    /**
     * 如果当前Activity是被内嵌的，返回父的Activity
     */
    private Activity mMainActivity;

    public NavigationBarHelper(Activity currentActivity, Activity mainActivity) {
        mCurrentActivity = currentActivity;
        mMainActivity = mainActivity;
        if (mMainActivity == null) {
            mMainActivity = mCurrentActivity;
        }
        initialNavControls();
    }
//	/**
//	 * 设置 可以双击顶部的地方 回滚到第一条
//	 */
//	public final static void setScrollToTop(Activity ac, OnDoubleClickListener doubleClick) {
//		TextView scrollView = (TextView) ac.findViewById(R.id.scroll_view_top);
//		if (scrollView != null) {
//			scrollView.setOnTouchListener(doubleClick);
//		}
//	}

    /**
     * 初始化标题栏按钮控件
     */
    private final void initialNavControls() {
        // 当前页面
        mNavGroup = (ViewGroup) mCurrentActivity.findViewById(R.id.include_nav);
        if (mNavGroup == null) {
            mNavGroup = (ViewGroup) mCurrentActivity.findViewById(R.id.nav_layout);
        }
        // 当前页面没有，父容器
        if (mNavGroup == null && mCurrentActivity.getParent() != null) {
            mNavGroup = (ViewGroup) mCurrentActivity.getParent().findViewById(R.id.include_nav);
            if (mNavGroup == null) {
                mNavGroup = (ViewGroup) mCurrentActivity.getParent().findViewById(R.id.nav_layout);
            }

            if (mNavGroup == null && mCurrentActivity.getParent().getParent() != null) {
                if (mNavGroup == null) {
                    mNavGroup = (ViewGroup) mCurrentActivity.getParent().getParent().findViewById(R.id.nav_layout);
                }
            }
        }

        if (mNavGroup != null) {
            mNavGroup.setVisibility(View.VISIBLE);

            if (mNavGroup.findViewById(R.id.nav_left_layout) != null) {
                mNavLeftGroup = (ViewGroup) mNavGroup.findViewById(R.id.nav_left_layout);
                mNavLeftTextControl = (TextView) mNavGroup.findViewById(R.id.nav_left_text);
            }

            if (mNavGroup.findViewById(R.id.nav_right_layout) != null) {
                mNavRightGroup = (ViewGroup) mNavGroup.findViewById(R.id.nav_right_layout);
                mNavRightTextControl = (TextView) mNavGroup.findViewById(R.id.nav_right_text);
                mNavRightIconControl = (TextView) mNavGroup.findViewById(R.id.nav_right_icon);
            }

            if (mNavGroup.findViewById(R.id.label_title) != null) {
                mLabelTitle = (TextView) mNavGroup.findViewById(R.id.label_title);
            }

            if (mNavRightTextControl != null) {
                mNavRightTextControl.setVisibility(View.INVISIBLE);
            }
            if (mNavLeftTextControl != null) {
                mNavLeftTextControl.setVisibility(View.INVISIBLE);
            }

            if (mNavLeftGroup != null) {
                mNavLeftGroup.setVisibility(View.INVISIBLE);
            }
            if (mNavRightGroup != null) {
                mNavRightGroup.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * 将应用程序顶部标题栏左侧按钮设置为取消，点击可以返回上一个界面
     */
    public final void setNavLeftGroupToTextCancel() {
        if (mNavLeftGroup != null) {
            mNavLeftGroup.setVisibility(View.VISIBLE);
            mNavLeftTextControl.setVisibility(View.VISIBLE);
            mNavLeftTextControl.setText(R.string.text_cancel);
            mNavLeftTextControl.setOnClickListener(BACK_CLICK_LISTENER);
        }
    }

    /**
     * 将应用程序顶部标题栏左侧按钮设置为取消，点击可以返回上一个界面
     */
    public final void setNavLeftGroupToTextCancelWithDown() {
        if (mNavLeftGroup != null) {
            mNavLeftGroup.setVisibility(View.VISIBLE);
            mNavLeftTextControl.setVisibility(View.VISIBLE);
            mNavLeftTextControl.setText(R.string.text_cancel);
            mNavLeftTextControl.setOnClickListener(BACK_CLICK_LISTENER_WITH_DOWN);
        }
    }

    /**
     * 将应用程序顶部标题栏左侧按钮设置为关闭，点击可以返回上一个界面
     */
    public final void setNavLeftGroupToTextClose() {
        if (mNavLeftGroup != null) {
            mNavLeftGroup.setVisibility(View.VISIBLE);
            mNavLeftTextControl.setVisibility(View.VISIBLE);
            mNavLeftTextControl.setText(R.string.text_close);
            mNavLeftTextControl.setOnClickListener(BACK_CLICK_LISTENER);
        }
    }

    /**
     * 将应用程序顶部标题栏左侧按钮设置为返回，点击可以返回上一个界面
     */
    public final void setNavLeftGroupToTextBack() {
        if (mNavLeftGroup != null) {
            mNavLeftGroup.setVisibility(View.VISIBLE);
            mNavLeftTextControl.setVisibility(View.VISIBLE);
            mNavLeftTextControl.setText(R.string.text_back);
            Drawable drawable = ContextCompat.getDrawable(mCurrentActivity, R.drawable.icon_white_arrow_left);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            mNavLeftTextControl.setCompoundDrawables(drawable, null, null, null);
            mNavLeftTextControl.setOnClickListener(BACK_CLICK_LISTENER);
        }
    }

    /**
     * 只有返回箭头 没有文本 将应用程序顶部标题栏左侧按钮设置为返回，点击可以返回上一个界面
     */
    public final void setNavLeftGroupToBack() {
        if (mNavLeftGroup != null) {
            mNavLeftGroup.setVisibility(View.VISIBLE);
            mNavLeftTextControl.setVisibility(View.VISIBLE);
            mNavLeftTextControl.setText("");
            Drawable drawable = ContextCompat.getDrawable(mCurrentActivity, R.drawable.icon_black_arrow_left);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            mNavLeftTextControl.setCompoundDrawables(drawable, null, null, null);
            mNavLeftTextControl.setOnClickListener(BACK_CLICK_LISTENER);
        }
    }

    /**
     * 将应用程序顶部标题栏左侧按钮设置为返回，点击可以返回上一个界面
     */
    public final void setNavLeftGroupToTextBackResult() {
        if (mNavLeftGroup != null) {
            mNavLeftGroup.setVisibility(View.VISIBLE);
            mNavLeftTextControl.setVisibility(View.VISIBLE);
            mNavLeftTextControl.setText(R.string.text_back);
            Drawable drawable = ContextCompat.getDrawable(mCurrentActivity, R.drawable.icon_white_arrow_left);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            mNavLeftTextControl.setCompoundDrawables(drawable, null, null, null);
            mNavLeftTextControl.setOnClickListener(BACK_RESULT_CLICK_LISTENER);
        }
    }

    /**
     * 将应用程序顶部标题栏左侧按钮设置指定点击事件
     */
    public final void setNavLeftGroupToTextBack(OnClickListener onClickListener) {
        if (mNavLeftGroup != null) {
            mNavLeftGroup.setVisibility(View.VISIBLE);
            mNavLeftTextControl.setVisibility(View.VISIBLE);
            Drawable drawable = ContextCompat.getDrawable(mCurrentActivity, R.drawable.icon_white_arrow_left);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            mNavLeftTextControl.setCompoundDrawables(drawable, null, null, null);
            mNavLeftTextControl.setText(R.string.text_back);
            mNavLeftTextControl.setOnClickListener(onClickListener);
        }
    }

    /**
     * 将应用程序顶部标题栏左侧按钮设置指定点击事件
     */
    public final void setNavLeftGroupToTextCancel(OnClickListener onClickListener) {
        if (mNavLeftGroup != null) {
            mNavLeftGroup.setVisibility(View.VISIBLE);
            mNavLeftTextControl.setVisibility(View.VISIBLE);
            mNavLeftTextControl.setText(R.string.text_cancel);
            mNavLeftTextControl.setOnClickListener(onClickListener);
        }
    }

    /**
     * 将应用程序顶部标题栏右侧按钮设置为text
     */
    public final void setNavRightGroupEvent(int textResId, OnClickListener listener) {
        setNavRightGroupEvent(mCurrentActivity.getString(textResId), listener);
    }

    /**
     * 将应用程序顶部标题栏右侧按钮设置为text
     */
    public final void setNavRightGroupEvent(String text, OnClickListener listener) {
        if (mNavRightGroup != null) {
            mNavRightGroup.setVisibility(View.VISIBLE);
            mNavRightIconControl.setVisibility(View.GONE);
            mNavRightTextControl.setVisibility(View.VISIBLE);
            mNavRightTextControl.setText(text);
            mNavRightTextControl.setOnClickListener(listener);
        }
    }

    /**
     * 将应用程序顶部标题栏右侧按钮设置为图片
     */
    public final void setNavRightGroupIconEvent(int id, OnClickListener listener) {
        if (mNavRightGroup != null) {
            mNavRightGroup.setVisibility(View.VISIBLE);
            mNavRightTextControl.setVisibility(View.GONE);
            mNavRightIconControl.setVisibility(View.VISIBLE);
            mNavRightIconControl.setBackgroundResource(id);
            mNavRightIconControl.setOnClickListener(listener);
        }
    }

    /**
     * 将应用程序顶部标题栏右侧按钮设置为text
     */
    public final void setNavLeftGroupEvent(int textResId, OnClickListener listener) {
        setNavLeftGroupEvent(mCurrentActivity.getString(textResId), listener);
    }

    /**
     * 将应用程序顶部标题栏左侧按钮设置为text
     */
    public final TextView setNavLeftGroupEvent(String text, OnClickListener listener) {
        if (mNavLeftGroup != null) {
            mNavLeftGroup.setVisibility(View.VISIBLE);
            mNavLeftTextControl.setVisibility(View.VISIBLE);
            mNavLeftTextControl.setText(text);
            mNavLeftTextControl.setOnClickListener(listener);
        }
        return mNavLeftTextControl;
    }

    /**
     * 回退
     */
    public final OnClickListener BACK_CLICK_LISTENER_WITH_DOWN = new OnClickListener() {

        @Override
        public void onClick(View v) {
            getMainActivity().finish();
        }

    };
    /**
     * 回退
     */
    public final OnClickListener BACK_CLICK_LISTENER = new OnClickListener() {

        @Override
        public void onClick(View v) {
            getMainActivity().finish();
        }

    };

    /**
     * 回退
     */
    public final OnClickListener BACK_RESULT_CLICK_LISTENER = new OnClickListener() {

        @Override
        public void onClick(View v) {
            getMainActivity().setResult(Activity.RESULT_OK);
            getMainActivity().finish();
        }

    };

    public Activity getMainActivity() {
        return mMainActivity;
    }


    /**
     * 清除 TextView 上下左右的 drawable 位图
     *
     * @param textview
     */
    public final static void cleanTextViewCompoundDrawables(TextView textview) {
        if (textview == null) {
            return;
        }
        textview.setCompoundDrawables(null, null, null, null);
    }

}
