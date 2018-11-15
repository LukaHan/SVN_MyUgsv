package com.tencent.qcloud.xiaoshipin.userinfo;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.tencent.qcloud.xiaoshipin.R;
import com.tencent.qcloud.xiaoshipin.common.widget.CircleImageView;
import com.tencent.qcloud.xiaoshipin.login.TCUserMgr;

/**
 * 用户资料展示页面
 */
public class TCUserInfoFragment extends Fragment {
    private CircleImageView ivHead;
    private TextView tvNickname;
    private TextView tvUserid;
    private TextView tvSign;
    private TextView tvRegion;
    private TextView tvZan;
    private TextView tvAttention;
    private TextView tvFans;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_info, container, false);
        ivHead = view.findViewById(R.id.iv_head);
        tvNickname = view.findViewById(R.id.tv_nickname);
        tvUserid = view.findViewById(R.id.tv_userid);
        tvSign = view.findViewById(R.id.tv_sign);
        tvRegion = view.findViewById(R.id.tv_region);
        tvZan = view.findViewById(R.id.tv_zan);
        tvAttention = view.findViewById(R.id.tv_attention);
        tvFans = view.findViewById(R.id.tv_fans);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TCUserMgr tcUserMgr  = TCUserMgr.getInstance();
        Glide.with(TCUserInfoFragment.this).applyDefaultRequestOptions(new RequestOptions().error(R.drawable.face)).load(tcUserMgr.getHeadPic()).into(ivHead);
        tvNickname.setText(tcUserMgr.getNickname());
        tvUserid.setText("ID: "+tcUserMgr.getUserId());
    }
}
