package com.example.rtlinechartviewdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 实时曲线
 */
public class RTLineChartView extends View {
    // 实时变量
    public static class RTVariable {
        public String identifier = "";
        public float value = 0.f;
        public Paint paint = new Paint();
        public RTVariable() {
            paint.setStyle(Paint.Style.STROKE);
            paint.setAntiAlias(true);
            paint.setColor(Color.WHITE);
            paint.setStrokeWidth(3);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
        }
    }
    // 折线中的点
    private static class RTPointData {
        public long timestamp = 0L;
        public float value = 0.f;
    }
    // 折线
    private static class RTLineData {
        public boolean isBinding = true;
        public RTVariable rtVariable;
        public List<RTPointData> dataList = new ArrayList<>();
    }

    // 网格区域背景
    public int gridBackgroundColor = Color.BLACK;
    private final Paint gridBackgroundPaint = new Paint();
    private final Path gridBackgroundPath = new Path();
    // x轴、y轴样式
    public int xAxisWidth = 3;
    public int xAxisColor = Color.WHITE;
    private final Paint paint_x_axis = new Paint();
    public int yAxisWidth = 3;
    public int yAxisColor = Color.WHITE;
    private final Paint paint_y_axis = new Paint();
    public int yAxisRightWidth = 3;
    public int yAxisRightColor = Color.WHITE;
    private final Paint paint_y_right_axis = new Paint();
    // x轴分割线
    public int xAxisGridCount = 4;
    public int xAxisGridWidth = 1;
    public int xAxisGridColor = 0xFF999999;
    private final Paint paint_x_axis_grid = new Paint();
    public int xAxisGridCount_2 = 2;
    public int xAxisGridWidth_2 = 1;
    public int xAxisGridColor_2 = 0xFF999999;
    private final Paint paint_x_axis_grid_2 = new Paint();
    // y轴分割线
    public int yAxisGridCount = 4;
    public int yAxisGridWidth = 1;
    public int yAxisGridColor = 0xFF999999;
    private final Paint paint_y_axis_grid = new Paint();
    public int yAxisGridCount_2 = 2;
    public int yAxisGridWidth_2 = 1;
    public int yAxisGridColor_2 = 0xFF999999;
    private final Paint paint_y_axis_grid_2 = new Paint();
    // x轴标注
    public int xAxisTextSize = 16;
    public int xAxisTextColor = Color.WHITE;
    private final Paint paint_x_axis_text = new Paint();
    public int xAxisTimeUnit = TIME_UNIT_SECOND;
    public static int TIME_UNIT_SECOND = 1;
    public static int TIME_UNIT_MINUTE = 2;
    public static int TIME_UNIT_HOUR = 3;
    public int xAxisTimeLength = 60;
    public int xAxisTimeFormat = TIME_FORMAT_MM_SS;
    public static int TIME_FORMAT_SS = 1;
    public static int TIME_FORMAT_MM = 2;
    public static int TIME_FORMAT_HH = 3;
    public static int TIME_FORMAT_MM_SS = 4;
    public static int TIME_FORMAT_HH_MM = 5;
    public static int TIME_FORMAT_HH_MM_SS = 6;
    public boolean showXAxisText = true;
    // y轴标注
    public int yAxisTextSize = 16;
    public int yAxisTextColor = Color.WHITE;
    private final Paint paint_y_axis_text = new Paint();
    public int yAxisDecimalPlaceCount = 2;
    private float yAxisMinValue = 0.f;
    private float yAxisMaxValue = 100.f;
    public RTLineChartYAxisDynamicValue yAxisDynamicValue;
    public boolean showYAxisText = true;
    // 右侧y轴标注
    public int yAxisRightTextSize = 16;
    public int yAxisRightTextColor = Color.WHITE;
    private final Paint paint_y_right_axis_text = new Paint();
    public int yAxisRightDecimalPlaceCount = 2;
    // 是否显示右侧y轴
    public boolean showRightYAxis = false;
    /**
     * y轴左侧和右侧值的比例关系。
     * eg: (l1-l2) ~ (r1, r2)，则转换比例: scale = (r2-r1) / (l2-l1)。
     * 给定左侧值left_value，则右侧值：right_value = left_value * scale;
     */
    public float rightYAxisValueConvertScale = 1.f;
    // 间距
    public int margin_left = 80;
    public int margin_bottom = 50;
    public int margin_top = 20;
    public int margin_right = 20;
    // 虚线虚实比例
    public float[] dashIntervals = new float[]{3, 8};
    // x轴原点处的时间戳(最小时间)
    private long minTimestamp = 0L;
    // 第一个分割线的时间戳
    private long firstGridTimestamp;
    // 第一个分割线距离Y轴的间距
    private float firstGridLeftMargin = 0.f;
    // timer
    private Timer timer;
    // 所有实时变量以及历史数据
    private final List<RTLineData> lineDataList = new ArrayList<>();
    // 折线 path
    private final Path linePath = new Path();
    // 遮挡，折线超出y轴范围时，进行遮挡
    private final Path topOverlayPath = new Path();
    private final Path bottomOverlayPath = new Path();
    private final Paint overlayPaint = new Paint();
    // RTLineChartYAXisValueFormat
    private RTLineChartYAXisValueFormat yAXisValueFormat;

