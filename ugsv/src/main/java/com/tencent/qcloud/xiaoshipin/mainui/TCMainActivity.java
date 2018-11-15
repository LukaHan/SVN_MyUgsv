package com.tencent.qcloud.xiaoshipin.mainui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.qcloud.xiaoshipin.R;
import com.tencent.qcloud.xiaoshipin.common.activity.TCBaseActivity;
import com.tencent.qcloud.xiaoshipin.common.utils.FileUtils;
import com.tencent.qcloud.xiaoshipin.common.utils.TCConstants;
import com.tencent.qcloud.xiaoshipin.common.widget.ShortVideoDialog;
import com.tencent.qcloud.xiaoshipin.mainui.list.TCAttentionFragment;
import com.tencent.qcloud.xiaoshipin.mainui.list.TCLiveListVpFragment;
import com.tencent.qcloud.xiaoshipin.mainui.list.TCMessageFragment;
import com.tencent.qcloud.xiaoshipin.userinfo.TCUserInfoFragment;

import java.io.File;
import java.io.IOException;

/**
 * 主界面: 短视频主页
 */
public class TCMainActivity extends TCBaseActivity implements View.OnClickListener {

    private TextView mBtnVideo, mBtnUser, mBtnAttention, mBtnMessage;
    private ImageButton mBtnSelect;
    private Fragment mCurrentFragment;
    private Fragment mTCLiveListVpFragment, mTCAttentionFragment, mTCMessageFragment, mTCUserInfoFragment;

    private long mLastClickPubTS = 0;

    private ShortVideoDialog mShortVideoDialog;
    private View vHome;
    private View vAttention;
    private View vMessage;
    private View vMine;
    private int index = 0;
    private LinearLayout flTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main_tc);
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
                if (checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    requestPermissions(permissions, REQUEST_CODE_CONTACT);
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
                String sdcardFolder = getExternalFilesDir(null).getAbsolutePath();
                File sdcardLicenceFile = new File(sdcardFolder + File.separator + TCConstants.UGC_LICENCE_NAME);
                if (sdcardLicenceFile.exists()) {
                    return;
                }
                try {
                    FileUtils.copyFromAssetToSdcard(TCMainActivity.this, TCConstants.UGC_LICENCE_NAME, sdcardFolder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void initView() {
        mBtnVideo = (TextView) findViewById(R.id.btn_home);
        mBtnAttention = (TextView) findViewById(R.id.btn_attention);
        mBtnSelect = (ImageButton) findViewById(R.id.btn_shot);
        mBtnMessage = (TextView) findViewById(R.id.btn_message);
        mBtnUser = (TextView) findViewById(R.id.btn_mine);
        vHome = findViewById(R.id.v_home);
        vAttention = findViewById(R.id.v_attention);
        vMessage = findViewById(R.id.v_message);
        vMine = findViewById(R.id.v_mine);
        flTab = findViewById(R.id.fl_tab);

        mBtnSelect.setOnClickListener(this);
        findViewById(R.id.rl_home).setOnClickListener(this);
        findViewById(R.id.rl_attention).setOnClickListener(this);
        findViewById(R.id.rl_message).setOnClickListener(this);
        findViewById(R.id.rl_mine).setOnClickListener(this);

        mShortVideoDialog = new ShortVideoDialog();
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
        if (i == R.id.btn_shot) {
            showSelect();
            index = 2;
        }
        if (index != 2) {
            RelativeLayout relativeLayout = (RelativeLayout) flTab.getChildAt(index);
            TextView textView = (TextView) relativeLayout.getChildAt(0);
            textView.setTextSize(15);
            textView.setTextColor(getResources().getColor(R.color.main_tab_unsel));
            relativeLayout.getChildAt(1).setVisibility(View.GONE);
        }
        if (i == R.id.rl_home) {
            showVideoFragment();
            vHome.setVisibility(View.VISIBLE);
            mBtnVideo.setTextSize(17);
            mBtnVideo.setTextColor(getResources().getColor(R.color.white));
            index = 0;
        } else if (i == R.id.rl_attention) {
            if (mTCAttentionFragment == null) {
                mTCAttentionFragment = new TCAttentionFragment();
            }
            showFragment(mTCAttentionFragment, "attention_fragment");
            vAttention.setVisibility(View.VISIBLE);
            mBtnAttention.setTextSize(17);
            mBtnAttention.setTextColor(getResources().getColor(R.color.white));
            index = 1;

        } else if (i == R.id.rl_message) {
            if (mTCMessageFragment == null) {
                mTCMessageFragment = new TCMessageFragment();
            }
            showFragment(mTCMessageFragment, "message_fragment");
            vMessage.setVisibility(View.VISIBLE);
            mBtnMessage.setTextSize(17);
            mBtnMessage.setTextColor(getResources().getColor(R.color.white));
            index = 3;
        } else if (i == R.id.rl_mine) {
            if (mTCUserInfoFragment == null) {
                mTCUserInfoFragment = new TCUserInfoFragment();
            }
            showFragment(mTCUserInfoFragment, "user_fragment");
            vMine.setVisibility(View.VISIBLE);
            mBtnUser.setTextSize(17);
            mBtnUser.setTextColor(getResources().getColor(R.color.white));
            index = 4;
        }
    }

    private void showSelect() {
        // 防止多次点击
        if (System.currentTimeMillis() - mLastClickPubTS > 500) {
            mLastClickPubTS = System.currentTimeMillis();
            if (mShortVideoDialog.isAdded())
                mShortVideoDialog.dismiss();
            else
                mShortVideoDialog.show(getFragmentManager(), "");
        }
    }

    private void showVideoFragment() {
        if (mTCLiveListVpFragment == null) {
            mTCLiveListVpFragment = new TCLiveListVpFragment();
        }
        showFragment(mTCLiveListVpFragment, "live_list_fragment");
    }

    private void showFragment(Fragment fragment, String tag) {
        if (fragment == mCurrentFragment) return;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
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
