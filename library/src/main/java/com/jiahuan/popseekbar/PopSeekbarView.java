package com.jiahuan.popseekbar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by doom on 15/5/30.
 */
public class PopSeekbarView extends View
{

    private static final String TAG = "PopSeekbarView";

    // Status
    private static final String INSTANCE_STATUS = "instance_status";
    private static final String STATUS_CY = "status_cy";

    // Default parameters
    private static final int DEFAULT_START_NUMBER = 10;
    private static final int DEFAULT_END_NUMBER = 60;

    // Default color
    private static final int DEFAULT_SMALL_CIRCLE_COLOR = 0xFFFFFFFF;
    private static final int DEFAULT_NUMBER_COLOR = 0xFFFA7777;
    private static final int DEFAULT_CIRCLE_SHADOW_COLOR = 0xFF909090;
    private static final int DEFAULT_SEEKBAR_COLOR = 0xFFF4EFE9;
    private static final int DEFAULT_SEEKBAR_HIGHLIGHT_COLOR = 0xFF68C4DB;


    // Default dimension in dp/pt
    private static final float DEFAULT_SMALL_CIRCLE_RADIUS = 12;
    private static final float DEFAULT_BIG_CIRCLE_RADIUS = 30;
    private static final float DEFAULT_SMALL_NUMBER_SIZE = 16;
    private static final float DEFAULT_BIG_NUMBER_SIZE = 20;
    private static final float DEFAULT_CIRCLE_SHADOW_RADIUS = 2;
    private static final float DEFAULT_CIRCLE_SHADOW_X_OFFSET = 0;
    private static final float DEFAULT_CIRCLE_SHADOW_Y_OFFSET = 0;
    private static final float DEFAULT_SEEKBAR_WIDTH = 5;
    private static final float DEFAULT_HANDLER_LENGTH = 20;

    // Dimension
    private float smallCircleRadius;
    private float smallNumberSize;
    private float circleShadowRadius;
    private float circleShadowXOffset;
    private float circleShadowYOffset;
    private float bigCircleRadius;
    private float bigNumberSize;
    private float seekbarWidth;  // Default
    private float seekbarHeight; // Related to the height of the view
    private float handlerLength;


    // Color
    private int circleShadowColor;
    private int circleColor;
    private int numberColor;
    private int seekbarColor;
    private int seekbarHighlightColor;

    // Paint
    private Paint smallCirclePaint;
    private Paint bigCirclePaint;
    private Paint seekbarPaint;
    private Paint seekbarHighlightPaint;
    private Paint smallNumberPaint;
    private Paint bigNumberPaint;

    // Parameters
    private int start;
    private int end;
    private float smallCircleCx;
    private float smallCircleCy;
    private RectF seekbarRect;
    private boolean isDownInSmallCircle;
    private boolean isRestored;
    private float preY;

    public PopSeekbarView(Context context)
    {
        this(context, null);
    }

