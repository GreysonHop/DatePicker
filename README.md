# DatePicker
#### 一、简介：

一个可以选择年月日、时分的dialog，选择的项有年月日（日历形）、时分（滚轮）

参考网上的demo，（源地址找不到）

###### 版本更新：

##### 1.1.0：

1. 优化日历视图的滚动动画：当滑动距离不足以切换上（下）一个月视图时闪动的问题。
2. 日期对话框增加可选模式，如“只显示日历、不显示时间”等，同时可以减少不必要的视图绘制。
3. 改为Dialog初始化和显示时不会默认选中任何一天，完全交由调用者决定选中哪一天，也是为了减少无用操作所产生的绘制。



#### 二、展示效果：

1、点击第一项，显示所有视图，并且点击了时间选择视图：

<div align=left>![pic1](https://raw.githubusercontent.com/GreysonHop/DatePicker/master/pic/DatePicker_1.png)

![pic2](https://raw.githubusercontent.com/GreysonHop/DatePicker/master/pic/DatePicker_2.png)

2、主页点击第二项，只显示日历的视图，并点击了“10月24日”：

![pic3](https://raw.githubusercontent.com/GreysonHop/DatePicker/master/pic/DatePicker_3.png)

3、主页点击第三项，只显示时间的视图：

![pic4](https://raw.githubusercontent.com/GreysonHop/DatePicker/master/pic/DatePicker_4.png)



4、点击确定按钮，可显示结果；样式可以自己参考代码调，目前的参数定制还比较少，希望对大家也有一些参考的价值：

![pic5](https://raw.githubusercontent.com/GreysonHop/DatePicker/master/pic/DatePicker_5.png)