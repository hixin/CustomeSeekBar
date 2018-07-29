package camera.cloudminds.com.strongbardemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.View;

public class MainActivity extends AppCompatActivity implements StrongerBar.OnProgressChangedListener{
    private static final String TAG = "MainActivity";
    private StrongerBar adjust1;
    private StrongerBar adjust2;
    private StrongerBar adjust3;
    private StrongerBar adjust4;

    private int mLastRawOrientation;
    private int mOrientation;
    private MyOrientationEventListener mOrientationListener;
    // Orientation hysteresis amount used in rounding, in degrees
    public static final int ORIENTATION_HYSTERESIS = 15;

    private class MyOrientationEventListener
            extends OrientationEventListener {
        public MyOrientationEventListener(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            // We keep the last known orientation. So if the user first orient
            // the camera then point the camera to floor or sky, we still have
            // the correct orientation.
            if (orientation == ORIENTATION_UNKNOWN) {
                // use mUserGuiding to keep portrait while in user guide mode
                return;
            }
            mOrientation = roundOrientation(orientation, mLastRawOrientation);
            if (mOrientation != mLastRawOrientation) {
                mLastRawOrientation = mOrientation;
                adjust3.setOrientation(mOrientation, true);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        adjust1 = findViewById(R.id.bar1);
        adjust1.setOnProgressChangedListener(this);
        adjust2 = findViewById(R.id.bar2);
        adjust2.setOnProgressChangedListener(this);
        adjust3 = findViewById(R.id.bar3);
        adjust3.setOnProgressChangedListener(this);
        adjust4 = findViewById(R.id.bar4);
        adjust4.setOnProgressChangedListener(this);
        mOrientationListener = new MyOrientationEventListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mOrientationListener.enable();
    }

    @Override
    public void onProgressChanged(int progress, View v) {
        Log.i(TAG, "onProgressChanged: ");
    }

    public static int roundOrientation(int orientation, int orientationHistory) {
        boolean changeOrientation = false;//test
        if (orientationHistory == OrientationEventListener.ORIENTATION_UNKNOWN) {
            changeOrientation = true;
        } else {
            int dist = Math.abs(orientation - orientationHistory);
            dist = Math.min(dist, 360 - dist);
            changeOrientation = (dist >= 45 + ORIENTATION_HYSTERESIS);
        }
        if (changeOrientation) {
            return ((orientation + 45) / 90 * 90) % 360;
        }
        return orientationHistory;
    }

}
