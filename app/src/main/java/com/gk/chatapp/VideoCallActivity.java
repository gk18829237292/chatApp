package com.gk.chatapp;

import android.app.Activity;
import android.content.Context;
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

import com.gk.chatapp.constant.Constant;
import com.gk.chatapp.p2p.PeerConnectionClient;
import com.gk.chatapp.p2p.PercentFrameLayout;
import com.gk.chatapp.p2p.SnapshotVideoRenderer;
import com.gk.chatapp.p2p.audiomanager.AppRTCAudioManager;
import com.gk.chatapp.utils.SprefUtils;
import com.gk.chatapp.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.Camera1Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.RendererCommon.ScalingType;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 视频通话页面
 */
public class VideoCallActivity extends Activity implements View.OnClickListener,PeerConnectionClient.PeerConnectionEvents {

    private static final String TAG = "VideoCallActivity";

    private static final String VIDEO_CODEC = "H264";
    private static final String AUDIO_CODEC = "opus";
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

    private final List<VideoRenderer.Callbacks> remoteRenderers = new ArrayList<>();
    private SurfaceViewRenderer localRender;
    private SurfaceViewRenderer remoteRenderScreen;
    private PercentFrameLayout localRenderLayout;
    private PercentFrameLayout remoteRenderLayout;
    private Button mBtnMute, mBtnHung, mBtnSnap, mBtnChangeCamera, mBtnClose, mBtnAccept, mBtnHandsFree;
    private PeerConnectionClient peerConnectionClient = null;
    private PeerConnectionClient.PeerConnectionParameters peerConnectionParameters;
    private ScalingType scalingType;
    private EglBase rootEglBase;
    private int mIsCaller = Constant.IS_CALLER_YES;
    private String mSdpStr;
    private boolean mMicEnabled = true;
    private boolean mIsIceConnected;
    private SnapshotVideoRenderer mSnapshotRenderer;
    private boolean mCallControlFragmentVisible = true;
    private boolean mIsMirror = true;
    private View mViewCall, mViewPrevAnswer, mViewOperation;
    // 确保对方EventBus注册之后，再发ICE信息，防止对方无法处理
    private boolean mCanSendIce = false;
    private LinkedList<IceCandidate> mQueuedLocalCandidates = new LinkedList<>();
    private AppRTCAudioManager audioManager = null;
    private MediaPlayer mMediaPlayer;
    private Vibrator mVibrator;

    private TextView mTvCallName,mTvPeerName;

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

        mIsIceConnected = false;
        scalingType = ScalingType.SCALE_ASPECT_BALANCED;
        initData();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Video is not paused for screencapture. See onPause.
        if (peerConnectionClient != null) {
            peerConnectionClient.startVideoSource();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Don't stop the video when using screencapture to allow user to show other apps to the remote end.
//        if (peerConnectionClient != null) {
//            peerConnectionClient.stopVideoSource();
//        }
    }

