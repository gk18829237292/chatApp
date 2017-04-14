package com.gk.chatapp;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.gk.chatapp.app.App;
import com.gk.chatapp.constant.Constant;
import com.gk.chatapp.utils.SprefUtils;
import com.gk.chatapp.utils.StringUtils;
import com.gk.chatapp.utils.ToastUtils;
import com.hobot.p2p.HrP2PEngine;
import com.hobot.p2p.signal.JsonSignalChannel;
import com.hobot.p2p.webtrc.PeerConnectionClient.PeerConnectionParameters;
import com.hobot.p2p.webtrc.PercentFrameLayout;
import com.hobot.p2p.webtrc.SnapshotVideoRenderer;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.RendererCommon.ScalingType;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * 视频通话页面
 */
public class VideoCallActivity extends Activity implements View.OnClickListener {

    private static final String TAG = VideoCallActivity.class.getSimpleName();

    // Peer connection statistics callback period in ms.
    private static final int STAT_CALLBACK_PERIOD = 1000;
    // Local preview screen position before call is connected.
    private static final int LOCAL_X_CONNECTING = 0;
    private static final int LOCAL_Y_CONNECTING = 0;
    private static final int LOCAL_WIDTH_CONNECTING = 100;
    private static final int LOCAL_HEIGHT_CONNECTING = 100;
    // Local preview screen position after call is connected.
    private static final int LOCAL_X_CONNECTED = 2;
    private static final int LOCAL_Y_CONNECTED = 1;
    private static final int LOCAL_WIDTH_CONNECTED = 22;
    private static final int LOCAL_HEIGHT_CONNECTED = 35;
    // Remote video screen position
    private static final int REMOTE_X = 0;
    private static final int REMOTE_Y = 0;
    private static final int REMOTE_WIDTH = 100;
    private static final int REMOTE_HEIGHT = 100;

    private static final String ACTION_PHONE_STATE = "android.intent.action.PHONE_STATE";
    private static final String ACTION_NEW_OUTGOING_CALL = "android.intent.action.NEW_OUTGOING_CALL";

    private final List<VideoRenderer.Callbacks> remoteRenderers = new ArrayList<>();
    private SurfaceViewRenderer localRender;
    private SurfaceViewRenderer remoteRenderScreen;
    private PercentFrameLayout localRenderLayout;
    private PercentFrameLayout remoteRenderLayout;
    private TextView mTvCallName, mTvPeerName;
    private Button mBtnMute, mBtnHung, mBtnSnap, mBtnChangeCamera, mBtnShowLog, mBtnClose, mBtnAccept, mBtnHandsFree;
    private String mGroupID, mPeerID, mPeerName;
    private PeerConnectionParameters peerConnectionParameters;
    private ScalingType scalingType;
    private EglBase rootEglBase;
    private int mIsCaller = Constant.IS_CALLER_YES;
    private String mSdpStr;
    private boolean mIsIceConnected;
    private SnapshotVideoRenderer mSnapshotRenderer;
    private boolean mCallControlFragmentVisible = true;
    private SprefUtils mPrefs;
    private View mViewCall, mViewPrevAnswer, mViewLogInfo, mViewOperation;
    // log信息
    private boolean mLogInfoShow = false;
    private boolean mIsShowDebug = false;
    private TextView mTvGroupPeers, mTvConnInfo, mTvIceInfo;
    private ListView mLvInfo, mLvReceiveInfo;
    private String mConnectionInfo = "";
    private String mIceInfo = "";
    private List<String> mSendLogInfo = new ArrayList<>();
    private List<String> mReceiveLogInfo = new ArrayList<>();
    private ArrayAdapter<String> mAdapterSend;
    private ArrayAdapter<String> mAdapterReceive;
    private MediaPlayer mMediaPlayer;
    private Vibrator mVibrator;


