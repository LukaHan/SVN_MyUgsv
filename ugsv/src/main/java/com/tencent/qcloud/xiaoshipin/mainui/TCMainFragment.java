package com.tencent.qcloud.xiaoshipin.mainui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.tencent.qcloud.xiaoshipin.R;
import com.tencent.qcloud.xiaoshipin.common.utils.FileUtils;
import com.tencent.qcloud.xiaoshipin.common.utils.TCConstants;
import com.tencent.qcloud.xiaoshipin.common.utils.TCUtils;
import com.tencent.qcloud.xiaoshipin.common.widget.ShortVideoDialog;
import com.tencent.qcloud.xiaoshipin.login.TCLoginActivity;
import com.tencent.qcloud.xiaoshipin.login.TCUserMgr;
import com.tencent.qcloud.xiaoshipin.mainui.list.TCAttentionFragment;
import com.tencent.qcloud.xiaoshipin.mainui.list.TCLiveListFragment;
import com.tencent.qcloud.xiaoshipin.mainui.list.TCLiveVideoListFragment;
import com.tencent.qcloud.xiaoshipin.mainui.list.TCMessageFragment;
import com.tencent.qcloud.xiaoshipin.userinfo.TCUserInfoFragment;

import java.io.File;
import java.io.IOException;

/**
 * 主界面: 短视频列表，用户信息页
 */
public class TCMainFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "TCMainActivity";

    private Button mBtnVideo, mBtnSelect, mBtnUser,mBtnAttention,mBtnMessage;
    private Fragment mCurrentFragment;
    private Fragment mTCLiveListFragment, mTCAttentionFragment,mTCMessageFragment,mTCUserInfoFragment;

    private long mLastClickPubTS = 0;

    private ShortVideoDialog mShortVideoDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_tc,container,false);
        mBtnVideo = (Button) view.findViewById(R.id.btn_home);
        mBtnSelect = (Button) view.findViewById(R.id.btn_shot);
        mBtnUser = (Button) view.findViewById(R.id.btn_mine);
        mBtnAttention = (Button) view.findViewById(R.id.btn_attention);
        mBtnMessage = (Button) view.findViewById(R.id.btn_message);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();

        showVideoFragment();

        if (checkPermission()) return;

        copyLicenceToSdcard();
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_CONTACT = 101;
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            //验证是否许可权限
            for (String str : permissions) {
                if (getActivity().checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    getActivity().requestPermissions(permissions, REQUEST_CODE_CONTACT);
                    return true;
                }
            }
        }
        return false;
    }

    private void copyLicenceToSdcard() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String sdcardFolder = getActivity().getExternalFilesDir(null).getAbsolutePath();
                File sdcardLicenceFile = new File(sdcardFolder + File.separator + TCConstants.UGC_LICENCE_NAME);
                if(sdcardLicenceFile.exists()){
                    return;
                }
                try {
                    FileUtils.copyFromAssetToSdcard(getActivity(), TCConstants.UGC_LICENCE_NAME, sdcardFolder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void initView() {
        mShortVideoDialog = new ShortVideoDialog();

        mBtnUser.setOnClickListener(this);
        mBtnVideo.setOnClickListener(this);
        mBtnSelect.setOnClickListener(this);
        mBtnAttention.setOnClickListener(this);
        mBtnMessage.setOnClickListener(this);
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        if (TextUtils.isEmpty(TCUserMgr.getInstance().getUserToken())) {
//            if (TCUtils.isNetworkAvailable(this) && TCUserMgr.getInstance().hasUser()) {
//                TCUserMgr.getInstance().autoLogin(null);
//            }
//        }
//    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_home) {
            showVideoFragment();

        } else if (i == R.id.btn_attention) {
            if (mTCAttentionFragment == null) {
                mTCAttentionFragment = new TCAttentionFragment();
            }
            showFragment(mTCAttentionFragment, "attention_fragment");

        } else if (i == R.id.btn_shot) {
            showSelect();

        }else if (i == R.id.btn_message) {
            if (mTCMessageFragment == null) {
                mTCMessageFragment = new TCMessageFragment();
            }
            showFragment(mTCMessageFragment, "message_fragment");

        }else if (i == R.id.btn_mine) {
            if (mTCUserInfoFragment == null) {
                mTCUserInfoFragment = new TCUserInfoFragment();
            }
            showFragment(mTCUserInfoFragment, "user_fragment");
        }
    }

    private void showSelect() {
        if (!TCUserMgr.getInstance().hasUser()) {
            Intent intent = new Intent(getActivity(), TCLoginActivity.class);
            startActivity(intent);
        } else {
            // 防止多次点击
            if (System.currentTimeMillis() - mLastClickPubTS > 1000) {
                mLastClickPubTS = System.currentTimeMillis();
                if (mShortVideoDialog.isAdded())
                    mShortVideoDialog.dismiss();
                else
                    mShortVideoDialog.show(getActivity().getFragmentManager(), "");
            }
        }
    }

    private void showUserFragment() {
//        mBtnVideo.setBackgroundResource(R.drawable.ic_home_video_normal);
//        mBtnUser.setBackgroundResource(R.drawable.ic_user_selected);
        if (mTCUserInfoFragment == null) {
            mTCUserInfoFragment = new TCUserInfoFragment();
        }
        showFragment(mTCUserInfoFragment, "user_fragment");
    }

    private void showVideoFragment() {
//        mBtnVideo.setBackgroundResource(R.drawable.ic_home_video_selected);
//        mBtnUser.setBackgroundResource(R.drawable.ic_user_normal);
        if (mTCLiveListFragment == null) {
//            mTCLiveListFragment = new TCLiveListFragment();
            mTCLiveListFragment = new TCLiveVideoListFragment();
        }
        showFragment(mTCLiveListFragment, "live_list_fragment");
    }

    private void showFragment(Fragment fragment, String tag) {
        if (fragment == mCurrentFragment) return;
//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        if (mCurrentFragment != null) {
            transaction.hide(mCurrentFragment);
        }
        if (!fragment.isAdded()) {
            transaction.add(R.id.contentPanel, fragment, tag);
        } else {
            transaction.show(fragment);
        }
        mCurrentFragment = fragment;
        transaction.commit();
    }

}
