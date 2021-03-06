package org.firstinspires.ftc.teamcode.gamecode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.RC;
import org.firstinspires.ftc.teamcode.opmodesupport.AutoOpMode;
import org.firstinspires.ftc.teamcode.robots.Fermion;
import org.firstinspires.ftc.teamcode.robots.Robot;

/**
 * Created by Windows on 2017-01-15.
 */
@Autonomous
public class Fermion2ShotCapBlue extends AutoOpMode {

        @Override
        public void runOp() throws InterruptedException {
            final Fermion muon = new Fermion(true);

            muon.startShooterControl();
            muon.addVeerCheckRunnable();
            muon.prime();
            waitForStart();
            muon.resetTargetAngle();

            sleep((long)RC.globalDouble("WaitTime"));

            muon.right(1);
            sleep(1800);
            muon.stop();

            if(RC.globalDouble("NumBalls") > 0){
                muon.shoot();

                if(RC.globalDouble("NumBalls") == 2){
                    muon.waitForState(Fermion.LOADED);
                    muon.door.goToPos("open");
                    muon.collector.setPower(-1);
                    muon.shoot();
                    sleep(1000);
                    muon.door.goToPos("close");
                    muon.setCollectorState(Robot.STOP);
                }//if

                muon.waitForState(Fermion.FIRE);
            }

            if(RC.globalBool("Ramp")){
                muon.imuTurnL(45, 0.6);

                muon.backward(1);
                sleep(2400);
                muon.stop();
            }


            //getting capball
            if(RC.globalBool("Cap-ball")) {
                muon.imuTurnR(90, 0.6);

                muon.forward(1);
                sleep(1750);
                muon.stop();
            }
            muon.stop();
        }//runOp
}
