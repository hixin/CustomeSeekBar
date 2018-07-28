package camera.cloudminds.com.strongbardemo;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity implements StrongerBar.OnProgressChangedListener,  StrongerBar.OnStateChangeListener{
    private StrongerBar adjust1;
    private StrongerBar adjust2;
    private StrongerBar adjust3;
    private StrongerBar adjust4;
    private Canvas mCanvas;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCanvas = new Canvas();
        adjust1 = findViewById(R.id.bar1);
        adjust1.setOnProgressChangedListener(this);
        adjust1.setOnStateChangeListener(this);
        adjust2 = findViewById(R.id.bar2);
        adjust2.setOnProgressChangedListener(this);
        adjust2.setOnStateChangeListener(this);
        adjust3 = findViewById(R.id.bar3);
        adjust3.setOnProgressChangedListener(this);
        adjust3.setOnStateChangeListener(this);
        adjust4 = findViewById(R.id.bar4);
        adjust4.setOnProgressChangedListener(this);
        adjust4.setOnStateChangeListener(this);

    }

    @Override
    public void onProgressChanged(int progress, View v) {

    }

    @Override
    public void onColorChangeListener(int colorBarPosition, int maxPosition, int color, StrongerBar strongerBar) {

    }

    @Override
    public Bitmap onThumbNeedAnimation(int currentPosition, int maxProgress, int radius, StrongerBar strongerBar) {
      /*  Drawable drawable = getResources().getDrawable(R.drawable.bar1);
        return drawableToBitmap(radius, drawable);*/
      return null;
    }

    @Override
    public String onBubbleTextNeedUpdate(int currentPosition, int maxProgress, StrongerBar strongerBar) {
        return String.valueOf(currentPosition);
    }

    @Override
    public Bitmap onDisableState(int mCurrentPosition, int mMaxPosition, int i, StrongerBar strongerBar) {
        return null;
    }

    @Override
    public void onLongPress(StrongerBar strongerBar) {

    }

    private Bitmap drawableToBitmap(int width, Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(
                width,
                width,
                Bitmap.Config.ARGB_8888);
        mCanvas.setBitmap(null);
        mCanvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, width, width);
        drawable.draw(mCanvas);

        return bitmap;
    }
}
