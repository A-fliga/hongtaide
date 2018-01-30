package org.htd.app;

import android.app.Application;
import android.util.DisplayMetrics;

import com.uuzuche.lib_zxing.DisplayUtil;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by www on 2018/1/2.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //极光推送
        JPushInterface.init(this);
        JPushInterface.setDebugMode(true);

        /**
         * 初始化尺寸工具类
         */
        initDisplayOpinion();
    }

    private void initDisplayOpinion() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        DisplayUtil.density = dm.density;
        DisplayUtil.densityDPI = dm.densityDpi;
        DisplayUtil.screenWidthPx = dm.widthPixels;
        DisplayUtil.screenHighPx = dm.heightPixels;
        DisplayUtil.screenWidthDip = DisplayUtil.px2dip(getApplicationContext(), dm.widthPixels);
        DisplayUtil.screenHighDip = DisplayUtil.px2dip(getApplicationContext(), dm.heightPixels);
    }
}
