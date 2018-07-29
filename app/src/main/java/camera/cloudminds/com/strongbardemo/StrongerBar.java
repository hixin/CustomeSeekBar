package camera.cloudminds.com.strongbardemo;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ArrayRes;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class StrongerBar extends View {
    private static final String TAG = "StrongerBar";
    private static final int MAX_PROGRESS_MOVE_TIME = 500;
    private static Matrix mMatrix;
    private int mBackgroundColor = 0x22222222;
    private int[] mColorSeeds = new int[]{0x30FFFFFF, 0xFF9900FF, 0xFF0000FF, 0xFF00FF00, 0xFF00FFFF, 0xFFFF0000, 0xFFFF00FF, 0xFFFF6600, 0xFFFFFF00, 0xFFFFFFFF, 0xFF000000};
    private int c0, c1, mAlpha, mRed, mGreen, mBlue;
    private int mTouchSlop;
    private float x, y;
    private float mThumbRadius;
    private int mThumbHeight = 20;
    private int mBarHeight = 2;

    private static final int ANIMATION_SPEED = 270; // 270 deg/sec
    private int mCurrentDegree = 0; // [0, 359]
    private int mStartDegree = 0;
    private int mTargetDegree = 0;
    private boolean mClockwise = false, mEnableAnimation = true;
    private long mAnimationStartTime = 0;
    private long mAnimationEndTime = 0;

    private OnStateChangeListener mOnStateChangeListener;
    private OnProgressChangedListener mOnProgressChangedListener;
    private OnInitDoneListener mOnInitDoneListener;
    private OnLongClickRunnable mOnLongClickRunnable;

    private LinkedList<Integer> mIndexQueue;
    private List<Integer> mColors = new ArrayList<>();

    private boolean mIsShowAlphaBar = false;
    private boolean mIsVertical;
    private boolean mMovingColorBar;
    private boolean mMovingAlphaBar;
    private boolean mRotatable;

    private Bitmap mTransparentBitmap;
    private Bitmap mThumbBitmap;
    private Canvas mThumbCanvas;

    private RectF mColorRect;
    private RectF mSecondRect;
    private RectF mAlphaRect;
    private RectF mColorStroke;
    private RectF rf;
    private Rect mBubbleBounds;
    private RectF mBubbleBoundsRectF;


    private LinearGradient mColorGradient;

    private Paint mColorRectPaint;
    private Paint mColorPaint;
    private Paint mClearPaint;
    private TextPaint mTextPaint;

    private int realLeft;
    private int realRight;
    private int realTop;
    private int realBottom;
    private int mBarWidth;
    private int mMaxPosition;

    private int mCurrentPosition;
    private int mAlphaBarPosition;
    private int mBarMargin = 0;
    private int mViewWidth;
    private int mViewHeight;
    private int mAlphaMinPosition = 0;
    private int mAlphaMaxPosition = 255;

    private int mColorsToInvoke = -1;
    private boolean mInit = false;
    private boolean mFirstDraw = true;
    private int mSencondColor;
    private String mTextFacePath;
    private int mFrameColor;
    private int mFrameWidth;
    private int mBubbleMargin;
    private int mBubbleWidth;
    private int mBubbleHeight;
    private int mBubbleTextSize;

    private boolean mIsShowBubble;
    private int mOrientation;
    private boolean mColorChangeCallBack;
    private float mPreviousPointer = 0;

    private float downX;


    private float mMoveIntervalRate;
    private boolean mIsInAnimation = false;
    protected Context mContext;
    private int mRadiusX;
    private int mRadiusY;
    private boolean mTextAboveBar;
    private boolean mTextOnTheRightBar;

    public StrongerBar(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public StrongerBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public StrongerBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public StrongerBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    protected void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        applyStyle(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mViewWidth = widthMeasureSpec;
        mViewHeight = heightMeasureSpec;
        int widthSpeMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpeMode = MeasureSpec.getMode(heightMeasureSpec);
        int barHeight = mIsShowAlphaBar ? mBarHeight * 2 : mBarHeight;
        int thumbHeight = mIsShowAlphaBar ? mThumbHeight * 2 : mThumbHeight;
        if (isVertical()) {
            if (widthSpeMode == MeasureSpec.AT_MOST || widthSpeMode == MeasureSpec.UNSPECIFIED) {
                mViewWidth = thumbHeight + barHeight + mBarMargin;
                setMeasuredDimension(mViewWidth, mViewHeight);
            }

        } else {
            if (widthSpeMode == MeasureSpec.AT_MOST || widthSpeMode == MeasureSpec.UNSPECIFIED) {
                mViewHeight = thumbHeight + barHeight + mBarMargin;
                setMeasuredDimension(mViewWidth, mViewHeight);
            }
        }
    }


    protected void applyStyle(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mContext = context;
        //get attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StrongerBar, defStyleAttr, defStyleRes);
        int colorsId = a.getResourceId(R.styleable.StrongerBar_colorGradient, 0);
        mMaxPosition = a.getInteger(R.styleable.StrongerBar_maxPosition, 100);
        mCurrentPosition = a.getInteger(R.styleable.StrongerBar_currentPosition, 0);
        mAlphaBarPosition = a.getInteger(R.styleable.StrongerBar_alphaBarPosition, mAlphaMinPosition);
        mIsVertical = a.getBoolean(R.styleable.StrongerBar_isVertical, false);
        mIsShowAlphaBar = a.getBoolean(R.styleable.StrongerBar_showAlphaBar, false);
        mBackgroundColor = a.getColor(R.styleable.StrongerBar_bgColor, Color.TRANSPARENT);
        mSencondColor = a.getColor(R.styleable.StrongerBar_secondColor, Color.TRANSPARENT);
        mBarHeight = (int) a.getDimension(R.styleable.StrongerBar_barHeight, (float) dp2px(2));
        mThumbHeight = (int) a.getDimension(R.styleable.StrongerBar_thumbHeight, (float) dp2px(30));
        mBarMargin = (int) a.getDimension(R.styleable.StrongerBar_barMargin, (float) dp2px(0));
        mIsShowBubble = a.getBoolean(R.styleable.StrongerBar_isShowBubble, false);
        mColorChangeCallBack = a.getBoolean(R.styleable.StrongerBar_ColorChangeCallBack, false);
        mTextFacePath = a.getString(R.styleable.StrongerBar_textFacePath);
        mFrameColor = a.getColor(R.styleable.StrongerBar_frameColor, Color.TRANSPARENT);
        mFrameWidth = a.getInt(R.styleable.StrongerBar_frameWidth, 0);
        mBubbleMargin = (int) a.getDimension(R.styleable.StrongerBar_bubbleMargin, 5);
        mBubbleWidth = (int) a.getDimension(R.styleable.StrongerBar_bubbleWidth, 0);
        mBubbleHeight = (int) a.getDimension(R.styleable.StrongerBar_bubbleHeight, 0);
        mBubbleTextSize = a.getInt(R.styleable.StrongerBar_bubbleTextSize, 20);
        mRadiusX = (int) a.getDimension(R.styleable.StrongerBar_longAxisRadius, 15);
        mRadiusY = (int) a.getDimension(R.styleable.StrongerBar_shortAxisRadius, 15);
        mTextAboveBar = a.getBoolean(R.styleable.StrongerBar_textAboveBar, true);
        mTextOnTheRightBar = a.getBoolean(R.styleable.StrongerBar_textOffsideBar, true);

        final Drawable d = a.getDrawable(R.styleable.StrongerBar_thumbSrc);
        if (d != null) {
            mThumbCanvas = new Canvas();
            mThumbBitmap = drawableToBitmap(mThumbHeight, d);
        }

        mOnLongClickRunnable = new OnLongClickRunnable();

        mSecondRect = new RectF();
        mBubbleBounds = new Rect();
        mBubbleBoundsRectF = new RectF();
        rf = new RectF();
        a.recycle();
        TypedArray b = context.obtainStyledAttributes(attrs,
                R.styleable.StrongerBarOrientation, defStyleAttr, defStyleRes);
        mOrientation = b.getInt(R.styleable.StrongerBarOrientation_rotation, -1);

        b.recycle();
        if (colorsId != 0) mColorSeeds = getColorsById(colorsId);

        if (mColorSeeds.length == 1) {
            mColorSeeds = new int[]{mColorSeeds[0], mColorSeeds[0]};
            mColorChangeCallBack = false;
        }
        mColorPaint = new Paint();
        mClearPaint = new Paint();
        mColorRectPaint = new Paint();
        mTextPaint = new TextPaint();

        mClearPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mClearPaint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR));
        setBackgroundColor(mBackgroundColor);
        initData();
    }

    private void initData() {
        mBubbleMargin = dp2px(mBubbleMargin);
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        Canvas canvas = new Canvas();
        int intrinsicWidth = drawable.getIntrinsicWidth();
        int intrinsicHeight = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(
                intrinsicWidth,
                intrinsicHeight,
                Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, intrinsicWidth, intrinsicHeight);
        drawable.draw(canvas);

        return bitmap;
    }

    private int[] getColorsById(int id) {
        if (isInEditMode()) {
            String[] s = mContext.getResources().getStringArray(id);
            int[] colors = new int[s.length];
            for (int j = 0; j < s.length; j++) {
                colors[j] = Color.parseColor(s[j]);
            }
            return colors;
        } else {
            TypedArray typedArray = mContext.getResources().obtainTypedArray(id);
            int[] colors = new int[typedArray.length()];
            for (int j = 0; j < typedArray.length(); j++) {
                colors[j] = typedArray.getColor(j, Color.BLACK);
            }
            typedArray.recycle();
            return colors;
        }
    }

    private void init() {
        mThumbRadius = mThumbHeight / 2;
        Log.i(TAG, "init  getWidth(): " + getWidth() + " ,getHeight(): " + getHeight());
        int viewBottom = getHeight() - getPaddingBottom();
        int viewRight = getWidth() - getPaddingRight();
        realLeft = mIsVertical ? getPaddingLeft() + Math.max(mThumbHeight, mBubbleHeight) / 2 : getPaddingLeft() + Math.max(mThumbHeight, mBubbleWidth) / 2;
        realRight = mIsVertical ? viewBottom - Math.max(mThumbHeight, mBubbleHeight) / 2 : viewRight - Math.max(mThumbHeight, mBubbleWidth) / 2;
        realTop = mIsVertical ? getWidth() / 2 - mBarHeight / 2 : getHeight() / 2 - mBarHeight / 2;
        mBarWidth = realRight - realLeft;

        if (mFrameColor != Color.TRANSPARENT) {
            if (mIsVertical) {
                realLeft += 1;
                mColorRect = new RectF(realLeft + mFrameWidth, realTop, realRight - mFrameWidth + 1, realTop + mBarHeight);
                mColorStroke = new RectF(realLeft + mFrameWidth / 2, realTop - mFrameWidth / 2, realRight - mFrameWidth / 2, realTop + mBarHeight + mFrameWidth / 2);
            } else {
                mColorRect = new RectF(realLeft + mFrameWidth, realTop, realRight - mFrameWidth, realTop + mBarHeight);
                mColorStroke = new RectF(realLeft + mFrameWidth / 2, realTop - mFrameWidth / 2 - 1, realRight - mFrameWidth / 2 - 1, realTop + mBarHeight + mFrameWidth / 2);
            }

        } else {
            mColorRect = new RectF(realLeft, realTop, realRight, realTop + mBarHeight);
            mColorStroke = new RectF(realLeft + mFrameWidth / 2, realTop - mFrameWidth / 2, realRight - mFrameWidth / 2, realTop + mBarHeight + mFrameWidth / 2);
        }
        mColorGradient = new LinearGradient(0, 0, mColorRect.width(), 0, mColorSeeds, null, Shader.TileMode.MIRROR);
        mColorRectPaint.setShader(mColorGradient);
        mColorRectPaint.setAntiAlias(true);
        mMatrix = new Matrix();

        cacheColors();
        setAlphaValue();
        mMoveIntervalRate = MAX_PROGRESS_MOVE_TIME / mMaxPosition;
        mIndexQueue = new LinkedList<>();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w <= 0 | h <= 0) {
            return;
        }
        if (mIsVertical) {
            mTransparentBitmap = Bitmap.createBitmap(h, w, Bitmap.Config.ARGB_4444);
        } else {
            mTransparentBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
        }
        mTransparentBitmap.eraseColor(Color.TRANSPARENT);
        init();
        mInit = true;
        if (mColorsToInvoke != -1) setColor(mColorsToInvoke);
    }


    private void cacheColors() {
        if (mBarWidth < 1) return;
        mColors.clear();
        for (int i = 0; i <= mMaxPosition; i++) {
            mColors.add(pickColor(i));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mColorStroke == null) {
            return;
        }
        if (mIsVertical) {
            canvas.rotate(-90);
            canvas.translate(-getHeight(), 0);
            updateOrientation();
        }
        mColorPaint.setAntiAlias(true);
        float colorPosition = (float) mCurrentPosition / mMaxPosition * mBarWidth;
        Log.i(TAG, "onDraw colorPosition: " + colorPosition);
        int color = getColor(false);
        int colorStartTransparent = Color.argb(mAlphaMaxPosition, Color.red(color), Color.green(color), Color.blue(color));
        int colorEndTransparent = Color.argb(mAlphaMinPosition, Color.red(color), Color.green(color), Color.blue(color));
        Paint.Style style = mColorPaint.getStyle();
        mColorPaint.setStyle(Paint.Style.STROKE);
        mColorPaint.setStrokeWidth(mFrameWidth);
        mColorPaint.setColor(mFrameColor);
        canvas.drawRect(mColorStroke, mColorPaint);
        mColorPaint.setStyle(style);
        int[] toAlpha = new int[]{colorStartTransparent, colorEndTransparent};
        //clear
        canvas.drawBitmap(mTransparentBitmap, 0, 0, null);
        canvas.drawRoundRect(mColorRect, mRadiusX, mRadiusY, mColorRectPaint);

        float thumbX = colorPosition + realLeft;
        float v = thumbX < 0 ? 0 : thumbX;
        float right = v > realRight ? realRight : v;
        mPreviousPointer = right - mFrameWidth;
        Log.i(TAG, "onDraw mPreviousPointer: " + mPreviousPointer);
        if (mSencondColor != -1) {
            mColorPaint.setColor(mSencondColor);
            mSecondRect = new RectF(realLeft + mFrameWidth, realTop, (int) right - mFrameWidth, realTop + mBarHeight);
            canvas.drawRoundRect(mSecondRect, mRadiusX, mRadiusY, mColorPaint);

        }

        float thumbY = mColorRect.top + mColorRect.height() / 2;
        mColorPaint.setColor(Color.BLACK);


      /*
      Bitmap bitmap = mThumbBitmap;
      if (mOnStateChangeListener != null) {
            if (isEnabled()) {
                bitmap = mOnStateChangeListener.onThumbNeedAnimation(mCurrentPosition, mMaxPosition, (int) mThumbRadius * 2, this);
            } else {
                bitmap = mOnStateChangeListener.onDisableState(mCurrentPosition, mMaxPosition, (int) mThumbRadius * 2, this);
            }
            bitmap = BitmapFactory.;
        }*/
        if (mThumbBitmap != null) {
            canvas.drawCircle(thumbX, thumbY, mThumbRadius, mClearPaint);
            if (mIsVertical) {
                Log.i(TAG, "onDraw mTargetDegree: " + mTargetDegree);
                drawRotateBitmap(canvas, mColorPaint, mThumbBitmap, 360 - mCurrentDegree, thumbX - mThumbRadius, thumbY - mThumbRadius);
            } else {
                canvas.drawBitmap(mThumbBitmap, thumbX - mThumbRadius, thumbY - mThumbRadius, mColorPaint);
            }
        }
        if (mIsShowBubble) {
            canvas.save();
            if (isVertical()) {
                canvas.rotate(90);
                constructVerticalBubbleRectF(thumbX, thumbY, mTextOnTheRightBar);
            } else {
                constructHorizontalBubbleRectF(thumbX, thumbY, mTextAboveBar);
            }
            drawBubbleRectFAndText(canvas, initDrawBubbleText());
            canvas.restore();
        }

        if (mIsShowAlphaBar) {
            int top = (int) (mThumbHeight + mThumbRadius + mBarHeight + mBarMargin);
            mAlphaRect = new RectF(realLeft, top, realRight, top + mBarHeight);
            Paint alphaBarPaint = new Paint();
            alphaBarPaint.setAntiAlias(true);
            LinearGradient alphaBarShader = new LinearGradient(0, 0, mAlphaRect.width(), 0, toAlpha, null, Shader.TileMode.MIRROR);
            alphaBarPaint.setShader(alphaBarShader);
            canvas.drawRoundRect(mAlphaRect, mRadiusX, mRadiusY, alphaBarPaint);

            float alphaPosition = (float) (mAlphaBarPosition - mAlphaMinPosition) / (mAlphaMaxPosition - mAlphaMinPosition) * mBarWidth;
            float alphaThumbX = alphaPosition + realLeft;
            float alphaThumbY = mAlphaRect.top + mAlphaRect.height() / 2;
            canvas.drawCircle(alphaThumbX, alphaThumbY, mBarHeight / 2 + 5, mColorPaint);

            RadialGradient alphaThumbShader = new RadialGradient(alphaThumbX, alphaThumbY, mThumbRadius, toAlpha, null, Shader.TileMode.MIRROR);
            Paint alphaThumbGradientPaint = new Paint();
            alphaThumbGradientPaint.setAntiAlias(true);
            alphaThumbGradientPaint.setShader(alphaThumbShader);
            canvas.drawCircle(alphaThumbX, alphaThumbY, mThumbHeight / 2, alphaThumbGradientPaint);
        }

        if (mFirstDraw) {

            if (mOnStateChangeListener != null && mColorChangeCallBack) {
                mOnStateChangeListener.onColorChangeListener(mCurrentPosition, mMaxPosition, getColor(), this);
            }

            mFirstDraw = false;

            if (mOnInitDoneListener != null) {
                mOnInitDoneListener.done();
            }
        }

        super.onDraw(canvas);
    }

    private String initDrawBubbleText() {
        String testString = "";
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTypeface(getTypeface(null));
        mTextPaint.setTextSize(dip2px(mContext, mBubbleTextSize));
        if (!isEnabled()) {
            mTextPaint.setColor(Color.GRAY);
        }
        if (mOnProgressChangedListener != null) {
            testString = mOnProgressChangedListener.onBubbleTextNeedUpdate(mCurrentPosition, mMaxPosition, this);
        }
        mTextPaint.getTextBounds(testString, 0, testString.length(), mBubbleBounds);
        return testString;
    }

    private void constructHorizontalBubbleRectF(float thumbX, float thumbY, boolean isTextAboveBar) {
        Log.i(TAG, "constructHorizontalBubbleRectF thumbX: " + thumbX + " ," + thumbY);
        int left = (int) (thumbX - mBubbleWidth / 2);
        int top;
        if (isTextAboveBar) {
            top = (int) (thumbY - (mThumbRadius + mBubbleMargin + mBubbleHeight));
        } else {
            top = (int) (thumbY + (mThumbRadius + mBubbleMargin));
        }
        mBubbleBoundsRectF.set(left, top, left + mBubbleWidth, top + mBubbleHeight);
    }

    private void constructVerticalBubbleRectF(float thumbX, float thumbY, boolean isTextOnTheRightBar) {
        Log.i(TAG, "constructVerticalBubbleRectF thumbX: " + thumbX + " ," + thumbY);
        int left;
        if (isTextOnTheRightBar) {
            left = (int) (thumbY + (mThumbRadius + mBubbleMargin));
        } else {
            left = (int) (thumbY - (mThumbRadius + mBubbleMargin + mBubbleWidth));
        }
        int bottom = (int) -thumbX + mBubbleHeight / 2;
        mBubbleBoundsRectF.set(left, bottom - mBubbleHeight, left + mBubbleWidth, bottom);
    }

    private void drawBubbleRectFAndText(Canvas canvas, String text) {
        mColorPaint.setColor(Color.WHITE);
        mTextPaint.setColor(Color.parseColor("#000000"));
        if ( mRotatable) {
            float centerX = mBubbleBoundsRectF.centerX();
            float centerY = mBubbleBoundsRectF.centerY();
            canvas.translate(centerX, centerY);
            canvas.rotate(360 - mCurrentDegree);
            canvas.translate(- centerX, - centerY);
            canvas.drawRoundRect(mBubbleBoundsRectF, 12, 12, mColorPaint);
            canvas.drawText(text, mBubbleBoundsRectF.left + (mBubbleBoundsRectF.width() - mBubbleBounds.width()) / 2,
                    mBubbleBoundsRectF.bottom - (mBubbleBoundsRectF.height() - mBubbleBounds.height()) / 2, mTextPaint);
        } else {
            canvas.drawRoundRect(mBubbleBoundsRectF, 12, 12, mColorPaint);
            canvas.drawText(text, mBubbleBoundsRectF.left + (mBubbleBoundsRectF.width() - mBubbleBounds.width()) / 2,
                    mBubbleBoundsRectF.bottom - (mBubbleBoundsRectF.height() - mBubbleBounds.height()) / 2, mTextPaint);
        }
    }

    private Bitmap drawableToBitmap(int width, Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(
                width,
                width,
                Bitmap.Config.ARGB_8888);
        mThumbCanvas.setBitmap(null);
        mThumbCanvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, width, width);
        if(mIsVertical) {
            mThumbCanvas.translate(width/2, width/2);
            mThumbCanvas.rotate(90);
            mThumbCanvas.translate(-width/2, -width/2);
        }
        drawable.draw(mThumbCanvas);
        return bitmap;
    }

    public class OnLongClickRunnable implements Runnable {
        @Override
        public void run() {
            if (mOnStateChangeListener != null) {
                mOnStateChangeListener.onLongPress(StrongerBar.this);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        x = mIsVertical ? getHeight() - event.getY() : event.getX();
        y = mIsVertical ? event.getX() : event.getY();
        Log.i(TAG, "onTouchEvent event.getX(): " + event.getX());
        Log.i(TAG, "onTouchEvent event.getY(): " + event.getY());
        Log.i(TAG, "onTouchEvent event.getHeight(): " + getHeight());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float touchX = mIsVertical ? getHeight() - event.getY() : event.getX();
                float touchY = mIsVertical ? event.getX() : event.getY();
                if (!isOnThumb(touchX, touchY)) {
                    return false;
                }
                removeCallbacks(mOnLongClickRunnable);
                postDelayed(mOnLongClickRunnable, ViewConfiguration.getLongPressTimeout());
                downX = mIsVertical ? event.getY() : event.getX();
                if (isOnBar(mColorRect, x, y)) {
                    mMovingColorBar = true;
                } else if (mIsShowAlphaBar) {
                    if (isOnBar(mAlphaRect, x, y)) {
                        mMovingAlphaBar = true;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float v = mIsVertical ? getHeight() - event.getY() : event.getX();
                if (!isOnThumb(v, mIsVertical ? event.getX() : event.getY())) {
                    removeCallbacks(mOnLongClickRunnable);
                }
                if ((int) v == (int) downX || !isEnabled()) {
                    return false;
                }
                getParent().requestDisallowInterceptTouchEvent(true);
                if (mMovingColorBar) {
                    float value = (x - realLeft) / mBarWidth * mMaxPosition;
                    mCurrentPosition = (int) value;
                    Log.i(TAG, "onTouchEvent ACTION_MOVE mCurrentPosition: " + mCurrentPosition);
                    if (mCurrentPosition < 0) mCurrentPosition = 0;
                    if (mCurrentPosition > mMaxPosition) mCurrentPosition = mMaxPosition;
                } else if (mIsShowAlphaBar) {
                    if (mMovingAlphaBar) {
                        float value = (x - realLeft) / (float) mBarWidth * (mAlphaMaxPosition - mAlphaMinPosition) + mAlphaMinPosition;
                        mAlphaBarPosition = (int) value;
                        if (mAlphaBarPosition < mAlphaMinPosition)
                            mAlphaBarPosition = mAlphaMinPosition;
                        else if (mAlphaBarPosition > mAlphaMaxPosition)
                            mAlphaBarPosition = mAlphaMaxPosition;
                        setAlphaValue();
                    }
                }
                if (mColorChangeCallBack && mOnStateChangeListener != null && (mMovingAlphaBar || mMovingColorBar)) {
                    mOnStateChangeListener.onColorChangeListener(mCurrentPosition, mMaxPosition, getColor(), this);
                }
                invalidate();
                if (mOnProgressChangedListener != null)
                    mOnProgressChangedListener.onProgressChanged(mCurrentPosition, this);
                break;
            case MotionEvent.ACTION_UP:
                mMovingColorBar = false;
                mMovingAlphaBar = false;
                removeCallbacks(mOnLongClickRunnable);
                break;
        }
        return true;
    }

    public void setProgress(int index) {
        mCurrentPosition = index;
        invalidate();
    }

    public void setMove(int index) {
        mCurrentPosition = index;
        if (mOnProgressChangedListener != null) {
            mOnProgressChangedListener.onProgressChanged(index, this);
        }
        invalidate();
    }

    public void setProgressWithAnimation(int index) {
        if (mIsInAnimation) {
            if (mIndexQueue.peek() == null || mIndexQueue.getLast() != index)
                mIndexQueue.add(index);
            return;
        }
        ObjectAnimator animator = ObjectAnimator.ofInt(this, "progress", mCurrentPosition, index);
        animator.setDuration((int) (mMoveIntervalRate * Math.abs(mCurrentPosition - index)));
        animator.setInterpolator(new LinearInterpolator());
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mIsInAnimation = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mIsInAnimation = false;
                if (!mIndexQueue.isEmpty()) {
                    //setProgressWithAnimation(mIndexQueue.poll());
                    int lastIndex = mIndexQueue.getLast();
                    mIndexQueue.clear();
                    setProgressWithAnimation(lastIndex);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    private boolean isOnThumb(float x, float y) {
        Log.i(TAG, "isOnThumb x: " + x);
        if (realLeft - x > mTouchSlop || x - realRight > mTouchSlop) {
            return false;
        }
        if (mPreviousPointer == 0) {
            if (x <= mThumbRadius * 2 + mTouchSlop) {
                return true;
            }
            return false;
        }
        if (x <= mPreviousPointer + mThumbRadius + mTouchSlop && x >= mPreviousPointer - mThumbRadius - mTouchSlop) {
            return true;
        }
        return false;
    }

    public int getIndexFromProgress(int progress) {
        // need to be overrided.
        return 0;
    }

    public int getAlphaMaxPosition() {
        return mAlphaMaxPosition;
    }

    /***
     *
     * @param alphaMaxPosition <= 255 && > alphaMinPosition
     */
    public void setAlphaMaxPosition(int alphaMaxPosition) {
        mAlphaMaxPosition = alphaMaxPosition;
        if (mAlphaMaxPosition > 255) {
            mAlphaMaxPosition = 255;
        } else if (mAlphaMaxPosition <= mAlphaMinPosition) {
            mAlphaMaxPosition = mAlphaMinPosition + 1;
        }

        if (mAlphaBarPosition > mAlphaMinPosition) {
            mAlphaBarPosition = mAlphaMaxPosition;
        }
        invalidate();
    }

    public int getAlphaMinPosition() {
        return mAlphaMinPosition;
    }

    /***
     *
     * @param alphaMinPosition >=0 && < alphaMaxPosition
     */
    public void setAlphaMinPosition(int alphaMinPosition) {
        this.mAlphaMinPosition = alphaMinPosition;
        if (mAlphaMinPosition >= mAlphaMaxPosition) {
            mAlphaMinPosition = mAlphaMaxPosition - 1;
        } else if (mAlphaMinPosition < 0) {
            mAlphaMinPosition = 0;
        }

        if (mAlphaBarPosition < mAlphaMinPosition) {
            mAlphaBarPosition = mAlphaMinPosition;
        }
        invalidate();
    }

    /**
     * @param r
     * @param x
     * @param y
     * @return whether MotionEvent is performing on bar or not
     */
    private boolean isOnBar(RectF r, float x, float y) {
        if (r.left - mThumbRadius < x && x < r.right + mThumbRadius && r.top - mThumbRadius < y && y < r.bottom + mThumbRadius) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return
     * @deprecated use {@link #setOnInitDoneListener(OnInitDoneListener)} instead.
     */
    public boolean isFirstDraw() {
        return mFirstDraw;
    }


    /**
     * @param value
     * @return color
     */
    private int pickColor(int value) {
        return pickColor((float) value / mMaxPosition * mBarWidth);
    }

    /**
     * @param position
     * @return color
     */
    private int pickColor(float position) {
        float unit = position / mBarWidth;
        if (unit <= 0.0)
            return mColorSeeds[0];

        if (unit >= 1)
            return mColorSeeds[mColorSeeds.length - 1];

        float colorPosition = unit * (mColorSeeds.length - 1);
        int i = (int) colorPosition;
        colorPosition -= i;
        c0 = mColorSeeds[i];
        c1 = mColorSeeds[i + 1];
//         mAlpha = mix(Color.alpha(c0), Color.alpha(c1), colorPosition);
        mRed = mix(Color.red(c0), Color.red(c1), colorPosition);
        mGreen = mix(Color.green(c0), Color.green(c1), colorPosition);
        mBlue = mix(Color.blue(c0), Color.blue(c1), colorPosition);
        return Color.rgb(mRed, mGreen, mBlue);
    }

    /**
     * @param start
     * @param end
     * @param position
     * @return
     */
    private int mix(int start, int end, float position) {
        return start + Math.round(position * (end - start));
    }

    public int getColor() {
        return getColor(mIsShowAlphaBar);
    }

    /**
     * Set color, it must correspond to the value, if not , setColorBarPosition(0);
     *
     * @paam color
     */
    public void setColor(int color) {
        int withoutAlphaColor = Color.rgb(Color.red(color), Color.green(color), Color.blue(color));

        if (mInit) {
            int value = mColors.indexOf(withoutAlphaColor);
//            mColorsToInvoke = color;
            setColorBarPosition(value);
        } else {
            mColorsToInvoke = color;
        }

    }

    /**
     * @param withAlpha
     * @return
     */
    public int getColor(boolean withAlpha) {
        //pick mode
        if (mCurrentPosition >= mColors.size()) {
            int color = pickColor(mCurrentPosition);
            if (withAlpha) {
                return color;
            } else {
                return Color.argb(getAlphaValue(), Color.red(color), Color.green(color), Color.blue(color));
            }
        }

        //cache mode
        int color = mColors.get(mCurrentPosition);

        if (withAlpha) {
            return Color.argb(getAlphaValue(), Color.red(color), Color.green(color), Color.blue(color));
        }
        return color;
    }

    public int getAlphaBarPosition() {
        return mAlphaBarPosition;
    }

    public void setAlphaBarPosition(int value) {
        this.mAlphaBarPosition = value;
        setAlphaValue();
        invalidate();
    }

    public int getAlphaValue() {
        return mAlpha;
    }

    /**
     * @param onStateChangeListener
     */
    public void setOnStateChangeListener(OnStateChangeListener onStateChangeListener) {
        this.mOnStateChangeListener = onStateChangeListener;
    }

    /**
     * @param onProgressChangedListener
     */
    public void setOnProgressChangedListener(OnProgressChangedListener onProgressChangedListener) {
        this.mOnProgressChangedListener = onProgressChangedListener;
    }


    public int dp2px(float dpValue) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * Set colors by resource id. The resource's type must be ArrayRes
     *
     * @param resId
     */
    public void setColorSeeds(@ArrayRes int resId) {
        setColorSeeds(getColorsById(resId));
    }

    public void setColorSeeds(int[] colors) {
        mColorSeeds = colors;
        init();
        invalidate();
        if (mOnStateChangeListener != null)
            mOnStateChangeListener.onColorChangeListener(mCurrentPosition, mMaxPosition, getColor(), this);
    }

    /**
     * @param color
     * @return the color's position in the bar, if not in the bar ,return -1;
     */
    public int getColorIndexPosition(int color) {
        return mColors.indexOf(Color.argb(255, Color.red(color), Color.green(color), Color.blue(color)));
    }

    public List<Integer> getColors() {
        return mColors;
    }

    public boolean isShowAlphaBar() {
        return mIsShowAlphaBar;
    }

    public void setShowAlphaBar(boolean show) {
        mIsShowAlphaBar = show;
        refreshLayoutParams();
        invalidate();
        if (mOnStateChangeListener != null && mColorChangeCallBack)
            mOnStateChangeListener.onColorChangeListener(mCurrentPosition, mMaxPosition, getColor(), this);
    }

    private void refreshLayoutParams() {
        setLayoutParams(getLayoutParams());
    }

    public boolean isVertical() {
        return mIsVertical;
    }

    /**
     * @param px
     */
    public void setBarHeightPx(int px) {
        mBarHeight = px;
        refreshLayoutParams();
        invalidate();
    }

    private void setAlphaValue() {
        mAlpha = 255 - mAlphaBarPosition;
    }

    public int getMaxValue() {
        return mMaxPosition;
    }

    public void setMaxPosition(int value) {
        this.mMaxPosition = value;
        invalidate();
        cacheColors();
    }

    /**
     * set margin between bars
     *
     * @param mBarMargin
     */
    public void setBarMarginPx(int mBarMargin) {
        this.mBarMargin = mBarMargin;
        refreshLayoutParams();
        invalidate();
    }


    public void setBubbleOrientation(int orientation) {
        this.mOrientation = orientation;
        refreshLayoutParams();
        invalidate();
    }


    public void setOrientation(int degree, boolean animation) {
        mEnableAnimation = animation;
        mRotatable = true;
        // make sure in the range of [0, 359]
        degree = degree >= 0 ? degree % 360 : degree % 360 + 360;
        if (degree == mTargetDegree) return;

        mTargetDegree = degree;
        if (mEnableAnimation) {
            mStartDegree = mCurrentDegree;
            mAnimationStartTime = AnimationUtils.currentAnimationTimeMillis();

            int diff = mTargetDegree - mCurrentDegree;
            diff = diff >= 0 ? diff : 360 + diff; // make it in range [0, 359]

            // Make it in range [-179, 180]. That's the shorted distance between the
            // two angles
            diff = diff > 180 ? diff - 360 : diff;

            mClockwise = diff >= 0;
            mAnimationEndTime = mAnimationStartTime
                    + Math.abs(diff) * 1000 / ANIMATION_SPEED;
        } else {
            mCurrentDegree = mTargetDegree;
        }
        invalidate();
    }

    private void updateOrientation() {
        if (mCurrentDegree != mTargetDegree) {
            long time = AnimationUtils.currentAnimationTimeMillis();
            if (time < mAnimationEndTime) {
                int deltaTime = (int) (time - mAnimationStartTime);
                int degree = mStartDegree + ANIMATION_SPEED
                        * (mClockwise ? deltaTime : -deltaTime) / 1000;
                degree = degree >= 0 ? degree % 360 : degree % 360 + 360;
                mCurrentDegree = degree;
                invalidate();
            } else {
                mCurrentDegree = mTargetDegree;
            }
        }
    }
    /**
     * Set the value of color bar, if out of bounds , it will be 0 or maxValue;
     *
     * @param value
     */
    public void setColorBarPosition(int value) {
        this.mCurrentPosition = value;
        mCurrentPosition = mCurrentPosition > mMaxPosition ? mMaxPosition : mCurrentPosition;
        mCurrentPosition = mCurrentPosition < 0 ? 0 : mCurrentPosition;
        invalidate();
        if (mOnStateChangeListener != null && mColorChangeCallBack)
            mOnStateChangeListener.onColorChangeListener(mCurrentPosition, mMaxPosition, getColor(), this);
    }

    public void setOnInitDoneListener(OnInitDoneListener listener) {
        this.mOnInitDoneListener = listener;
    }

    /**
     * set thumb's height by pixels
     *
     * @param px
     */
    public void setThumbHeightPx(int px) {
        this.mThumbHeight = px;
        mThumbRadius = mThumbHeight / 2;
        refreshLayoutParams();
        invalidate();
    }

    public int getCurrentPosition() {
        return mCurrentPosition;
    }

    public int getBarHeight() {
        return mBarHeight;
    }

    /**
     * @param dp
     */
    public void setBarHeight(float dp) {
        mBarHeight = dp2px(dp);
        refreshLayoutParams();
        invalidate();
    }

    public int getThumbHeight() {
        return mThumbHeight;
    }

    /**
     * set thumb's height by dpi
     *
     * @param dp
     */
    public void setThumbHeight(float dp) {
        this.mThumbHeight = dp2px(dp);
        mThumbRadius = mThumbHeight / 2;
        refreshLayoutParams();
        invalidate();
    }

    public int getBarMargin() {
        return mBarMargin;
    }

    /**
     * set margin between bars
     *
     * @param mBarMargin
     */
    public void setBarMargin(float mBarMargin) {
        this.mBarMargin = dp2px(mBarMargin);
        refreshLayoutParams();
        invalidate();
    }

    public float getColorBarValue() {
        return mCurrentPosition;
    }

    /*public Bitmap rotateToDegrees(Bitmap tmpBitmap, float degrees) {
        mMatrix.reset();
        mMatrix.setRotate(degrees);
        return Bitmap.createBitmap(tmpBitmap, 0, 0, tmpBitmap.getWidth(), tmpBitmap.getHeight(), mMatrix, true);
    }*/

    private void drawRotateBitmap(Canvas canvas, Paint paint, Bitmap bitmap,
                                  float rotation, float posX, float posY) {
        mMatrix.reset();
        int offsetX = bitmap.getWidth() / 2;
        int offsetY = bitmap.getHeight() / 2;
        mMatrix.postTranslate(-offsetX, -offsetY);
        mMatrix.postRotate(rotation);
        mMatrix.postTranslate(posX + offsetX, posY + offsetY);
        canvas.drawBitmap(bitmap, mMatrix, paint);
    }

    public interface OnStateChangeListener {
        /**
         * @param colorBarPosition between 0-maxValue
         * @param maxPosition      maxValue
         * @param color            return the color contains alpha value whether showAlphaBar is true or without alpha value
         */
        void onColorChangeListener(int colorBarPosition, int maxPosition, int color, StrongerBar strongerBar);

        Bitmap onThumbNeedAnimation(int currentPosition, int maxProgress, int radius, StrongerBar strongerBar);

        Bitmap onDisableState(int mCurrentPosition, int mMaxPosition, int i, StrongerBar strongerBar);

        void onLongPress(StrongerBar strongerBar);
    }

    public interface OnProgressChangedListener {
        // note: this is called only from user action.
        void onProgressChanged(int progress, View v);
        String onBubbleTextNeedUpdate(int currentPosition, int maxProgress, StrongerBar strongerBar);
    }

    public interface OnInitDoneListener {
        void done();
    }


    private Typeface getTypeface(String path) {
        Typeface mTypeface;
        if (path == null || !path.contains("ttf")) {
            mTypeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/RobotoBold.ttf");
        } else {
            mTypeface = Typeface.createFromAsset(mContext.getAssets(), path);
        }
        return mTypeface;
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}
