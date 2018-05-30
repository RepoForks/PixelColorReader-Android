## Pixel Color Reader - Android
Pixel color reader is a minimalist library to read pixel colors off the display, during runtimes.


## Backstory
Since android doesn't have any built in api(s) to do display -read- related tasks, it was a hard time for me to implement a screen color reader in an android reader. Luckily I found a solution and decided to create a library of it so any one else having the same necessities can use it too.

## Info About Library
Since android doesn't provide a direct method to take screenshots in android, what we do is that we use the [MediaProjection API](https://developer.android.com/reference/android/media/projection/MediaProjection)  in order to create a [Virtual Display](https://developer.android.com/reference/android/hardware/display/VirtualDisplay) through which we get screenshots and read pixel color(s) off it.

## Usage
***Adding it to your project***

**Step 1.**
Add the jitpack repository to your app.
In your root build.gradle file. Add `maven {url 'https://jitpack.io' }` in allprojects -> repositories so that it looks like 

    allprojects {
    		repositories {
    			...
    			maven { url 'https://jitpack.io' }
    		}
    	}

**Step 2.**
Clone the library. To do this add the following dependency

    implementation 'com.github.hdsrivastava:PixelColorReader-Android:0.1-stable'

After doing these changes, hit **sync now**, to build the library

***Using the library***

To use the library, you must create the `ScreenColorPicker` object by passing in the context, screen width and height. Example - 

    ScreenColorPicker scpObj = new ScreenColorPicker(this, 1080, 1920);

here `this`is the context I am passing, and 1080 , 1920 are my screens resolution. You can also pass Point or DisplayMetrics referenced  variables, but they must be of int type. If you are not sure about the device's measurements. Refer to [this
](https://alvinalexander.com/android/how-to-determine-android-screen-size-dimensions-orientation) article for more info.

After this, you need to initialise the screen capture service through object. For this call the initialise() method, just after object declaration.

    scpObj.initialise();

Now, here's the important part. As I said, this library depends upon MediaProjection and VirtualDisplays we now need to request the capture permission before we can begin. In order to avoid NullPointerExceptions it is safe that we request the callback everytime we start MediaProjection. So in order to start the capture service, we first need to check if the permission has been granted or not.
For this override the onActivityResult in your activity and call the following function

    screenColorPicker.checkPermission(new ScreenColorPicker.PermissionCallbacks() {  
        @Override  
      public void onPermissionGranted() {  
            //add code to be performed when permission is granted.
      }  
      
        @Override  
      public void onPermissionDenied() {  
            //add code to be performed when permission is denied.
      }  
    },int requestCode, int resultCode, Intent data);

Example
  

    @Override  
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
        super.onActivityResult(requestCode, resultCode, data);  
      screenColorPicker.checkPermission(new ScreenColorPicker.PermissionCallbacks() {  
            @Override  
      public void onPermissionGranted() {  
                Log.d("Permission","Granted");  
      }  
      
            @Override  
      public void onPermissionDenied() {  
                finish();  
      }  
        },requestCode, resultCode, data);  
    }

Now it's time to start capturing data. Since we already made sure that the permission is granted, we now need to start the MediaProjection Service. For this call `start()` 
***Note : Call start() only when you have made sure the capturing permission is granted, or else it will produce a NullPointerException***

Example

    scrObj.start();

Similarly to stop the service just call, `stop()`
Example

    scrObj.stop();
***Note : You don't need to re - initialise the object.***

**Getting Color Data**
The library comes with two standard color callbacks.
***INT Color Code***
Usage

    int color = scrObj.getColorInt(int x, int y);
Returns integer color code of the pixel at provided co ordinates.

***Hex Color Code***
Usage

    String color = scrObj.getColorHex(int x, int y);
Returns the hex color code of the pixel at provided co ordinates.

Apart from this, since the service keeps reading bitmaps off screen, you can also get the latest bitmap by using `getLatestBitmap()` to get the latest loaded bitmap (screen shot if you say) 
Usage

    Bitmap wholeScreenInAVariable = scrObj.getLatestBitmap();

## Some Points to be kept in Note
**Always make sure that the co ordinates you entered for color reading, don't exceed the screen size, or else an exception will be thrown by android system.e**

## Examples
Check inside the app directory of this repo to find example code. Also make sure you explicitly grant storage permissions to the app, if you try the code out.

## Refrences
This library is a direct touch up on [Manos Tsahakis
](https://github.com/mtsahakis)' MediaProjection Demo, which can be found [here](https://github.com/mtsahakis/MediaProjectionDemo/blob/master/src/com/mtsahakis/mediaprojectiondemo/ScreenCaptureImageActivity.java).

**All kudos to him!**

## Need some help?
Still having any difficulty?
You can pm me at my telegram **@MyNameIsRage**
I would be happy to help.

K thenks Bye now.
