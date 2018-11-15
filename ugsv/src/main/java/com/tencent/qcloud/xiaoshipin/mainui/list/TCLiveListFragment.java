package com.tencent.qcloud.xiaoshipin.mainui.list;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.tencent.qcloud.xiaoshipin.R;
import com.tencent.qcloud.xiaoshipin.common.utils.CommonUtils;
import com.tencent.qcloud.xiaoshipin.common.utils.DownloadUtil;
import com.tencent.qcloud.xiaoshipin.common.utils.TCConstants;
import com.tencent.qcloud.xiaoshipin.common.utils.TCUtils;
import com.tencent.qcloud.xiaoshipin.common.widget.CircleImageView;
import com.tencent.qcloud.xiaoshipin.common.widget.VerticalViewPager;
import com.tencent.qcloud.xiaoshipin.login.TCUserMgr;
import com.tencent.qcloud.xiaoshipin.videorecord.TCVideoRecordActivity;
import com.tencent.rtmp.ITXVodPlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXVodPlayConfig;
import com.tencent.rtmp.TXVodPlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.ugc.TXRecordCommon;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoInfoReader;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 短视频列表页面
 * 界面展示使用：GridView+SwipeRefreshLayout
 * 列表数据Adapter：TCLiveListAdapter, TCUGCVideoListAdapter
 * 数据获取接口： TCLiveListMgr
 */
public class TCLiveListFragment extends Fragment implements ITXVodPlayListener, SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "TCLiveListFragment";

    private List<TCVideoInfo> mVideoList;
    private VerticalViewPager mVerticalViewPager;
    private TCLiveListFragment.MyPagerAdapter mPagerAdapter;
    private TXCloudVideoView mTXCloudVideoView;
    private TextView mTvBack;
    private ImageView mIvCover;
    // 合拍相关
    private Button mBtnFollowShot;
    private ProgressDialog mDownloadProgressDialog;
    private TXVideoInfoReader mVideoInfoReader;
    // 发布者id 、视频地址、 发布者名称、 头像URL、 封面URL
    private List<TCVideoInfo> mTCLiveInfoList;
    private int mInitTCLiveInfoPosition;
    private int mCurrentPosition;

    /**
     * SDK播放器以及配置
     */
    private TXVodPlayer mTXVodPlayer;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public void onRefresh() {
        reloadLiveList();
    }


    class PlayerInfo {
        public TXVodPlayer txVodPlayer;
        public String playURL;
        public boolean isBegin;
        public View playerView;
        public int pos;
        public int reviewstatus;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_videolist_player, container, false);

        //新增获取数据
        reloadLiveList();

        initDatas();
        initViews(view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initPlayerSDK();
        initPhoneListener();

        //在这里停留，让列表界面卡住几百毫秒，给sdk一点预加载的时间，形成秒开的视觉效果
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void initPhoneListener() {
        if (mPhoneListener == null)
            mPhoneListener = new TCLiveListFragment.TXPhoneStateListener(mTXVodPlayer);
        TelephonyManager tm = (TelephonyManager) getActivity().getApplicationContext().getSystemService(Service.TELEPHONY_SERVICE);
        tm.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
    }


    private void initDatas() {
//        Intent intent = getActivity().getIntent();
//        mTCLiveInfoList = (List<TCVideoInfo>) intent.getSerializableExtra(TCConstants.TCLIVE_INFO_LIST);
//        mInitTCLiveInfoPosition = intent.getIntExtra(TCConstants.TCLIVE_INFO_POSITION, 0);

        mTCLiveInfoList = mVideoList;
        mInitTCLiveInfoPosition = 0;
    }

    private void initViews(View view) {
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout_list);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mTXCloudVideoView = (TXCloudVideoView) view.findViewById(R.id.player_cloud_view);
        mIvCover = (ImageView) view.findViewById(R.id.player_iv_cover);
        mTvBack = (TextView) view.findViewById(R.id.player_tv_back);
        mTvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        mBtnFollowShot = (Button)view.findViewById(R.id.btn_follow_shot);
        mBtnFollowShot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mVideoInfoReader == null){
                    mVideoInfoReader = TXVideoInfoReader.getInstance();
                }
                // 上报合唱
                TCUserMgr.getInstance().uploadLogs(TCConstants.ELK_ACTION_VIDEO_CHORUS, TCUserMgr.getInstance().getUserId(), 0, "合唱事件", new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                    }
                });
                // 合拍之前先下载视频
                downloadVideo();
            }
        });
        mDownloadProgressDialog = new ProgressDialog(getActivity());
        mDownloadProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // 设置进度条的形式为圆形转动的进度条
        mDownloadProgressDialog.setCancelable(false);                           // 设置是否可以通过点击Back键取消
        mDownloadProgressDialog.setCanceledOnTouchOutside(false);               // 设置在点击Dialog外是否取消Dialog进度条

        mVerticalViewPager = (VerticalViewPager) view.findViewById(R.id.vertical_view_pager);
        mVerticalViewPager.setOffscreenPageLimit(3);
        mVerticalViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.i(TAG, "mVerticalViewPager, onPageScrolled position = " + position);
