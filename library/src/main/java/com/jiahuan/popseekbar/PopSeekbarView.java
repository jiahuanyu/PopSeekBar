package com.jiahuan.popseekbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by doom on 15/6/30.
 */
public class PopSeekBarView extends View
{

    private static final int DEFAULT_NUMBER_COLOR = 0xFFFA7777;
    private static final int DEFAULT_SEEK_BAR_COLOR = 0xFFF4EFE9;
    private static final int DEFAULT_SEEK_BAR_HIGHLIGHT_COLOR = 0xFF68C4DB;

    //
    private static final float DEFAULT_SEEK_BAR_WIDTH = 5;
    private static final float DEFAULT_SMALL_NUMBER_SIZE = 16;
    private static final float DEFAULT_BIG_NUMBER_SIZE = 30;


    // Color
    private int mNumberColor;
    private int mSeekBarColor;
    private int mSeekBarHighlightColor;

    // Paint
    private Paint mCircleButtonPaint;
    private Paint mHandlerPaint;
    private Paint mSeekBarPaint;
    private Paint mSeekBarHighlightPaint;
    private Paint mSmallNumberPaint;
    private Paint mBigNumberPaint;

    // Dimen
    private float mSeekBarWidth;  // Default
    private float mSmallNumberSize;
    private float mBigNumberSize;

    //Bitmap
    private Bitmap mCircleButtonBitmap;
    private Bitmap mHandlerBitmap;


    // Rect
    private RectF mSeekBarRect;


    //
    private float mCircleButtonY;
    private float mPreY;
    private boolean isDownInSmallCircle;
    private float mSeekBarHeight;

    private int mStart = 10;
    private int mEnd = 60;
    private int mDifference = mEnd - mStart;

    public PopSeekBarView(Context context)
    {
        this(context, null);
    }

