# Meter Number Picker
The android library that provides a simple and customizable NumberPicker styled as meter. It's based on [NumberPicker](https://github.com/ShawnLin013/NumberPicker).

<img src="https://raw.githubusercontent.com/alexzaitsev/meter-number-picker/master/art/screenshot.png" height="533" width="300"/>

## Content
The library contains 2 views: [MeterNumberPicker](https://github.com/alexzaitsev/meter-number-picker/blob/master/meternumberpicker/src/main/java/com/alexzaitsev/meternumberpicker/MeterNumberPicker.java) and [MeterView](https://github.com/alexzaitsev/meter-number-picker/blob/master/meternumberpicker/src/main/java/com/alexzaitsev/meternumberpicker/MeterView.java). `MeterNumberPicker` is a base block for `MeterView`. On the screenshot above whole view is the `MeterView` and a single block of it is `MeterNumberPicker`. They are pretty simple, you can easily create your own meter class based on them.

## Usage
Firstly, create a style for your number picker:
```
<style name="MeterNumberPickerStyle">
    <item name="mnp_min">0</item>
    <item name="mnp_max">9</item>
    <item name="mnp_textColor">@android:color/white</item>
    <item name="mnp_textSize">50sp</item>
    <item name="mnp_paddingHorizontal">5dp</item>
    <item name="mnp_paddingVertical">25dp</item>
</style>
```
Then, create a style for your meter view and pass the previous style:
```
<style name="MeterViewStyle">
    <item name="mv_firstColor">@android:color/black</item>
    <item name="mv_numberOfFirst">5</item>
    <item name="mv_numberOfSecond">1</item>
    <item name="mv_pickerStyle">@style/MeterNumberPickerStyle</item>
    <item name="mv_secondColor">@android:color/holo_red_dark</item>
</style>
```
Almost there! Now create your view and apply the style:
```
<com.alexzaitsev.meternumberpicker.MeterView
    android:id="@+id/meterView"
    style="@style/MeterViewStyle"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"/>
```

You can use `MeterNumberPicke` directly without `MeterView` wrapper. Look at the [MeterView](https://github.com/alexzaitsev/meter-number-picker/blob/master/meternumberpicker/src/main/java/com/alexzaitsev/meternumberpicker/MeterView.java) sources to catch more details.  
You may also want to check the [sample](https://github.com/alexzaitsev/meter-number-picker/tree/master/sample).

## Attributes

#### MeterNumberPicker
|attribute name|attribute description|
|:-:|:-:|
|mnp_min|The min value of this widget.|
|mnp_max|The max value of this widget.|
|mnp_value|The current value of this widget.|
|mnp_textColor|The text color of the numbers.|
|mnp_textSize|The text size of the numbers.|
|mnp_typeface|The typeface of the numbers.|
|mnp_minWidth|The min width of this widget.|
|mnp_minHeight|The min height of this widget.|
|mnp_paddingHorizontal|Internal horizontal padding of this widget (left/right).|
|mnp_paddingVertical|Internal vertical padding of this widget (top/bottom).|

#### MeterView
|attribute name|attribute description|
|:-:|:-:|
|mv_numberOfFirst|Number of the first-placed blocks.|
|mv_numberOfSecond|Number of the second-placed blocks.|
|mv_firstColor|Background color for the first-placed blocks.|
|mv_secondColor|Background color for the second-placed blocks.|
|mv_pickerStyle|The style for the `MeterNumberPicker`.|

## License
[MeterNumberPicker](https://github.com/alexzaitsev/meter-number-picker) is under [Apache 2.0](https://github.com/alexzaitsev/meter-number-picker/blob/master/LICENSE).
