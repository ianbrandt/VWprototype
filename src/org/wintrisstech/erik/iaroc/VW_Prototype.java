package org.wintrisstech.erik.iaroc;

import android.os.SystemClock;
import ioio.lib.api.exception.ConnectionLostException;
import org.wintrisstech.vw.VicswagenAdapter;
import org.wintrisstech.vw.VicswagenInterface;

/**
 * A VW_Prototype is an implementation of the VicwagenInterface. 
 * It is entirely event driven.
 *
 * @author Erik
 */
public class VW_Prototype extends VicswagenAdapter
{

    private static final String TAG = "VW_Prototype";
    private final Dashboard dashboard;

    private enum State
    {

        FORWARD,
        BACKWARD,
        TURNING_RIGHT,
        TURNING_LEFT,
        STOPPED;
    }
    private int amount = 0; //always >= 0
    private State state;

    /**
     * Constructs a VW_Prototype, an amazing machine!
     *
     * @param vicswagen an implementation of the VicswagenInterface
     * @param dashboard the Dashboard instance that is connected to the
     * VW_Prototype
     * @throws ConnectionLostException
     */
    public VW_Prototype(VicswagenInterface vicswagen, Dashboard dashboard) // throws ConnectionLostException
    {
        super(vicswagen);
        this.dashboard = dashboard;
    }

    public void initialize() throws ConnectionLostException
    {
        dashboard.log("===========Start===========");
        state = State.FORWARD;
        amount = 300;
        dashboard.log("Driving forward: " + amount);
        driveDirect(50, 50);
        dashboard.log("... driving");
    }

    /**
     * This method is called repeatedly
     *
     * @throws ConnectionLostException
     */
    public void loop() throws ConnectionLostException
    {

        try
        {
            SystemClock.sleep(100);
            dashboard.log("State: " + state.toString());
            switch (state)
            {
                case FORWARD:
                    if (getDistance() >= amount)
                    {
                        state = State.BACKWARD;
                        amount = 300;
                        driveDirect(-10, -10);
                    }
                    break;
                case BACKWARD:
                    if (-getDistance() >= amount)
                    {
                        state = State.TURNING_RIGHT;
                        amount = 90;
                        driveDirect(10, -10);
                    }
                    break;
                case TURNING_LEFT:
                    if (getAngle() >= amount)
                    {
                        state = State.TURNING_LEFT;
                        amount = 180;
                        driveDirect(-10, 10);
                    }
                    break;
                case TURNING_RIGHT:
                    if (-getAngle() >= amount)
                    {
                        state = State.STOPPED;
                        amount = 0;
                        driveDirect(0, 0);
                    }
                    break;
                case STOPPED:
                    break;
            }
            dashboard.log("L = " + getLeftUltrasonicDistance() + " R = " + getRightUltrasonicDistance());
        } catch (InterruptedException ex)
        {
        }
    }

    private int getDistance()
    {
        return (getLeftWheelDistance() + getRightWheelDistance()) / 2;
    }
}
