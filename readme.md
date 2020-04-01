# Test LibVLC Application

Note: This application is not intended for general use.  
So please do not take it as an example of how to use LibVLC with android.  

## Purpose of Application
This is the bare minium example to demonstrate an issue with the LibVLC library, and multicast streaming. The issue this aims to show is that there is a memory leak occurring within LibVLC, after changing the media source to be played back multiple times.

## Issue That is Being Demonstrated

The problem that this application is demonstrating, is that of a suspected memory leak in the LibVLC library for android. This library, which the .aar file for has been compiled from the latest state of the master branch in the "VLC-Android" repository on code.videolan.org

The issue that is occuring, is that after a number of media source changes of multicast network streams, LibVLC reaches a point where it just straight up breaks. The native memory usage of the app climbs over time, until the app eventually dies. When it does, if the app is restarted, LibVLC will sit there, not playing any network streams until the device the app is on is restarted. If you look at the memory usage of the app when it is in this state, in the memory profiler in Android Studio, you can see the native memory usage just going up and up and up, when nothing is happening.  

However, this issue does not just affect the one app, to require the restart. Any other copy of LibVLC on the device, such as the full version this app is mimicking some of the behavior of, and even the full VLC-Android application, is unable to play a network stream until the device is restarted. This is quite a severe issue, as it appears the scope of any problems are leaking outside of just merely the one app with the issue, it affects the device to a much greater extent.

# Usage of the Application

## Initial Setup

This application is made to run on Android TVs, running either Android 8 or 9.  
When preparing to use this application, the first thing you need is two multicast streams going out on the network that the TV is on, which are to be cycled between as the two media sources.

You then need to enter these IP addresses into the two fields in the code, as shown below:  
```  
  // |---------------------------|
  // | Configure stream IPs here |
  // |---------------------------|

  ArrayList<String> streamAddresses = new ArrayList<String>(){{
    add("udp://@238.1.1.1:1234");
    add("udp://@238.1.1.2:1234");
  }};
```

Keep to the format as shown here, using the '@' symbol, as otherwise VLC will not pick these up as valid stream sources.  

You then have the choice of whether you will use an external box to send in IR signals to the TV, to change the channel up and down, or to use the inbuilt timer in the app, to automatically start cycling between the streams every 10 seconds.  

If you have something like an AMX box which can be used for the first option, set it up to send the "Channel up" pulse 2x times, wait for ten seconds, "Channel down" pulse 2x times, wait ten seconds, and repeat.  

For those of you who would just rather use the automatic timer and avoid that hassle (I only have the channel up/down button option as a way to prove that part is not the issue), merely uncomment these two lines: 

```
    // |------------------------------------|
    // | Optional automatic stream changing |
    // |------------------------------------|

    //  runAutomaticTimer = true;
    //  runTimedStreamChange();
```
You can find these two lines at the bottom of the "createLibVLC" Method.

## Optional VLC Parameters

If you want to pass additional parameters to LibVLC, simply add them to the section in the code, that matches the following: 
```
    // |-----------------------------|
    // | Additional LibVLC Arguments |
    // |-----------------------------|

    addArg("fullscreen", "--fullscreen");
    addArg("verbose", "-vvv");
```

This can be found in the "onCreate" Method of the application.

## Usage

Once this has been done, you can then compile the application (Ideally as a debug apk), and install it onto the Android TV of your choosing. Once this has happened and the app has been started, it will immediately start looking to play one of the two streams, and will continue to do so when the source it is looking at is changed. You can see the stream that it is attempting to playback in the text box at the top of the application.

The amount of time for the issue to arise varies across devices (Presumably depending on how long it takes for memory to start being filled up), but for me it has always just reached a point where it will suddenly stop playing and just sit there frozen, potentially crashing fully.  
Any attempts to then close the app, and reopen it to continue playing will fail, and the entire device will need to be restarted for it to begin working again.  
Also at this time, if you try to open up the VLC-Android application on the TV, and to playback one of the network streams you have been playing in this app, you should find it will also fail to do so, until the device is restarted.

Another note: If you look in the logs of the application when the app is restarted after crashing, and if the verbose output is enabled for LibVLC, you should see that it is indeed still buffering in the stream from the network, it just never appears to do anything with it after it reaches 100% buffered.