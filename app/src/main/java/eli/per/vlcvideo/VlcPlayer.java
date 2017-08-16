package eli.per.vlcvideo;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import org.videolan.libvlc.EventHandler;
import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaList;
import org.videolan.libvlc.Util;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VlcPlayer implements SurfaceHolder.Callback{

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Activity activity;
    private String url;

    private Handler handler = new SurfaceHandler(this);

    private LibVLC libVLC;
    private int viewWidth;
    private int viewHeight;
    private boolean isRecording = false;
    private static final int VideoSizeChanged = -1;
    private static final String TAG = "VlcPlayer";

    public VlcPlayer(SurfaceView surfaceView, Activity activity, String url) {
        this.surfaceView = surfaceView;
        this.activity = activity;
        this.url = url;

        this.surfaceView.setKeepScreenOn(true);
        this.surfaceHolder = surfaceView.getHolder();
        this.surfaceHolder.addCallback(this);
    }

    /**
     * 创建VLC显示
     */
    public void createPlayer() {
        releasePlayer();
        try {
            libVLC = Util.getLibVlcInstance();
            libVLC.setSubtitlesEncoding("");
            libVLC.setAout(LibVLC.AOUT_OPENSLES);
            libVLC.setTimeStretching(true);
            libVLC.setChroma("RV32");
            libVLC.setVerboseMode(true);
            libVLC.restart(activity);
            EventHandler.getInstance().addHandler(handler);
            surfaceHolder.setFormat(PixelFormat.RGBX_8888);
            surfaceHolder.setKeepScreenOn(true);
            MediaList list = libVLC.getMediaList();
            list.clear();
            Media media = new Media(libVLC, LibVLC.PathToURI(url));
            media.getWidth();
            media.getHeight();
            list.add(media, false);
            libVLC.playIndex(0);
        } catch (Exception e) {
        }
    }

    /**
     * 开始播放
     */
    public void play() {
        if (libVLC == null)
            createPlayer();
        libVLC.play();
    }

    /**
     * 暂停播放
     */
    public void pause() {
        if (libVLC != null)
            libVLC.pause();
    }

    /**
     * 释放资源
     */
    public void releasePlayer() {
        if (libVLC == null)
            return;
        EventHandler.getInstance().removeHandler(handler);
        libVLC.stop();
        libVLC.detachSurface();
        surfaceHolder = null;
        libVLC.closeAout();
        libVLC.destroy();
        libVLC = null;

        viewWidth = 0;
        viewHeight = 0;
    }

    /**
     * 是否正在播放
     * @return
     */
    public boolean isPlaying() {
        if (libVLC == null)
            return false;
        return libVLC.isPlaying();
    }

    /**
     * 截图
     */
    public void snapShot() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String path = "/sdcard/1/IMG_" + sdf.format(new Date()) + ".png";
            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }

            if (viewWidth > 0 && viewHeight > 0) {
                if(!libVLC.takeSnapShot(path, viewWidth, viewHeight)) {
                    file.delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 录像
     */
    public void screenRecord() {
        try {
            if (libVLC.videoIsRecording()) {
                libVLC.videoRecordStop();
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String path = "/sdcard/1/VID_" + sdf.format(new Date());
                libVLC.videoRecordStart(path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 重置画面
     * @param width     画面宽度
     * @param height    画面高度
     */
    private void setSize(int width, int height) {
        viewWidth = width;
        viewHeight = height;

        if (viewWidth * viewHeight <= 1)
            return;

        int screenWidth = activity.getWindow().getDecorView().getWidth();
        int screenHeight = activity.getWindow().getDecorView().getHeight();

        boolean isPortrait = activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        if ( (screenWidth > screenHeight && isPortrait) || (screenWidth < screenHeight && !isPortrait) ) {
            int i = screenWidth;
            screenWidth = screenHeight;
            screenHeight = i;
        }

        float videoAR = (float) viewWidth / (float) viewHeight;
        float screenAR = (float) screenWidth / (float) screenHeight;

        if (screenAR >= videoAR) {
            screenWidth = (int) (screenHeight / videoAR);
        }
        screenHeight = (screenWidth * 9) / 16;
        Log.i(TAG, "setSize: \n\tViewWidth:" + viewWidth + "\tViewHeight:" + viewHeight + "\n\tScreenWidth:" + screenWidth + "\tScreenHeight:" + screenHeight);

        surfaceHolder.setFixedSize(viewWidth, viewHeight);

        ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
        lp.width = screenWidth;
        lp.height = screenHeight;
        surfaceView.setLayoutParams(lp);
        surfaceView.invalidate();
    }

    //定义播放器接口
    IVideoPlayer videoPlayer = new IVideoPlayer() {
        @Override
        public void setSurfaceSize(int width, int height, int visible_width, int visible_height, int sar_num, int sar_den) {
            Log.e(TAG, "setSurfaceSize: \nwidth:" + width + "\theight:" + height + "\nvisible_width:" + visible_width + "\nvisible_height:" + visible_height);
            Message msg = Message.obtain(handler, VideoSizeChanged, width, height);
            msg.sendToTarget();
        }
    };

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (libVLC != null) {
            libVLC.attachSurface(holder.getSurface(), videoPlayer);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "\t\tWidth:" + width + "\n\t\tHeight:" + height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    class SurfaceHandler extends Handler {

        private WeakReference<VlcPlayer> owner;

        public SurfaceHandler(VlcPlayer owner) {
            this.owner = new WeakReference<VlcPlayer>(owner);
        }

        @Override
        public void handleMessage(Message msg) {
            VlcPlayer player = owner.get();

            if (msg.what == VideoSizeChanged) {
                player.setSize(msg.arg1, msg.arg2);
                return;
            }

            Bundle b = msg.getData();
            switch (b.getInt("event")) {

                case EventHandler.MediaPlayerEndReached:
                    player.releasePlayer();
                    break;
                case EventHandler.MediaPlayerPlaying:break;
                case EventHandler.MediaPlayerPaused:break;
                case EventHandler.MediaPlayerStopped:break;
                default:break;
            }
        }
    }
}
