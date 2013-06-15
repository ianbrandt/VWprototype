package org.wintrisstech.erik.iaroc;

import android.os.SystemClock;
import ioio.lib.api.exception.ConnectionLostException;
import org.wintrisstech.vw.VicswagenAdapter;
import org.wintrisstech.vw.VicswagenInterface;

/**
 * A VW_Prototype is an implementation of the VicwagenInterface. It is entirely
 * event driven.
 * 
 * @author Erik
 */
public class VW_Prototype extends VicswagenAdapter {
	private static final String TAG = "VW_Prototype";
	/**
	 * To calibrate, program the robot to go straight for 1 meter (= 1000 mm).
	 * If d is the distance in mm it actually goes, multiply this value by 1000 / d.
	 */
	private static final float VELOCITY_FACTOR = 0.545F; 
	private static final float ANGULAR_VELOCITY_FACTOR = 0.00312F; 
	private final Dashboard dashboard;

	private enum State {

		FORWARD, BACKWARD, TURNING_RIGHT, TURNING_LEFT, STOPPED;
	}

	private int amount = 0; // always >= 0
	private State state;

	/**
	 * Constructs a VW_Prototype, an amazing machine!
	 * 
	 * @param vicswagen
	 *            an implementation of the VicswagenInterface
	 * @param dashboard
	 *            the Dashboard instance that is connected to the VW_Prototype
	 * @throws ConnectionLostException
	 */
	public VW_Prototype(VicswagenInterface vicswagen, Dashboard dashboard) // throws
																			// ConnectionLostException
	{
		super(vicswagen);
		this.dashboard = dashboard;
	}

	public void initialize() throws ConnectionLostException {
		this.calibrateVelocity(VELOCITY_FACTOR);
		this.calibrateAngularVelocity(ANGULAR_VELOCITY_FACTOR);
		dashboard.log("===========Start===========");
		state = State.TURNING_LEFT;
		amount = 720;
		dashboard.log("Turning left: " + amount);
		driveDirect(-100, 100);
	}

	/**
	 * This method is called repeatedly
	 * 
	 * @throws ConnectionLostException
	 */
	public void loop() throws ConnectionLostException {

		try {
			SystemClock.sleep(100);
			dashboard.log("State: " + state.toString());
			int distance = getDistance();
			int angle = getAngle();
			switch (state) {
			case FORWARD: {
				dashboard.log("distance = " + distance);
				if (distance >= amount) {
					state = State.STOPPED;
					amount = 0;
					driveDirect(0, 0);
				}
				break;
			}
			case BACKWARD: {
				dashboard.log("distance = " + distance);
				if (-distance >= amount) {
					state = State.STOPPED;
					amount = 0;
					driveDirect(0, 0);
				}
				break;
			}
			case TURNING_LEFT: {
				dashboard.log("angle = " + angle);
				if (angle >= amount) {
					state = State.STOPPED;
					amount = 0;
					driveDirect(0, 0);
				}
				break;
			}
			case TURNING_RIGHT: {
				dashboard.log("angle = " + angle);
				if (-angle >= amount) {
					state = State.STOPPED;
					amount = 0;
					driveDirect(0, 0);
				}
				break;
			}
			case STOPPED:
				break;
			}
			dashboard.log("L = " + getLeftUltrasonicDistance() + " R = "
					+ getRightUltrasonicDistance());
		} catch (InterruptedException ex) {
		}
	}

	private int getDistance() {
		return (getLeftWheelDistance() + getRightWheelDistance()) / 2;
	}
}
