package com.LRTNZ.testvlcapplication;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Timer;
import java.util.TimerTask;
import org.jetbrains.annotations.NotNull;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.interfaces.IVLCVout;
import timber.log.Timber;

/**
 * Main Activity of the application
 */
public class App extends Activity implements IVLCVout.Callback{


  /**
   * {@link LibVLC} instance variable, to be used as required
   */
  LibVLC libVLC = null;

  /**
   * {@link org.videolan.libvlc.MediaPlayer} instance, used to play the media in the app
   */
  org.videolan.libvlc.MediaPlayer mediaPlayer = null;

  /**
   * {@link Media} source instance, provides the source for the {@link #mediaPlayer} instance
   */
  static Media mediaSource;


  /**
   * {@link IVLCVout} instance to be used in the app
   */
  IVLCVout vlcOut;

  // Surfaces for the stream to be displayed on
  SurfaceHolder vidHolder;
  SurfaceView vidSurface;


  /**
   * {@link Integer} value of which of the two streams is being played
   */
  // Actual first stream to be played is the inverse of this, just the quick logic setup I put together needs it this way
  int currentStreamIndex = 1;


  /**
   * {@link String} value of the network address of the current streaming source to be played back
   */
  String currentStreamAddress = "";

  // Text box at the top of the screen, that will have the current stream name/index being played in it, to make it easy to see what is happening in the application

  /**
   * {@link EditText} that is the box at the top of the screen showing the details about the current stream being played
   */
  EditText streamName;


  /**
   * {@link ArrayList}<{@link String}> of the two IP addresses of the multicast streams that are to be cycled through.
   * These are where you load in the addressed of the two multicast streams you are creating on your own network, to run this application.
   */

  // |---------------------------|
  // | Configure stream IPs here |
  // |---------------------------|

  ArrayList<String> streamAddresses = new ArrayList<String>(){{
    add("udp://@239.2.2.2:1234");
    add("udp://@239.1.1.1:1234");
  }};

  ArrayList<String> videoFiles = new ArrayList<String>(){{
    add("resort_flyover.mp4");
    add("waves_crashing.mp4");
  }};


  static boolean streamOrFile = true;

  static int numPlaybacks = 0;

  @Override
  protected void onCreate(Bundle savedInstance){

    // Run the super stuff for this method
    super.onCreate(savedInstance);

    // Creates the timber debug output, and sets the tag for the log messages
    if (BuildConfig.DEBUG) {
      Timber.plant(new Timber.DebugTree() {
        @Override
        protected void log(int priority, String tag, @NotNull String message, Throwable t) {
          super.log(priority, "Test-VLC", message, t);
        }
      });
      Timber.d("In debug mode");
    }

    // Sets the main view
    setContentView(R.layout.main);

    // Populates and loads the two values for the video layout stuff
    vidSurface = findViewById(R.id.video_layout);
    vidSurface.setVisibility(View.VISIBLE);
    vidHolder = vidSurface.getHolder();

    // Adds arguments to the list of args to pass when initialising the lib VLC instance.
    // If you need to add in more arguments to the vlc instance, just follow the format below

    // |-----------------------------|
    // | Additional LibVLC Arguments |
    // |-----------------------------|

    addArg("fullscreen", "--fullscreen");
    addArg("verbose", "-vvv");

  //  addArg("deinterlace", "--deinterlace=1");
    //addArg("mode","--deinterlace-mode=yadif");
   // addArg("filter","--video-filter=deinterlace");

    // Load the editText variable with a reference to what it needs to fill in the layout
    streamName = findViewById(R.id.stream_ID);

    // Run the libVLC creation/init method
    createLibVLC();
  }

