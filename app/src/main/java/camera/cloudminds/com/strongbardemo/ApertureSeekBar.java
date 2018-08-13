package camera.cloudminds.com.strongbardemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class ApertureSeekBar extends StrongerBar  {

    private static final float COS_30 = 0.866025f;
    private static final float MIN_RADIUS = 17.5f/COS_30;
    private static final float MAX_RADIUS = 40f/COS_30;
    private static final int SPACE = 4;
    private static final int BLADE_COLOR = Color.parseColor("#FFFFFFFF");
    private static final int BACKGROUND_COLOR_ACTIVE = Color.parseColor("#20000000");

    public static final String[] APERTURE_VALUE = {"f/1.4", "f/2.0", "f/2.8", "f/4.0", "f/5.6", "f/8.0", "f/11", "f/16", "f/22"};
    private final static String TAG = "ApertureSeekBar";
    private int mCircleRadius;
    private int mWidth;
    private PointF[] mPoints = new PointF[6];
    private Bitmap mBlade;
    private Paint mPaint;
    private Path mPath;
    private Bitmap mBmp;
    private Canvas mBmpCanvas;

    public ApertureSeekBar(Context context) {
        this(context, null);
    }

    public ApertureSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ApertureSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mWidth = getThumbHeight();
        mCircleRadius = mWidth / 2;
        mBmp = Bitmap.createBitmap(mWidth, mWidth, Bitmap.Config.ARGB_8888);
        mBmpCanvas = new Canvas(mBmp);
        mPaint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        mPaint.setAntiAlias(true);
        for (int i = 0; i < 6; i++) {
            mPoints[i] = new PointF();
        }
        mPath = new Path();
        mPath.addCircle(0, 0, mCircleRadius, Path.Direction.CW);
        createBlade();
    }

    @Override
    public int getIndexFromProgress(int progress) {
        int index;
        if ((progress % 13) == 0) {
            index = progress / 13;
        } else {
            index = progress / 13 + 1;
        }
        return index;
    }

    private void createBlade() {
        mBlade = Bitmap.createBitmap(mCircleRadius,
                (int) (mCircleRadius * 2 * COS_30), Bitmap.Config.ARGB_8888);
        Path path = new Path();
        Canvas canvas = new Canvas(mBlade);
        path.moveTo(SPACE / 2 / COS_30, SPACE);
        path.lineTo(mBlade.getWidth(), mBlade.getHeight());
        path.lineTo(mBlade.getWidth(), SPACE);
        path.close();
        canvas.clipPath(path);
        canvas.drawColor(BLADE_COLOR);
    }


    private void calculatePoints(float progress) {
        if (mCircleRadius - SPACE <= 0) {
            Log.e(TAG, "the size of view is too small and Space is too large");
            return;
        }
        float curRadius = MIN_RADIUS + progress * (MAX_RADIUS-MIN_RADIUS);
        mPoints[0].x = curRadius / 2;
        mPoints[0].y = -curRadius * COS_30;
        mPoints[1].x = -mPoints[0].x;
        mPoints[1].y = mPoints[0].y;
        mPoints[2].x = -curRadius;
        mPoints[2].y = 0;
        mPoints[3].x = mPoints[1].x;
        mPoints[3].y = -mPoints[1].y;
        mPoints[4].x = -mPoints[3].x;
        mPoints[4].y = mPoints[3].y;
        mPoints[5].x = curRadius;
        mPoints[5].y = 0;
    }

    private Bitmap getBitmapFromProgress(float progress, boolean enabled) {
        Log.i(TAG, "onDraw: ");
        mBmpCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);//清理画布, 防止透明背景出现叠加
        mBmpCanvas.save();
        calculatePoints(progress);
        mBmpCanvas.translate(mWidth / 2, mWidth / 2);
        mBmpCanvas.clipPath(mPath);
        mBmpCanvas.drawColor(BACKGROUND_COLOR_ACTIVE);
        for (int i = 0; i < 6; i++) {
            mBmpCanvas.save();
            mBmpCanvas.translate(mPoints[i].x, mPoints[i].y);
            mBmpCanvas.rotate(-i * 60);
            mBmpCanvas.drawBitmap(mBlade , 0, 0, mPaint);
            mBmpCanvas.restore();
        }
        mBmpCanvas.restore();

        return mBmp;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isEnabled())
            return super.onTouchEvent(event);
        else
            return true;
    }

    @Override
    public Bitmap onThumbNeedAnimation(int currentPosition, int maxProgress, int radius) {
        return getBitmapFromProgress((currentPosition / (float) maxProgress), true);
    }

    @Override
    public String onBubbleTextNeedUpdate(int currentPosition, int maxProgress) {
        return APERTURE_VALUE[getIndexFromProgress(currentPosition)];
    }

    @Override
    public Bitmap onDisableState(int currentPosition, int maxProgress, int radius) {
        return getBitmapFromProgress((currentPosition / (float) maxProgress), false);
    }
}
