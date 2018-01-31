package org.htd.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.htd.R;
import org.htd.device.DeviceUtil;
import org.htd.android2js.Android2Js;
import org.htd.utils.CheckPermissionUtils;
import org.htd.utils.NetUtil;
import org.htd.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cn.jpush.android.api.JPushInterface;

import static org.htd.constants.Constants.REQUEST_CAMERA_PERM;
import static org.htd.constants.Constants.REQUEST_CODE;

public class MainActivity extends AppCompatActivity {
    public static MainActivity activity;
    //用来接收扫码时服务器的参数
    private List<Integer> infoCode;
    private WebView webView;
    private Boolean canFinish = false;//按两次退出APP的标志
    private TimerTask task;
    private Timer timer = new Timer();
    private ImageView refresh;
    //是否第一次resume,是否加载完成了网页
    private Boolean isFirstResume = true, isLoaded = false;

    private static final String URL = "http://39.104.82.75/driver/index";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        EventBus.getDefault().register(this);
        activity = this;
        //初始化极光alias
        JPushInterface.setAlias(this, 1001, DeviceUtil.getUUID(this));
        initWeight();
        initView(true);
    }

    public static MainActivity getInstance() {
        return activity;
    }

    private void initWeight() {
        refresh = (ImageView) findViewById(R.id.refresh_btn);
        webView = (WebView) findViewById(R.id.logistics_webView);
    }

    private void initView(Boolean isCreate) {
        refresh.setOnClickListener(getOnClickListener(isCreate));
        if (NetUtil.isConnect()) {
            refresh.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
            if (isCreate)
                initWebView();
        } else {
            refresh.setVisibility(View.VISIBLE);
            webView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isFirstResume) {
            if (isLoaded)
                initView(false);
            else initView(true);
        }
        isFirstResume = false;
    }


    public void ScanningRequest(List<Integer> infoCode) {
        if (infoCode.size() != 0) {
            this.infoCode = infoCode;
            initPermission();
        }
    }


    public void postData(List<Map<String, String>> jsonList) {
        if (jsonList.size() != 0) {
            Log.d("ceshi", new Gson().toJson(jsonList) + "");
            Android2Js.setScanningResult(jsonList);
            webView.loadUrl("javascript:saveData()");
        }
    }

    private View.OnClickListener getOnClickListener(final Boolean isCreate) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.refresh_btn:
                        initView(isCreate);
                        break;
                }
            }
        };
    }


    private void initWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setSupportZoom(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setDefaultTextEncodingName("utf-8");
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        webSettings.setDomStorageEnabled(true);

        webSettings.setAppCacheMaxSize(1024 * 1024 * 8);

        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();

        webSettings.setAppCachePath(appCachePath);

        webSettings.setAllowFileAccess(true);

        webSettings.setAppCacheEnabled(true);
        webView.addJavascriptInterface(new Android2Js(this), "hongtaide");//Android2JS类对象映射到js的test对象
        webView.setWebViewClient(new webViewClient());
        webView.loadUrl(URL);
    }


    //Web视图
    private class webViewClient extends WebViewClient {
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            isLoaded = true;
        }
    }


    private void toScanning() {
        Intent intent = new Intent(MainActivity.this, ScanningActivity.class);
        Bundle bundle = new Bundle();
        bundle.putIntegerArrayList("infoCodeList", (ArrayList<Integer>) infoCode);
        intent.putExtras(bundle);
        startActivityForResult(intent, REQUEST_CODE);
    }

    /**
     * 初始化权限事件
     */
    private void initPermission() {
        //检查权限
        String[] permissions = CheckPermissionUtils.checkPermission(this);
        if (permissions.length != 0) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CAMERA_PERM);
        } else {
            toScanning();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CAMERA_PERM:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    toScanning();
                else
                    ToastUtil.l("没有相机权限,请在手机权限管理里打开");
                break;
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (!canFinish) {
                canFinish = true;
                Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        task = new TimerTask() {
                            @Override
                            public void run() {
                                canFinish = false;
                            }
                        };
                        timer.schedule(task, 2500);
                    }
                }).start();
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.clearHistory();
            ((ViewGroup) webView.getParent()).removeView(webView);
            webView.destroy();
            webView = null;
        }
        if (task != null) {
            task.cancel();
        }
//        EventBus.getDefault().unregister(this);
    }
}