    private BroadcastReceiver mHangUpReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 关机、有电话的情况下，要挂断视频通话
            if (intent != null && (Intent.ACTION_SHUTDOWN.equals(intent.getAction())
                    || ACTION_PHONE_STATE.equals(intent.getAction())
                    || ACTION_NEW_OUTGOING_CALL.equals(intent.getAction()))) {
                Log.d(TAG, intent.getAction());
                finish();
            }
        }
    };

    private HrP2PEngine.HRP2PListener hrp2PListener = new HrP2PEngine.HRP2PListener() {
        @Override
        public void onSingalMessageReceived(String s) {
            try {
                final JSONObject json = new JSONObject(s);
                String operationStr = json.getString(JsonSignalChannel.Field.type.name());
                JsonSignalChannel.operations operation = JsonSignalChannel.operations.valueOf(operationStr);
                switch (operation) {
                    case group_peers: // 房间信息变化，就会同步各个终端 ，加入，离开，连接断开
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    mTvGroupPeers.setText(json.getJSONArray(JsonSignalChannel.Field.peers.name()).toString());
                                } catch (JSONException e) {
                                    Log.e(TAG, "JSON parse failed:" + e);
                                }
                            }
                        });
                        break;
                    case connect_err:
                        String message = json.getString(JsonSignalChannel.Field.message.name());
                        ToastUtils.showShortToast(getApplicationContext(), message);
                        Log.d(TAG, "finish in connect_err");
                        finish();
                        break;
                    case app_leaving:
                        // 收到App_leaving 结束
                        Log.d(TAG, "finish in app_leaving");
                        finish();
                        break;
                    case close_stream:
                        Log.d(TAG, "finish in close_stream");
                        finish();
                    default:
                        break;
                }
            } catch (JSONException e) {
                Log.e(TAG, "JSON parse failed:" + e);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Not supported signal type:" + e);
            }
        }

        @Override
        public void onOfferReceived(OfferInfo offerInfo) {

        }

        @Override
        public void onOfferReInvite(OfferInfo offerInfo) {
        }

        @Override
        public void onTimerCountDown() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.showShortToast(getApplicationContext(), "无响应");
                    Log.d(TAG, "finish in CountDownTimer");
                    finish();
                }
            });
        }

        @Override
        public void onTimerTick(long l) {

        }

        @Override
        public void onConnected() {
            mIsIceConnected = true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mViewCall.setVisibility(View.GONE);
                    mViewOperation.setVisibility(View.VISIBLE);
                    callConnected();
                }
            });
        }

        @Override
        public void onDisconnected() {
            mIsIceConnected = false;
//            App.getInstance().getHrP2PEngine().enableStatsEvents(false, STAT_CALLBACK_PERIOD);
        }

        @Override
        public void onIceCandidate(final IceCandidate candidate) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSendLogInfo.add(1, StringUtils.changeToString(candidate.sdp));
                    mAdapterSend.notifyDataSetChanged();
                }
            });
        }

        @Override
        public void onPeerConnectionStatsReady(final StatsReport[] statsReports) {

        }

        @Override
        public void onPeerConnectionStatueChange(final PeerConnection.SignalingState newState) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!StringUtils.isSpace(mConnectionInfo)) {
                        mConnectionInfo += " => ";
                    }
                    mConnectionInfo += newState.toString();
                    mTvConnInfo.setText("state: " + mConnectionInfo);
                    Log.d(TAG, "state: " + mConnectionInfo);
                }
            });
        }

        @Override
        public void onIceStatueChange(final PeerConnection.IceConnectionState iceConnectionState) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!StringUtils.isSpace(mIceInfo)) {
                        mIceInfo += " => ";
                    }
                    mIceInfo += iceConnectionState.toString();
                    mTvIceInfo.setText("ICE state: " + mIceInfo);
                    Log.d(TAG, "ICE state: " + mIceInfo);
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set window styles for fullscreen-window size. Needs to be done before
        // adding content.
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_KEEP_SCREEN_ON
                | LayoutParams.FLAG_SHOW_WHEN_LOCKED | LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        setContentView(R.layout.activity_video_call);

        getIntentData();
        initView();
        initData();

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Video is not paused for screencapture. See onPause.
        App.getInstance().getHrP2PEngine().startVideoSource();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        App.getInstance().getHrP2PEngine().stopVideoSource();
    }

    @Override
    protected void onDestroy() {
        if (mHangUpReceiver != null) {
            unregisterReceiver(mHangUpReceiver);
        }

        stopRing();
        App.getInstance().getHrP2PEngine().removeListener(hrp2PListener);
        disconnect();
        rootEglBase.release();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_accept:
                stopRing();
                //这里汇报 准备连接事件
                Countly.sharedInstance().recordEvent(Constant.COUNTLY_CONNECTING_ANSWER);
                App.getInstance().getHrP2PEngine().accept();
                mViewPrevAnswer.setVisibility(View.GONE);
                break;
            case R.id.btn_refuse:
            case R.id.btn_close:
                App.getInstance().getHrP2PEngine().hangup();
                Log.d(TAG, "finish in btn_close");
                finish();
                break;
            case R.id.btn_changeCamera:
                //切换本地摄像头
                App.getInstance().getHrP2PEngine().switchLocalCamera();
                break;
            case R.id.btn_mute:
                //静音（使对方听不到声音）
                boolean mute = App.getInstance().getHrP2PEngine().mute();
                if (mute) {
                    mBtnMute.setText("静音 关");
                } else {
                    mBtnMute.setText("静音 开");
                }
                break;
            case R.id.btn_hands_free:
                // 免提 SPEAKER_PHONE, WIRED_HEADSET, EARPIECE
                boolean handsFree = App.getInstance().getHrP2PEngine().handsFree();
                if (handsFree) {
                    mBtnHandsFree.setText("免提 开");
                } else {
                    mBtnHandsFree.setText("免提 关");
                }
                break;
            case R.id.btn_hung:
                // 挂断，不离开房间
                App.getInstance().getHrP2PEngine().hangup();
                Log.d(TAG, "finish in btn_hung");
                finish();
                break;
            case R.id.btn_snap:
                if (mSnapshotRenderer != null) {
                    mSnapshotRenderer.takeSnapshot();
                }
                break;
            case R.id.btn_showLog:
                mLogInfoShow = !mLogInfoShow;
                if (mLogInfoShow) {
                    mBtnShowLog.setText("隐藏Log");
                    mViewLogInfo.setVisibility(View.VISIBLE);
                } else {
                    mBtnShowLog.setText("显示Log");
                    mViewLogInfo.setVisibility(View.INVISIBLE);
                }
                break;
            case R.id.btn_debug:
                mIsShowDebug = !mIsShowDebug;
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                if (mIsShowDebug) {
                    ft.hide(hudFragment);
                } else {
                    ft.show(hudFragment);
                }
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.commit();
                break;
            default:
                break;
        }
    }

    /**
     * 接受传过来的参数
     */
    private void getIntentData() {
        mIsCaller = getIntent().getExtras().getInt(Constant.PARAM_IS_CALLER, Constant.IS_CALLER_YES);
        mGroupID = getIntent().getExtras().get(Constant.PARAM_GROUP_ID).toString();
        mPeerID = getIntent().getExtras().get(Constant.PARAM_PEER_ID).toString();
        mPeerName = getIntent().getExtras().get(Constant.PARAM_PEER_NAME).toString();
        if (mIsCaller != Constant.IS_CALLER_YES) {
            mSdpStr = getIntent().getExtras().get(Constant.PARAM_SDP).toString();
        }
    }

    /**
     * 初始化控件
     */
    private void initView() {
        mPrefs = new SprefUtils(this);
        localRender = (SurfaceViewRenderer) findViewById(R.id.local_video_view);
        remoteRenderScreen = (SurfaceViewRenderer) findViewById(R.id.remote_video_view);
        localRenderLayout = (PercentFrameLayout) findViewById(R.id.local_video_layout);
        remoteRenderLayout = (PercentFrameLayout) findViewById(R.id.remote_video_layout);
        mTvCallName = (TextView) findViewById(R.id.tv_call_name);
        mTvPeerName = (TextView) findViewById(R.id.tv_peer_name);
        mBtnClose = (Button) findViewById(R.id.btn_close);
        mBtnAccept = (Button) findViewById(R.id.btn_accept);
        mBtnChangeCamera = (Button) findViewById(R.id.btn_changeCamera);
        mBtnShowLog = (Button) findViewById(R.id.btn_showLog);
        mLvInfo = (ListView) findViewById(R.id.lv_logInfo);
        mLvReceiveInfo = (ListView) findViewById(R.id.lv_logReceiveInfo);
        mBtnMute = (Button) findViewById(R.id.btn_mute);
        mBtnHung = (Button) findViewById(R.id.btn_hung);
        mBtnSnap = (Button) findViewById(R.id.btn_snap);
        mTvGroupPeers = (TextView) findViewById(R.id.tv_group_peers);
        mTvConnInfo = (TextView) findViewById(R.id.tv_connInfo);
        mTvIceInfo = (TextView) findViewById(R.id.tv_iceInfo);
        mViewCall = findViewById(R.id.rl_call);
        mViewPrevAnswer = findViewById(R.id.rl_prev_answer);
        mViewLogInfo = findViewById(R.id.ll_log);
        mViewOperation = findViewById(R.id.ll_operation);
        mBtnHandsFree = (Button) findViewById(R.id.btn_hands_free);

        mBtnClose.setOnClickListener(this);
        mBtnAccept.setOnClickListener(this);
        (findViewById(R.id.btn_refuse)).setOnClickListener(this);
        mBtnMute.setOnClickListener(this);
        mBtnHandsFree.setOnClickListener(this);
        mBtnHung.setOnClickListener(this);
        mBtnSnap.setOnClickListener(this);
        mBtnChangeCamera.setOnClickListener(this);
        mBtnShowLog.setOnClickListener(this);
        findViewById(R.id.btn_debug).setOnClickListener(this);

        IntentFilter hangUpFilter = new IntentFilter();
        hangUpFilter.addAction(Intent.ACTION_SHUTDOWN);
        hangUpFilter.addAction(ACTION_PHONE_STATE);
        hangUpFilter.addAction(ACTION_NEW_OUTGOING_CALL);
        registerReceiver(mHangUpReceiver, hangUpFilter);

        if (mLogInfoShow) {
            mBtnShowLog.setText("隐藏Log");
            mViewLogInfo.setVisibility(View.VISIBLE);
        } else {
            mBtnShowLog.setText("显示Log");
            mViewLogInfo.setVisibility(View.INVISIBLE);
        }

        mIsIceConnected = false;
        scalingType = ScalingType.SCALE_ASPECT_BALANCED;

        // Show/hide call control fragment on view click.
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleCallControlFragmentVisibility();
            }
        };

        localRender.setOnClickListener(listener);
        remoteRenderScreen.setOnClickListener(listener);
        remoteRenderers.add(remoteRenderScreen);
        mLvInfo.setOnItemClickListener(null);
        mSendLogInfo.add("send Ice:");
        mAdapterSend = new ArrayAdapter<String>(this, R.layout.list_item_debug_info, mSendLogInfo);
        mReceiveLogInfo.add("reveive Ice:");
        mAdapterReceive = new ArrayAdapter<String>(this, R.layout.list_item_debug_info, mReceiveLogInfo);
        mLvInfo.setAdapter(mAdapterSend);
        mLvReceiveInfo.setAdapter(mAdapterReceive);
        mLvInfo.setDivider(null);
        mLvReceiveInfo.setDivider(null);

        // Create video renderers.
        rootEglBase = EglBase.create();
        localRender.init(rootEglBase.getEglBaseContext(), null);
        remoteRenderScreen.init(rootEglBase.getEglBaseContext(), null);

        // Create snapshot renderers.
        mSnapshotRenderer = new SnapshotVideoRenderer("/hobot/snapshot/", 480, 640, rootEglBase.getEglBaseContext());
        remoteRenderers.add(mSnapshotRenderer);

        localRender.setZOrderMediaOverlay(true);
        updateVideoView();

        /**
         PeerConnectionParameters(boolean videoCallEnabled, int videoWidth, int videoHeight, int videoFps, int videoMaxBitrate, String videoCodec,
         boolean videoCodecHwAcceleration, int audioStartBitrate, String audioCodec,
         boolean aecDump, boolean useOpenSLES, boolean disableBuiltInAEC, boolean disableBuiltInNS, boolean enableLevelControl)
         */
        boolean videoCallEnabled = mPrefs.getBoolean(Constant.KEY_DEBUG_VIDEO_CALL, true);
        int fps = mPrefs.getInt(Constant.KEY_DEBUG_FPS, 0);
        String videoCodec = mPrefs.getString(Constant.KEY_DEBUG_VIDEO_CODEC, Constant.DEFAULT_VIDEO_CODEC);
        // videoWidth videoHeight 是给对方传视频 设置的参数
        int videoWidth = 640;
        int videoHeight = 480;
        String videoResolution = mPrefs.getString(Constant.KEY_DEBUG_VIDEO_RESOLUTION, Constant.DEFAULT_VIDEO_RESOLUTION);
        if (videoResolution.contains("720")) {
            videoWidth = 1280;
            videoHeight = 720;
        } else if (videoResolution.contains("240")) {
            videoWidth = 320;
            videoHeight = 240;
        }
        String audioCodec = mPrefs.getString(Constant.KEY_DEBUG_AUDIO_CODEC, Constant.DEFAULT_AUDIO_CODEC);
        boolean aec = mPrefs.getBoolean(Constant.KEY_DEBUG_AEC, false);
        boolean ns = mPrefs.getBoolean(Constant.KEY_DEBUG_NS, false);
        peerConnectionParameters = new PeerConnectionParameters(videoCallEnabled, videoWidth, videoHeight, fps, 0, videoCodec,
                0, audioCodec, aec, ns);

        App.getInstance().getHrP2PEngine().initPeerConnection(rootEglBase.getEglBaseContext(), peerConnectionParameters, localRender, remoteRenderers);
        App.getInstance().getHrP2PEngine().addListener(hrp2PListener);

        if (mIsCaller == Constant.IS_CALLER_YES) {
            App.getInstance().getHrP2PEngine().startTimer();
            mViewCall.setVisibility(View.VISIBLE);
            mViewOperation.setVisibility(View.INVISIBLE);
            mViewPrevAnswer.setVisibility(View.GONE);
            mTvCallName.setText(mPeerName + " 拨通中...");
            // Create offer. Offer SDP will be sent to answering client in
            // PeerConnectionEvents.onLocalDescription event.
            Countly.sharedInstance().recordEvent(Constant.COUNTLY_CONNECTING_OFFER);
            App.getInstance().getHrP2PEngine().makeCall(mGroupID, mPeerID);
        } else {
            mViewCall.setVisibility(View.GONE);
            mViewOperation.setVisibility(View.VISIBLE);
            mViewPrevAnswer.setVisibility(View.VISIBLE);
            mTvPeerName.setText(mPeerName + " 邀请你视频聊天");
            startRing();

            App.getInstance().getHrP2PEngine().setRemoteDescription(SessionDescription.Type.OFFER, mSdpStr);
        }
    }

    /**
     * 初始化数据
     */
    private void initData() {
    }

    @Override
    public void onBackPressed() {
        // 屏蔽返回键
    }


    // Should be called from UI thread
    private void callConnected() {
        // Update video view.
        updateVideoView();
    }

    private void updateVideoView() {
        remoteRenderLayout.setPosition(REMOTE_X, REMOTE_Y, REMOTE_WIDTH, REMOTE_HEIGHT);
        remoteRenderScreen.setScalingType(scalingType);
        remoteRenderScreen.setMirror(false);

        if (mIsIceConnected) {
            localRenderLayout.setPosition(
                    LOCAL_X_CONNECTED, LOCAL_Y_CONNECTED, LOCAL_WIDTH_CONNECTED, LOCAL_HEIGHT_CONNECTED);
            localRender.setScalingType(ScalingType.SCALE_ASPECT_FIT);
        } else {
            localRenderLayout.setPosition(
                    LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING, LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING);
            localRender.setScalingType(scalingType);
        }
        localRender.setMirror(true);

        localRender.requestLayout();
        remoteRenderScreen.requestLayout();
    }

    // Disconnect from remote resources, dispose of local resources, and exit.
    private void disconnect() {

        if (localRender != null) {
            localRender.release();
            localRender = null;
        }
        if (remoteRenderScreen != null) {
            remoteRenderScreen.release();
            remoteRenderScreen = null;
        }
        if (mSnapshotRenderer != null) {
            mSnapshotRenderer.release();
            mSnapshotRenderer = null;
        }
    }

    private void toggleCallControlFragmentVisibility() {
        if (!mIsIceConnected) {
            //没有连接的时候 一直显示
            return;
        }
        mCallControlFragmentVisible = !mCallControlFragmentVisible;
        if (mCallControlFragmentVisible) {
            mViewOperation.setVisibility(View.VISIBLE);
        } else {
            mViewOperation.setVisibility(View.INVISIBLE);
        }
    }

    private void startRing() {
        mMediaPlayer = MediaPlayer.create(this, getSystemDefultRingtoneUri());
        mMediaPlayer.setLooping(true);
        try {
            mMediaPlayer.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.start();

        try {
            mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            long[] pattern = {800, 150, 400, 130}; // OFF/ON/OFF/ON...
            mVibrator.vibrate(pattern, 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopRing() {
        try {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (null != mVibrator) {
                mVibrator.cancel();
                mVibrator = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取系统默认铃声的Uri
     */
    private Uri getSystemDefultRingtoneUri() {
        return RingtoneManager.getActualDefaultRingtoneUri(this,
                RingtoneManager.TYPE_RINGTONE);
    }
}