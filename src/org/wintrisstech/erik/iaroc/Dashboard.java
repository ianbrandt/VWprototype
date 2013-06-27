package org.wintrisstech.erik.iaroc;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.widget.ScrollView;
import android.widget.TextView;
import ioio.lib.api.DigitalInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.PulseInput;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

import java.io.IOException;
import java.net.URL;

/**
 * This is the main activity of the iRobot2012 application.
 *
 * <p>
 * This class assumes that there are 4 ultrasonic sensors attached to the robot.
 * An instance of the Dashboard class will display the readings of these 4
 * sensors.
 *
 * <p>
 * There should be no need to modify this class. Modify VW_Prototype instead.
 *
 * @author Erik Colban
 *
 */
public class Dashboard extends IOIOActivity
{

    private TextView mText;
    private ScrollView scroller;
    private int frontDistance;
    private int rearDistance;
    private int leftDistance;
    private int rightDistance;
    private PwmOutput rightMotorClock;
    private PwmOutput leftMotorClock;
    private int pulseWidth = 10;//microseconds
    private PulseInput front;
    private PulseInput rear;
    private PulseInput left;
    private PulseInput right;
    private DigitalOutput rightMotorClockPulse;
    private DigitalOutput leftMotorClockPulse;
    private DigitalOutput frontStrobe;
    private DigitalOutput rearStrobe;
    private DigitalOutput leftStrobe;
    private DigitalOutput rightStrobe;
    private DigitalOutput rightMotorDirection;
    private DigitalOutput leftMotorDirection;
    private DigitalOutput halfFull;
    private DigitalOutput motorEnable; // Must be true for motors to run.
    private DigitalOutput reset; // Must be true for motors to run.
    private DigitalOutput control;//Decay mode selector high = slow, low = fast.
    private DigitalOutput motorControllerControl;// Decay mode selector, high = slow decay, low = fast decay
    private static final int MOTOR_ENABLE_PIN = 3;//Low turns off all power to motors***
    private static final int MOTOR_RIGHT_DIRECTION_OUTPUT_PIN = 20;//High = clockwise, low = counter-clockwise
    private static final int MOTOR_LEFT_DIRECTION_OUTPUT_PIN = 21;
    private static final int MOTOR_CONTROLLER_CONTROL_PIN = 6;// For both motors //pin 1 for original wagon***
    private static final int REAR_STROBE_ULTRASONIC_OUTPUT_PIN = 14;//pin 15 for original wagon (output from ioio)
    private static final int FRONT_STROBE_ULTRASONIC_OUTPUT_PIN = 16;
    private static final int LEFT_STROBE_ULTRASONIC_OUTPUT_PIN = 17;
    private static final int RIGHT_STROBE_ULTRASONIC_OUTPUT_PIN = 15;
    private static final int FRONT_ULTRASONIC_INPUT_PIN = 12;
    private static final int REAR_ULTRASONIC_INPUT_PIN = 10;//pin 6 for original wagon (input to ioio)//pin 23 on Bill's board...wrong...must be changed
    private static final int RIGHT_ULTRASONIC_INPUT_PIN = 11;
    private static final int LEFT_ULTRASONIC_INPUT_PIN = 13;
    private static final int MOTOR_HALF_FULL_STEP_PIN = 7;//For both motors//pin 8 for original wagon***
    private static final int MOTOR_RESET = 22;//For both motors
    private static final int MOTOR_CLOCK_LEFT_PIN = 27;
    private static final int MOTOR_CLOCK_RIGHT_PIN = 28;
    private int leftMotorPWMfrequency = 100;
    private int rightMotorPWMfrequency = 100;
    URL soundFileAddress;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        /*
         * Since the android device is carried by the iRobot Create, we want to
         * prevent a change of orientation, which would cause the activity to
         * pause.
         */
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.main);

        mText = (TextView) findViewById(R.id.text);
        scroller = (ScrollView) findViewById(R.id.scroller);
        log(getString(R.string.wait_ioio));
    }

    @Override
    public void onPause()
    {
        log("Pausing");
        log("=================> VicsWagon version 9.90");
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    public void onInit(int arg0)
    {
        log("in init");
    }

    @Override
    public IOIOLooper createIOIOLooper()
    {
        return new IOIOLooper()
        {
            public void setup(IOIO ioio) throws ConnectionLostException, InterruptedException
            {
                log("in setup");
                /*
                 * When the setup() method is called the IOIO is connected.
                 */
                log(getString(R.string.ioio_connected));

                /*
                 * Establish communication between the android and the iRobot
                 * Create through the IOIO board.
                 */
                log(getString(R.string.wait_create));
                log(getString(R.string.create_connected));

//                reset = ioio.openDigitalOutput(MOTOR_RESET);//both motors
//                reset.write(false);
//                reset.write(true);
//
//                motorControllerControl = ioio.openDigitalOutput(MOTOR_CONTROLLER_CONTROL_PIN);
//                motorControllerControl.write(true);//Slow decay
//
//                halfFull = ioio.openDigitalOutput(MOTOR_HALF_FULL_STEP_PIN);//both motors
//                halfFull.write(true);//True = half step
//
//                rightMotorDirection = ioio.openDigitalOutput(MOTOR_RIGHT_DIRECTION_OUTPUT_PIN);
//                rightMotorDirection.write(true);
//
//                leftMotorDirection = ioio.openDigitalOutput(MOTOR_LEFT_DIRECTION_OUTPUT_PIN);
//                leftMotorDirection.write(false);
//
//                motorEnable = ioio.openDigitalOutput(MOTOR_ENABLE_PIN);//both motors
//                motorEnable.write(true);

//                    rearStrobe = ioio.openDigitalOutput(REAR_STROBE_ULTRASONIC_OUTPUT_PIN, false);
//                    rear = ioio.openPulseInput(new DigitalInput.Spec(REAR_ULTRASONIC_INPUT_PIN), PulseInput.ClockRate.RATE_62KHz,PulseInput.PulseMode.POSITIVE, false);
////                    
//                    frontStrobe = ioio.openDigitalOutput(FRONT_STROBE_ULTRASONIC_OUTPUT_PIN, false);
//                    front = ioio.openPulseInput(new DigitalInput.Spec(FRONT_ULTRASONIC_INPUT_PIN), PulseInput.ClockRate.RATE_62KHz, PulseInput.PulseMode.POSITIVE, false);
//////
//                    leftStrobe = ioio.openDigitalOutput(LEFT_STROBE_ULTRASONIC_OUTPUT_PIN, false);
//                    left = ioio.openPulseInput(new DigitalInput.Spec(LEFT_ULTRASONIC_INPUT_PIN), PulseInput.ClockRate.RATE_62KHz, PulseInput.PulseMode.POSITIVE, true);
//
//                    rightStrobe = ioio.openDigitalOutput(RIGHT_STROBE_ULTRASONIC_OUTPUT_PIN, false);
//                    right = ioio.openPulseInput(new DigitalInput.Spec(RIGHT_ULTRASONIC_INPUT_PIN), PulseInput.ClockRate.RATE_62KHz, PulseInput.PulseMode.POSITIVE, true);

//                rightMotorClock = ioio.openPwmOutput(MOTOR_CLOCK_RIGHT_PIN, rightMotorPWMfrequency);//pin #, frequency right motor// pin 10 on original wagon...right
//                leftMotorClock = ioio.openPwmOutput(MOTOR_CLOCK_LEFT_PIN, leftMotorPWMfrequency);//pin #, frequency right motor// pin 10 on original wagon...right
//                rightMotorClock.setPulseWidth(pulseWidth);
//                leftMotorClock.setPulseWidth(pulseWidth);
//             MediaPlayer mp_file = MediaPlayer.create(this, R.raw.your_song_file);
//             mp_file.start();

            }

            public void loop() throws ConnectionLostException, InterruptedException
            {

                log("in loop");
                SystemClock.sleep(1000);
                
                MediaPlayer mediaPlayer = null;

                try
                {
                    mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.hotrod);
                    log("playing sound");
                    mediaPlayer.start();
                    log("sleeping for 10s");
                    SystemClock.sleep(10000);
                    log("sleep finished");
                }
                catch (IllegalStateException e)
                {
                    log(e.getMessage());
                }
                finally
                {
                    log("in finally");
                    if (mediaPlayer != null)
                    {
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }
                }

            }

            public void disconnected()
            {
                log(getString(R.string.ioio_disconnected));
            }

            public void incompatible()
            {
            }
        };
    }

    /**
     * Writes a message to the Dashboard instance.
     *
     * @param msg the message to write
     */
    public void log(final String msg)
    {
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                mText.append(msg);
                mText.append("\n");
                scroller.smoothScrollTo(0, mText.getBottom());
            }
        });
    }
}