import lejos.hardware.Sound;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.utility.Delay;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

public class LightSampler {
  private static final String CSV_FILE_NAME = "samples.csv";
  private static final DateFormat CSV_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
  private static final Port SENSOR_PORT = SensorPort.S1;
  private static final int EXPECTED_SAMPLE_SIZE = 1;
  private static final int SAMPLE_INTERVAL_MS = 1000;
  private static final float MIN_LIGHT_LEVEL = 0.03f;

  public static void main(String[] args) throws Exception {
    final Logger logger = logger();
    try {
      Sound.beepSequenceUp();
      try (EV3ColorSensor colorSensor = new EV3ColorSensor(SENSOR_PORT)) {
        SensorMode sensorMode = colorSensor.getAmbientMode();
        int sampleSize = sensorMode.sampleSize();
        if (sampleSize != EXPECTED_SAMPLE_SIZE) {
          throw new Exception(
              String.format("expected sample size of %d, but was %d ", EXPECTED_SAMPLE_SIZE, sampleSize));
        }
        float[] sampleValue = new float[sampleSize];

        try (FileWriter csvFileWriter = new FileWriter(CSV_FILE_NAME)) {
          float lightLevel = Float.MAX_VALUE;
          while (lightLevel > MIN_LIGHT_LEVEL) {
            sensorMode.fetchSample(sampleValue, 0);
            lightLevel = sampleValue[0];
            logger.info("ambient light = " + lightLevel);
            writeCsv(csvFileWriter, lightLevel);
            Delay.msDelay(SAMPLE_INTERVAL_MS);
          }
        }
      }
    } catch (Exception e) {
      logger.log(Level.SEVERE, "exception thrown from main", e);
    } finally {
      Sound.beepSequence();
    }
  }

  private static void writeCsv(Writer writer, float value) throws IOException {
    String dateTime = CSV_DATE_FORMAT.format(new Date());
    writer.append(dateTime).append(",").append(Float.toString(value)).append("\n");
    writer.flush();
  }

  private static Logger logger() throws IOException {
    LogManager logManager = LogManager.getLogManager();
    logManager.readConfiguration(LightSampler.class.getResourceAsStream("logging.properties"));
    Logger logger = Logger.getLogger(LightSampler.class.getName());
    if (logger == null) throw new NullPointerException("logger");
    return logger;
  }
}
