package org.wintrisstech.erik.iaroc;

import org.wintrisstech.vw.VicswagenInterface;

import android.os.SystemClock;
import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.PulseInput;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;

/**
 * A final class that implements the VicswagenInterface.
 *
 */
public final class SimpleVicswagenDebug implements VicswagenInterface
{


    /* The following static variable require calibration */
    private static final float WHEEL_RADIUS = 60; // in mm
    private static final float WHEEL_DISTANCE = 400; //in mm
    private static final int VELOCITY_TO_FREQUENCY =
            (int) (400F / Math.PI * WHEEL_RADIUS);
    private float PULSE_LENGTH_TO_DISTANCE = 18000F;
    /* Instance variables */
    private final IOIO ioio;
    private final Dashboard dashboard;
    private int frontDistance;
    private PwmOutput leftPwmOutput;
    private PwmOutput rightPwmOutput;
    private boolean leftPwmOutputOpen = false;
    private boolean rightPwmOutputOpen = false;
    private PulseInput frontDistanceInput;
    private PulseInput rearDistanceInput;
    private PulseInput leftDistanceInput;
    private PulseInput rightDistanceInput;
    private DigitalOutput frontStrobe;
    private DigitalOutput rearStrobe;
    private DigitalOutput leftStrobe;
    private DigitalOutput rightStrobe;
    private DigitalOutput rightDirection;
    private DigitalOutput leftDirection;
    private AnalogInput inX;
    private AnalogInput inY;
    private AnalogInput inZ;
    private long startTime;
    private float leftVelocity;
    private float rightVelocity;

    public SimpleVicswagenDebug(IOIO ioio, Dashboard dashboard) throws ConnectionLostException
    {
        this.ioio = ioio;
        this.dashboard = dashboard;
        this.initialize();
    }

    private void initialize() throws ConnectionLostException
    {
    	dashboard.log("In initialize()");
        rearStrobe = ioio.openDigitalOutput(REAR_STROBE_ULTRASONIC_OUTPUT_PIN);
        rearStrobe.write(false);
        frontStrobe = ioio.openDigitalOutput(FRONT_STROBE_ULTRASONIC_OUTPUT_PIN);
        frontStrobe.write(false);
        leftStrobe = ioio.openDigitalOutput(LEFT_STROBE_ULTRASONIC_OUTPUT_PIN);
        leftStrobe.write(false);
        rightStrobe = ioio.openDigitalOutput(RIGHT_STROBE_ULTRASONIC_OUTPUT_PIN);
        rightStrobe.write(false);

        frontDistanceInput = ioio.openPulseInput(FRONT_ULTRASONIC_INPUT_PIN, PulseInput.PulseMode.POSITIVE);
        leftDistanceInput = ioio.openPulseInput(LEFT_ULTRASONIC_INPUT_PIN, PulseInput.PulseMode.POSITIVE);
        rightDistanceInput = ioio.openPulseInput(RIGHT_ULTRASONIC_INPUT_PIN, PulseInput.PulseMode.POSITIVE);
        //Single precision for the rear sensor beacuse there is maximum 3 double precision modules.
        rearDistanceInput = ioio.openPulseInput(
                new DigitalInput.Spec(REAR_ULTRASONIC_INPUT_PIN),
                PulseInput.ClockRate.RATE_2MHz,
                PulseInput.PulseMode.POSITIVE, false);

        inX = ioio.openAnalogInput(X_AXIS_ANALOG_INPUT_PIN);
        inY = ioio.openAnalogInput(Y_AXIS_ANALOG_INPUT_PIN);
        inZ = ioio.openAnalogInput(Z_AXIS_ANALOG_INPUT_PIN);

        this.rightDirection = ioio.openDigitalOutput(RIGHT_DIRECTION_OUTPUT_PIN);
        this.leftDirection = ioio.openDigitalOutput(LEFT_DIRECTION_OUTPUT_PIN);
    }

