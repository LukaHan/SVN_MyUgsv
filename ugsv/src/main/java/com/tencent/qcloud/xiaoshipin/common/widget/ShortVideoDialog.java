package com.tencent.qcloud.xiaoshipin.common.widget;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.qcloud.xiaoshipin.R;
import com.tencent.qcloud.xiaoshipin.videochoose.TCPictureChooseActivity;
import com.tencent.qcloud.xiaoshipin.videochoose.TCVideoChooseActivity;
import com.tencent.qcloud.xiaoshipin.videorecord.TCVideoRecordActivity;

/**
 * 短视频选择界面
 */
public class ShortVideoDialog extends DialogFragment implements View.OnClickListener {

    private TextView mTVVideo;
    private ImageView mIVClose;
    private TextView mTVEditer;
    private TextView mTVPicture;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Dialog dialog = new Dialog(getActivity(), R.style.BottomDialog);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_short_video);
        dialog.setCanceledOnTouchOutside(true);

        mTVVideo = (TextView) dialog.findViewById(R.id.tv_record);
        mTVEditer = (TextView) dialog.findViewById(R.id.tv_editer);
        mTVPicture = (TextView) dialog.findViewById(R.id.tv_picture);
        mIVClose = (ImageView) dialog.findViewById(R.id.iv_close);

        mTVVideo.setOnClickListener(this);
        mTVEditer.setOnClickListener(this);
        mTVPicture.setOnClickListener(this);
        mIVClose.setOnClickListener(this);

        // 设置宽度为屏宽, 靠近屏幕底部。
        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.BOTTOM;
        lp.width = WindowManager.LayoutParams.MATCH_PARENT; // 宽度持平
        window.setAttributes(lp);

        return dialog;
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.tv_record) {
            dismissDialog();
            startActivity(new Intent(getActivity(), TCVideoRecordActivity.class));

        } else if (i == R.id.tv_editer) {
            dismissDialog();

            Intent intent = new Intent(getActivity(), TCVideoChooseActivity.class);
            startActivity(intent);

        } else if (i == R.id.tv_picture) {
            dismissDialog();

            Intent intent2 = new Intent(getActivity(), TCPictureChooseActivity.class);
            startActivity(intent2);

        } else if (i == R.id.iv_close) {
            dismissDialog();

        }
    }

    private void dismissDialog() {
        if (ShortVideoDialog.this.isAdded()) {
            ShortVideoDialog.this.dismiss();
        }
    }
}
