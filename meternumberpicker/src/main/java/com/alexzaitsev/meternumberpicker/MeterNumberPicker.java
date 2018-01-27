package com.alexzaitsev.meternumberpicker;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import java.util.Locale;

public class MeterNumberPicker extends View {

    private static final int DEFAULT_MIN_HEIGHT_DP = 20;
    private static final int DEFAULT_MIN_WIDTH_DP = 14;
    private static final int DEFAULT_MAX_VALUE = 9;
    private static final int DEFAULT_MIN_VALUE = 0;
    private static final int DEFAULT_VALUE = 0;
    private static final int DEFAULT_TEXT_COLOR = 0xFF000000;
    private static final float DEFAULT_TEXT_SIZE_SP = 25f;
    /**
     * The default internal padding for the text (do not mix up with view paddings -
     * this is separate thing)
     */
    private static final int DEFAULT_PADDING = 2;
    private static final int SELECTOR_ADJUSTMENT_DURATION_MILLIS = 800;

    private int minHeight = DEFAULT_MIN_HEIGHT_DP;
    private int minWidth = DEFAULT_MIN_WIDTH_DP;
    private int minValue = DEFAULT_MIN_VALUE;
    private int maxValue = DEFAULT_MAX_VALUE;
    private int value = DEFAULT_VALUE;
    private Paint textPaint;
    private int textColor = DEFAULT_TEXT_COLOR;
    private float textSize = DEFAULT_TEXT_SIZE_SP;
    private Typeface typeface;
    /**
     * Current Y scroll offset
     */
    private int currentScrollOffset;
    /**
     * Current value offset
     */
    private int currentValueOffset;
    /**
     * The height of the text itself excluding paddings
     */
    private int textHeight;
    /**
     * Internal horizontal (left and right) padding
     */
    private int paddingHorizontal = DEFAULT_PADDING;
    /**
     * Internal vertical (top and bottom) padding
     */
    private int paddingVertical = DEFAULT_PADDING;
    /**
     * The Y position of the last down event
     */
    private float lastDownEventY;
    /**
     * The Y position of the last down or move event
     */
    private float lastDownOrMoveEventY;
    /**
     * The {@link Scroller} responsible for adjusting the selector
     */
    private Scroller adjustScroller;
    /**
     * The last Y position of adjustment scroller
     */
    private int scrollerLastY = 0;

    public MeterNumberPicker(Context context) {
        super(context);
        initWithAttrs(context, null, 0, 0);
    }

    public MeterNumberPicker(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initWithAttrs(context, attrs, 0, 0);
    }

    public MeterNumberPicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initWithAttrs(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MeterNumberPicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initWithAttrs(context, attrs, defStyleAttr, defStyleRes);
    }

    public MeterNumberPicker(Context context, @StyleRes int styleId) {
        super(context);
        initWithStyle(context, styleId);
    }

    private void initWithAttrs(Context context, @Nullable AttributeSet attrs, int defStyleAttrs, int defStyleRes) {
        TypedArray attributesArray = context.obtainStyledAttributes(attrs, R.styleable.MeterNumberPicker, defStyleAttrs, defStyleRes);
        init(context, attributesArray);
        attributesArray.recycle();
    }

    private void initWithStyle(Context context, @StyleRes int styleId) {
        TypedArray styleTypedArray = context.obtainStyledAttributes(styleId, R.styleable.MeterNumberPicker);
        init(context, styleTypedArray);
        styleTypedArray.recycle();
    }

    private void init(Context context, TypedArray attributesArray) {
        if (attributesArray == null) {
            textSize = spToPx(textSize);
            minWidth = (int) dpToPx(minWidth);
            minHeight = (int) dpToPx(minHeight);
            paddingHorizontal = (int) dpToPx(paddingHorizontal);
            paddingVertical = (int) dpToPx(paddingVertical);
        } else {
            minValue = attributesArray.getInt(R.styleable.MeterNumberPicker_mnp_min, minValue);
            maxValue = attributesArray.getInt(R.styleable.MeterNumberPicker_mnp_max, maxValue);
            value = attributesArray.getInt(R.styleable.MeterNumberPicker_mnp_value, value);
            textColor = attributesArray.getColor(R.styleable.MeterNumberPicker_mnp_textColor, textColor);
            textSize = attributesArray.getDimensionPixelSize(R.styleable.MeterNumberPicker_mnp_textSize, (int) spToPx(textSize));
            typeface = Typeface.create(attributesArray.getString(R.styleable.MeterNumberPicker_mnp_typeface), Typeface.NORMAL);
            minWidth = attributesArray.getDimensionPixelSize(R.styleable.MeterNumberPicker_mnp_minWidth, (int) dpToPx(minWidth));
            minHeight = attributesArray.getDimensionPixelSize(R.styleable.MeterNumberPicker_mnp_minHeight, (int) dpToPx(minHeight));
            paddingHorizontal = attributesArray.getDimensionPixelSize(R.styleable.MeterNumberPicker_mnp_paddingHorizontal,
                    (int) dpToPx(paddingHorizontal));
            paddingVertical = attributesArray.getDimensionPixelSize(R.styleable.MeterNumberPicker_mnp_paddingVertical,
                    (int) dpToPx(paddingVertical));
        }

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
        textPaint = paint;
        setTextColorInt(textColor);
        setTextSizePx(textSize);
        setTypeface(typeface);

        setValue(value);
        setMaxValue(maxValue);
        setMinValue(minValue);

        adjustScroller = new Scroller(context, new DecelerateInterpolator(2.5f));
    }

