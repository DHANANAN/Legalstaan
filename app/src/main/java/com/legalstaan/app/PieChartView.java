package com.legalstaan.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * Lightweight donut-style pie chart for the test result screen.
 * Shows three segments — Correct (green), Wrong (red), Skipped (grey) —
 * with the score percentage rendered in the centre. No third-party
 * charting dependency.
 */
public class PieChartView extends View {

    private static final int COLOR_CORRECT = 0xFF1B5E20;
    private static final int COLOR_WRONG   = 0xFFB71C1C;
    private static final int COLOR_SKIPPED = 0xFF9AA6B4;
    private static final int COLOR_TRACK   = 0x22000000;

    private int correct = 0;
    private int wrong   = 0;
    private int skipped = 0;
    private int total   = 0;

    private final Paint segmentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint centerPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint percentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcRect      = new RectF();

    public PieChartView(Context ctx) { super(ctx); init(); }
    public PieChartView(Context ctx, AttributeSet attrs) { super(ctx, attrs); init(); }
    public PieChartView(Context ctx, AttributeSet attrs, int defStyle) {
        super(ctx, attrs, defStyle); init();
    }

    private void init() {
        segmentPaint.setStyle(Paint.Style.STROKE);
        segmentPaint.setStrokeCap(Paint.Cap.BUTT);
        centerPaint.setStyle(Paint.Style.FILL);
        percentPaint.setColor(0xFFFFFFFF);
        percentPaint.setTextAlign(Paint.Align.CENTER);
        percentPaint.setFakeBoldText(true);
        labelPaint.setColor(0xFFCCCCCC);
        labelPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(int correct, int wrong, int skipped) {
        this.correct = Math.max(0, correct);
        this.wrong   = Math.max(0, wrong);
        this.skipped = Math.max(0, skipped);
        this.total   = this.correct + this.wrong + this.skipped;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        int size = Math.min(w, h);
        float strokeWidth = size * 0.18f;
        segmentPaint.setStrokeWidth(strokeWidth);

        float pad = strokeWidth / 2f + dp(2);
        arcRect.set(pad, pad, size - pad, size - pad);
        // Centre the chart within the view if non-square
        float dx = (w - size) / 2f;
        float dy = (h - size) / 2f;
        arcRect.offset(dx, dy);

        // Background track for visual reference when segments don't sum to full
        segmentPaint.setColor(COLOR_TRACK);
        canvas.drawArc(arcRect, 0f, 360f, false, segmentPaint);

        if (total == 0) {
            drawCenterText(canvas, "0%", "no attempts yet");
            return;
        }

        float startAngle = -90f;       // start at 12 o'clock
        startAngle = drawSegment(canvas, startAngle, correct, COLOR_CORRECT);
        startAngle = drawSegment(canvas, startAngle, wrong,   COLOR_WRONG);
                     drawSegment(canvas, startAngle, skipped, COLOR_SKIPPED);

        int pct = Math.round((correct * 100f) / total);
        drawCenterText(canvas, pct + "%", correct + " / " + total + " correct");
    }

    private float drawSegment(Canvas canvas, float startAngle, int value, int color) {
        if (value <= 0) return startAngle;
        float sweep = (value / (float) total) * 360f;
        // Tiny gap between segments so they read as distinct slices
        float gap = total > 1 ? 1.5f : 0f;
        segmentPaint.setColor(color);
        canvas.drawArc(arcRect, startAngle + gap, sweep - gap, false, segmentPaint);
        return startAngle + sweep;
    }

    private void drawCenterText(Canvas canvas, String pct, String label) {
        float cx = arcRect.centerX();
        float cy = arcRect.centerY();
        percentPaint.setTextSize(arcRect.height() * 0.28f);
        labelPaint.setTextSize(arcRect.height() * 0.09f);
        Paint.FontMetrics fm = percentPaint.getFontMetrics();
        float pctY = cy - (fm.ascent + fm.descent) / 2f - dp(4);
        canvas.drawText(pct,   cx, pctY, percentPaint);
        canvas.drawText(label, cx, pctY + percentPaint.getTextSize() * 0.55f, labelPaint);
    }

    private float dp(float v) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics());
    }
}
