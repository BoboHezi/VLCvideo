package eli.per.vlcvideo;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "MainActivity";

    private SurfaceView surfaceView;
    private VlcPlayer vlcPlayer;
    private Button playerControl;
    private Button switchScreen;
    private Button screenShot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        playerControl = (Button) findViewById(R.id.playerControl);
        playerControl.setOnClickListener(this);

        switchScreen = (Button) findViewById(R.id.switchScreen);
        switchScreen.setOnClickListener(this);

        screenShot = (Button) findViewById(R.id.screenshot);
        screenShot.setOnClickListener(this);

        surfaceView = (SurfaceView) findViewById(R.id.surface);
        String url = "/sdcard/1/video.mov";

        //  /sdcard/1/video.mov
        //  rtsp://192.168.2.1/test.264
        //  http://live.hkstv.hk.lxdns.com/live/hks/playlist.m3u8
        //  http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear2/prog_index.m3u8
        //  rtmp://live.hkstv.hk.lxdns.com/live/hks
        //  rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov
        //  http://mirror.aarnet.edu.au/pub/TED-talks/911Mothers_2010W-480p.mp4

        vlcPlayer = new VlcPlayer(surfaceView, this, url);
        vlcPlayer.createPlayer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        vlcPlayer.play();
    }

    @Override
    protected void onPause() {
        super.onPause();
        vlcPlayer.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        vlcPlayer.releasePlayer();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.playerControl:
                if (vlcPlayer.isPlaying()) {
                    vlcPlayer.pause();
                    playerControl.setText("Start");
                }
                else {
                    vlcPlayer.play();
                    playerControl.setText("Stop");
                }
                break;

            case R.id.switchScreen:
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                }
                else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                }
                break;

            case R.id.screenshot:

                break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.i(TAG, "onConfigurationChanged: ");
        super.onConfigurationChanged(newConfig);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;

        LinearLayout.LayoutParams lp ;

        //横屏
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            lp = new LinearLayout.LayoutParams(screenWidth ,screenHeight);
            surfaceView.setLayoutParams(lp);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

        }
    }
}
