package org.htd.android2js;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;


import com.google.gson.Gson;

import org.htd.activity.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by www on 2018/1/2.
 */

public class Android2Js extends Object {
    private Context context;
    private static List<Map<String, String>> resultList;

    public Android2Js(Context context) {
        this.context = context;
    }

    //给js提供设备ID
    @JavascriptInterface
    public String getRegistrationId() {
        Log.d("uuid", JPushInterface.getRegistrationID(context));
        return JPushInterface.getRegistrationID(context);
    }

    //关闭推送
    @JavascriptInterface
    public void stopPush() {
        JPushInterface.stopPush(context);
    }

    //恢复推送
    @JavascriptInterface
    public void resumePush() {
        JPushInterface.resumePush(context);
    }

    //调用扫码
    @JavascriptInterface
    public void callCode(int ex_order_id, int car_id, int ex_order_list_id) {
        List<Integer> infoCode = new ArrayList<>();
        infoCode.add(ex_order_id);
        infoCode.add(car_id);
        infoCode.add(ex_order_list_id);
        MainActivity.getInstance().ScanningRequest(infoCode);
    }

    //返回json数据给js
    @JavascriptInterface
    public String getScanningResult() {
        return new Gson().toJson(resultList);
    }

    public static void setScanningResult(List<Map<String, String>> jsonList) {
        resultList = new ArrayList<>();
        resultList.clear();
        resultList.addAll(jsonList);
    }
}
