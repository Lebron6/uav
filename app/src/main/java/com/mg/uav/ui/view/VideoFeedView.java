package com.mg.uav.ui.view;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;

import com.mg.uav.callback.LiveShowStatusCallback;
import com.mg.uav.constant.Constant;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import dji.midware.usb.P3.UsbAccessoryService;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.sdkmanager.LiveStreamManager;
import dji.thirdparty.rx.Observable;
import dji.thirdparty.rx.Subscription;
import dji.thirdparty.rx.android.schedulers.AndroidSchedulers;
import dji.thirdparty.rx.functions.Action1;

import static android.media.MediaCodec.MetricsConstants.MIME_TYPE;

/**
 * VideoView will show the live video for the given video feed.
 */
public class VideoFeedView extends TextureView implements SurfaceTextureListener {
    //region Properties
    private final static String TAG = "DULFpvWidget";
    private DJICodecManager codecManager = null;
    private VideoFeeder.VideoDataListener videoDataListener = null;
    private int videoWidth;
    private int videoHeight;
    private AtomicLong lastReceivedFrameTime = new AtomicLong(0);
    private Context context;
    public VideoFeedView(Context context) {
        this(context, null, 0);
    }

    public VideoFeedView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoFeedView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context=context;
        init();
    }

    public DJICodecManager getCodecManager() {
        return codecManager;
    }

    private void init() {
        // Avoid the rending exception in the Android Studio Preview view.
        if (isInEditMode()) {
            return;
        }
        setSurfaceTextureListener(this);
        videoDataListener = new VideoFeeder.VideoDataListener() {
            @Override
            public void onReceive(byte[] videoBuffer, int size) {
                if (codecManager!=null){
//                    Logger.e("codecManager.isDecoderOK()="+codecManager.isDecoderOK());
                    codecManager.sendDataToDecoder(videoBuffer,size);
//                    if(codecManager.isDecoderOK()){
//                        Constant.H264FLAG=true;
//                    }
                }
            }
        };
    }

    //region Logic  理解为从dji实时获取到的图像编码，通过注册监听videoDataListener发送到解码器
    public VideoFeeder.VideoDataListener registerLiveVideo(VideoFeeder.VideoFeed videoFeed) {

        if (videoDataListener != null && videoFeed != null && !videoFeed.getListeners().contains(videoDataListener)) {
            videoFeed.addVideoDataListener(videoDataListener);
            return videoDataListener;
        }
        return null;
    }
    //这四个 视频相关
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (codecManager == null) {
            Logger.e("onSurfaceTextureAvailable");
            codecManager = new DJICodecManager(context, surface, width, height);
            //For M300RTK, you need to actively request an I frame.
            codecManager.resetKeyFrame();
            EventBus.getDefault().post(Constant.START_LIVE);
        }

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.e(TAG, "onSurfaceTextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.e(TAG, "onSurfaceTextureDestroyed");
        if (codecManager != null) {
            codecManager.cleanSurface();
            codecManager = null;
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
//        EventBus.getDefault().post(Constant.START_LIVE);
//        if (videoHeight != codecManager.getVideoHeight() || videoWidth != codecManager.getVideoWidth()) {
//            videoWidth = codecManager.getVideoWidth();
//            videoHeight = codecManager.getVideoHeight();
//            adjustAspectRatio(videoWidth, videoHeight);
//        }
    }

    public void changeSourceResetKeyFrame() {
        if (codecManager != null) {
            codecManager.resetKeyFrame();
        }
    }
    //endregion

    //region Helper method

    /**
     * This method should not to be called until the size of `TextureView` is fixed.
     */
    private void adjustAspectRatio(int videoWidth, int videoHeight) {

        int viewWidth = this.getWidth();
        int viewHeight = this.getHeight();
        double aspectRatio = (double) videoHeight / videoWidth;

        int newWidth, newHeight;
        if (viewHeight > (int) (viewWidth * aspectRatio)) {
            // limited by narrow width; restrict height
            newWidth = viewWidth;
            newHeight = (int) (viewWidth * aspectRatio);
        } else {
            // limited by short height; restrict width
            newWidth = (int) (viewHeight / aspectRatio);
            newHeight = viewHeight;
        }
        int xoff = (viewWidth - newWidth) / 2;
        int yoff = (viewHeight - newHeight) / 2;

        Matrix txform = new Matrix();
        this.getTransform(txform);
        txform.setScale((float) newWidth / viewWidth, (float) newHeight / viewHeight);
        txform.postTranslate(xoff, yoff);
        this.setTransform(txform);
    }
    //endregion



    MediaCodec mediaCodec;
    MediaFormat mediaFormat;
    ByteBuffer[] inputBuffers;
    ByteBuffer[] outputBuffers;
    MediaCodec.BufferInfo bufferInfo;
    public void initAndStart(int width, int height){
        try {
            mediaCodec = MediaCodec.createDecoderByType(MIME_TYPE);
        }catch (IOException e) {
            Log.e(TAG, "Error:" + e.toString());
        }
        //初始化解码器格式 预设宽高
        mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE, width, height);
        //设置帧率
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
            //fix 4.1 Bug
            mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 0);
        }
        mediaCodec.configure(mediaFormat, null, null, 0);
        mediaCodec.start();
        inputBuffers = mediaCodec.getInputBuffers();
        outputBuffers = mediaCodec.getOutputBuffers();
        bufferInfo = new MediaCodec.BufferInfo();
    }

    int inIndex;
    private void input(byte[] frameData) {
        ByteBuffer inputBuffer = null;
        //API>=21区分
        // 1.-1表示一直等待 2.0表示不等待 3.大于0表示等待的时间(us)
        inIndex = mediaCodec.dequeueInputBuffer(-1);
        if (inIndex >= 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                inputBuffer = mediaCodec.getInputBuffer(inIndex);
            } else {
                inputBuffer = inputBuffers[inIndex];
            }
            inputBuffer.clear();
            inputBuffer.put(frameData, 0, frameData.length);
            mediaCodec.queueInputBuffer(inIndex,0, frameData.length, 2000,0);
            inIndex = mediaCodec.dequeueInputBuffer(0);
        } else if(inIndex == -1);
    }
    public void release() {
        mediaCodec.stop();
        mediaCodec.release();
        mediaCodec = null;
        inputBuffers = null;
        outputBuffers = null;
    }
}
