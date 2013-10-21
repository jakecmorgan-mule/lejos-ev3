package lejos.hardware.sensor;

import lejos.hardware.port.I2CPort;
import lejos.hardware.port.Port;
import lejos.robotics.SampleProvider;

/**
 * This class supports the <a href="http://www.hitechnic.com">HiTechnic</a>
 * barometric sensor.
 * 
 * @author Matthias Paul Scholz
 * 
 */
public class HiTechnicBarometer extends I2CSensor implements SampleProvider {

	private static final int BAROMETRIC_TEMPERATURE = 0x42;
	private static final int BAROMETRIC_PRESSURE = 0x44;
	private static final int BAROMETRIC_PRESSURE_CALIBRATION = 0x46;
	private final double INHG_TO_HPA = 2992 / 1013.25;

	private final byte[] buffer = new byte[2];

	/**
	 * Constructor.
	 * 
	 * @param port the {@link I2CPort} the sensor is connected to.
	 */
	public HiTechnicBarometer(final I2CPort port) {
		super(port, DEFAULT_I2C_ADDRESS);
	}

	/**
	 * Constructor.
	 * 
	 * @param port the {@link I2CPort} the sensor is connected to.
	 * @param address the address
	 */
	public HiTechnicBarometer(final I2CPort port, final int address) {
		super(port, address);
	}

	public HiTechnicBarometer(final Port port, final int address) {
	        super(port, address, TYPE_LOWSPEED);
	}

    public HiTechnicBarometer(final Port port) {
        this(port, DEFAULT_I2C_ADDRESS);
    }

	/**
	 * Re-calibrates the sensor.
	 * 
	 * @param calibrationImperial
	 *            the recalibration value in units of 1/1000 inches of mercury
	 *            (inHg).
	 */
	public void recalibrate(final int calibrationImperial) {
		buffer[0] = (byte) ((calibrationImperial & 0xff00) >> 8);
		buffer[1] = (byte) (calibrationImperial & 0x00ff);
		sendData(BAROMETRIC_PRESSURE_CALIBRATION, buffer, 2);
	}

	/**
	 * @return the present calibration value in pascals.
	 *         Will be 0 in case no explicit recalibration has been performed.
	 */
	public float getCalibrationMetric() {
		getData(BAROMETRIC_PRESSURE_CALIBRATION, buffer, 2);
		int result = ((buffer[0] & 0xff) << 8) + buffer[1];
		return (float) ((result / INHG_TO_HPA) * 10);
	}

	@Override
	public int sampleSize() {
		return 1;
	}

	@Override
	public void fetchSample(float[] sample, int offset) {
		getData(BAROMETRIC_PRESSURE, buffer, 2);
		sample[0] = (float) (((((buffer[0] & 0xff) << 8) + buffer[1]) / INHG_TO_HPA) * 10);
	}
	
	public SampleProvider getTemperatureMode() {
		return new TemperatureMode();
	}
	
	private class TemperatureMode implements SampleProvider {
		@Override
		public int sampleSize() {
			return 1;
		}

		@Override
		public void fetchSample(float[] sample, int offset) {
			getData(BAROMETRIC_TEMPERATURE, buffer, 2);
		    sample[offset] =  (float) (((buffer[0] << 2) | (buffer[1] & 0xFF)) * 10 + 273.15);	
		}	
	}
}