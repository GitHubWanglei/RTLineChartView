# RTLineChartView
A real-time curve component that only needs to bind variables for real-time display.  

![ezgif-6-7a443b1556](https://github.com/GitHubWanglei/RTLineChartView/assets/16434720/6c72afe1-4cd0-448b-9b13-ed86dc8c16c0)

### 使用：
1. 创建实时变量：
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
   rtLineChartView.startListening(60);
   ```
### 使用注意点：
1.每个实时变量的`identifier`需唯一，否则重复的`identifier`变量将无法绑定. 
2.关于采样率：   
   采样率为每秒对变量的采样次数，也是曲线每秒的刷新次数. 
   采样率越大，x时间轴移动越丝滑，但在x轴时间段内累积的采样点越多，所占内存也越大. 
   因此，调用`public void startListening(float sampleRate);`时，采样率参数`sampleRate`最好不要大于屏幕刷新率，以避免不必要的性能浪费.