  /**
   * Method that handles the creation of the {@link LibVLC} instance
   */
  public void createLibVLC() {

    // Get the list of arguments from the provided arguments above
    ArrayList<String> args = new ArrayList<>(arguments.values());

    // Debug: Print out the passed in arguments
    Timber.d("Arguments for VLC: %s", args);

    // Create the LibVLC instance, with the provided arguments
    libVLC = new LibVLC(this, args);

    // Create the new media player instance to be used
    mediaPlayer = new org.videolan.libvlc.MediaPlayer(libVLC);

    // Get the details of the display
    DisplayMetrics displayMetrics = new DisplayMetrics();

    // Load displayMetrics with the details of the default display of the device
    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

    // Set the size of the mediaplayer to match the resolution of the device's screen
    mediaPlayer.getVLCVout().setWindowSize(displayMetrics.widthPixels, displayMetrics.heightPixels);

    // Load vlcOut with the value from the created media player
    vlcOut = mediaPlayer.getVLCVout();

    // Passes the event listener for the media player to use to be this runnable/lambda
    mediaPlayer.setEventListener(event -> {

      // Standard switch between all the different events thrown by the mediaplayer
      switch (event.type) {
        case MediaPlayer.Event.Buffering:
         // Timber.d("onEvent: Buffering");
          break;
        case MediaPlayer.Event.EncounteredError:
          Timber.d("onEvent: EncounteredError");
          break;
        case MediaPlayer.Event.EndReached:
          //Timber.d("onEvent: EndReached");
          break;
        case MediaPlayer.Event.ESAdded:
         // Timber.d("onEvent: ESAdded");
          break;
        case MediaPlayer.Event.ESDeleted:
         // Timber.d("onEvent: ESDeleted");
          break;
        case MediaPlayer.Event.MediaChanged:
          Timber.d("onEvent: MediaChanged");
          //mediaPlayer.setVolume(0);
          break;
        case MediaPlayer.Event.Opening:
          Timber.d("onEvent: Opening");
          break;
        case MediaPlayer.Event.PausableChanged:
         // Timber.d("onEvent: PausableChanged");
          break;
        case MediaPlayer.Event.Paused:
        //  Timber.d("onEvent: Paused");
          break;
        case MediaPlayer.Event.Playing:
          Timber.d("onEvent: Playing");
          break;
        case MediaPlayer.Event.PositionChanged:
          //  Timber.d("onEvent: PositionChanged");
          break;
        case MediaPlayer.Event.SeekableChanged:
         // Timber.d("onEvent: SeekableChanged");
          break;
        case MediaPlayer.Event.Stopped:
          Timber.d("onEvent: Stopped");
          break;
        case MediaPlayer.Event.TimeChanged:
          //  Timber.d("onEvent: TimeChanged");
          break;
        case MediaPlayer.Event.Vout:
         // Timber.d("onEvent: Vout");
          break;
      }
    });

    // Call the change stream, to preload the first stream at startup, instead of waiting for an input
    changeStream();

    // If you do not have the means to automatically generate an alternative two pulse up/two pulse down signal input for the Android TV,
    // these two lines can be uncommented in order to enable the automatic up/down changing.
    // The reason there are the two input options, is to prove it is not the source of the call to changing the stream that is causing the issues with the crashing.

    // |------------------------------------|
    // | Optional automatic stream changing |
    // |------------------------------------|

     runAutomaticTimer = true;
     runTimedStreamChange();
  }


  /**
   * {@link LinkedHashMap}<{@link String}, {@link String}> of the arguments that are to be passed to the LibVLC instance
   */
  static LinkedHashMap<String, String> arguments = new LinkedHashMap<>();

  /**
   * Method that takes a k/v pair and adds it to the map of arguments to be used when creating the LibVLC instance.
   * The key is used in the full application, as the potential to remove existing arguments is present there.
   *
   * @param argName {@link String} value of the name to use as the key for the argument
   * @param argValue {@link String} value of the argument that will be recognised when passed to LibVLC
   */
  public void addArg(String argName, String argValue) {

    // If the argument with the key already exists, just update the existing one to the new value
    if (arguments.containsKey(argName)) {
      arguments.replace(argName, argValue);
    } else {
      // Otherwise if the argument does not exist, add it as a new one to the list
      arguments.put(argName, argValue);
    }
  }


  @Override
  public void onNewIntent(Intent intent) {

    // Standard android stuff
    super.onNewIntent(intent);
    Timber.d("Player ran new intent");

    setIntent(intent);
  }

  @Override
  public void onStart() {

    // Run super stuff
    super.onStart();

    // Set the output view to use for the video to be the surface
    vlcOut.setVideoView(vidSurface);

    // Add the callback for the vlcOut to be this class
    mediaPlayer.getVLCVout().addCallback(this);
    // Attach the video views passed to the output
    vlcOut.attachViews();

  }

  @Override
  public void onResume() {
    super.onResume();
    Timber.d("App ran resume");
  }

  @Override
  public void onPause() {
    super.onPause();
    Timber.d("App ran paused");
  }

  @Override
  public void onStop() {
    super.onStop();

    // Release the various VLC things when the activity is stopped
    mediaPlayer.stop();
    runAutomaticTimer = false;
    mediaPlayer.getVLCVout().detachViews();
    mediaPlayer.getVLCVout().removeCallback(this);

    Timber.d("Player ran stop");
  }


  /**
   * {@link Boolean} value that stores whether or not the automatic timer should cancel, once it has been set going
   */
  volatile boolean runAutomaticTimer = false;