    public PopSeekbarView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public PopSeekbarView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize()
    {
        Log.d(TAG, "initialize");
        // Parameter
        start = DEFAULT_START_NUMBER;
        end = DEFAULT_END_NUMBER;

        // Set default dimension or read from xml attributes
        smallCircleRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_SMALL_CIRCLE_RADIUS, getContext().getResources().getDisplayMetrics());
        smallNumberSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_SMALL_NUMBER_SIZE, getContext().getResources().getDisplayMetrics());
        circleShadowRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_CIRCLE_SHADOW_RADIUS, getContext().getResources().getDisplayMetrics());
        circleShadowXOffset = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_CIRCLE_SHADOW_X_OFFSET, getContext().getResources().getDisplayMetrics());
        circleShadowYOffset = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_CIRCLE_SHADOW_Y_OFFSET, getContext().getResources().getDisplayMetrics());
        bigCircleRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_BIG_CIRCLE_RADIUS, getContext().getResources().getDisplayMetrics());
        bigNumberSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_BIG_NUMBER_SIZE, getContext().getResources().getDisplayMetrics());
        seekbarWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_SEEKBAR_WIDTH, getContext().getResources().getDisplayMetrics());
        handlerLength = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_HANDLER_LENGTH, getContext().getResources().getDisplayMetrics());

        // Set default color or read from xml attributes
        seekbarColor = DEFAULT_SEEKBAR_COLOR;
        circleColor = DEFAULT_SMALL_CIRCLE_COLOR;
        circleShadowColor = DEFAULT_CIRCLE_SHADOW_COLOR;
        numberColor = DEFAULT_NUMBER_COLOR;
        seekbarHighlightColor = DEFAULT_SEEKBAR_HIGHLIGHT_COLOR;

        // Paint
        seekbarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        seekbarPaint.setStyle(Paint.Style.FILL);
        seekbarPaint.setColor(seekbarColor);

        smallCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        smallCirclePaint.setStyle(Paint.Style.FILL);
        smallCirclePaint.setColor(circleColor);
        smallCirclePaint.setShadowLayer(circleShadowRadius, circleShadowXOffset, circleShadowYOffset, circleShadowColor);

        smallNumberPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        smallNumberPaint.setColor(numberColor);
        smallNumberPaint.setTextSize(smallNumberSize);
        smallNumberPaint.setTextAlign(Paint.Align.CENTER);

        seekbarHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        seekbarHighlightPaint.setStyle(Paint.Style.FILL);
        seekbarHighlightPaint.setColor(seekbarHighlightColor);

        bigCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bigCirclePaint.setColor(circleColor);
        bigCirclePaint.setStyle(Paint.Style.FILL);
        bigCirclePaint.setShadowLayer(circleShadowRadius, circleShadowXOffset, circleShadowYOffset, circleShadowColor);

        bigNumberPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bigNumberPaint.setColor(numberColor);
        bigNumberPaint.setTextSize(bigNumberSize);
        bigNumberPaint.setTextAlign(Paint.Align.CENTER);
    }


    private float getFontHeight(Paint paint, String str)
    {
        // FontMetrics sF = paint.getFontMetrics();
        // return sF.descent - sF.ascent;
        Rect rect = new Rect();
        paint.getTextBounds(str, 0, str.length(), rect);
        return rect.height();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        Log.d(TAG, "onDraw");
//        canvas.drawColor(Color.RED);
        // Seekbar
        canvas.drawRoundRect(seekbarRect, seekbarWidth / 2, seekbarWidth / 2, seekbarPaint);

        // Highlight Seekbar
        RectF hlSeekbarRect = new RectF((getWidth() - seekbarWidth) / 2, smallCircleCy, (getWidth() + seekbarWidth) / 2, getHeight() - smallCircleRadius - circleShadowRadius);
        canvas.drawRoundRect(hlSeekbarRect, seekbarWidth / 2, seekbarWidth / 2, seekbarHighlightPaint);

        if (isDownInSmallCircle)
        {
            // Big circle
            canvas.drawCircle(smallCircleCx, smallCircleCy - handlerLength - bigCircleRadius, bigCircleRadius, bigCirclePaint);
            // Handler
            canvas.save();
            canvas.clipRect(smallCircleCx - smallCircleRadius - circleShadowRadius, smallCircleCy - handlerLength - circleShadowRadius, smallCircleCx + smallCircleRadius + circleShadowRadius, smallCircleCy + smallCircleRadius + circleShadowRadius);
            RectF r = new RectF(smallCircleCx - smallCircleRadius, smallCircleCy - handlerLength - bigCircleRadius, smallCircleCx + smallCircleRadius, smallCircleCy + smallCircleRadius);
            canvas.drawRoundRect(r, smallCircleRadius, smallCircleRadius, bigCirclePaint);
            canvas.restore();
            // Big number
            canvas.drawText(((int) (start + (end - start) / seekbarHeight * (seekbarHeight - smallCircleCy + circleShadowRadius + 2 * bigCircleRadius + handlerLength))) + "", smallCircleCx, smallCircleCy - handlerLength - bigCircleRadius + getFontHeight(bigNumberPaint, "0") / 2, bigNumberPaint);
        }
        else
        {
            // Small circle
            canvas.drawCircle(smallCircleCx, smallCircleCy, smallCircleRadius, smallCirclePaint);
            // Small Number
            canvas.drawText(((int) (start + (end - start) / seekbarHeight * (seekbarHeight - smallCircleCy + circleShadowRadius + 2 * bigCircleRadius + handlerLength))) + "", smallCircleCx, smallCircleCy + getFontHeight(smallNumberPaint, "0") / 2, smallNumberPaint);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        switch (event.getAction() & event.getActionMasked())
        {
            case MotionEvent.ACTION_DOWN:
                if (isInCircle(event.getX(), event.getY()))
                {
                    Log.d(TAG, "InCircle");
                    preY = event.getY();
                    isDownInSmallCircle = true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (isDownInSmallCircle)
                {
                    smallCircleCy += (event.getY() - preY);
                    if (smallCircleCy > getHeight() - smallCircleRadius - circleShadowRadius)
                    {
                        smallCircleCy = getHeight() - smallCircleRadius - circleShadowRadius;
                    }
                    else if (smallCircleCy < (circleShadowRadius + 2 * bigCircleRadius + handlerLength))
                    {
                        smallCircleCy = circleShadowRadius + 2 * bigCircleRadius + handlerLength;
                    }
                    preY = event.getY();
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
        if (Math.sqrt((x - smallCircleCx) * (x - smallCircleCx) + (y - smallCircleCy) * (y - smallCircleCy)) < smallCircleRadius)
        {
            return true;
        }
        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        Log.d(TAG, "onMeasure");
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int width = (int) ((bigCircleRadius + circleShadowRadius) * 2); // Whatever, the width is the double bigCircleRadius
        seekbarHeight = height - smallCircleRadius - handlerLength - 2 * bigCircleRadius - 2 * circleShadowRadius;
        smallCircleCx = width / 2;
        if (!isRestored)
        {
            smallCircleCy = height - smallCircleRadius - circleShadowRadius;
            isRestored = false;
        }
        seekbarRect = new RectF((width - seekbarWidth) / 2, 2 * bigCircleRadius + circleShadowRadius + handlerLength, (width + seekbarWidth) / 2, height - smallCircleRadius - circleShadowRadius);
        setMeasuredDimension(width, height);
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        Log.d(TAG, "onSaveInstanceState");
        Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE_STATUS, super.onSaveInstanceState());
        bundle.putFloat(STATUS_CY, smallCircleCy);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        Log.d(TAG, "onRestoreInstanceState");
        if (state instanceof Bundle)
        {
            Bundle bundle = (Bundle) state;
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_STATUS));
            smallCircleCy = bundle.getFloat(STATUS_CY);
            isRestored = true;
            return;
        }
        super.onRestoreInstanceState(state);
    }
}