//                mCurrentPosition = position;
            }

            @Override
            public void onPageSelected(int position) {
                Log.i(TAG, "onPageSelected position = " + position);
                mCurrentPosition = position;
                // 滑动界面，首先让之前的播放器暂停，并seek到0
                Log.i(TAG, "滑动后，让之前的播放器暂停，mTXVodPlayer = " + mTXVodPlayer);
                if (mTXVodPlayer != null) {
                    mTXVodPlayer.seek(0);
                    mTXVodPlayer.pause();
                }

                if (position == 0) {
                    mSwipeRefreshLayout.setEnabled(true);
                } else {
                    mSwipeRefreshLayout.setEnabled(false);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        mVerticalViewPager.setPageTransformer(false, new ViewPager.PageTransformer() {
            @Override
            public void transformPage(View page, float position) {
//                Log.i(TAG, "mVerticalViewPager, transformPage pisition = " + position + " mCurrentPosition" + mCurrentPosition);
                if (position != 0) {
                    return;
                }

                ViewGroup viewGroup = (ViewGroup) page;
                mIvCover = (ImageView) viewGroup.findViewById(R.id.player_iv_cover);
                mTXCloudVideoView = (TXCloudVideoView) viewGroup.findViewById(R.id.player_cloud_view);

                TCLiveListFragment.PlayerInfo playerInfo = mPagerAdapter.findPlayerInfo(mCurrentPosition);
                if (playerInfo != null) {
                    playerInfo.txVodPlayer.resume();
                    mTXVodPlayer = playerInfo.txVodPlayer;
                }
            }
        });

        mPagerAdapter = new TCLiveListFragment.MyPagerAdapter();
        mVerticalViewPager.setAdapter(mPagerAdapter);
    }

    private void downloadVideo() {
        mDownloadProgressDialog.show();
        TCVideoInfo tcVideoInfo = mTCLiveInfoList.get(mCurrentPosition);
        File downloadFileFolder = new File(Environment.getExternalStorageDirectory(), TCConstants.OUTPUT_DIR_NAME);
        File downloadFile = new File(downloadFileFolder, DownloadUtil.getNameFromUrl(tcVideoInfo.playurl));

        if(downloadFile.exists()){
            mDownloadProgressDialog.dismiss();
            TXVideoEditConstants.TXVideoInfo txVideoInfo = mVideoInfoReader.getVideoFileInfo(downloadFile.getAbsolutePath());
            startRecordActivity(downloadFile.getAbsolutePath(), (int) txVideoInfo.fps, txVideoInfo.audioSampleRate);
            return;
        }
        mDownloadProgressDialog.setMessage("正在下载...");

        DownloadUtil.get().download(tcVideoInfo.playurl, TCConstants.OUTPUT_DIR_NAME, new DownloadUtil.DownloadListener() {
            @Override
            public void onDownloadSuccess(final String path) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDownloadProgressDialog.dismiss();
                        TXVideoEditConstants.TXVideoInfo txVideoInfo = mVideoInfoReader.getVideoFileInfo(path);
                        startRecordActivity(path, (int) txVideoInfo.fps, txVideoInfo.audioSampleRate);
                    }
                });
            }

            @Override
            public void onDownloading(final int progress) {
                Log.i(TAG, "downloadVideo, progress = " + progress);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDownloadProgressDialog.setMessage("正在下载..." + progress + "%");
                    }
                });
            }

            @Override
            public void onDownloadFailed() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDownloadProgressDialog.dismiss();
                        Toast.makeText(getActivity(), "下载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void startRecordActivity(String path, int fps, int audioSampleRate) {
        if(fps <= 0){
            fps = 20;
        }
        int audioSampleRateType = TXRecordCommon.AUDIO_SAMPLERATE_48000;
        if(audioSampleRate == 8000){
            audioSampleRateType = TXRecordCommon.AUDIO_SAMPLERATE_8000;
        }else if(audioSampleRate == 16000){
            audioSampleRateType = TXRecordCommon.AUDIO_SAMPLERATE_16000;
        }else if(audioSampleRate == 32000){
            audioSampleRateType = TXRecordCommon.AUDIO_SAMPLERATE_32000;
        }else if(audioSampleRate == 44100){
            audioSampleRateType = TXRecordCommon.AUDIO_SAMPLERATE_44100;
        }else{
            audioSampleRateType = TXRecordCommon.AUDIO_SAMPLERATE_48000;
        }
        Intent intent = new Intent(getActivity(), TCVideoRecordActivity.class);
        intent.putExtra(TCConstants.VIDEO_RECORD_TYPE, TCConstants.VIDEO_RECORD_TYPE_FOLLOW_SHOT);
        intent.putExtra(TCConstants.VIDEO_EDITER_PATH, path);
        intent.putExtra(TCConstants.VIDEO_RECORD_DURATION, mTXVodPlayer.getDuration());
        intent.putExtra(TCConstants.VIDEO_RECORD_AUDIO_SAMPLE_RATE_TYPE, audioSampleRateType);
        intent.putExtra(TCConstants.RECORD_CONFIG_FPS, fps);
        startActivity(intent);
    }

    class MyPagerAdapter extends PagerAdapter {

        ArrayList<TCLiveListFragment.PlayerInfo> playerInfoList = new ArrayList<>();


        protected TCLiveListFragment.PlayerInfo instantiatePlayerInfo(int position) {
            Log.d(TAG, "instantiatePlayerInfo " + position);

            TCLiveListFragment.PlayerInfo playerInfo = new TCLiveListFragment.PlayerInfo();
            TXVodPlayer vodPlayer = new TXVodPlayer(getActivity());
            vodPlayer.setRenderRotation(TXLiveConstants.RENDER_ROTATION_PORTRAIT);
            vodPlayer.setRenderMode(TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION);
            vodPlayer.setVodListener(TCLiveListFragment.this);
            TXVodPlayConfig config = new TXVodPlayConfig();
            config.setCacheFolderPath(Environment.getExternalStorageDirectory().getPath() + "/txcache");
            config.setMaxCacheItems(5);
            vodPlayer.setConfig(config);
            vodPlayer.setAutoPlay(false);

            TCVideoInfo tcLiveInfo = mTCLiveInfoList.get(position);
            playerInfo.playURL = TextUtils.isEmpty(tcLiveInfo.hlsPlayUrl) ? tcLiveInfo.playurl : tcLiveInfo.hlsPlayUrl;
            playerInfo.txVodPlayer = vodPlayer;
//            playerInfo.reviewstatus = tcLiveInfo.review_status;
            //设置先不用审核
            playerInfo.reviewstatus = 1;
            playerInfo.pos = position;
            playerInfoList.add(playerInfo);

            return playerInfo;
        }

        protected void destroyPlayerInfo(int position) {
            while (true) {
                TCLiveListFragment.PlayerInfo playerInfo = findPlayerInfo(position);
                if (playerInfo == null)
                    break;
                playerInfo.txVodPlayer.stopPlay(true);
                playerInfoList.remove(playerInfo);

                Log.d(TAG, "destroyPlayerInfo " + position);
            }
        }

        public TCLiveListFragment.PlayerInfo findPlayerInfo(int position) {
            for (int i = 0; i < playerInfoList.size(); i++) {
                TCLiveListFragment.PlayerInfo playerInfo = playerInfoList.get(i);
                if (playerInfo.pos == position) {
                    return playerInfo;
                }
            }
            return null;
        }

        public TCLiveListFragment.PlayerInfo findPlayerInfo(TXVodPlayer player) {
            for (int i = 0; i < playerInfoList.size(); i++) {
                TCLiveListFragment.PlayerInfo playerInfo = playerInfoList.get(i);
                if (playerInfo.txVodPlayer == player) {
                    return playerInfo;
                }
            }
            return null;
        }

        public void onDestroy() {
            for (TCLiveListFragment.PlayerInfo playerInfo : playerInfoList) {
                playerInfo.txVodPlayer.stopPlay(true);
            }
            playerInfoList.clear();
        }

        @Override
        public int getCount() {
            return mTCLiveInfoList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Log.i(TAG, "MyPagerAdapter instantiateItem, position = " + position);
            final TCVideoInfo tcLiveInfo = mTCLiveInfoList.get(position);

            View view = LayoutInflater.from(container.getContext()).inflate(R.layout.view_player_content_fg, null);
            view.setId(position);
            // 封面
            ImageView coverImageView = (ImageView) view.findViewById(R.id.player_iv_cover);
            TCUtils.blurBgPic(getActivity(), coverImageView, tcLiveInfo.frontcover, R.drawable.bg);
            // 头像
            CircleImageView ivAvatar = (CircleImageView) view.findViewById(R.id.player_civ_avatar);
            Glide.with(TCLiveListFragment.this).applyDefaultRequestOptions(new RequestOptions().error(R.drawable.face)).load(tcLiveInfo.avatar).into(ivAvatar);
            // 姓名
            TextView tvName = (TextView) view.findViewById(R.id.player_tv_publisher_name);
            if (TextUtils.isEmpty(tcLiveInfo.nickname) || "null".equals(tcLiveInfo.nickname)) {
                tvName.setText(TCUtils.getLimitString(tcLiveInfo.userid, 10));
            } else {
                tvName.setText(tcLiveInfo.nickname);
            }
            TextView tvTitle = (TextView) view.findViewById(R.id.tv_title);
            tvTitle.setText(tcLiveInfo.title);

            view.findViewById(R.id.iv_attention).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CommonUtils.showToast(getActivity(),"关注:"+tcLiveInfo.userid);
                }
            });

            // 获取此player
            TXCloudVideoView playView = (TXCloudVideoView) view.findViewById(R.id.player_cloud_view);
            TCLiveListFragment.PlayerInfo playerInfo = instantiatePlayerInfo(position);
            playerInfo.playerView = playView;
            playerInfo.txVodPlayer.setPlayerView(playView);

            if (playerInfo.reviewstatus == TCVideoInfo.REVIEW_STATUS_NORMAL) {
                playerInfo.txVodPlayer.startPlay(playerInfo.playURL);
            } else if (playerInfo.reviewstatus == TCVideoInfo.REVIEW_STATUS_NOT_REVIEW) { // 审核中
            } else if (playerInfo.reviewstatus == TCVideoInfo.REVIEW_STATUS_PORN) {       // 涉黄

            }

            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            Log.i(TAG, "MyPagerAdapter destroyItem, position = " + position);

            destroyPlayerInfo(position);

            container.removeView((View) object);
        }
    }

    private void initPlayerSDK() {
        mVerticalViewPager.setCurrentItem(mInitTCLiveInfoPosition);
    }

    private void restartPlay() {
        if (mTXVodPlayer != null) {
            mTXVodPlayer.resume();
        }
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (mTXCloudVideoView != null) {
//            mTXCloudVideoView.onResume();
//        }
//        if (mTXVodPlayer != null) {
//            mTXVodPlayer.resume();
//        }
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        if (mTXCloudVideoView != null) {
//            mTXCloudVideoView.onPause();
//        }
//        if (mTXVodPlayer != null) {
//            mTXVodPlayer.pause();
//        }
//    }
//
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (mTXCloudVideoView != null) {
//            mTXCloudVideoView.onDestroy();
//            mTXCloudVideoView = null;
//        }
//        stopPlay(true);
//        mTXVodPlayer = null;
//
//        if (mPhoneListener != null) {
//            TelephonyManager tm = (TelephonyManager) this.getApplicationContext().getSystemService(Service.TELEPHONY_SERVICE);
//            tm.listen(mPhoneListener, PhoneStateListener.LISTEN_NONE);
//            mPhoneListener = null;
//        }
//    }

    protected void stopPlay(boolean clearLastFrame) {
        if (mTXVodPlayer != null) {
            mTXVodPlayer.stopPlay(clearLastFrame);
        }
    }

    @Override
    public void onPlayEvent(TXVodPlayer player, int event, Bundle param) {
        if (event == TXLiveConstants.PLAY_EVT_CHANGE_RESOLUTION) {
            int width = param.getInt(TXLiveConstants.EVT_PARAM1);
            int height = param.getInt(TXLiveConstants.EVT_PARAM2);
            if (width > height) {
                player.setRenderRotation(TXLiveConstants.RENDER_ROTATION_LANDSCAPE);
            } else {
                player.setRenderRotation(TXLiveConstants.RENDER_ROTATION_PORTRAIT);
            }
        } else if (event == TXLiveConstants.PLAY_EVT_PLAY_END) {
            restartPlay();
        } else if (event == TXLiveConstants.PLAY_EVT_RCV_FIRST_I_FRAME) {// 视频I帧到达，开始播放

            TCLiveListFragment.PlayerInfo playerInfo = mPagerAdapter.findPlayerInfo(player);
            if (playerInfo != null) {
                playerInfo.isBegin = true;
            }
            if (mTXVodPlayer == player) {
                Log.i(TAG, "onPlayEvent, event I FRAME, player = " + player);
                mIvCover.setVisibility(View.GONE);
                TCUserMgr.getInstance().uploadLogs(TCConstants.ELK_ACTION_VOD_PLAY, TCUserMgr.getInstance().getUserId(), event, "点播播放成功", new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                    }
                });
            }
        } else if (event == TXLiveConstants.PLAY_EVT_VOD_PLAY_PREPARED) {
            if (mTXVodPlayer == player) {
                Log.i(TAG, "onPlayEvent, event prepared, player = " + player);
                mTXVodPlayer.resume();
            }
        } else if (event == TXLiveConstants.PLAY_EVT_PLAY_BEGIN) {
            TCLiveListFragment.PlayerInfo playerInfo = mPagerAdapter.findPlayerInfo(player);
            if (playerInfo != null && playerInfo.isBegin) {
                mIvCover.setVisibility(View.GONE);
                Log.i(TAG, "onPlayEvent, event begin, cover remove");
            }
        } else if (event < 0) {
            if (mTXVodPlayer == player) {
                Log.i(TAG, "onPlayEvent, event prepared, player = " + player);

                String desc = null;
                switch (event) {
                    case TXLiveConstants.PLAY_ERR_GET_RTMP_ACC_URL_FAIL:
                        desc = "获取加速拉流地址失败";
                        break;
                    case TXLiveConstants.PLAY_ERR_FILE_NOT_FOUND:
                        desc = "文件不存在";
                        break;
                    case TXLiveConstants.PLAY_ERR_HEVC_DECODE_FAIL:
                        desc = "h265解码失败";
                        break;
                    case TXLiveConstants.PLAY_ERR_HLS_KEY:
                        desc = "HLS解密key获取失败";
                        break;
                    case TXLiveConstants.PLAY_ERR_GET_PLAYINFO_FAIL:
                        desc = "获取点播文件信息失败";
                        break;
                }
                TCUserMgr.getInstance().uploadLogs(TCConstants.ELK_ACTION_VOD_PLAY, TCUserMgr.getInstance().getUserId(), event, desc, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i(TAG, "onFailure");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Log.i(TAG, "onResponse");
                    }
                });
            }
            Toast.makeText(getActivity(), "event:"+ event, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNetStatus(TXVodPlayer player, Bundle status) {

    }

//    @Override
//    protected void showErrorAndQuit(String errorMsg) {
//        mTXCloudVideoView.onPause();
//        stopPlay(true);
//        Intent rstData = new Intent();
//        rstData.putExtra(TCConstants.ACTIVITY_RESULT, errorMsg);
//        setResult(TCLiveListFragment.START_LIVE_PLAY, rstData);
//        super.showErrorAndQuit(errorMsg);
//    }

    /**
     * ==========================================来电监听==========================================
     */
    private PhoneStateListener mPhoneListener = null;

    static class TXPhoneStateListener extends PhoneStateListener {
        WeakReference<TXVodPlayer> mPlayer;

        public TXPhoneStateListener(TXVodPlayer player) {
            mPlayer = new WeakReference<TXVodPlayer>(player);
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            TXVodPlayer player = mPlayer.get();
            switch (state) {
                //电话等待接听
                case TelephonyManager.CALL_STATE_RINGING:
                    if (player != null) player.setMute(true);
                    break;
                //电话接听
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    if (player != null) player.setMute(true);
                    break;
                //电话挂机
                case TelephonyManager.CALL_STATE_IDLE:
                    if (player != null) player.setMute(false);
                    break;
            }
        }
    }


    /**
     * 新增，获取视频上下滑动列表页数据
     */
    private boolean reloadLiveList() {
        mVideoList = new ArrayList<TCVideoInfo>();
        TCVideoListMgr.getInstance().fetchUGCList(new TCVideoListMgr.Listener() {
            @Override
            public void onVideoList(final int retCode, final ArrayList<TCVideoInfo> result, final int index, final int total, final boolean refresh) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (retCode == 0) {
                                if (result != null) {
                                    mVideoList.addAll((ArrayList<TCVideoInfo>) result.clone());
                                    mPagerAdapter.notifyDataSetChanged();
                                    if(mSwipeRefreshLayout.isRefreshing()){
                                        mSwipeRefreshLayout.setRefreshing(false);
                                    }
                                }
                            } else {
                                Toast.makeText(getActivity(), "刷新列表失败", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
        return true;
    }
}
