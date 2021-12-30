package com.netease.nim.uikit.common.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.netease.nimlib.sdk.auth.LoginInfo;

/**
 * @author: xiangyun_liu
 * @date: 2019/2/28 16:32
 */
public class LoginInfoHelper {
    private static final String IM_LOGIN_INFO = "im_login_info";
    private static final String ACCOUNT = "account";
    private static final String TOKEN = "token";

    /**
     * 获取当前登录的用户
     *
     * @return 返回null为没有登录
     */
    public static LoginInfo getLoginInfo(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(IM_LOGIN_INFO, Context.MODE_PRIVATE);
        String account = preferences.getString(ACCOUNT, "");
        String token = preferences.getString(TOKEN, "");
        if (TextUtils.isEmpty(account) || TextUtils.isEmpty(token)) {
            return null;
        }
        return new LoginInfo(account, token);
    }


    /**
     * 保存当前登录的用户
     */
    public static void saveLoginInfo(Context context, LoginInfo loginInfo) {
        SharedPreferences preferences = context.getSharedPreferences(IM_LOGIN_INFO, Context.MODE_PRIVATE);
        preferences.edit().putString(ACCOUNT, loginInfo.getAccount()).commit();
        preferences.edit().putString(TOKEN, loginInfo.getAccount()).commit();
    }
}
