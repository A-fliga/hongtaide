package org.htd.adapter;

import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import org.htd.R;

import java.util.List;
import java.util.Map;

/**
 * Created by www on 2018/1/23.
 */

public class ScanningAdapter extends BaseQuickAdapter<Map<String, String>, BaseViewHolder> {


    public ScanningAdapter(@LayoutRes int layoutResId, @Nullable List<Map<String, String>> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Map<String, String> item) {
        helper.setText(R.id.serial_number, helper.getAdapterPosition() + 1 + ".   " + item.get("serial_number"))
                .setText(R.id.number, item.get("number"));
    }
}
