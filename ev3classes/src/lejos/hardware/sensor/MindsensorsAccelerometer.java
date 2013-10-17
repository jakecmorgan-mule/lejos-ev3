package lejos.hardware.sensor;

import lejos.hardware.port.I2CPort;
import lejos.hardware.port.Port;
import lejos.robotics.SampleProvider;
import lejos.utility.EndianTools;

/**
 * This class works with the Mindsensors acceleration (tilt) sensor ACCL-Nx-v2/v3.
 * 
 * Documentation: http://www.mindsensors.com/index.php?module=pagemaster&PAGE_user_op=view_page&PAGE_id=101
 * Some details from MSAC-driver.h from http://botbench.com/blog/robotc-driver-suite/
 * 
 * @author Lawrie Griffiths
 */
public class MindsensorsAccelerometer extends I2CSensor implements SampleProvider {
	private static final byte BASE_TILT = 0x42;
	private static final byte BASE_ACCEL = 0x45;
	private static final byte OFF_X_ACCEL = 0x00;
	private static final byte OFF_Y_ACCEL = 0x02;
	private static final byte OFF_Z_ACCEL = 0x04;
	private static final float TO_SI = 0.00980665f;
	
	private SampleProvider tiltMode;
	private byte[] buf = new byte[6];
	
	/**
	 * Creates a SampleProvider for the Mindsensors ACCL-Nx
	 * 
	 * @param port the I2C port
	 * @param address the I2C address of the sensor
	 */
	public MindsensorsAccelerometer(I2CPort port, int address) {
		super(port, address);
	}
	
	/**
	 * Creates a SampleProvider for the Mindsensors ACCL-Nx
	 * 
	 * @param port the I2C port
	 */
	public MindsensorsAccelerometer(I2CPort port) {
		super(port);
	}
	
	/**
	 * Creates a SampleProvider for the Mindsensors ACCL-Nx
	 * 
	 * @param port the sensor port
	 * @param address the I2C address of the sensor
	 */
	public MindsensorsAccelerometer(Port port, int address) {
		super(port, address, TYPE_LOWSPEED_9V);
	}
	
	/**
	 * Creates a SampleProvider for the Mindsensors ACCL-Nx
	 * 
	 * @param port the sensor port
	 */
    public MindsensorsAccelerometer(Port port) {
        this(port, DEFAULT_I2C_ADDRESS);
    }
	
	@Override
	public int sampleSize() {
		return 3;
	}
	
	@Override
	public int fetchSample(float[] sample, int offset) {
		int ret = getData(BASE_ACCEL, buf, 0, 6);
		if (ret == 0) {
			sample[offset+0] = EndianTools.decodeShortLE(buf, OFF_X_ACCEL) * TO_SI;
			sample[offset+1] = EndianTools.decodeShortLE(buf, OFF_Y_ACCEL) * TO_SI;
			sample[offset+2] = EndianTools.decodeShortLE(buf, OFF_Z_ACCEL) * TO_SI;
		} else for(int i=0;i<3;i++) sample[offset=i] = Float.NaN;
		return ret;	
	}
	
	/**
	 * Return a SampleProvider in acceleration mode
	 */
	public SampleProvider getAccelerationMode() {
		return this;
	}
	
	/**
	 * Return a SampleProvider in tilt mode
	 */
	public SampleProvider getTiltMode() {
		if (tiltMode == null) {
			tiltMode = new TiltMode();
		}
		return tiltMode;
	}

	private class TiltMode implements SampleProvider {
		@Override
		public int sampleSize() {
			return 3;
		}

		@Override
		public int fetchSample(float[] sample, int offset) {
			int ret = getData(BASE_TILT, buf, 0, 3);
			if (ret == 0) {			
				sample[offset+0] = (buf[0] & 0xFF) - 128;
				sample[offset+1] = (buf[1] & 0xFF) - 128;
				sample[offset+2] = (buf[2] & 0xFF) - 128;
			} else for(int i=0;i<3;i++) sample[offset+i] = Float.NaN;
			return ret;
		}
	}
}