  /**
   * Method that can be called to start a timer to automatically change the stream every 10 seconds from inside the application.
   */
  void runTimedStreamChange(){

    Timer timer = new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        if(!runAutomaticTimer){
          this.cancel();
        }
        runOnUiThread(() -> changeStream());
      }
    }, 5000, 10000);
  }


  /**
   * Method that is called to change the multicast stream VLC is currently playing
   */
  void changeStream(){

    // If the current stream being played is the first
    if(currentStreamIndex == 0){

      currentStreamIndex = 1;

      if(streamOrFile){
        Timber.d("Selected Stream: 1");
        currentStreamAddress = streamAddresses.get(1);

      } else {
        Timber.d("Selected Video: 1 - %s", videoFiles.get(1));
        currentStreamAddress = videoFiles.get(1);
      }

      // Perform the inverse if the second stream is currently playing
    } else {

      currentStreamIndex = 0;

      if(streamOrFile){
        Timber.d("Selected Stream: 0");
        currentStreamAddress = streamAddresses.get(0);

      } else {
        Timber.d("Selected Video: 0 - %s", videoFiles.get(0));
        currentStreamAddress = videoFiles.get(0);
      }
    }

    // Load the values of the current stream and index into the textbox at the top of the screen, to make it easier to see what is happening
    streamName.setText(String.format("Stream: %s/%s", currentStreamIndex,currentStreamAddress));

    // If the current media source is not null, as it would be at start up, release it.
    if (mediaSource != null) {
      mediaSource.release();
    }

    if(streamOrFile){
      mediaSource = new Media(this.libVLC, Uri.parse(this.currentStreamAddress));
    } else {
      try {
        mediaSource = new Media(this.libVLC, getAssets().openFd(this.currentStreamAddress));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    //mediaSource.setHWDecoderEnabled(true, true);

    // Finish up the process of loading the stream into the player
    finishPlayer();
  }

  /**
   * Method that is called to load in a new mediasource and to set it playing out the output, from VLC
   */
  void finishPlayer(){

    if(mediaPlayer.isPlaying()){
      mediaPlayer.stop();
    }

    // Add the option to be in fullscreen to the new mediasource
    mediaSource.addOption(":fullscreen");

   // mediaPlayer.
    // Set the player to use the provided media source
    mediaPlayer.setMedia(mediaSource);

    // Release the media source
    mediaSource.release();

    // Start the media player
    mediaPlayer.play();

    Timber.d("Number of playbacks: %s", numPlaybacks);
    numPlaybacks ++;
  }

  // Required handler things for the vlcOut interface

  @Override
  public void onSurfacesCreated(IVLCVout ivlcVout) {

  }

  @Override
  public void onSurfacesDestroyed(IVLCVout ivlcVout) {

  }



  /**
   * {@link Boolean} value that stores whether button inputs are to be observed or not at the current time by the app.
   */
  volatile boolean buttonLockout = false;

  /**
   * Enum that represents the direction the channel change button on the remote was pressed
   */
  private enum directionPressedEnum{
    STREAM_UP,
    STREAM_DOWN
  }

  boolean pressedOnce = false;
  boolean secondPress = false;
  /**
   * Stores the curremt {@link directionPressedEnum} of what button direction was last pushed
   */
  static directionPressedEnum directionPressed = null;

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    // Debug: Log that a button that can be read in by the program has been pressed
    Timber.v("Remote button was pressed");

    // If the app is to observe the button presses or not. Required due to the fact the TV this is being tested on (Sony Bravia) likes to sometimes read in extraneous button presses that are non existent.
    if(!buttonLockout){

      // If the button pressed was the channel up
      if(keyCode == KeyEvent.KEYCODE_CHANNEL_UP){

        // Debug
        Timber.d("Channel up pressed");

        // If the direction that was last pressed was down/app is starting
        if(directionPressed == directionPressedEnum.STREAM_DOWN || directionPressed == null){


          Timber.d("First up press");

          // Set the direction that has been pressed, and that the channel button has been pressed once
          directionPressed = directionPressedEnum.STREAM_UP;
          pressedOnce = true;

          // Otherwise if the last press was already in this direction
        } else if(directionPressed == directionPressedEnum.STREAM_UP && pressedOnce){
          Timber.d("Second up press");

          // Button being pressed for a second time, so lock out taking in any more inputs
          secondPress = true;
          buttonLockout = true;
        }

        // Same as above, just for the channel down key on the remote
      } else if(keyCode == KeyEvent.KEYCODE_CHANNEL_DOWN){

        Timber.d("Channel down pressed");
        if(directionPressed == directionPressedEnum.STREAM_UP || directionPressed == null){

          Timber.d("First down press");
          directionPressed = directionPressedEnum.STREAM_DOWN;
          pressedOnce = true;
        } else if(directionPressed == directionPressedEnum.STREAM_DOWN && pressedOnce){
          Timber.d("Second down press");
          secondPress = true;
          buttonLockout = true;
        }

        // Catches other button presses that the program has received
      } else {
        Timber.d("Other button press");
        //return super.onKeyDown(keyCode, event);
      }

      // If the button has been pressed for a second time, and the button input has been locked out
      if(secondPress && buttonLockout){
        Timber.d("Change stream called");

        // Reset variables
        pressedOnce = false;
        secondPress = false;

        // Call the stream change
        changeStream();

        // Call the handler to reset the lockout after a timeout
        handleButtonLockout();
      }

      // Return true to stop the OS pulling you out of this app on any button press
      return true;
    }

    // return true so any buttons read in that the program doesn't handle, doesn't close the program
    return true;
  }

  /**
   * Handler to reset the {@link #buttonLockout} value after a timeout period
   */
  void handleButtonLockout(){

    // Create new Handler
    Handler handler = new Handler();
    // Run this runnable after a second
    handler.postDelayed(() -> buttonLockout = false, 1000);

  }



}