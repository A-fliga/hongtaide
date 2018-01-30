package org.htd.activity;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.uuzuche.lib_zxing.activity.CaptureFragment;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import org.htd.R;
import org.htd.adapter.ScanningAdapter;
import org.htd.utils.DialogUtil;
import org.htd.utils.ToastUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by www on 2018/1/23.
 */

public class ScanningActivity extends AppCompatActivity {
    private int ex_order_id, car_id, ex_order_list_id;
    private List<Map<String, String>> jsonList;
    private ScanningAdapter adapter;
    private Boolean alReadyCommit = false;
    private CaptureFragment captureFragment;
    //闪光灯
    public static boolean isOpen = false;
    private RecyclerView scanning_info_recycler;
    private List<String> orderIdList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanning);
        getData();
        initFragment();
        initView();
    }

    private void getData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            ArrayList<Integer> infoList = bundle.getIntegerArrayList("infoCodeList");
            if (infoList != null && infoList.size() != 0) {
                ex_order_id = infoList.get(0);
                car_id = infoList.get(1);
                ex_order_list_id = infoList.get(2);
            }
        }
    }

    private void initFragment() {
        captureFragment = new CaptureFragment();
        // 为二维码扫描界面设置定制化界面
        CodeUtils.setFragmentArgs(captureFragment, R.layout.camera_view);
        captureFragment.setAnalyzeCallback(analyzeCallback);
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_my_container, captureFragment).commit();
    }

    private void initView() {
        jsonList = new ArrayList<>();
        scanning_info_recycler = (RecyclerView) findViewById(R.id.scanning_info_recycler);
        initRecycler(jsonList, scanning_info_recycler);
        ImageView scanning_back_img = (ImageView) findViewById(R.id.scanning_back_img);
        TextView scanning_commit = (TextView) findViewById(R.id.scanning_commit);
        scanning_back_img.setOnClickListener(onClickListener);
        scanning_commit.setOnClickListener(onClickListener);
        ImageView open_light = (ImageView) findViewById(R.id.open_light);
        open_light.setOnClickListener(onClickListener);
    }

    private void initRecycler(List<Map<String, String>> resultList, RecyclerView recyclerView) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(OrientationHelper.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new ScanningAdapter(R.layout.item_scanning, resultList);
        adapter.openLoadAnimation();
        recyclerView.setAdapter(adapter);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.scanning_back_img:
                    showMsg();
                    break;
                case R.id.scanning_commit:
                    alReadyCommit = true;
                    if (jsonList.size() == 0) {
                        ToastUtil.l("您还没有扫描货物");
                    } else {
                        MainActivity.getInstance().postData(jsonList);
                        finish();
//                        EventBus.getDefault().post(jsonList);
                    }
                    break;
                case R.id.open_light:
                    if (!isOpen) {
                        CodeUtils.isLightEnable(true);
                        isOpen = true;
                    } else {
                        CodeUtils.isLightEnable(false);
                        isOpen = false;
                    }
                    break;
            }
        }
    };

    private void showMsg() {
        if (!alReadyCommit && jsonList.size() != 0) {
            showMDialog("您还未提交结果，确定要退出吗", "不提交，退出", "马上去提交");
        } else finish();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return sure2Quit(keyCode, event);
    }

    private boolean sure2Quit(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (!alReadyCommit && jsonList.size() != 0) {
                showMDialog("您还未提交，确定要退出吗", "不提交，退出", "马上去提交");
                return false;
            } else return super.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }


    private void showMDialog(String message, String sure, String cancel) {
        DialogUtil.showDialog(this, message, sure, cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case -2:
                        dialog.dismiss();
                        break;
                    case -1:
                        finish();
                        break;
                }
            }
        });
    }

    /**
     * 二维码解析回调函数
     */
    CodeUtils.AnalyzeCallback analyzeCallback = new CodeUtils.AnalyzeCallback() {
        @Override
        public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
            addScanningData(result);
        }

        @Override
        public void onAnalyzeFailed() {
            ToastUtil.l("解析失败");
        }
    };


    private void addScanningData(String contents) {
        String[] str = contents.split(" ");
        List<String> contentList = new ArrayList<>();
        for (String aStr : str) {
            if (!(aStr.replaceAll(" ", "").isEmpty())) {
                contentList.add(aStr);
            }
        }
        if (contentList.size() != 4) {
            ToastUtil.l("二维码内容不符合规范，请检查");
        } else {
            String order_id = contentList.get(0);
            Boolean exist = false;
            for (int i = 0; i < orderIdList.size(); i++) {
                if (order_id.equals(orderIdList.get(i))) {
                    exist = true;
                }
            }
            if (!exist) {
                orderIdList.add(order_id);
                String number = String.valueOf(Integer.parseInt(contentList.get(contentList.size() - 1)) / 1000);
                String sum_number = String.valueOf(Integer.parseInt(contentList.get(1).substring(0, 12)) / 1000);
                StringBuffer sb = new StringBuffer();
                sb.append(contentList.get(1));
                sb.append("-");
                sb.append(contentList.get(2));
                sb.delete(0, 12);
                String serial_number = sb.toString();

                Map<String, String> jsonMap = new HashMap<>();
                jsonMap.put("ex_order_id", String.valueOf(ex_order_id));
                jsonMap.put("car_id", String.valueOf(car_id));
                jsonMap.put("ex_order_list_id", String.valueOf(ex_order_list_id));
                jsonMap.put("order_id", order_id);
                jsonMap.put("sum_number", sum_number);
                jsonMap.put("serial_number", serial_number);
                jsonMap.put("number", number);
                jsonList.add(jsonMap);
                adapter.notifyItemInserted(jsonList.size());
                scanning_info_recycler.smoothScrollToPosition(jsonList.size());
                Log.d("ceshi", new Gson().toJson(jsonList) + "");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