    // =============================================================================================
    // -------------------------------------- MEASURING --------------------------------------------
    // =============================================================================================

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthSize = measureWidth(widthMeasureSpec);
        final int heightSize = measureHeight(heightMeasureSpec);
        setMeasuredDimension(widthSize, heightSize);
    }

    private int measureWidth(int widthMeasureSpec) {
        int specMode = MeasureSpec.getMode(widthMeasureSpec);
        int specSize = MeasureSpec.getSize(widthMeasureSpec);

        int result;
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = Math.max(minWidth, calculateTextWidthWithInternalPadding()) + getPaddingLeft() + getPaddingRight();
        }
        return result;
    }

    private int measureHeight(int heightMeasureSpec) {
        int specMode = MeasureSpec.getMode(heightMeasureSpec);
        int specSize = MeasureSpec.getSize(heightMeasureSpec);

        int result;
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = Math.max(minHeight, calculateTextHeightWithInternalPadding()) + getPaddingTop() + getPaddingBottom();
        }
        return result;
    }

    private int calculateTextWidthWithInternalPadding() {
        return calculateTextWidth() + paddingHorizontal * 2;
    }

    private int calculateTextHeightWithInternalPadding() {
        return calculateTextHeight() + paddingVertical * 2;
    }

    private int calculateTextWidth() {
        float maxDigitWidth = 0;
        for (int i = 0; i <= 9; i++) {
            final float digitWidth = textPaint.measureText(formatNumberWithLocale(i));
            if (digitWidth > maxDigitWidth) {
                maxDigitWidth = digitWidth;
            }
        }
        int numberOfDigits = 0;
        int current = maxValue;
        while (current > 0) {
            numberOfDigits++;
            current = current / 10;
        }
        return (int) (numberOfDigits * maxDigitWidth);
    }

    private int calculateTextHeight() {
        Rect bounds = new Rect();
        textPaint.getTextBounds("0", 0, 1, bounds);
        return textHeight = bounds.height();
    }

    // =============================================================================================
    // -------------------------------------- DRAWING ----------------------------------------------
    // =============================================================================================

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float x = (getRight() - getLeft()) / 2;
        float y = (getBottom() - getTop()) / 2 + textHeight / 2;

        int currentValueStart = (int) (y + currentScrollOffset);
        int prevValueStart = currentValueStart - getMeasuredHeight();
        int nextValueStart = currentValueStart + getMeasuredHeight();

        canvas.drawText(getValue(currentValueOffset + 1) + "", x, prevValueStart, textPaint);
        canvas.drawText(getValue(currentValueOffset) + "", x, currentValueStart, textPaint);
        canvas.drawText(getValue(currentValueOffset - 1) + "", x, nextValueStart, textPaint);
    }

    // =============================================================================================
    // ----------------------------------- TOUCH & SCROLL ------------------------------------------
    // =============================================================================================

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        int action = event.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                if (!adjustScroller.isFinished()) {
                    adjustScroller.forceFinished(true);
                }

                lastDownEventY = event.getY();
            }
            break;
            case MotionEvent.ACTION_MOVE: {
                lastDownOrMoveEventY = event.getY();
                int rawScrollOffset = (int) (lastDownOrMoveEventY - lastDownEventY);
                calculateCurrentOffsets(rawScrollOffset, getMeasuredHeight());
                invalidate();
            }
            break;
            case MotionEvent.ACTION_UP: {
                int rawScrollOffset = (int) (lastDownOrMoveEventY - lastDownEventY);
                int measuredHeight = getMeasuredHeight();
                int adjustedValueOffset = calculateAdjustedValueOffset(rawScrollOffset, measuredHeight);
                calculateCurrentOffsets(rawScrollOffset, measuredHeight);
                value = getValue(adjustedValueOffset);

                scrollerLastY = currentScrollOffset;
                int scrollYFrom;
                if (adjustedValueOffset == currentValueOffset) {
                    // we're scrolling the same number as on the screen
                    scrollYFrom = currentScrollOffset;
                } else {
                    // it's needed to scroll to the next number
                    if (currentScrollOffset < 0) {
                        scrollYFrom = currentScrollOffset + measuredHeight;
                    } else {
                        scrollYFrom = currentScrollOffset - measuredHeight;
                    }
                }
                adjustScroller.startScroll(0, scrollYFrom, 0, -scrollYFrom, SELECTOR_ADJUSTMENT_DURATION_MILLIS);
                currentValueOffset = 0;

                invalidate();
            }
            break;
        }
        return true;
    }

    @Override
    public void computeScroll() {
        if (adjustScroller.isFinished()) {
            return;
        }
        adjustScroller.computeScrollOffset();
        int currentScrollerY = adjustScroller.getCurrY();
        int diffScrollY = scrollerLastY - currentScrollerY;
        currentScrollOffset -= diffScrollY;
        scrollerLastY = currentScrollerY;
        invalidate();
    }

    private void calculateCurrentOffsets(int rawScrollOffset, int measuredHeight) {
        currentValueOffset = rawScrollOffset / measuredHeight;
        currentScrollOffset = Math.abs(rawScrollOffset) - Math.abs(currentValueOffset) * measuredHeight;
        currentScrollOffset *= rawScrollOffset < 0 ? -1 : 1;
    }

    private int calculateAdjustedValueOffset(int rawScrollOffset, int measuredHeight) {
        double currentValueOffset = (double) rawScrollOffset / (double) measuredHeight;
        return (int) (currentValueOffset + 0.5d * (currentValueOffset < 0 ? -1d : 1d));
    }

    // =============================================================================================
    // -------------------------------------- UTILS ------------------------------------------------
    // =============================================================================================

    private int getValue(int offset) {
        offset %= maxValue - minValue;
        if (value + offset < minValue) {
            return maxValue - (Math.abs(offset) - (value - minValue)) + 1;
        } else if (value + offset > maxValue) {
            return minValue + offset - (maxValue - value) - 1;
        }
        return value + offset;
    }

    private String formatNumberWithLocale(int value) {
        return String.format(Locale.getDefault(), "%d", value);
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private float spToPx(float sp) {
        return sp * getResources().getDisplayMetrics().scaledDensity;
    }

    // =============================================================================================
    // --------------------------------- GETTERS & SETTERS -----------------------------------------
    // =============================================================================================

    public void setMinValue(int minValue) {
        if (minValue < 0) {
            throw new IllegalArgumentException("minValue must be >= 0");
        }
        this.minValue = minValue;
        if (value < minValue) {
            value = minValue;
        }
        invalidate();
    }

    public void setMaxValue(int maxValue) {
        if (maxValue < 0) {
            throw new IllegalArgumentException("maxValue must be >= 0");
        }
        this.maxValue = maxValue;
        if (value > maxValue) {
            value = maxValue;
        }
        invalidate();
    }

    public void setValue(int value) {
        if (value < minValue) {
            throw new IllegalArgumentException("value must be >= minValue");
        }
        if (value > maxValue) {
            throw new IllegalArgumentException("value must be <= maxValue");
        }
        this.value = value;
        invalidate();
    }

    public void setTextColorInt(@ColorInt int color) {
        textPaint.setColor(textColor = color);
        invalidate();
    }

    public void setTextColorRes(@ColorRes int colorRes) {
        setTextColorInt(getResources().getColor(colorRes));
    }

    public void setTextSizePx(float size) {
        textPaint.setTextSize(textSize = size);
        invalidate();
    }

    public void setTextSizeRes(@DimenRes int textSizeRes) {
        setTextSizePx(getResources().getDimensionPixelSize(textSizeRes));
    }

    public void setTypeface(Typeface typeface) {
        this.typeface = typeface == null ? Typeface.DEFAULT : typeface;
        textPaint.setTypeface(this.typeface);
    }

    public void setTypeface(String string, int style) {
        if (TextUtils.isEmpty(string)) {
            return;
        }
        setTypeface(Typeface.create(string, style));
    }

    public void setTypeface(String string) {
        setTypeface(string, Typeface.NORMAL);
    }

    public void setTypeface(@StringRes int stringId, int style) {
        setTypeface(getResources().getString(stringId), style);
    }

    public void setTypeface(@StringRes int stringId) {
        setTypeface(stringId, Typeface.NORMAL);
    }

    public void setMinWidthPx(int width) {
        minWidth = width;
        requestLayout();
    }

    public void setMinWidthRes(@DimenRes int width) {
        setMinWidthPx(getResources().getDimensionPixelSize(width));
    }

    public void setMinHeightPx(int height) {
        minHeight = height;
        requestLayout();
    }

    public void setMinHeightRes(@DimenRes int height) {
        setMinHeightPx(getResources().getDimensionPixelSize(height));
    }

    public void setVerticalPaddingPx(int padding) {
        paddingVertical = padding;
        requestLayout();
    }

    public void setVerticalPaddingRes(int padding) {
        setVerticalPaddingPx(getResources().getDimensionPixelSize(padding));
    }

    public void setHorizontalPaddingPx(int padding) {
        paddingHorizontal = padding;
        requestLayout();
    }

    public void setHorizontalPaddingRes(int padding) {
        setHorizontalPaddingPx(getResources().getDimensionPixelSize(padding));
    }

    public int getMinHeight() {
        return minHeight;
    }

    public int getMinWidth() {
        return minWidth;
    }

    public int getMinValue() {
        return minValue;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public int getValue() {
        return value;
    }

    public int getTextColor() {
        return textColor;
    }

    public float getTextSize() {
        return textSize;
    }

    public Typeface getTypeface() {
        return typeface;
    }

    public int getPaddingHorizontal() {
        return paddingHorizontal;
    }

    public int getPaddingVertical() {
        return paddingVertical;
    }
}
