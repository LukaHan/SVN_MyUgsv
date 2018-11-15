package com.tencent.qcloud.xiaoshipin.mainui.list;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tencent.qcloud.xiaoshipin.R;
import com.tencent.qcloud.xiaoshipin.common.activity.FansAndCommentActivity;

/**
 * 粉丝、评论页
 */
public class TCMessageFragment extends Fragment implements View.OnClickListener {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);
        view.findViewById(R.id.rl_fans).setOnClickListener(this);
        view.findViewById(R.id.rl_comment).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(getActivity(), FansAndCommentActivity.class);
        if (view.getId() == R.id.rl_fans) {

        } else {

        }
        startActivity(intent);
    }
}