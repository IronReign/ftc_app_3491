package org.firstinspires.ftc.teamcode.gamecode;

import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.vuforia.PIXEL_FORMAT;
import com.vuforia.Vuforia;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;
import org.firstinspires.ftc.teamcode.R;
import org.firstinspires.ftc.teamcode.RC;
import org.firstinspires.ftc.teamcode.opmodesupport.AutoOpMode;
import org.firstinspires.ftc.teamcode.roboticslibrary.FXTCamera;
import org.firstinspires.ftc.teamcode.robots.Fermion;
import org.firstinspires.ftc.teamcode.robots.Robot;
import org.firstinspires.ftc.teamcode.util.VortexUtils;

/**
 * Created by Windows on 2017-01-21.
 */

@Autonomous
public class FermionRedNoVuf extends AutoOpMode {
    Fermion top;

    @Override
    public void runOp() throws InterruptedException {
        top = new Fermion(true);

        FXTCamera cam = new FXTCamera(FXTCamera.FACING_BACKWARD, true);

        top.startShooterControl();
        top.prime();
        waitForStart();
        top.addVeerCheckRunnable();
        top.resetTargetAngle();

        top.forward(0.5);

        while(opModeIsActive() && top.getLight(Robot.LEFT) < Fermion.LIGHT_THRESHOLD){
            idle();
            Log.i(TAG, "runOp: " + top.getLight(Robot.LEFT));
        }

        top.stop();

        top.absoluteSingleIMUTurn(-45, 1);
        top.absoluteIMUTurn(-90, 1);

        top.stop();

        top.left(0.3);

        while(opModeIsActive() && top.getLight(Robot.RIGHT) < Fermion.LIGHT_THRESHOLD){
            idle();
            Log.i(TAG, "runOp: " + top.getLight(Robot.RIGHT));
        }
        top.stop();

        top.right(0.3);

        while(opModeIsActive() && top.getLight(Robot.LEFT) < Fermion.LIGHT_THRESHOLD){
            idle();
            Log.i(TAG, "runOp: " + top.getLight(Robot.LEFT));
        }
        top.stop();



        while (opModeIsActive() && top.ultra.getDistance() < 300) {
            top.backward(1);
        }//while
        top.stop();

        int config = VortexUtils.getBeaconConfig(cam.getImage());

        sleep(1000);

        RC.t.addData("Beacon", config);
        Log.i(TAG, "runOp: " + config);

        int sensor = 0;
        if(config == VortexUtils.BEACON_BLUE_RED){
            top.left(0.3);

            sensor = Robot.RIGHT;
            while (opModeIsActive() && top.getLight(sensor) < 0.2){
                Log.i("light", "" + top.getLight(sensor));
            }
            top.stop();
            sleep(100);
        }

        top.forward(0.3);
        sleep(1000);
        top.backward(1);
        sleep(500);
        top.stop();



        /*
        sleep(900);
        top.stop();
        top.shoot();


        top.waitForState(Fermion.FIRE);
        top.absoluteIMUTurn(90, 0.5);

        top.strafe(-40, 1, true);
        while(opModeIsActive() && top.getLight(Robot.LEFT) < Fermion.LIGHT_THRESHOLD){
            Log.i(TAG, "runOp: ultra" + top.ultra.getDistance());
            if(top.ultra.getDistance() < 410){
                top.strafe(-90, 0.3, true);
            }
        }

        top.stop();

        int config = VortexUtils.getBeaconConfig(cam.getImage());

        RC.t.addData("Beacon", config);

        sleep(10000);*/

        cam.destroy();

    }
}
