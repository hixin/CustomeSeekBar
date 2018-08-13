package camera.cloudminds.com.strongbardemo;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ShutterSpeedSeekBar extends StrongerBar{
    private Drawable mShutterPointer;
    private Drawable mShutter;
    private Canvas mCanvas;
    private Resources mRes;
    private static int SHUTTERPOINT_WIDTH = 8;
    private static int SHUTTERPOINT_HEIGHT = 55;
    public static final String[] SHUTTER_SPEED_VALUE = {"1", "1/2", "1/4", "1/8", "1/15", "1/24", "1/25", "1/30", "1/48", "1/50", "1/60", "1/125", "1/250", "1/500", "1/1000"};
    public ShutterSpeedSeekBar(Context context) {
        this(context, null);
    }

    public ShutterSpeedSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShutterSpeedSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initResources();
    }

    private void initResources() {
        mRes = mContext.getResources();
        mCanvas = new Canvas();
        mShutter = mRes.getDrawable(R.drawable.red_shutter);
        mShutterPointer = mRes.getDrawable(R.drawable.shutter_pointer);
    }

    public Bitmap getThumbBitMap(int max, int progress, int width, boolean enabled) {
        float range = max / 180f;
        float v = progress / range;
        return drawableToBitmap(v, width);
    }

    public Bitmap drawableToBitmap(float range, int width) {
        Drawable bg = mShutter;
        Drawable pointer = mShutterPointer;

        Bitmap bitmap = Bitmap.createBitmap(
                width,
                width,
                Bitmap.Config.ARGB_8888);
        if (range <= 5) {
            range = 5;
        }
        if (range >= 175) {
            range = 175;
        }
        mCanvas.setBitmap(null);
        mCanvas.setBitmap(bitmap);
        bg.setBounds(0, 0, width, width);
        if (mIsVertical) {
            mCanvas.translate(width/2, width/2);
            mCanvas.rotate(90);
            mCanvas.translate(-width/2, -width/2);
        }
        bg.draw(mCanvas);
        pointer.setBounds((width - SHUTTERPOINT_WIDTH) / 2 , 3, (width + SHUTTERPOINT_WIDTH) / 2, SHUTTERPOINT_HEIGHT + 3);
        mCanvas.translate(width / 2, width / 2 );
        mCanvas.rotate(-90 + range);
        mCanvas.translate(-width / 2, -width / 2 );
        pointer.draw(mCanvas);
        return bitmap;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return !isEnabled() || super.onTouchEvent(event);
    }

    @Override
    public Bitmap onThumbNeedAnimation(int currentPosition, int maxProgress, int radius) {
        return getThumbBitMap(maxProgress, currentPosition, radius, true);
    }

    @Override
    public int getIndexFromProgress(int progress) {
        return progress / 10;
    }

    @Override
    public String onBubbleTextNeedUpdate(int currentPosition, int maxProgress) {
        return SHUTTER_SPEED_VALUE[getIndexFromProgress(currentPosition)];
    }
}
