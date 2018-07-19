package com.tencent.qcloud.xiaoshipin.mainui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.tencent.qcloud.xiaoshipin.R;
import com.tencent.qcloud.xiaoshipin.userinfo.TCUserInfoFragment;

/**
 * 主界面: 短视频列表，用户信息页
 */
public class TCMainVpActivity extends FragmentActivity {

    private ViewPager mVpContent;
    private Fragment mTCMainFragment, mTCUserInfoFragment;
    private Fragment[] mFragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_vp);

        initView();
    }

    private void initView() {
        mVpContent = (ViewPager) findViewById(R.id.vpContent);

        mTCMainFragment = new TCMainFragment();
        mTCUserInfoFragment = new TCUserInfoFragment();

        mFragments = new Fragment[]{mTCMainFragment, mTCUserInfoFragment};

        mVpContent.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mFragments[position];
            }

            @Override
            public int getCount() {
                return mFragments.length;
            }
        });
    }
}
