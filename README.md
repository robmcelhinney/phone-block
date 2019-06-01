![Logo of the project](https://github.com/robmcelhinney/PhoneBlock/raw/master/app/src/main/res/drawable/ic_stat_notify_driving.png)
# PhoneBlock
> Android Application aiming to prevent phone use while driving.

Native Android Application designed to detect when a user is driving and then
prevent the user from using other applications. Hoped to use way drivers enter
car as way to differentiate them from passengers, by the movement of the phone
in the user’s pants pocket. Created training dataset of sitting into car 
activity by recording the using accelerometer data and trained a Long Short Term
Neural Network for Human Activity Recognition in Tensorflow. The model
created from this training is exported to the Android application. Detects user’s activity,
and records the device’s accelerometer data to compare against the model, predicting a match with
entering a car. If the device is detected to be in a vehicle, through the activity recognition 
API, block activates.

## Installing / Getting started

```
git clone https://github.com/robmcelhinney/PhoneBlock.git
cd PhoneBlock
```
You can now open the project with Android Studio.
To build the app from the command line
```
./gradlew assembleDebug
```
The app will be found at `/build/outputs/apk/`

Run the emulator and drag app-debug.apk in, then you can open PhoneBlock.

![PhoneBlock homescreen](https://github.com/robmcelhinney/PhoneBlock/raw/master/readme_assets/homescreen.png)

## Features

Using Google’s Activity Recognition API, it is possible to detect a user’s activity,
such as when they are on their feet. Then the application records the device’s
accelerometer data and compares it to the model, predicting a match with
entering a car. If the match is deemed sufficiently similar, when the device is
detected to be in a vehicle, through the activity recognition API, the block is
activated. There is also an option to activate the block once the user is assumed
to be in a vehicle and is connected to a Bluetooth headset.

What's all the bells and whistles this project can perform?
* Neural Network for Activity Recognition
* Detecting When Sitting Into Car
* Blocking Other Applications
* Bluetooth Detection

![PhoneBlock notification](https://github.com/robmcelhinney/PhoneBlock/raw/master/readme_assets/notification.png)
![LSTM](https://github.com/robmcelhinney/PhoneBlock/raw/master/readme_assets/lstm.png)

## Help
- https://github.com/ricvalerio/foregroundappchecker 
- https://aqibsaeed.github.io/2016-11-04-human-activity-recognition-cnn/
- http://curiousily.com/data-science/2017/06/03/tensorflow-for-hackers-part-
6.html

## Licensing
"The code in this project is licensed under MIT license."