    public PopSeekBarView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public PopSeekBarView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PopSeekBarView);
        mCircleButtonBitmap = drawableToBitmap(typedArray.getDrawable(R.styleable.PopSeekBarView_pop_button_drawable));
        mHandlerBitmap = drawableToBitmap(typedArray.getDrawable(R.styleable.PopSeekBarView_pop_push_drawable));
        typedArray.recycle();
        initialize();
    }

    private void initialize()
    {
        mSeekBarWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_SEEK_BAR_WIDTH, getContext().getResources().getDisplayMetrics());
        mSmallNumberSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_SMALL_NUMBER_SIZE, getContext().getResources().getDisplayMetrics());
        mBigNumberSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_BIG_NUMBER_SIZE, getContext().getResources().getDisplayMetrics());


        mSeekBarColor = DEFAULT_SEEK_BAR_COLOR;
        mSeekBarHighlightColor = DEFAULT_SEEK_BAR_HIGHLIGHT_COLOR;
        mNumberColor = DEFAULT_NUMBER_COLOR;


        mSeekBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSeekBarPaint.setStyle(Paint.Style.FILL);
        mSeekBarPaint.setColor(mSeekBarColor);

        mCircleButtonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mHandlerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mSeekBarHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSeekBarHighlightPaint.setStyle(Paint.Style.FILL);
        mSeekBarHighlightPaint.setColor(mSeekBarHighlightColor);

        mSmallNumberPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSmallNumberPaint.setStyle(Paint.Style.FILL);
        mSmallNumberPaint.setColor(mNumberColor);
        mSmallNumberPaint.setTextSize(mSmallNumberSize);

        mBigNumberPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBigNumberPaint.setStyle(Paint.Style.FILL);
        mBigNumberPaint.setColor(mNumberColor);
        mBigNumberPaint.setTextSize(mBigNumberSize);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        canvas.drawRoundRect(mSeekBarRect, mSeekBarWidth / 2, mSeekBarWidth / 2, mSeekBarPaint);
        RectF rect = new RectF((getWidth() - mSeekBarWidth) / 2, mCircleButtonY, (getWidth() + mSeekBarWidth) / 2, getHeight() - mCircleButtonBitmap.getHeight() / 2);
        canvas.drawRoundRect(rect, mSeekBarWidth / 2, mSeekBarWidth / 2, mSeekBarHighlightPaint);
        int value = (int) (mStart + mDifference * ((getHeight() - mCircleButtonY - mCircleButtonBitmap.getWidth() / 2) / mSeekBarHeight));
        if (!isDownInSmallCircle)
        {
            canvas.drawBitmap(mCircleButtonBitmap, (getMeasuredWidth() - mCircleButtonBitmap.getWidth()) / 2, mCircleButtonY - mCircleButtonBitmap.getHeight() / 2, mCircleButtonPaint);
            canvas.drawText(value + "", (getWidth() - mSmallNumberPaint.measureText(value + "")) / 2, mCircleButtonY + getFontHeight(mSmallNumberPaint, value + "") / 2, mSmallNumberPaint);
        }
        else
        {
            canvas.drawBitmap(mHandlerBitmap, (getMeasuredWidth() - mHandlerBitmap.getWidth()) / 2, mCircleButtonY - mHandlerBitmap.getHeight() + mCircleButtonBitmap.getHeight() / 2, mHandlerPaint);
            canvas.drawText(value + "", (getWidth() - mBigNumberPaint.measureText(value + "")) / 2, mCircleButtonY - (mHandlerBitmap.getHeight() - mCircleButtonBitmap.getHeight() / 2 - mHandlerBitmap.getWidth() / 2) + getFontHeight(mBigNumberPaint, value + "") / 2, mBigNumberPaint);
        }
    }

    private float getFontHeight(Paint paint, String str)
    {
        Rect rect = new Rect();
        paint.getTextBounds(str, 0, str.length(), rect);
        return rect.height();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        switch (event.getAction() & event.getActionMasked())
        {
            case MotionEvent.ACTION_DOWN:
                if (isInCircle(event.getX(), event.getY()))
                {
                    mPreY = event.getY();
                    isDownInSmallCircle = true;
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (isDownInSmallCircle)
                {
                    mCircleButtonY += (event.getY() - mPreY);
                    if (mCircleButtonY > getHeight() - mCircleButtonBitmap.getHeight() / 2)
                    {
                        mCircleButtonY = getHeight() - mCircleButtonBitmap.getHeight() / 2;
                    }
                    else if (mCircleButtonY < mHandlerBitmap.getHeight() - mCircleButtonBitmap.getHeight() / 2)
                    {
                        mCircleButtonY = mHandlerBitmap.getHeight() - mCircleButtonBitmap.getHeight() / 2;
                    }
                    mPreY = event.getY();
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_UP:
                if (isDownInSmallCircle)
                {
                    isDownInSmallCircle = false;
                    invalidate();
                }
                break;
        }
        return true;
    }


    private boolean isInCircle(float x, float y)
    {
        if (Math.sqrt((x - getWidth() / 2) * (x - getWidth() / 2) + (y - mCircleButtonY) * (y - mCircleButtonY)) < mCircleButtonBitmap.getWidth() / 2)
        {
            return true;
        }
        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int width = mHandlerBitmap.getWidth();
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
        mSeekBarRect = new RectF((width - mSeekBarWidth) / 2, mHandlerBitmap.getHeight() - mCircleButtonBitmap.getHeight() / 2, (width + mSeekBarWidth) / 2, height - mCircleButtonBitmap.getHeight() / 2);
        mCircleButtonY = getMeasuredHeight() - mCircleButtonBitmap.getHeight() / 2;
        mSeekBarHeight = getHeight() - mHandlerBitmap.getHeight();
    }

    private Bitmap drawableToBitmap(Drawable drawable)
    {
        if (drawable == null)
            return null;
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap.Config config =
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                        : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

}