    @Override
    protected void onDestroy() {
        stopRing();
        disconnect();
        rootEglBase.release();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_accept:
                stopRing();
                if (peerConnectionClient != null) {
                    peerConnectionClient.createAnswer();
                }
                mViewPrevAnswer.setVisibility(View.GONE);
                break;
            case R.id.btn_refuse:
            case R.id.btn_close:
                //TODO close完善
//                MessageUtil.sendCloseStream(mGroupID, mPeerID);
                finish();
                break;
            case R.id.btn_changeCamera:
                //切换本地摄像头
                if (peerConnectionClient != null) {
                    mIsMirror = !mIsMirror;
                    localRender.setMirror(mIsMirror);
                    peerConnectionClient.switchCamera();
                }
                break;
            case R.id.btn_mute:
                //静音（使对方听不到声音）
                if (peerConnectionClient != null) {
                    mMicEnabled = !mMicEnabled;
                    peerConnectionClient.setAudioEnabled(mMicEnabled);
                    if (mMicEnabled) {
                        mBtnMute.setText("静音 关");
                    } else {
                        mBtnMute.setText("静音 开");
                    }
                }
                break;
            case R.id.btn_hands_free:
                // 免提 SPEAKER_PHONE, WIRED_HEADSET, EARPIECE
                if (audioManager.getSelectedAudioDevice().equals(AppRTCAudioManager.AudioDevice.EARPIECE)) {
                    audioManager.setAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
                    mBtnHandsFree.setText("免提 开");
                } else if (audioManager.getSelectedAudioDevice().equals(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE)) {
                    audioManager.setAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE);
                    mBtnHandsFree.setText("免提 关");
                }
                break;
            case R.id.btn_hung:
                // 挂断，不离开房间
//TODO                MessageUtil.sendCloseStream(mGroupID, mPeerID);
                finish();
                break;
            case R.id.btn_snap:
                if (mSnapshotRenderer != null) {
                    mSnapshotRenderer.takeSnapshot();
                }
                break;
        }
    }

    /**
     * 初始化控件
     */
    private void initView() {
        localRender = (SurfaceViewRenderer) findViewById(R.id.local_video_view);
        remoteRenderScreen = (SurfaceViewRenderer) findViewById(R.id.remote_video_view);
        localRenderLayout = (PercentFrameLayout) findViewById(R.id.local_video_layout);
        remoteRenderLayout = (PercentFrameLayout) findViewById(R.id.remote_video_layout);
        mTvCallName = (TextView) findViewById(R.id.tv_call_name);
        mTvPeerName = (TextView) findViewById(R.id.tv_peer_name);
        mBtnClose = (Button) findViewById(R.id.btn_close);
        mBtnAccept = (Button) findViewById(R.id.btn_accept);
        mBtnChangeCamera = (Button) findViewById(R.id.btn_changeCamera);
        mBtnMute = (Button) findViewById(R.id.btn_mute);
        mBtnHung = (Button) findViewById(R.id.btn_hung);
        mBtnSnap = (Button) findViewById(R.id.btn_snap);
        mViewCall = findViewById(R.id.rl_call);
        mViewPrevAnswer = findViewById(R.id.rl_prev_answer);
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
         *  PeerConnectionParameters(boolean videoCallEnabled, boolean loopback, boolean tracing,
         int videoWidth, int videoHeight, int videoFps, int videoMaxBitrate, String videoCoadaddec,
         boolean videoCodecHwAcceleration, int audioStartBitrate, String audioCodec,
         boolean noAudioProcessing, boolean aecDump, boolean useOpenSLES, boolean disableBuiltInAEC,
         boolean disableBuiltInAGC, boolean disableBuiltInNS, boolean enableLevelControl) {
         */
        // videoWidth videoHeight 是给对方传视频 设置的参数
        peerConnectionParameters =new PeerConnectionClient.PeerConnectionParameters(true, false, false, 640, 480, 30, 0, VIDEO_CODEC, true, 0, AUDIO_CODEC,
                        false, false, false, false, false, false, false);
        peerConnectionClient = PeerConnectionClient.getInstance();
        peerConnectionClient.createPeerConnectionFactory(
                VideoCallActivity.this, peerConnectionParameters, VideoCallActivity.this);
        VideoCapturer videoCapturer = null;
        if (peerConnectionParameters.videoCallEnabled) {
            videoCapturer = createVideoCapturer();
        }
        LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<>();
        //TODO
//        try {
//            String iceServerConfig = mPrefs.getString(Constant.KEY_ICE_SERVICES, "");
//            if (!StringUtils.isSpace(iceServerConfig)) {
//                JSONObject json = new JSONObject(iceServerConfig);
//                JSONArray jservers = json.getJSONArray(JsonSignalChannel.Field.iceServers.name());
//                for (int i = 0; i < jservers.length(); i++) {
//                    JSONObject jserver = jservers.getJSONObject(i);
//                    iceServers.add(new PeerConnection.IceServer(jserver.getString(JsonSignalChannel.Field.urls.name()),
//                            mPrefs.getString(Constant.KEY_DEVICE_ID, ""), WebSocketSignalClient.getPassword()));
//                }
//            }
//        } catch (JSONException e) {
//            Log.e(TAG, "JSON parse failed:" + e);
//        }
        if (iceServers.size() == 0) {
            // 为了保证连接成功，先设置一些默认的ice_services TODO 默认的iceserver需要确认

        }

        peerConnectionClient.createPeerConnection(rootEglBase.getEglBaseContext(), localRender,
                remoteRenderers, videoCapturer, iceServers);

        // 如果是主叫方，直接接受ice；如果是被叫方，同意发answer了，再接受主叫方发过来的ice
        if (mIsCaller == Constant.IS_CALLER_YES) {
            mViewCall.setVisibility(View.VISIBLE);
            mViewOperation.setVisibility(View.INVISIBLE);
            mViewPrevAnswer.setVisibility(View.GONE);
            //TODO 设置名称 mTvCallName.setText(mPeerName + " 拨通中...");
            // Create offer. Offer SDP will be sent to answering client in
            // PeerConnectionEvents.onLocalDescription event.
            peerConnectionClient.createOffer();
        } else {
            mViewCall.setVisibility(View.GONE);
            mViewOperation.setVisibility(View.VISIBLE);
            mViewPrevAnswer.setVisibility(View.VISIBLE);
            //TODO 显示名称mTvPeerName.setText(mPeerName + " 邀请你视频聊天");
            startRing();
            if (!StringUtils.isSpace(mSdpStr)) {
                mCanSendIce = true;
                SessionDescription sdp = new SessionDescription(SessionDescription.Type.OFFER, mSdpStr);
                peerConnectionClient.setRemoteDescription(sdp);
                // Create answer. Answer SDP will be sent to offering client in
                // PeerConnectionEvents.onLocalDescription event.
//                peerConnectionClient.createPrAnswer();
            }
        }

        // Create and audio manager that will take care of audio routing,
        // audio modes, audio device enumeration etc.
        audioManager = AppRTCAudioManager.create(this, new Runnable() {
            // This method will be called each time the audio state (number and
            // type of devices) has been changed.
            @Override
            public void run() {
            }
        });
        // Store existing audio settings and change audio mode to
        // MODE_IN_COMMUNICATION for best possible VoIP performance.
        Log.d(TAG, "Initializing the audio manager...");
        audioManager.init();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        mIsCaller = Constant.IS_CALLER_YES;
        // getIntent().getExtras().getInt(Constant.PARAM_IS_CALLER, Constant.IS_CALLER_YES);
//        mGroupID = getIntent().getExtras().get(Constant.PARAM_GROUP_ID).toString();
//        mPeerID = getIntent().getExtras().get(Constant.PARAM_PEER_ID).toString();
//        mPeerName = getIntent().getExtras().get(Constant.PARAM_PEER_NAME).toString();
//        if (mIsCaller != Constant.IS_CALLER_YES) {
//            mSdpStr = getIntent().getExtras().get(Constant.PARAM_SDP).toString();
//        }
    }

    @Override
    public void onBackPressed() {
        // 屏蔽返回键
    }

    // -----Implementation of PeerConnectionClient.PeerConnectionEvents.---------
    // Send local peer connection SDP and ICE candidates to remote party.
    // All callbacks are invoked from peer connection client looper thread and
    // are routed to UI thread.
    @Override
    public void onLocalDescription(final SessionDescription sdp) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (sdp == null) {
                    return;
                }
                String type = sdp.type.name().toUpperCase();
                if (SessionDescription.Type.OFFER.name().equals(type)) { // offer创建成功
  //TODO 发送OFFER                  MessageUtil.sendOffer(sdp, mGroupID, mPeerID);
                } else if (SessionDescription.Type.PRANSWER.name().equals(type)
                        || SessionDescription.Type.ANSWER.name().equals(type)) {
                    //TODO 发送answer MessageUtil.sendAnswer(sdp, mGroupID, mPeerID);
                }
                if (peerConnectionParameters.videoMaxBitrate > 0) {
                    Log.d(TAG, "Set video maximum bitrate: " + peerConnectionParameters.videoMaxBitrate);
                    peerConnectionClient.setVideoMaxBitrate(peerConnectionParameters.videoMaxBitrate);
                }
            }
        });
    }

    @Override
    public void onIceCandidate(final IceCandidate candidate) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mCanSendIce) {
                    //TODO 发送ICE MessageUtil.sendICECandidate(candidate, mGroupID, mPeerID);
                } else {
                    mQueuedLocalCandidates.add(candidate);
                }
            }
        });
    }

    @Override
    public void onIceCandidatesRemoved(final IceCandidate[] candidates) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //IceCandiate 不做处理
            }
        });
    }

    @Override
    public void onIceConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mIsIceConnected = true;
                callConnected();
            }
        });
    }

    @Override
    public void onIceDisconnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mIsIceConnected = false;
            }
        });
    }

    @Override
    public void onIceClosed() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mIsIceConnected = false;
                disconnect();
                finish();
            }
        });
    }

    @Override
    public void onPeerConnectionClosed() {
    }

    @Override
    public void onPeerConnectionStatsReady(final StatsReport[] reports) {
        // 可以监听debug信息
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (mIsIceConnected) {
//                    hudFragment.updateEncoderStatistics(reports);
//                }
//            }
//        });
    }

    @Override
    public void onPeerConnectionError(final String description) {
        // 处理错误
//        reportError(description);
    }

    @Override
    public void onPeerConnectionStatueChange(final PeerConnection.SignalingState newState) {
    }

    @Override
    public void onIceStatueChange(final PeerConnection.IceConnectionState newState) {
    }

    private VideoCapturer createVideoCapturer() {
        Log.d(TAG, "Creating capturer using camera1 API.");
        VideoCapturer videoCapturer = createCameraCapturer(new Camera1Enumerator(false));
        if (videoCapturer == null) {
//            reportError("Failed to open camera");
            return null;
        }
        return videoCapturer;
    }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        Log.d(TAG, "Looking for front facing cameras.");
        for (String deviceName : deviceNames) {
            Log.d(TAG, deviceName + " : " + enumerator.getSupportedFormats(deviceName).toString());
            if (enumerator.isFrontFacing(deviceName)) {
                Log.d(TAG, "Creating front facing camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        Log.d(TAG, "Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Log.d(TAG, "Creating other camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    // Should be called from UI thread
    private void callConnected() {
        if (peerConnectionClient == null) {
            Log.w(TAG, "Call is connected in closed or error state");
            return;
        }
        // Update video view.
        updateVideoView();
        // Enable statistics callback.
        peerConnectionClient.enableStatsEvents(true, STAT_CALLBACK_PERIOD);
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
        if (peerConnectionClient != null) {
            peerConnectionClient.close();
            peerConnectionClient = null;
        }
        if (localRender != null) {
            localRender.release();
            localRender = null;
        }
        if (remoteRenderScreen != null) {
            remoteRenderScreen.release();
            remoteRenderScreen = null;
        }
        if (audioManager != null) {
            audioManager.close();
            audioManager = null;
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