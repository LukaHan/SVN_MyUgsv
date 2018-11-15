package com.tencent.qcloud.xiaoshipin.mainui.list;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tencent.qcloud.xiaoshipin.R;

public class TCLiveListVpFragment extends Fragment {
    private ViewPager mVpContent;
    private Fragment  mTCLiveListFragment,mTCLiveUserListFragment;
    private Fragment[] mFragments;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_vp, container, false);
        mVpContent = (ViewPager) view.findViewById(R.id.vpContent);
        initData();
        return view;
    }

    private void initData() {
        mTCLiveListFragment = new TCLiveListFragment();
        mTCLiveUserListFragment = new TCLiveUserListFragment();

        mFragments = new Fragment[]{mTCLiveListFragment,mTCLiveUserListFragment};

        mVpContent.setAdapter(new FragmentPagerAdapter(getChildFragmentManager()) {
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