    public RTLineChartView(Context context) {
        this(context, null);
    }

    public RTLineChartView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RTLineChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        configData();
    }

    private void configData() {

        gridBackgroundPaint.setStyle(Paint.Style.FILL);
        gridBackgroundPaint.setColor(gridBackgroundColor);

        paint_x_axis.setStyle(Paint.Style.STROKE);
        paint_x_axis.setColor(xAxisColor);
        paint_x_axis.setStrokeWidth(xAxisWidth);
        paint_x_axis.setStrokeCap(Paint.Cap.ROUND);

        paint_y_axis.setStyle(Paint.Style.STROKE);
        paint_y_axis.setColor(yAxisColor);
        paint_y_axis.setStrokeWidth(yAxisWidth);
        paint_y_axis.setStrokeCap(Paint.Cap.ROUND);

        paint_y_right_axis.setStyle(Paint.Style.STROKE);
        paint_y_right_axis.setColor(yAxisRightColor);
        paint_y_right_axis.setStrokeWidth(yAxisRightWidth);
        paint_y_right_axis.setStrokeCap(Paint.Cap.ROUND);

        paint_x_axis_grid.setStyle(Paint.Style.STROKE);
        paint_x_axis_grid.setColor(xAxisGridColor);
        paint_x_axis_grid.setStrokeWidth(xAxisGridWidth);
        paint_x_axis_grid.setStrokeCap(Paint.Cap.ROUND);
        PathEffect effects = new DashPathEffect(dashIntervals, 0);
        paint_x_axis_grid.setPathEffect(effects);

        paint_x_axis_grid_2.setStyle(Paint.Style.STROKE);
        paint_x_axis_grid_2.setColor(xAxisGridColor_2);
        paint_x_axis_grid_2.setStrokeWidth(xAxisGridWidth_2);
        paint_x_axis_grid_2.setStrokeCap(Paint.Cap.ROUND);
        paint_x_axis_grid_2.setPathEffect(effects);

        paint_y_axis_grid.setStyle(Paint.Style.STROKE);
        paint_y_axis_grid.setColor(yAxisGridColor);
        paint_y_axis_grid.setStrokeWidth(yAxisGridWidth);
        paint_y_axis_grid.setStrokeCap(Paint.Cap.ROUND);
        paint_y_axis_grid.setPathEffect(effects);

        paint_y_axis_grid_2.setStyle(Paint.Style.STROKE);
        paint_y_axis_grid_2.setColor(yAxisGridColor_2);
        paint_y_axis_grid_2.setStrokeWidth(yAxisGridWidth_2);
        paint_y_axis_grid_2.setStrokeCap(Paint.Cap.ROUND);
        paint_y_axis_grid_2.setPathEffect(effects);

        paint_x_axis_text.setAntiAlias(true);
        paint_x_axis_text.setTextSize(xAxisTextSize);
        paint_x_axis_text.setFakeBoldText(true);
        paint_x_axis_text.setColor(xAxisTextColor);
        paint_x_axis_text.setTextAlign(Paint.Align.CENTER);

        paint_y_axis_text.setAntiAlias(true);
        paint_y_axis_text.setTextSize(yAxisTextSize);
        paint_y_axis_text.setFakeBoldText(true);
        paint_y_axis_text.setColor(yAxisTextColor);
        paint_y_axis_text.setTextAlign(Paint.Align.RIGHT);

        paint_y_right_axis_text.setAntiAlias(true);
        paint_y_right_axis_text.setTextSize(yAxisRightTextSize);
        paint_y_right_axis_text.setFakeBoldText(true);
        paint_y_right_axis_text.setColor(yAxisRightTextColor);
        paint_y_right_axis_text.setTextAlign(Paint.Align.LEFT);

        overlayPaint.setStyle(Paint.Style.FILL);

    }

    /**
     * 动态设置y轴最大、最小值，防止曲线越界
     * @param dynamicValue 回调参数，重写方法时，需动态返回最大、最小值
     */
    public void setYAxisDynamicValue(RTLineChartYAxisDynamicValue dynamicValue) {
        yAxisDynamicValue = dynamicValue;
    }

    /**
     * 自定义y轴文字的显示格式，
     * @param format
     */
    public void setYAxisValueFormat(RTLineChartYAXisValueFormat format) {
        yAXisValueFormat = format;
    }

    /**
     * 刷新样式，修改样式后，调用此方法使样式生效
     */
    public void refreshStyle() {
        configData();
    }

    /**
     * 开始监听
     * @param sampleRate 采样率
     */
    public void startListening(float sampleRate) {

        if (sampleRate <= 0) {
            sampleRate = 1;
        }

        refreshStyle();

        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        if (xAxisGridCount <= 0) xAxisGridCount = 1;
        if (xAxisGridCount_2 <= 0) xAxisGridCount_2 = 1;
        if (yAxisGridCount <= 0) yAxisGridCount = 1;
        if (yAxisGridCount_2 <= 0) yAxisGridCount_2 = 1;

        firstGridTimestamp = System.currentTimeMillis() - getAxisTimeInterval(xAxisTimeLength, xAxisTimeUnit);
        firstGridLeftMargin = (getWidth() - margin_left - margin_right) / (float) xAxisGridCount;

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                        long timestamp = System.currentTimeMillis();
                        minTimestamp = timestamp - getAxisTimeInterval(xAxisTimeLength, xAxisTimeUnit);
                        if (minTimestamp >= firstGridTimestamp) {
                            firstGridTimestamp += getAxisTimeInterval(xAxisTimeLength, xAxisTimeUnit)/xAxisGridCount;
                        }
                        firstGridLeftMargin = (firstGridTimestamp - minTimestamp) / (float)getAxisTimeInterval(xAxisTimeLength, xAxisTimeUnit) * (getWidth() - margin_left - margin_right);

                        float minValueInLine = lineDataList.size() > 0 ? Float.MAX_VALUE : 0.f;
                        float maxValueInLine = lineDataList.size() > 0 ? Float.MIN_VALUE : 100.f;
                        for (int i = lineDataList.size() - 1; i >= 0; i--) {
                            RTLineData lineData = lineDataList.get(i);
                            // 添加当前时间点的数据
                            if (lineData.isBinding) {
                                RTPointData pointData = new RTPointData();
                                pointData.timestamp = timestamp;
                                pointData.value = lineData.rtVariable.value;
                                lineData.dataList.add(pointData);
                            }
                            // 将x轴时间段之外的数据删除
                            for (int j = lineData.dataList.size() - 1; j >= 0; j--) {
                                if (lineData.dataList.get(j).timestamp < minTimestamp) {
                                    lineData.dataList.remove(j);
                                }
                            }
                            // 已解除绑定的变量历史数据，随着时间的流逝，历史数据已被全部删除完，则删除此实时变量
                            if (!lineData.isBinding && lineData.dataList.size() == 0) {
                                lineDataList.remove(i);
                            }
                            // 找最大、最小值
                            for (int j = 0; j < lineData.dataList.size(); j++) {
                                float value = lineData.dataList.get(j).value;
                                if (minValueInLine > value) {
                                    minValueInLine = value;
                                }
                                if (maxValueInLine < value) {
                                    maxValueInLine = value;
                                }
                            }
                        }

                        // 获取最大、最小值
                        if (yAxisDynamicValue != null) {
                            yAxisMinValue = yAxisDynamicValue.minValue(minValueInLine);
                            yAxisMaxValue = yAxisDynamicValue.maxValue(maxValueInLine);
                        } else {
                            yAxisMinValue = 0.f;
                            yAxisMaxValue = 100.f;
                        }
                        if (yAxisMaxValue <= yAxisMinValue) {
                            return;
                        }

                        // 网格背景path
                        gridBackgroundPath.reset();
                        gridBackgroundPath.moveTo(margin_left, getHeight()-margin_bottom);
                        gridBackgroundPath.lineTo(margin_left, margin_top);
                        gridBackgroundPath.lineTo(getWidth()-margin_right, margin_top);
                        gridBackgroundPath.lineTo(getWidth()-margin_right, getHeight()-margin_bottom);
                        gridBackgroundPath.close();

                        // 顶部遮挡
                        topOverlayPath.reset();
                        topOverlayPath.moveTo(margin_left, margin_top-yAxisGridWidth);
                        topOverlayPath.lineTo(getWidth()-margin_right+2, margin_top-yAxisGridWidth);
                        topOverlayPath.lineTo(getWidth()-margin_right+2, 0);
                        topOverlayPath.lineTo(margin_left, 0);
                        // 底部遮挡
                        bottomOverlayPath.reset();
                        bottomOverlayPath.moveTo(margin_left, getHeight()-margin_bottom+xAxisWidth/2.f);
                        bottomOverlayPath.lineTo(getWidth()-margin_right+2, getHeight()-margin_bottom+xAxisWidth/2.f);
                        bottomOverlayPath.lineTo(getWidth()-margin_right+2, getHeight());
                        bottomOverlayPath.lineTo(margin_left, getHeight());
                        // overlay画笔
                        Drawable background = getBackground();
                        if (background instanceof ColorDrawable) {
                            overlayPaint.setColor(((ColorDrawable)background).getColor());
                        } else {
                            overlayPaint.setColor(Color.BLACK);
                        }

                        invalidate();

                    }
                });
            }
        }, 2, (long) (1.f / sampleRate * 1000));
    }

    /**
     * 停止监听，页面即将销毁时需主动调用，防止内存泄漏
     */
    public void stopListening() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    /**
     * 绑定实时变量
     * @param variable 实时变量
     */
    public void bindRTVariable(RTVariable variable) {
        boolean isExist = false;
        for (int i = 0; i < lineDataList.size(); i++) {
            if (lineDataList.get(i).rtVariable.identifier.equals(variable.identifier)) {
                isExist = true;
            }
        }
        if (isExist) return;
        RTLineData lineData = new RTLineData();
        lineData.rtVariable = variable;
        lineDataList.add(lineData);
    }

    /**
     * 解绑实时变量，此变量的历史数据不会立即删除(折线断开，但不会立即消失)，随着时间流逝，历史数据会被逐渐删除
      * @param variable 实时变量
     */
    public void unbindRTVariable(RTVariable variable) {
        for (int i = 0; i < lineDataList.size(); i++) {
            if (lineDataList.get(i).rtVariable.identifier.equals(variable.identifier)) {
                lineDataList.get(i).isBinding = false;
                break;
            }
        }
    }

    /**
     * 解绑并立即删除历史数据，此变量的折线立即消失
     * @param variable 实时变量
     */
    public void unbindAndClearRTVariable(RTVariable variable) {
        for (int i = 0; i < lineDataList.size(); i++) {
            if (lineDataList.get(i).rtVariable.identifier.equals(variable.identifier)) {
                lineDataList.remove(i);
                break;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float xAxisLength = getWidth() - margin_left - margin_right;
        float yAxisLength = getHeight() - margin_top - margin_bottom;
        // 网格背景
        canvas.drawPath(gridBackgroundPath, gridBackgroundPaint);
        // x轴
        canvas.drawLine(margin_left, yAxisLength+margin_top, margin_left+xAxisLength, yAxisLength+margin_top, paint_x_axis);
        // y轴
        canvas.drawLine(margin_left, yAxisLength+margin_top, margin_left, margin_top, paint_y_axis);
        // 右侧y轴
        if (showRightYAxis) {
            canvas.drawLine(margin_left+yAxisWidth/2.f+xAxisLength, yAxisLength+margin_top, margin_left+yAxisWidth/2.f+xAxisLength, margin_top, paint_y_right_axis);
        }
        // x轴网格线
        for (int i = 0; i < xAxisGridCount; i++) {
            float start_x = margin_left + firstGridLeftMargin + (xAxisLength / xAxisGridCount) * i;
            if (start_x > margin_left + xAxisLength) {
                break;
            }
            canvas.drawLine(start_x, margin_top+yAxisLength-xAxisWidth, start_x, margin_top, paint_x_axis_grid);
            // 次分割线(网格之间再次分割)
            for (int j = 0; j < xAxisGridCount_2; j++) {
                if (j + 1 == xAxisGridCount_2) break; // 最后一条分割线不用画
                if (i == 0) { // 第一段分割线之前的次分割线
                    float margin_left_offset = firstGridLeftMargin - (xAxisLength / xAxisGridCount);
                    float start_x_2 = margin_left + margin_left_offset + (xAxisLength / xAxisGridCount) / xAxisGridCount_2 * (j + 1);
                    if (start_x_2 > margin_left) {
                        canvas.drawLine(start_x_2, margin_top+yAxisLength-xAxisWidth, start_x_2, margin_top, paint_x_axis_grid_2);
                    }
                }
                float start_x_2 = start_x + (xAxisLength / xAxisGridCount) / xAxisGridCount_2 * (j + 1);
                if (start_x_2 <= margin_left + xAxisLength) {
                    canvas.drawLine(start_x_2, margin_top+yAxisLength-xAxisWidth, start_x_2, margin_top, paint_x_axis_grid_2);
                }
            }
        }
        // y轴网格线
        for (int i = 0; i < yAxisGridCount; i++) {
            float start_y = getHeight() - margin_bottom - (yAxisLength / yAxisGridCount) * (i + 1);
            canvas.drawLine(margin_left, start_y, margin_left+xAxisLength, start_y, paint_y_axis_grid);
            // y轴文字
            if (showYAxisText) {
                float valueInterval = (yAxisMaxValue - yAxisMinValue) / yAxisGridCount;
                float value = yAxisMinValue + valueInterval * (i + 1);
                String valueStr = "";
                if (yAXisValueFormat != null && yAXisValueFormat.leftValueFormat(value) != null) {
                    valueStr = yAXisValueFormat.leftValueFormat(value);
                } else {
                    if (yAxisDecimalPlaceCount > 0) {
                        String format = "%."+yAxisDecimalPlaceCount+"f";
                        valueStr = String.format(format, value);
                    } else {
                        valueStr = String.format(Locale.CHINA, "%d", (int)value);
                    }
                }
                Paint.FontMetrics fontMetrics = paint_y_axis_text.getFontMetrics();
                float offset_y = fontMetrics.ascent / 2.8f;
                if (i == yAxisGridCount - 1) {
                    offset_y = fontMetrics.ascent+5;
                }
                canvas.drawText(valueStr, margin_left-8, start_y-offset_y, paint_y_axis_text);
            }
            // 右侧y轴
            if (showRightYAxis) {
                float valueInterval = (yAxisMaxValue - yAxisMinValue) / yAxisGridCount;
                float value = (yAxisMinValue + valueInterval * (i + 1)) * rightYAxisValueConvertScale;
                String valueStr = "";
                if (yAXisValueFormat != null && yAXisValueFormat.rightValueFormat(value) != null) {
                    valueStr = yAXisValueFormat.rightValueFormat(value);
                } else {
                    if (yAxisRightDecimalPlaceCount > 0) {
                        String format = "%."+yAxisRightDecimalPlaceCount+"f";
                        valueStr = String.format(format, value);
                    } else {
                        valueStr = String.format(Locale.CHINA, "%d", (int)value);
                    }
                }
                Paint.FontMetrics fontMetrics = paint_y_right_axis_text.getFontMetrics();
                float offset_y = fontMetrics.ascent / 2.8f;
                if (i == yAxisGridCount - 1) {
                    offset_y = fontMetrics.ascent+5;
                }
                canvas.drawText(valueStr, margin_left+xAxisLength+yAxisRightWidth+8, start_y-offset_y, paint_y_right_axis_text);
            }
            // 次划线(网格之间再次分割)
            for (int j = 0; j < yAxisGridCount_2; j++) {
                if (j + 1 == yAxisGridCount_2) break; // 最后一条分割线不用画
                if (i == 0) {
                    float start_y_2 = getHeight() - margin_bottom - (yAxisLength / yAxisGridCount) / yAxisGridCount_2 * (j + 1);
                    canvas.drawLine(margin_left+yAxisWidth, start_y_2, margin_left+xAxisLength, start_y_2, paint_y_axis_grid_2);
                }
                float start_y_2 = start_y + (yAxisLength / yAxisGridCount) / yAxisGridCount_2 * (j + 1);
                canvas.drawLine(margin_left+yAxisWidth, start_y_2, margin_left+xAxisLength, start_y_2, paint_y_axis_grid_2);
            }
        }
        // y轴文字(最小值)
        if (showYAxisText) {
            String valueStr = "";
            if (yAXisValueFormat != null && yAXisValueFormat.leftValueFormat(yAxisMinValue) != null) {
                valueStr = yAXisValueFormat.leftValueFormat(yAxisMinValue);
            } else {
                if (yAxisDecimalPlaceCount > 0) {
                    String format = "%."+yAxisDecimalPlaceCount+"f";
                    valueStr = String.format(format, yAxisMinValue);
                } else {
                    valueStr = String.format(Locale.CHINA, "%d", (int)yAxisMinValue);
                }
            }
            canvas.drawText(valueStr, margin_left-8, getHeight()-margin_bottom, paint_y_axis_text);
        }
        // 右侧y轴文字(最小值)
        if (showRightYAxis) {
            String valueStr = "";
            if (yAXisValueFormat != null && yAXisValueFormat.rightValueFormat(yAxisMinValue) != null) {
                valueStr = yAXisValueFormat.rightValueFormat(yAxisMinValue);
            } else{
                if (yAxisRightDecimalPlaceCount > 0) {
                    String format = "%."+yAxisRightDecimalPlaceCount+"f";
                    valueStr = String.format(format, yAxisMinValue*rightYAxisValueConvertScale);
                } else {
                    valueStr = String.format(Locale.CHINA, "%d", (int)(yAxisMinValue*rightYAxisValueConvertScale));
                }
            }
            canvas.drawText(valueStr, margin_left+xAxisLength+yAxisRightWidth+8, getHeight()-margin_bottom, paint_y_right_axis_text);
        }
        // 折线
        boolean isOutTopRange = false;
        boolean isOutBottomRange = false;
        for (int i = 0; i < lineDataList.size(); i++) {
            RTLineData lineData = lineDataList.get(i);
            if (lineData.dataList.size() == 0) continue;
            float start_x = margin_left + yAxisWidth + (lineData.dataList.get(0).timestamp - minTimestamp) / (float)getAxisTimeInterval(xAxisTimeLength, xAxisTimeUnit) * (xAxisLength - yAxisWidth);
            float start_y = yAxisLength - (lineData.dataList.get(0).value - yAxisMinValue) / (yAxisMaxValue - yAxisMinValue) * yAxisLength + margin_top;
            linePath.reset();
            linePath.moveTo(start_x, start_y);
            for (int j = 1; j < lineData.dataList.size(); j++) {
                RTPointData pointData = lineData.dataList.get(j);
                float end_x = margin_left + yAxisWidth + (pointData.timestamp - minTimestamp) / (float)getAxisTimeInterval(xAxisTimeLength, xAxisTimeUnit) * (xAxisLength - yAxisWidth);
                float end_y = yAxisLength - (pointData.value - yAxisMinValue) / (yAxisMaxValue - yAxisMinValue) * yAxisLength + margin_top;
                linePath.lineTo(end_x, end_y);
                if (!isOutTopRange && end_y < margin_top) {
                    isOutTopRange = true;
                }
                if (!isOutBottomRange && end_y > (getHeight()-margin_bottom)) {
                    isOutBottomRange = true;
                }
            }
            canvas.drawPath(linePath, lineData.rtVariable.paint);
        }

        // 超出y轴范围，则进行遮挡
        if (isOutTopRange) {
            canvas.drawPath(topOverlayPath, overlayPaint);
        }
        if (isOutBottomRange) {
            canvas.drawPath(bottomOverlayPath, overlayPaint);
        }

        // x轴时间文字
        if (showXAxisText) {
            for (int i = 0; i < xAxisGridCount; i++) {
                float start_x = margin_left + firstGridLeftMargin + (xAxisLength / xAxisGridCount) * i;
                if (start_x > margin_left + xAxisLength) {
                    break;
                }
                long timeStamp = firstGridTimestamp + getAxisTimeInterval(xAxisTimeLength, xAxisTimeUnit) / xAxisGridCount * i;
                String timeStr = formatTimestamp(timeStamp, getFormat(xAxisTimeFormat));
                canvas.drawText(timeStr, start_x, margin_top + yAxisLength + 20, paint_x_axis_text);
            }
        }

    }

    private String formatTimestamp(long timestamp, String formatString) {
        SimpleDateFormat sdf = new SimpleDateFormat(formatString, Locale.getDefault());
        Date date = new Date(timestamp);
        return sdf.format(date);
    }

    private String getFormat(int timeFormat) {
        if (timeFormat == TIME_FORMAT_SS) {
            return "ss";
        } else if (timeFormat == TIME_FORMAT_MM) {
            return "mm";
        } else if (timeFormat == TIME_FORMAT_HH) {
            return "HH";
        } else if (timeFormat == TIME_FORMAT_MM_SS) {
            return "mm:ss";
        } else if (timeFormat == TIME_FORMAT_HH_MM) {
            return "HH:mm";
        } else if (timeFormat == TIME_FORMAT_HH_MM_SS) {
            return "HH:mm:ss";
        }
        return "mm:ss";
    }

    private long getAxisTimeInterval(long length, int unit) {
        if (unit == TIME_UNIT_MINUTE) {
            return length * 60 * 1000;
        } else if (unit == TIME_UNIT_HOUR) {
            return length * 60 * 60 * 1000;
        } else {
            return length * 1000;
        }
    }

    public interface RTLineChartYAxisDynamicValue {
        float minValue(float minValueInLine);
        float maxValue(float maxValueInLine);
    }

    public interface RTLineChartYAXisValueFormat {
        String leftValueFormat(float value);
        String rightValueFormat(float value);
    }

}

