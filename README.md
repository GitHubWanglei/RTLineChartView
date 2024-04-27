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
