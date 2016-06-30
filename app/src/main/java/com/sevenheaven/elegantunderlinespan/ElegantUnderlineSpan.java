package com.sevenheaven.elegantunderlinespan;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Build;
import android.text.Layout;
import android.text.style.LeadingMarginSpan;
import android.text.style.UpdateAppearance;

/**
 * Created by 7heaven on 16/6/29.
 */
public class ElegantUnderlineSpan implements LeadingMarginSpan, UpdateAppearance {

    private final Rect mBounds = new Rect();
    private final Paint mStroke = new Paint();
    private final Path mUnderline = new Path();
    private final Path mOutline = new Path();

    private int spanStart;
    private int spanEnd;

    private float mLineStrokeWidth;

    public ElegantUnderlineSpan(int spanStart, int spanEnd, float lineStrokeWidth) {
        this.spanStart = spanStart;
        this.spanEnd = spanEnd;

        mLineStrokeWidth = lineStrokeWidth;

        mStroke.setStyle(Paint.Style.FILL_AND_STROKE);
        mStroke.setStrokeWidth(10);
        mStroke.setStrokeCap(Paint.Cap.BUTT);
    }

    public int getLeadingMargin(boolean first) {
        return 0;
    }

    public void drawLeadingMargin(Canvas c, Paint p,
                                  int x, int dir,
                                  int top, int baseline, int bottom,
                                  CharSequence text, int start, int end,
                                  boolean first, Layout layout) {

//        Log.d("drawLeadingMargin", String.format("canvas:%s, paint:%s, x:%d, dir:%d, top:%d, baseline:%d, bottom:%d, text:%s, start:%d, end:%d, first:%s, layout:%s", c.toString(), p.toString(), x, dir, top, baseline, bottom, text.toString(), start, end, first ? "true" : "false", layout.toString()));

        if (layout != null) {
            CharSequence totalText = layout.getText();
            int startLine = layout.getLineForOffset(spanStart);
            int endLine = layout.getLineForOffset(spanEnd);

            Rect lineBounds = new Rect();

            int line = layout.getLineForOffset(start);

            boolean startInLine = startLine == line;
            boolean endInLine = endLine == line;

            int lineOffsetStart;
            int lineOffsetEnd;


            layout.getLineBounds(line, lineBounds);
            int lBaseline = layout.getLineBaseline(line) + 5;

            if (startInLine && endInLine) {
                lineOffsetStart = spanStart;
                lineOffsetEnd = spanEnd;

                lineBounds.left = (int) layout.getPrimaryHorizontal(lineOffsetStart);
                lineBounds.right = (int) layout.getSecondaryHorizontal(lineOffsetEnd);
            } else if (startInLine && !endInLine) {
                lineOffsetStart = spanStart;
                lineOffsetEnd = layout.getLineEnd(line);

                lineBounds.left = (int) layout.getPrimaryHorizontal(lineOffsetStart);
                float[] width = new float[1];
                String t = layout.getText().subSequence(layout.getLineEnd(line) - 1, layout.getLineEnd(line)).toString();
                layout.getPaint().getTextWidths(t, width);
                lineBounds.right = (int) (layout.getSecondaryHorizontal(lineOffsetEnd) + width[0]);
            } else if (!startInLine && endInLine) {
                lineOffsetStart = layout.getLineStart(line);
                lineOffsetEnd = spanEnd;
                lineBounds.right = (int) layout.getSecondaryHorizontal(lineOffsetEnd);
            } else {
                lineOffsetStart = layout.getLineStart(line);
                lineOffsetEnd = layout.getLineEnd(line);

                float[] width = new float[1];
                String t = layout.getText().subSequence(lineOffsetEnd - 1, lineOffsetEnd).toString();
                layout.getPaint().getTextWidths(t, width);
                lineBounds.right = (int) (layout.getSecondaryHorizontal(lineOffsetEnd) + width[0]);
            }

            mUnderline.reset();
            mOutline.reset();
            p.getTextBounds(totalText.toString(), lineOffsetStart, lineOffsetEnd, mBounds);
            p.getTextPath(totalText.toString(), lineOffsetStart, lineOffsetEnd, 0.0f, 0.0f, mOutline);
            buildUnderline(p.getFontMetrics().descent);

            c.save();
            c.translate(lineBounds.left, lBaseline - mLineStrokeWidth);
            c.drawPath(mUnderline, p);
            c.restore();


        }
    }

    //method from romain guy's repo https://github.com/romainguy/elegant-underline
    private void buildUnderline(float baseline) {
        Path strokedOutline = new Path();

        if (Build.VERSION.SDK_INT >= 19) {
            // Add the underline rectangle to a path
            mUnderline.addRect(
                    (float) mBounds.left, baseline - mLineStrokeWidth,
                    (float) mBounds.right, baseline,
                    Path.Direction.CW);

            // Intersects the text outline with the underline path to clip it
            mOutline.op(mUnderline, Path.Op.INTERSECT);

            // Stroke the clipped text outline and get the result as a fill path
            mStroke.getFillPath(mOutline, strokedOutline);

            // Subtract the stroked outline from the underline
            mUnderline.op(strokedOutline, Path.Op.DIFFERENCE);
        } else {
            // Create a rectangular region for the underline
            Rect underlineRect = new Rect(
                    mBounds.left, (int) (baseline - mLineStrokeWidth),
                    mBounds.right, (int) baseline);
            Region underlineRegion = new Region(underlineRect);

            // Create a region for the text outline and clip it with the underline
            Region outlineRegion = new Region();
            outlineRegion.setPath(mOutline, underlineRegion);

            // Extract the resulting region's path, we now have a clipped version
            // of the text outline
            mOutline.rewind();
            outlineRegion.getBoundaryPath(mOutline);

            // Stroke the clipped text and get the result as a fill path
            mStroke.getFillPath(mOutline, strokedOutline);

            // Create a region from the clipped stroked outline
            outlineRegion = new Region();
            outlineRegion.setPath(strokedOutline, new Region(mBounds));

            // Subtracts the clipped, stroked outline region from the underline
            underlineRegion.op(outlineRegion, Region.Op.DIFFERENCE);

            // Create a path from the underline region
            underlineRegion.getBoundaryPath(mUnderline);
        }
    }
}