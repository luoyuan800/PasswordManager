package com.zqx.pwd.global;

import com.zqx.pwd.util.SharedPreferencesUtil;


/**
 * Created by ZhangQixiang on 2017/4/7.
 */

public class GlobalData {

    private static boolean isHidePwd = SharedPreferencesUtil.getBoolean(Spkey.HIDE_PWD, true);//是否隐藏密码,默认隐藏

    public static boolean isHidePwd() {
        return isHidePwd;
    }

    public static void toggleHidePwd() {
        isHidePwd = !isHidePwd;
        SharedPreferencesUtil.putBoolean(Spkey.HIDE_PWD, isHidePwd);
    }

}