    public void driveDirect(int leftVelocity, int rightVelocity) throws ConnectionLostException
    {
        leftDirection.write(leftVelocity > 0);
        rightDirection.write(rightVelocity > 0);
        int leftFrequency = Math.abs(leftVelocity) * VELOCITY_TO_FREQUENCY;
        int rightFrequency = Math.abs(rightVelocity) * VELOCITY_TO_FREQUENCY;
        if (leftPwmOutputOpen)
        {
            leftPwmOutput.close();
            leftPwmOutputOpen = false;
        }
        if (rightPwmOutputOpen)
        {
            rightPwmOutput.close();
            rightPwmOutputOpen = false;
        }

        leftPwmOutput = ioio.openPwmOutput(LEFT_PWM_OUTPUT_PIN, leftFrequency);
        rightPwmOutput = ioio.openPwmOutput(RIGHT_PWM_OUTPUT_PIN, rightFrequency);
        leftPwmOutput.setDutyCycle(0.1F);
        rightPwmOutput.setDutyCycle(0.1F);
        startTime = SystemClock.elapsedRealtime();
        this.leftVelocity = leftVelocity;
        this.rightVelocity = rightVelocity;

    }

    /**
     * Get the angle since the last driveDirect call. The angle is negative if
     * turning clockwise, positive if turning counter-clockwise, 0 if moving
     * straight.
     *
     * @return the angle in degrees
     */
    public int getAngle()
    {
        if (leftVelocity == rightVelocity)
        {
            return 0;
        }
        float angularVelocity = (rightVelocity - leftVelocity) / WHEEL_DISTANCE;
        float angleRadians = (SystemClock.elapsedRealtime() - startTime) * angularVelocity;
        return (int) (Math.toDegrees(angleRadians));
    }

    public int getLeftWheelDistance()
    {
        return (int) ((SystemClock.elapsedRealtime() - startTime) * leftVelocity);
    }

    public int getRightWheelDistance()
    {
        return (int) ((SystemClock.elapsedRealtime() - startTime) * rightVelocity);
    }

    public int getInfraredByte()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getLeftVelocity()
    {
        return (int) leftVelocity;
    }

    public int getRightVelocity()
    {
        return (int) rightVelocity;
    }

    /**
     * Gets the turn radius. If the robot is turning on the spot, the turn
     * radius is 0. If the robot is going straight, then Integer.MAX_VALUE is
     * returned. Otherwise, the radius is positive if turning counter-clockwise,
     * negative is turning clockwise.
     *
     * @return the turn radius in mm
     */
    public int getTurnRadius()
    {
        if (leftVelocity == rightVelocity)
        {
            return Integer.MAX_VALUE;
        }

        return (int) ((leftVelocity + rightVelocity)
                / (rightVelocity - leftVelocity) * WHEEL_DISTANCE / 2);
    }

    public int getFrontUltrasonicDistance() throws InterruptedException
    {
        try
        {
            frontStrobe.write(true);
            frontStrobe.write(false);
            frontDistance = (int) (frontDistanceInput.waitPulseGetDuration() * 18000);
        } catch (ConnectionLostException ex)
        {
        }
        return frontDistance;
    }

    public int getRearUltrasonicDistance() throws InterruptedException
    {
        int rearDistance = 0;
        try
        {
            rearStrobe.write(true);
            rearStrobe.write(false);
            rearDistance = (int) (rearDistanceInput.waitPulseGetDuration() * 18000);
        } catch (ConnectionLostException ex)
        {
        }
        return rearDistance;
    }

    public int getLeftUltrasonicDistance() throws InterruptedException
    {
        int leftDistance = 0;
        try
        {
            leftStrobe.write(true);
            leftStrobe.write(false);
            leftDistance = (int) (leftDistanceInput.waitPulseGetDuration() * PULSE_LENGTH_TO_DISTANCE);
        } catch (ConnectionLostException ex)
        {
        }
        return leftDistance;
    }

    public int getRightUltrasonicDistance() throws InterruptedException
    {
        int rightDistance = 0;
        try
        {
            rightStrobe.write(true);
            rightStrobe.write(false);
            rightDistance = (int) (rightDistanceInput.waitPulseGetDuration() * PULSE_LENGTH_TO_DISTANCE);
        } catch (ConnectionLostException ex)
        {
        }
        return rightDistance;
    }
}
