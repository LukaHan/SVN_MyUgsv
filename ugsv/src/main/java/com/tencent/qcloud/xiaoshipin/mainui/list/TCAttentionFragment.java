package com.tencent.qcloud.xiaoshipin.mainui.list;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tencent.qcloud.xiaoshipin.R;

/**
 * 关注页
 */
public class TCAttentionFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_attention, container, false);
        return view;
    }

}