package com.tencent.qcloud.xiaoshipin.common.activity;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

/**
 * base activity to handle relogin info
 * Created by Administrator on 2016/9/20
 */
public class TCBaseActivity extends FragmentActivity {

    private static final String TAG = TCBaseActivity.class.getSimpleName();

    //错误消息弹窗
    private ErrorDialogFragment mErrDlgFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        mErrDlgFragment = new ErrorDialogFragment();
    }

    protected void showErrorAndQuit(String errorMsg) {
        if (!mErrDlgFragment.isAdded() && !this.isFinishing()) {
            Bundle args = new Bundle();
            args.putString("errorMsg", errorMsg);
            mErrDlgFragment.setArguments(args);
            mErrDlgFragment.setCancelable(false);

            //此处不使用用.show(...)的方式加载dialogfragment，避免IllegalStateException
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.add(mErrDlgFragment, "loading");
            transaction.commitAllowingStateLoss();
        }
    }
}
