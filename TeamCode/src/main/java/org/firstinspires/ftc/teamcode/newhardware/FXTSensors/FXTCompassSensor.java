package org.firstinspires.ftc.teamcode.newhardware.FXTSensors;

import org.firstinspires.ftc.teamcode.RC;
import com.qualcomm.robotcore.hardware.CompassSensor;

/**
 * Created by FIXIT on 15-08-23.
 */
public class FXTCompassSensor extends FXTSensor {

    CompassSensor compass;

    public FXTCompassSensor(String address) {
        compass = RC.h.compassSensor.get(address);
        sensorType = FTC_COMPASS;
        sensorName = address;
    }

    @Override
    public String toString() {
        return "" + getValue();
    }

    @Override
    public String getName() {
        return "Compass";
    }


    public double returnValue() {
        return compass.getDirection();

    }

    @Override
    public CompassSensor getHardwareSensor() {
        return compass;
    }

}
