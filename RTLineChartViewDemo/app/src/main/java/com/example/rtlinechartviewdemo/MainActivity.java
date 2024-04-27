package com.example.rtlinechartviewdemo;

import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RTLineChartView rtLineChartView = findViewById(R.id.rt_line_chart_view);

//        rtLineChartView.xAxisColor = Color.YELLOW;
//        rtLineChartView.yAxisColor = Color.GREEN;

        rtLineChartView.xAxisGridCount = 6;
//        rtLineChartView.xAxisGridColor = Color.YELLOW;
        rtLineChartView.xAxisGridCount_2 = 4;
//        rtLineChartView.xAxisGridColor_2 = Color.WHITE;
        rtLineChartView.yAxisGridCount = 2;
//        rtLineChartView.yAxisGridColor = Color.RED;
        rtLineChartView.yAxisGridCount_2 = 3;
//        rtLineChartView.yAxisGridColor_2 = Color.WHITE;

        rtLineChartView.xAxisTextColor = Color.YELLOW;
//        rtLineChartView.yAxisTextColor = Color.RED;

        rtLineChartView.xAxisTimeLength = 60;
        rtLineChartView.xAxisTimeUnit = RTLineChartView.TIME_UNIT_SECOND;
        rtLineChartView.xAxisTimeFormat = RTLineChartView.TIME_FORMAT_HH_MM_SS;

//        rtLineChartView.yAxisMinValue = -100;
//        rtLineChartView.yAxisMaxValue = 500;
        rtLineChartView.yAxisGridCount = 5;
        rtLineChartView.yAxisDecimalPlaceCount = 1;

//        rtLineChartView.dashIntervals = new float[]{5, 5};
//        rtLineChartView.gridBackgroundColor = Color.BLUE;
//        rtLineChartView.setBackgroundColor(Color.RED);

        rtLineChartView.startListening(60);

        RTLineChartView.RTVariable var_1 = new RTLineChartView.RTVariable();
        var_1.identifier = "line_1";
        var_1.value = 246;
        var_1.paint.setStrokeWidth(3);
        var_1.paint.setColor(Color.RED);
        rtLineChartView.bindRTVariable(var_1);

        RTLineChartView.RTVariable var_2 = new RTLineChartView.RTVariable();
        var_2.identifier = "line_2";
        var_2.value = 200;
        var_2.paint.setStrokeWidth(3);
        var_2.paint.setColor(Color.GREEN);
        rtLineChartView.bindRTVariable(var_2);

        RTLineChartView.RTVariable var_3 = new RTLineChartView.RTVariable();
        var_3.identifier = "line_3";
        var_3.value = 150;
        var_3.paint.setStrokeWidth(3);
        var_3.paint.setColor(Color.YELLOW);
        rtLineChartView.bindRTVariable(var_3);

        // 动态设置y轴最大、最小值，防止曲线越界
        rtLineChartView.setYAxisDynamicValue(new RTLineChartView.RTLineChartYAxisDynamicValue() {
            @Override
            public float minValue(float minValueInLine) {
                if (minValueInLine < 0) {
                    return minValueInLine - 20;
                }
                return 0;
            }

            @Override
            public float maxValue(float maxValueInLine) {
                if (maxValueInLine > 500) {
                    return maxValueInLine + 50;
                }
                return 500;
            }
        });

        TextView tv_1 = findViewById(R.id.variable_1);
        tv_1.setText(var_1.value+"");
        TextView tv_2 = findViewById(R.id.variable_2);
        tv_2.setText(var_2.value+"");
        TextView tv_3 = findViewById(R.id.variable_3);
        tv_3.setText(var_3.value+"");

        // 模拟实时变化
        final boolean[] up = {true};
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (up[0]) {
                    var_1.value += new Random().nextInt(100);
                    var_2.value += 15;
                    var_3.value += 10;
                    if (var_1.value > 560) {
                        up[0] = false;
                    }
                } else {
                    var_1.value -= new Random().nextInt(100);
                    var_2.value -= 15;
                    var_3.value -= 10;
                    if (var_1.value < -10) {
                        up[0] = true;
                    }
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        tv_1.setText(var_1.value+"");
                        tv_2.setText(var_2.value+"");
                        tv_3.setText(var_3.value+"");
                    }
                });
            }
        }, 1500, 500);

    }
}