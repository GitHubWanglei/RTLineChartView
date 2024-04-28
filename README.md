# RTLineChartView
A real-time curve component that only needs to bind variables for real-time display.  

![ezgif-7-5b58b1fcd2](https://github.com/GitHubWanglei/RTLineChartView/assets/16434720/655b812c-966a-4cd9-9239-6c306a90cde7)

### 使用：   
1.创建实时变量：
   ```java
   RTLineChartView.RTVariable var_1 = new RTLineChartView.RTVariable();
   var_1.identifier = "line_1";
   var_1.value = 246;
   var_1.paint.setStrokeWidth(3);
   var_1.paint.setColor(Color.RED);
   rtLineChartView.bindRTVariable(var_1);
   ```
2.绑定实时变量:
   ```java
   rtLineChartView.bindRTVariable(var_1);
   ```
3.开始监听：
   ```java
   // 采样率sampleRate最好不要大于屏幕刷新率，以避免不必要的性能浪费.
   rtLineChartView.startListening(60);
   ```
4.可动态设置y轴范围，防止折线越界，显示不完整.
   ```java
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
   ```
### 使用注意点：
1.每个实时变量的`identifier`需唯一，否则重复的`identifier`变量将无法绑定.   
2.关于采样率：    
&nbsp;&nbsp;采样率为每秒对变量的采样次数，也是曲线每秒的刷新次数，也是x时间轴每秒的移动次数.  
&nbsp;&nbsp;采样率越大，x时间轴移动越丝滑，但在x轴时间段内累积的采样点越多，所占内存也越大.   
&nbsp;&nbsp;因此，调用`public void startListening(float sampleRate);`时，采样率参数`sampleRate`最好不要大于屏幕刷新率，以避免不必要的性能浪费.
