/* Copyright (c) 2014, 2015 Qualcomm Technologies Inc

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Qualcomm Technologies Inc nor the names of its contributors
may be used to endorse or promote products derived from this software without
specific prior written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */

package org.firstinspires.ftc.robotcontroller.internal;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.blocks.ftcrobotcontroller.BlocksActivity;
import com.google.blocks.ftcrobotcontroller.ProgrammingModeActivity;
import com.google.blocks.ftcrobotcontroller.ProgrammingModeControllerImpl;
import com.google.blocks.ftcrobotcontroller.runtime.BlocksOpMode;
import com.qualcomm.ftccommon.AboutActivity;
import com.qualcomm.ftccommon.ClassManagerFactory;
import com.qualcomm.ftccommon.Device;
import com.qualcomm.ftccommon.FtcEventLoop;
import com.qualcomm.ftccommon.FtcEventLoopIdle;
import com.qualcomm.ftccommon.FtcRobotControllerService;
import com.qualcomm.ftccommon.FtcRobotControllerSettingsActivity;
import com.qualcomm.ftccommon.LaunchActivityConstantsList;
import com.qualcomm.ftccommon.ProgrammingModeController;
import com.qualcomm.ftccommon.Restarter;
import com.qualcomm.ftccommon.UpdateUI;
import com.qualcomm.ftccommon.configuration.EditParameters;
import com.qualcomm.ftccommon.configuration.FtcLoadFileActivity;
import com.qualcomm.ftccommon.configuration.RobotConfigFile;
import com.qualcomm.ftccommon.configuration.RobotConfigFileManager;
import com.qualcomm.ftcrobotcontroller.R;
import com.qualcomm.hardware.HardwareFactory;
import com.qualcomm.robotcore.eventloop.opmode.OpModeRegister;
import com.qualcomm.robotcore.hardware.configuration.Utility;
import com.qualcomm.robotcore.robocol.PeerAppRobotController;
import com.qualcomm.robotcore.util.Dimmer;
import com.qualcomm.robotcore.util.ImmersiveMode;
import com.qualcomm.robotcore.util.ReadWriteFile;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.wifi.NetworkConnection;
import com.qualcomm.robotcore.wifi.NetworkConnectionFactory;
import com.qualcomm.robotcore.wifi.NetworkType;
import com.qualcomm.robotcore.wifi.WifiDirectAssistant;

import org.firstinspires.ftc.ftccommon.external.SoundPlayingRobotMonitor;
import org.firstinspires.ftc.robotcore.internal.AppUtil;
import org.firstinspires.ftc.robotcore.internal.network.CallbackResult;
import org.firstinspires.inspection.RcInspectionActivity;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import dalvik.system.DexFile;

public class FtcRobotControllerActivity extends Activity {

  public static final String TAG = "RCActivity";

  private static final int REQUEST_CONFIG_WIFI_CHANNEL = 1;
  private static final int NUM_GAMEPADS = 2;

  public static final String NETWORK_TYPE_FILENAME = "ftc-network-type.txt";

  protected WifiManager.WifiLock wifiLock;
  protected RobotConfigFileManager cfgFileMgr;

  protected ProgrammingModeController programmingModeController;

  protected UpdateUI.Callback callback;
  protected Context context;
  protected Utility utility;
  protected AppUtil appUtil = AppUtil.getInstance();

  protected ImageButton buttonMenu;
  protected TextView textDeviceName;
  protected TextView textNetworkConnectionStatus;
  protected TextView textRobotStatus;
  protected TextView[] textGamepad = new TextView[NUM_GAMEPADS];
  protected TextView textOpMode;
  protected TextView textErrorMessage;
  protected ImmersiveMode immersion;

  protected UpdateUI updateUI;
  protected Dimmer dimmer;
  protected LinearLayout entireScreenLayout;

  protected FtcRobotControllerService controllerService;
  protected NetworkType networkType;

  protected FtcEventLoop eventLoop;
  protected Queue<UsbDevice> receivedUsbAttachmentNotifications;

  protected List<String> opModeNames;
  protected String activeOpModeName;
  protected boolean driverStationEnabled = false;
  DexFile dxFile;
  protected Button initOpMode;
  protected Button runOpMode;
  protected Button stopOpMode;
  protected TextView audTelStatus;

  protected class RobotRestarter implements Restarter {

    public void requestRestart() {
      requestRobotRestart();
    }

  }

  static {
    initializeGlobals();
  }

