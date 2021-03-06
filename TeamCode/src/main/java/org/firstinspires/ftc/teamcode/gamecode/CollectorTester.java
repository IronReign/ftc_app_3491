package org.firstinspires.ftc.teamcode.gamecode;

import android.text.method.TextKeyListener;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.RC;
import org.firstinspires.ftc.teamcode.opmodesupport.TeleOpMode;
import org.firstinspires.ftc.teamcode.robots.Fermion;

/**
 * Created by Windows on 2016-12-31.
 */
public class CollectorTester extends TeleOpMode {
    double speed = 0;
    Fermion electron;
    @Override
    public void initialize() {
        electron = new Fermion(false);
        electron.door.goToPos("open");
    }

    @Override
    public void loopOpMode() {
        if(joy1.rightBumper() && getMilliSeconds() > 300){
            speed += 0.01;
            clearTimer();
        } else if(joy1.rightTrigger() && getMilliSeconds() > 300){
            speed -= 0.01;
            clearTimer();
        }
        electron.collector.setPower(speed);
        RC.t.addData("Speed", speed);
    }
}