  private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
    @Override
    public void onManagerConnected(int status) {
      switch (status) {
        case LoaderCallbackInterface.SUCCESS:
        {
          RobotLog.i(TAG, "OpenCV loaded successfully");
        } break;
        default:
        {
          super.onManagerConnected(status);
        } break;
      }
    }
  };

  protected ServiceConnection connection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      FtcRobotControllerService.FtcRobotControllerBinder binder = (FtcRobotControllerService.FtcRobotControllerBinder) service;
      onServiceBind(binder.getService());
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      RobotLog.vv(FtcRobotControllerService.TAG, "%s.controllerService=null", TAG);
      controllerService = null;
    }
  };

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);

    if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(intent.getAction())) {
      UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
      if (usbDevice != null) {  // paranoia
        // We might get attachment notifications before the event loop is set up, so
        // we hold on to them and pass them along only when we're good and ready.
        if (receivedUsbAttachmentNotifications != null) { // *total* paranoia
          receivedUsbAttachmentNotifications.add(usbDevice);
          passReceivedUsbAttachmentsToEventLoop();
        }
      }
    }
  }

  protected void passReceivedUsbAttachmentsToEventLoop() {
    if (this.eventLoop != null) {
      for (;;) {
        UsbDevice usbDevice = receivedUsbAttachmentNotifications.poll();
        if (usbDevice == null)
          break;
        this.eventLoop.onUsbDeviceAttached(usbDevice);
      }
    }
    else {
      // Paranoia: we don't want the pending list to grow without bound when we don't
      // (yet) have an event loop
      while (receivedUsbAttachmentNotifications.size() > 100) {
        receivedUsbAttachmentNotifications.poll();
      }
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    RobotLog.writeLogcatToDisk();
    RobotLog.vv(TAG, "onCreate()");

    receivedUsbAttachmentNotifications = new ConcurrentLinkedQueue<UsbDevice>();
    eventLoop = null;

    setContentView(R.layout.activity_ftc_controller);

    context = this;
    utility = new Utility(this);
    appUtil.setThisApp(new PeerAppRobotController(context));

    entireScreenLayout = (LinearLayout) findViewById(R.id.entire_screen);
    buttonMenu = (ImageButton) findViewById(R.id.menu_buttons);
    buttonMenu.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        AppUtil.getInstance().openOptionsMenuFor(FtcRobotControllerActivity.this);
      }
    });

    BlocksOpMode.setActivityAndWebView(this, (WebView) findViewById(R.id.webViewBlocksRuntime));

    ClassManagerFactory.processClasses();
    cfgFileMgr = new RobotConfigFileManager(this);

    // Clean up 'dirty' status after a possible crash
    RobotConfigFile configFile = cfgFileMgr.getActiveConfig();
    if (configFile.isDirty()) {
      configFile.markClean();
      cfgFileMgr.setActiveConfig(false, configFile);
    }

    textDeviceName = (TextView) findViewById(R.id.textDeviceName);
    textNetworkConnectionStatus = (TextView) findViewById(R.id.textNetworkConnectionStatus);
    textRobotStatus = (TextView) findViewById(R.id.textRobotStatus);
    textOpMode = (TextView) findViewById(R.id.textOpMode);
    textErrorMessage = (TextView) findViewById(R.id.textErrorMessage);
    textGamepad[0] = (TextView) findViewById(R.id.textGamepad1);
    textGamepad[1] = (TextView) findViewById(R.id.textGamepad2);
    immersion = new ImmersiveMode(getWindow().getDecorView());
    dimmer = new Dimmer(this);
    dimmer.longBright();

    programmingModeController = new ProgrammingModeControllerImpl(
        this, (TextView) findViewById(R.id.textRemoteProgrammingMode));

    updateUI = createUpdateUI();
    callback = createUICallback(updateUI);

    PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

    WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "");

    hittingMenuButtonBrightensScreen();

    wifiLock.acquire();
    callback.networkConnectionUpdate(WifiDirectAssistant.Event.DISCONNECTED);
    readNetworkType(NETWORK_TYPE_FILENAME);
    bindToService();

    try {
      dxFile = new DexFile(getPackageCodePath());
    } catch (IOException e) {
      e.printStackTrace();
    }//catch

    if (!OpenCVLoader.initDebug()) {
      RobotLog.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
      OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mOpenCVCallBack);
    } else {
      RobotLog.d("OpenCV", "OpenCV library found inside package. Using it!");
      mOpenCVCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);
    }//else

    initOpMode = (Button) findViewById(R.id.initOpMode);
    runOpMode = (Button) findViewById(R.id.runOpMode);
    stopOpMode = (Button) findViewById(R.id.stopOpMode);
    audTelStatus = (TextView) findViewById(R.id.audTelStatus);

  }

  public void initOpMode(View v) {
    FtcControllerUtils.initOpMode(activeOpModeName);
  }

  public void runOpMode(View v) {
    FtcControllerUtils.runOpMode();
  }

  public void stopOpMode(View v) {
    FtcControllerUtils.stopOpMode();
  }

  protected UpdateUI createUpdateUI() {
    Restarter restarter = new RobotRestarter();
    UpdateUI result = new UpdateUI(this, dimmer);
    result.setRestarter(restarter);
    result.setTextViews(textNetworkConnectionStatus, textRobotStatus, textGamepad, textOpMode, textErrorMessage, textDeviceName);
    return result;
  }

  protected UpdateUI.Callback createUICallback(UpdateUI updateUI) {
    UpdateUI.Callback result = updateUI.new Callback();
    result.setStateMonitor(new SoundPlayingRobotMonitor());
    return result;
  }

  @Override
  protected void onStart() {
    super.onStart();
    RobotLog.vv(TAG, "onStart()");

    // Undo the effects of shutdownRobot() that we might have done in onStop()
    updateUIAndRequestRobotSetup();

    cfgFileMgr.getActiveConfigAndUpdateUI();
    cfgFileMgr.changeBackground(R.color.opaque_dark_fixit_green, R.id.idActiveConfigHeader);

    entireScreenLayout.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        dimmer.handleDimTimer();
        return false;
      }
    });
  }

  @Override
  protected void onResume() {
    super.onResume();
    RobotLog.vv(TAG, "onResume()");
  }

  @Override
  public void onPause() {
    super.onPause();
    RobotLog.vv(TAG, "onPause()");
    if (programmingModeController.isActive()) {
      programmingModeController.stopProgrammingMode();
    }
  }

  @Override
  protected void onStop() {
    // Note: this gets called even when the configuration editor is launched. That is, it gets
    // called surprisingly often.
    super.onStop();
    RobotLog.vv(TAG, "onStop()");

    // We *do* shutdown the robot even when we go into configuration editing
    controllerService.shutdownRobot();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    RobotLog.vv(TAG, "onDestroy()");

    unbindFromService();
    wifiLock.release();
    RobotLog.cancelWriteLogcatToDisk();
  }

  protected void bindToService() {
    readNetworkType(NETWORK_TYPE_FILENAME);
    Intent intent = new Intent(this, FtcRobotControllerService.class);
    intent.putExtra(NetworkConnectionFactory.NETWORK_CONNECTION_TYPE, networkType);
    bindService(intent, connection, Context.BIND_AUTO_CREATE);
  }

  protected void unbindFromService() {
    if (controllerService != null) {
      unbindService(connection);
    }
  }

  public void writeNetworkTypeFile(String fileName, String fileContents){
    ReadWriteFile.writeFile(AppUtil.FIRST_FOLDER, fileName, fileContents);
  }

  protected void readNetworkType(String fileName) {
    NetworkType defaultNetworkType;
    File directory = RobotConfigFileManager.CONFIG_FILES_DIR;
    File networkTypeFile = new File(directory, fileName);
    if (!networkTypeFile.exists()) {
      if (Build.MODEL.equals(Device.MODEL_410C)) {
        defaultNetworkType = NetworkType.SOFTAP;
      } else {
        defaultNetworkType = NetworkType.WIFIDIRECT;
      }
      writeNetworkTypeFile(NETWORK_TYPE_FILENAME, defaultNetworkType.toString());
    }

    String fileContents = readFile(networkTypeFile);
    networkType = NetworkConnectionFactory.getTypeFromString(fileContents);
    programmingModeController.setCurrentNetworkType(networkType);

    // update the preferences
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    SharedPreferences.Editor editor = preferences.edit();
    editor.putString(NetworkConnectionFactory.NETWORK_CONNECTION_TYPE, networkType.toString());
    editor.commit();
  }

  private String readFile(File file) {
    return ReadWriteFile.readFile(file);
  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus){
    super.onWindowFocusChanged(hasFocus);
    // When the window loses focus (e.g., the action overflow is shown),
    // cancel any pending hide action. When the window gains focus,
    // hide the system UI.
    if (hasFocus) {
      if (ImmersiveMode.apiOver19()){
        // Immersive flag only works on API 19 and above.
        immersion.hideSystemUI();
      }
    } else {
      immersion.cancelSystemUIHide();
    }
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.ftc_robot_controller, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if (id == R.id.action_programming_mode) {
      if (cfgFileMgr.getActiveConfig().isNoConfig()) {
        // Tell the user they must configure the robot before starting programming mode.
        AppUtil.getInstance().showToast(
            context, context.getString(R.string.toastConfigureRobotBeforeProgrammingMode));
      } else {
        Intent programmingModeIntent = new Intent(ProgrammingModeActivity.launchIntent);
        programmingModeIntent.putExtra(
            LaunchActivityConstantsList.PROGRAMMING_MODE_ACTIVITY_NETWORK_TYPE, networkType);
        startActivity(programmingModeIntent);
      }
      return true;
    } else if (id == R.id.action_inspection_mode) {
      Intent inspectionModeIntent = new Intent(RcInspectionActivity.rcLaunchIntent);
      startActivity(inspectionModeIntent);
      return true;
    }
    else if (id == R.id.action_blocks) {
      Intent blocksIntent = new Intent(BlocksActivity.launchIntent);
      startActivity(blocksIntent);
      return true;
    }
    else if (id == R.id.action_restart_robot) {
      dimmer.handleDimTimer();
      AppUtil.getInstance().showToast(context, context.getString(R.string.toastRestartingRobot));
      requestRobotRestart();
      return true;
    }
    else if (id == R.id.action_configure_robot) {
      EditParameters parameters = new EditParameters();
      Intent intentConfigure = new Intent(FtcLoadFileActivity.launchIntent);
      parameters.putIntent(intentConfigure);
      startActivityForResult(intentConfigure, LaunchActivityConstantsList.FTC_CONFIGURE_REQUEST_CODE_ROBOT_CONTROLLER);
    }
    else if (id == R.id.action_settings) {
      Intent settingsIntent = new Intent(FtcRobotControllerSettingsActivity.launchIntent);
      startActivityForResult(settingsIntent, LaunchActivityConstantsList.FTC_CONFIGURE_REQUEST_CODE_ROBOT_CONTROLLER);
      return true;
    }
    else if (id == R.id.action_about) {
      Intent intent = new Intent(AboutActivity.launchIntent);
      intent.putExtra(LaunchActivityConstantsList.ABOUT_ACTIVITY_CONNECTION_TYPE, networkType);
      startActivity(intent);
      return true;
    }
    else if (id == R.id.action_globals) {
      Intent intent = new Intent(this, GlobalValuesActivity.class);
      startActivity(intent);
      return true;
    }
    else if (id == R.id.action_dservice_connect) {
      if (FtcControllerUtils.isSocketClosed()) {
        FtcControllerUtils.startDriverServiceInteraction();
        audTelStatus.setText("Audible Telemetry Open");
      } else {
        FtcControllerUtils.endDriverServiceInteraction();
        audTelStatus.setText("Audible Telemetry Closed");
      }//else
    }
    else if (id == R.id.action_driver) {

      final RelativeLayout driverStation = (RelativeLayout) findViewById(R.id.driverstation);

      if (!driverStationEnabled) {

        controllerService.shutdownRobot();
        FtcControllerUtils.setUpRobotWithoutWifi();

        final Spinner spinner = (Spinner) findViewById(R.id.opModeList);

        FtcControllerUtils.initializeSpinner(spinner);

        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            driverStation.setVisibility(View.VISIBLE);
            cfgFileMgr.changeBackground(R.color.opaque_dark_fixit_green, R.id.idActiveConfigHeader);
          }//run
        });

        driverStationEnabled = true;
      } else {
        requestRobotRestart();

        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            driverStation.setVisibility(View.INVISIBLE);
            cfgFileMgr.changeBackground(R.color.opaque_dark_fixit_green, R.id.idActiveConfigHeader);
          }//run
        });

        driverStationEnabled = false;
      }//else

      return true;
    }
    else if (id == R.id.action_exit_app) {
      finish();
      return true;
    }

   return super.onOptionsItemSelected(item);
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    // don't destroy assets on screen rotation
  }

  @Override
  protected void onActivityResult(int request, int result, Intent intent) {
    if (request == REQUEST_CONFIG_WIFI_CHANNEL) {
      if (result == RESULT_OK) {
        AppUtil.getInstance().showToast(context, context.getString(R.string.toastWifiConfigurationComplete));
      }
    }
    if (request == LaunchActivityConstantsList.FTC_CONFIGURE_REQUEST_CODE_ROBOT_CONTROLLER) {
      // We always do a refresh, whether it was a cancel or an OK, for robustness
      cfgFileMgr.getActiveConfigAndUpdateUI();
    }
  }

  public void onServiceBind(FtcRobotControllerService service) {
    RobotLog.vv(FtcRobotControllerService.TAG, "%s.controllerService=bound", TAG);
    controllerService = service;
    updateUI.setControllerService(controllerService);

    updateUIAndRequestRobotSetup();
  }

  private void updateUIAndRequestRobotSetup() {
    if (controllerService != null) {
      callback.networkConnectionUpdate(controllerService.getNetworkConnectionStatus());
      callback.updateRobotStatus(controllerService.getRobotStatus());
      requestRobotSetup();
    }
  }

  private void requestRobotSetup() {
    if (controllerService == null) return;

    HardwareFactory factory;
    RobotConfigFile file = cfgFileMgr.getActiveConfigAndUpdateUI();
    cfgFileMgr.changeBackground(R.color.opaque_dark_fixit_green, R.id.idActiveConfigHeader);

    HardwareFactory hardwareFactory = new HardwareFactory(context);
    try {
      hardwareFactory.setXmlPullParser(file.getXml());
    } catch (Resources.NotFoundException e) {
      file = RobotConfigFile.noConfig(cfgFileMgr);
      hardwareFactory.setXmlPullParser(file.getXml());
      cfgFileMgr.setActiveConfigAndUpdateUI(false, file);
    }
    factory = hardwareFactory;

    eventLoop = new FtcEventLoop(factory, createOpModeRegister(), callback, this, programmingModeController);
    FtcEventLoopIdle idleLoop = new FtcEventLoopIdle(factory, callback, this, programmingModeController);

    controllerService.setCallback(callback);
    controllerService.setupRobot(eventLoop, idleLoop);

    passReceivedUsbAttachmentsToEventLoop();
  }

  protected OpModeRegister createOpModeRegister() {
    return new FtcOpModeRegister();
  }

  private void requestRobotShutdown() {
    if (controllerService == null) return;
    controllerService.shutdownRobot();
  }

  private void requestRobotRestart() {
    requestRobotShutdown();
    requestRobotSetup();
  }

  protected void hittingMenuButtonBrightensScreen() {
    ActionBar actionBar = getActionBar();
    if (actionBar != null) {
      actionBar.addOnMenuVisibilityListener(new ActionBar.OnMenuVisibilityListener() {
        @Override
        public void onMenuVisibilityChanged(boolean isVisible) {
          if (isVisible) {
            dimmer.handleDimTimer();
          }
        }
      });
    }
  }


  public static void initializeGlobals() {
    HashMap<String, Object> values = new HashMap<>();

    try {

        BufferedReader globalsRead = new BufferedReader(new FileReader(
                AppUtil.getInstance().getApplication().getExternalFilesDir(null).getAbsolutePath() + "/globals.txt"));

        String toAdd = globalsRead.readLine();
        while (toAdd != null) {
            String[] data = toAdd.split(",;");

            String key = data[1];
            Object val = null;
            if (data[0].equals("d")) {
                val = Double.parseDouble(data[2]);
            } else if (data[0].equals("b")) {
                val = Boolean.parseBoolean(data[2]);
            } else {
                val = data[2];
            }//else

            values.put(key, val);

            toAdd = globalsRead.readLine();
        }//while

    } catch (Exception e) {
        e.printStackTrace();
    }//catch

    GlobalValuesActivity.add("RedAlliance", true);
    GlobalValuesActivity.add("TeleBeginAngle", 0);
    GlobalValuesActivity.add("EncoderDistance", 500);
    GlobalValuesActivity.add("VeerProportional", 0.7 / 90);
    GlobalValuesActivity.add("VeerDerivative", (0.5 / 90) / 10);
    GlobalValuesActivity.add("VeerIntegral", (0.1 / 90) / 1000);
    GlobalValuesActivity.addDashboard("WaitTime", 10000);
    GlobalValuesActivity.addDashboard("NumBalls", 1);
    GlobalValuesActivity.addDashboard("Cap-ball", true);
    GlobalValuesActivity.addDashboard("Ramp",false);

    for (Map.Entry<String, Object> entry : values.entrySet()) {
        Log.i(entry.getKey(), entry.getValue().toString());
        GlobalValuesActivity.add(entry.getKey(), entry.getValue());
    }//for

  }

}
