/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity {
    private MyProgress myProgress = null;
    private Handler mHandler;
    Boolean getHw_version = false;
    byte[][] Hw_version = new byte[6][64];
    int HW_index = 0;
    int Hw_dataindex = 6;
    TextView textView ;

    tUpdate_info Update_info = new tUpdate_info();
    MyNative myNative = new MyNative();

    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static Boolean receiveDataFlag = false;
    public static Boolean WriteCharacterRspFlag = false;

    UpdateOpt updateOpt = new UpdateOpt();
    public static Object object = new Object();
    Thread mthread;
    Boolean updateFlag = false;
    String newtime;
    public Button upDateButton,version;
    public TextView updateState;

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mConnectionState;
    private TextView mDataField;
    public static String mDeviceName;
    public static String mDeviceAddress;
    public static ExpandableListView mGattServicesList;
    public static BluetoothLeService mBluetoothLeService;
    public static ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    public static boolean mConnected = false;
    public static BluetoothGattCharacteristic mNotifyCharacteristic;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    public static BluetoothGattService mnotyGattService;
    public static BluetoothGattCharacteristic writecharacteristic;
    public static BluetoothGattService readMnotyGattService;
    public static BluetoothGattCharacteristic readCharacteristic;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
                Log.i("建立连接","-----------");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
                Log.i("断开连接","-----------");
            }
            //发现有可支持的服务
            else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                Log.i("发现服务","打印服务列表");
                //displayGattServices(mBluetoothLeService.getSupportedGattServices());
               //写数据的服务和characteristic
                mnotyGattService = mBluetoothLeService.getSupportedGattService(UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb"));
                writecharacteristic = mnotyGattService.getCharacteristic(UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb"));
            }
            //显示数据
            else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                //将数据显示在mDataField上
                //Log.i("显示接受数据","将接受数据显示在mDataField上");
                byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                receiveDataFlag = true;
                PrintLog.printHexString("接收到data*****************", data);
                displayData(PrintLog.returnHexString(data));
                sendMessage(1);
                if(getHw_version && data != null)
                {
                    if(data[0]  == 0x40)
                    {
                         HW_index = 0;
                    }
                    System.arraycopy(data,0,Hw_version[Hw_dataindex-1],HW_index,data.length);
                    HW_index = HW_index + data.length;
                    if( data[data.length -1] == 0x2A)
                    {
                        System.arraycopy(Hw_version[Hw_dataindex-1], 4, Hw_version[Hw_dataindex-1], 0,HW_index-7);
                        HW_index = 0;
                        Hw_dataindex --;
                        if(Hw_dataindex <=0)
                        {
                            Hw_dataindex =6;
                            //getHw_version = false;
                            Log.i("版本信息接受完毕","版本信息接受完毕");
                            textView.setText(new String(Hw_version[0] ) +"\n"
                                                +new String(Hw_version[1] ) +"\n"
                                                +new String(Hw_version[2]) +"\n"
                                                +new String(Hw_version[3]) +"\n"
                                                +new String(Hw_version[4]) +"\n"
                                                +new String(Hw_version[5]));

                        }
                    }

                }else if(!getHw_version && data != null)
                {
                    updateReceive_respons(data, data[1]);
                }
            }

            else if(BluetoothLeService.EXTRA_DATA.equals(action))
            {
                Log.i("显示EXTRA_DATA","EXTRA_DATA");
            }
            else if(BluetoothLeService.WRITE_STATUS.equals(action))
            {
                WriteCharacterRspFlag = true;
                //Log.i("写数据结果","回应成功");

            }
        }
    };

    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    if (mGattCharacteristics != null) {
                        final BluetoothGattCharacteristic characteristic =
                                mGattCharacteristics.get(groupPosition).get(childPosition);
                        final int charaProp = characteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            // If there is an active notification on a characteristic, clear
                            // it first so it doesn't update the data field on the user interface.
                            if (mNotifyCharacteristic != null) {
                                mBluetoothLeService.setCharacteristicNotification(
                                        mNotifyCharacteristic, false);
                                mNotifyCharacteristic = null;
                            }
                            writecharacteristic.setValue("0123456789");
                            mBluetoothLeService.readCharacteristic(writecharacteristic);
                            //Log.i("BLE读数据",characteristic.getStringValue(0));
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mNotifyCharacteristic = characteristic;
                            mBluetoothLeService.setCharacteristicNotification(
                                    characteristic, true);
                            Log.i("BLE通知",characteristic.getStringValue(0));
                        }
                        if((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE)>0){
                            //mBluetoothLeService.writeCharacteristic(characteristic);
                            mBluetoothLeService.writeCharacteristic(writecharacteristic);
                            //Log.i("BLE写数据",writecharacteristic.getStringValue(0));
                        }
                        return true;
                    }
                    return false;
                }
    };
    private int update_sendSize;

    private void clearUI() {
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText(R.string.no_data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
        mGattServicesList.setOnChildClickListener(servicesListClickListner);
        mGattServicesList.setVisibility(View.GONE);

        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.data_value);

        version = (Button) findViewById(R.id.version);
        upDateButton = (Button)findViewById(R.id.updateButton);
        myProgress = (MyProgress) findViewById(R.id.pgsBar);
        updateState = (TextView) findViewById(R.id.updateState);
        textView = (TextView)findViewById(R.id.textView);

        //textView.setMovementMethod(ScrollingMovementMethod.getInstance());


        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }

        upDateButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(mConnected)
                {
                    getHw_version = false;
                    Log.i("开始升级", "button onClick");
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    upDateButton.setClickable(false);

                    //int ret = update_fileParse(fileName);

                    updateFlag = true;
                    mthread = new Thread(sendData, "Update");
                    myNative.update_checkSetFlag(0);
                    mthread.start();
                }
                else
                {
                    Log.i("蓝牙连接状态", "蓝牙断开");
                }
            }
        });

//        new Handler().postDelayed(new Runnable(){
//            public void run() {
//                //execute the task
//                getHw_version = true;
//                mRunnable.run();
//            }
//        }, 3000);

    }

    public void version(View v)
    {
        getHw_version = true;
        mRunnable.run();
    }

    Runnable mRunnable = new Runnable() {
        public void run() {
            //自定义功能
            byte[] bytes = updateOpt.wakeupData;        //写入发送数据
            WriteComm(writecharacteristic, bytes, bytes.length);
            byte[] data = {0x00,0x00};
            Log.i("获取版本","获取版本");
            comm_send(COMM_TRANS_TYPE_SEND,COMM_CMD_TYPE_VERSION,data,2);
        }
    };

    public final int UpdateStepSendRequst = 0,
                    UpdateStepWaitRequestRes = 1,
                    UpdateStepSendImage = 2,
                    UpdateStepWaitImageRes = 3,
                    UpdateStepWaitCRCRes = 4,
                    UpdateStepCRCResRecv = 5;
    public int update_sendLen=0,filedataLen=0,updateIdex= 0,update_step = UpdateStepSendRequst;
    public long startTime=0,consumingTime=0;  //開始時間
    FileInputStream fin = null;
    byte [] buffer = null;
    String fileName = "classes.dex";

    Runnable sendData = new Runnable()
    {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            Time t=new Time(); // or Time t=new Time("GMT+8"); 加上Time Zone资料。
            t.setToNow(); // 取得系统时间。
            int year = t.year;
            int month = t.month + 1;
            int date = t.monthDay;

            newtime = String.valueOf(year)
                    +"-"+String.format("%02d",month)
                    +"-"+String.format("%02d",date);
            //读SD中的文件
            try{
                String filePath = updateOpt.getSdCardPath() + "/Downloads/image_W16.hyc";
                int retry =6;
                while(imageNum <1) {
                    try {
                        imageNum = myNative.update_fileParse(filePath.getBytes());
                    } catch (Exception e) {
                        Log.w("升级文件不存在：", "请放入升级文件");
                        sendMessage(6);
                    }
                    if(imageNum >0) break;
                    retry --;
                    if(retry == 0){
                        Log.i("升级文件个数", String.valueOf(imageNum));
                        break;
                    }
                }

                fin = new FileInputStream(filePath);
                int filedataLenTotal = fin.available();
                Log.i("文件字节数",String.valueOf(filedataLenTotal));
                if(imageNum >0) {
                    try {
                        fin.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                buffer = new byte[98];
            } catch(Exception e){
                e.printStackTrace();
            }
            byte[] bytes = updateOpt.wakeupData;        //写入发送数据
            //Boolean bool = WriteComm( writecharacteristic, bytes, bytes.length);
//
//                synchronized(object)
//                {
//                    try {
//                        //Log.i("加锁等待：", "wait...");
//                        object.wait(); // 暂停线程
//                    }catch(InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
            if(imageNum <1) {
                sendMessage(6);
                updateFlag = false;
            }else {
                int ret = myNative.update_getImageInfo(imageIndex, Update_info.ppVer_Str,
                        Update_info.hw_info,
                        Update_info.image_size,
                        Update_info.image_crc,
                        Update_info.image_data);
                while (updateFlag) {
                    //发送唤醒
                    if (update_step == 0) {
                        Log.i("唤醒蓝牙：", "wait...");
                        WriteComm(writecharacteristic, bytes, bytes.length);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            Log.i("等待延时：", "wait...");
                        }
                    }
                    //Log.i("升级流程切换：", "wait...");
                    update_step = update_Switch();

//                try {
//                    int offset = updateIdex * 98 +1024;
//                    if(offset <= filedataLen)
//                        update_sendLen = fin.read(buffer,offset ,98);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                if(update_sendLen > 0)
//                {
//                    Log.i("发送文件数据：", "wait...");
//                    //WriteComm(writecharacteristic, buffer, sendLen);
//                    comm_send(COMM_TRANS_TYPE_SEND, COMM_CMD_TYPE_UPDATE, buffer, update_sendLen);
//                }
                    if (update_step == 5) {
                        //升级完成后
                        updateFlag = false;
                        Message message = new Message();
                        message.what = 0;
                        handler.sendMessage(message);
                        break;
                    }

                    //updateFlag = false;
                }
            }

            Log.i("使能按键：", "wait...");
            update_step = 0;
            upDateButton.setClickable(true);
        }
    };

    public int update_Switch()
    {
        //startTime = System.currentTimeMillis();  //開始時間
        switch (update_step)
        {
            case UpdateStepSendRequst:
                //发送升级请求
                Log.i("发送升级请求：", "发送升级请求");
                sendMessage( 2 );
                receiveDataFlag = false;
                //while(!receiveDataFlag)
                {
                    update_sendUpdateReq();
                    try {
                        Thread.currentThread().sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                update_sendSize = 0;
                update_step++;
                startTime = System.currentTimeMillis();  //開始時間
                break;
            case UpdateStepSendImage:
                Log.i("发送升级文件：", "发送升级文件"+String.valueOf(update_sendSize)
                                                    + ":"+String.valueOf(filedataLen) );
                sendMessage( 3 );
                /* 发送升级数据 */
                if( update_sendSize >= filedataLen) {
                    update_step = UPDATE_STEP_WAIT_CRC_RES;
                    break;
                }
                update_sendLen = update_sendImageData();
                if( update_sendLen < 1 ) {
                    startTime = System.currentTimeMillis();  //開始時間
                    update_step = UPDATE_STEP_WAIT_CRC_RES;
                    break;
                }
                startTime = System.currentTimeMillis();  //開始時間
                update_step++;
                break;
            case UpdateStepWaitRequestRes:
                consumingTime = System.currentTimeMillis();
                if ((consumingTime - startTime) >= 2000)
                {
			        /* 超时重发 */
                    Log.i("发送升级请求：", "超时重发");
                    update_step = UPDATE_STEP_SEND_REQUEST;
                }
                break;
            case UpdateStepWaitImageRes:
                /* 等待升级请求和升级数据回应 */
                consumingTime = System.currentTimeMillis();
                if ((consumingTime - startTime) >= 400)
                {
			        /* 超时重发 */
                    Log.i("发送升级文件：", "超时重发");
                    update_step = UPDATE_STEP_SEND_IMAGE;
                }
                break;
            case UpdateStepWaitCRCRes:
                /* 等待升级请求和升级数据回应 */
                consumingTime = System.currentTimeMillis();
                if ((consumingTime - startTime) >= 5000)
                {
                    /* 超时 */
                    /* 重启，认为升级成功 */
                    //mySetRecvInfo("升级完成");
                    Log.i("升级完成：", "升级完成");
                    update_step = UPDATE_STEP_CRC_RES_RECV;
                }
                break;
            case UpdateStepCRCResRecv:
                Log.i("CRC校验正确", "升级完成");
                Log.i("升级完成", "升级完成");
                //升级成功
                break;
        }
        return update_step;
    }

    final Handler handler=new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch(msg.what){
                case 0:
                    update_step = 0;
                    //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    Toast.makeText(getApplicationContext(), "升级成功！！！", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    //Toast.makeText(getApplicationContext(), "收到回应", Toast.LENGTH_SHORT).show();
                    //receiveDataFlag = true;
                    updateState.setText("收到回应");
//                    synchronized(object)
//                    {
//                        Log.i("解锁通知：", "wait...");
//                        object.notify(); // 恢复线程
//                    }
                    break;
                case 2:
                    updateState.setText("发送升级请求");
                    break;
                case 6:
                    Toast.makeText(getApplicationContext(), "请确认升级文件存在根目录？", Toast.LENGTH_LONG).show();
                    break;
                case 7:
                    Toast.makeText(getApplicationContext(), "请确认网络连接？", Toast.LENGTH_LONG).show();
                case 8:
                    if(!list.isEmpty())
                        showSingleChoiceButton();
                    break;
                case 9:
                    updateState.setText("升级成功！！！");
                    break;
                case 11:
                    new Thread(httpdownload,"Download").start();
                    break;
                case 41:
                    new Thread(runnable,"GetFiles").start();
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
//        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
//        if (mBluetoothLeService != null) {
//            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
//            Log.d(TAG, "Connect request result=" + result);
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mGattUpdateReceiver);
        unbindService(mServiceConnection);
        mBluetoothLeService.disconnect();
        mBluetoothLeService.close();
        mBluetoothLeService = null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //do something...
            finish();
            //moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
            upDateButton.setClickable(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_files:

                sendMessage(41);
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    Runnable runnable=new Runnable() {
        @Override
        public void run() {
            loads();
            Looper.prepare();
            sendMessage(8);
        }
    };

    Runnable httpdownload =new Runnable() {
        @Override
        public void run() {
            downloadfile( list.get(index).get("Url"));
            Looper.prepare();
        }
    };

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    private void displayData(String data) {
        if (data != null) {
            mDataField.setText(data);
        }
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
        /*
            生成嵌套列表
         */
        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                R.layout.listitemidex_device,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 },
                gattCharacteristicData,
                R.layout.listitemchild_device,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 }
        );
        Log.i("重设列表","设置Adapter");
        mGattServicesList.setAdapter(gattServiceAdapter);

    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.WRITE_STATUS);
        return intentFilter;
    }

    //读SD中的文件
    public byte[] readFileSdcardFile(String fileName) throws IOException{
        String res="";
        byte [] buffer = null;
        try{
            FileInputStream fin = new FileInputStream(updateOpt.getSdCardPath() + fileName);
            int length = fin.available();
            buffer = new byte[length];
            fin.read(buffer);
            //res = EncodingUtils.getString(buffer, "UTF-8");
            fin.close();
        } catch(Exception e){
            e.printStackTrace();
        }
        return buffer;
    }

    public void sendMessage(int what)
    {
        Message message = new Message();
        message.what = what;
        handler.sendMessage(message);
    }

    void update_sendUpdateReq()
    {
        byte temp[] = new byte[32];
        int requestId;
        int len;
        //supportCipher = false;
	/* 已发送数据大小 */
        update_sendSize = 0;
        len = 0;
        //发送升级请求，并等待回应
        //requestId = UPDATE_REQUEST_ID;
        //memcpy(&temp[len], &requestId, 2);
        temp[len] = (byte)0xFF;
        temp[len+1] = (byte)0xFF;
        len += 2;
        //memcpy(&temp[len], &tUpdate_info.hw_info, 4);
        System.arraycopy(Update_info.hw_info,0,temp,len,4);
        len += 4;
        //memcpy(&temp[len], &tUpdate_info.image_size, 4);
        System.arraycopy(Update_info.image_size,0,temp,len,4);
        len += 4;
        //memcpy(&temp[len], &tUpdate_info.image_crc, 4);
        System.arraycopy(Update_info.image_crc,0,temp,len,4);
        len += 4;
        //comm_send(COMM_TRANS_TYPE_SEND, COMM_CMD_TYPE_UPDATE, &temp[1], len-1);//只为唤醒目标机
        //Delay(50);
        comm_send(COMM_TRANS_TYPE_SEND, COMM_CMD_TYPE_UPDATE, temp, len);
    }


/* 封包起始和结尾字节 */
    byte    COMM_PAKET_START_BYTE    = 0x40;
    byte    COMM_PAKET_END_BYTE		=(byte)	(0x2A);
    byte    COMM_PAKET1_END_BYTE		=(byte)	(0xA2);
/* 收发类型 */
    byte	COMM_TRANS_TYPE_SEND		=	(0x53);	/* 'S'---send */
    byte	COMM_TRANS_TYPE_RESP		=	(0x52);	/* 'R'---response */
/* 命令类型 */
    byte	COMM_CMD_TYPE_BARCODE		=	(0x01);	//条码传输
    byte	COMM_CMD_TYPE_BUTTON		=	(0x02);	//滑动按键，保留
    byte	COMM_CMD_TYPE_RESEVE0		=	(0x03);	//保留
    byte	COMM_CMD_TYPE_LED			=	(0x04);	//控制led(Simple)
    byte	COMM_CMD_TYPE_LED_ADV		=	(0x05);	//控制led(Advanced)
    byte	COMM_CMD_TYPE_BT_NAME		=	(0x06);	//蓝牙名称设置
    byte	COMM_CMD_TYPE_SUFFIX		=	(0x07);	//条码后缀设置
//byte	COMM_CMD_TYPE_BAR_PRIOR		=	(0x08);	//条码解码优先级设置
//byte	COMM_CMD_TYPE_BAR_PARAM		=	(0x09);	//条码解码参数设置
    byte	COMM_CMD_TYPE_BT_STA	=		(0x0A);	//BT连接状态报告
    byte	COMM_CMD_TYPE_BAT_LEV		=	(0x0A);	//电量获取
    byte	COMM_CMD_TYPE_CONNECT_STA	=	(0x0B);	//连接状态查询
    byte	COMM_CMD_TYPE_RSSI		    =    (0x0C);	//获取本地蓝牙的RSSI
    byte	COMM_CMD_TYPE_SCAN_MODE		=	(0x0E);	//扫描模式设置
    byte	COMM_CMD_TYPE_RD_BAR_PRIOR	=	(0x10);	//读取条码解码优先级
    byte	COMM_CMD_TYPE_WR_BAR_PRIOR	=	(0x11);	//设置条码解码优先级
    byte	COMM_CMD_TYPE_RD_BAR_PARAM	=	(0x12);	//读取条码解码参数
    byte	COMM_CMD_TYPE_WR_BAR_PARAM	=	(0x13);	//设置条码解码参数
    byte   COMM_CMD_TYPE_DEBUG_EN		=(byte)	(0xB0);	//debug开关
    byte   COMM_CMD_TYPE_DEBUG_SCAN	    =(byte)	(0xB1);	//debug scan开关
    byte	COMM_CMD_TYPE_WR_STAY_COMM   =(byte)	(0xC0);	//座充保持COMM模式
    byte	COMM_CMD_TYPE_WR_IN_USB		=(byte)	(0xC1);	//座充进入U盘模式
    byte	COMM_CMD_TYPE_UPDATE		=(byte)	(0xD0);	//软件升级
    byte	COMM_CMD_TYPE_VOICE		=(byte)	(0xD1);	//语音数据传输
    byte	COMM_CMD_TYPE_DONGLE_SN	=(byte)	(0xD2);	//dongle序列号z
    byte	COMM_CMD_TYPE_TOUCH		=	(byte)0xDF;	//touch数据
    byte	COMM_CMD_TYPE_VERSION		=	(byte)(0xE0);	//R11版本信息
/* 封包最小长度 */
    int	    COMM_PAKET_LEN_MIN	=	(6);
    int 	COMM_DATA_BUF_LEN	    =	(1024);
    int     UPDATE_SEND_PAKET_SIZE  = 92;

    //final int UPDATE_REQUEST_ID		=	(int)(0xFFFD);
    //final int UPDATE_CRC_RESP_ID		=	(int)(0xFFFC);
    final int UPDATE_REQUEST_ID	 =	(int)	(0xFFFF);
    final int UPDATE_CRC_RESP_ID 	=(int) (0xFFFE);

    final byte UPDATE_REQUST_OK			=		(0x00);//升级请求被接受
    final byte UPDATE_REJECT_REASON_HW_ERR		=	(0x01);//硬件版本错误
    final byte UPDATE_REJECT_REASON_SIZE_ERR	=	(0x02);//升级包大小错误(超过限制)

/*-------------------------------------------------------------------------
* 函数: comm_send
* 说明: 发送
* 参数: pData---数据buffer
		len-----条码长度
* 返回: HY_OK------发送成功
		HY_ERROR---发送失败
-------------------------------------------------------------------------*/
    Boolean comm_send(byte transType, byte cmd, byte[] pData, int len)
    {
        byte i;
        byte[] temp = new byte[len + 6];
        int sum=0;

        if (pData==null) return false;

        temp[0] = COMM_PAKET_START_BYTE;
        temp[1] = (byte)(len+2);
        temp[2] = (byte)transType;
        temp[3] = (byte)cmd;
        //memcpy(&temp[4], pData, len);
        System.arraycopy(pData,0,temp,4,len);
        temp[len+5] = COMM_PAKET_END_BYTE;
        for(i=0; i<len+3; i++)
        {
            sum += temp[i+1];
        }
        temp[len+4] = (byte)sum;
        //Log.i("调用特征值写：", "wait...");
        WriteComm(writecharacteristic,temp, len+6);

        return true;
    }

    public static Boolean WriteComm(BluetoothGattCharacteristic WriteCharacteristic, byte[] SendData, int DateCount)
    {
        Boolean bool = false;
        int count = 0;
        if(DateCount>20){
            for(int i = 0;i<DateCount;i=i+20)
            {
                bool = WriteCharacteristic.setValue(UpdateOpt.subBytes(SendData, i, 20));
                PrintLog.printHexString("Gatt写长数据",WriteCharacteristic.getValue());
                WriteCharacterRspFlag = false;
                BluetoothLeService.writeCharacteristic(WriteCharacteristic);//BluetoothLeService.writeCharacteristic(WriteCharacteristic);

                while (!WriteCharacterRspFlag)
                {
//                    count++;
//                    if(count == 5) {
//                        count = 0;
//                        Log.i("发送数据：", "分段发送5次失败");
//                        break;
//                    }
                    //BluetoothLeService.writeCharacteristic(WriteCharacteristic);
                    try {
                        Thread.currentThread().sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Log.i("回应标志：", WriteCharacterRspFlag.toString());
                WriteCharacterRspFlag = false;
            }
            bool = true;
        }else {
            bool = WriteCharacteristic.setValue(SendData);
            PrintLog.printHexString("Gatt写短数据",WriteCharacteristic.getValue());
            if (bool) {
                BluetoothLeService.writeCharacteristic(WriteCharacteristic);
                WriteCharacterRspFlag = false;
                while (!WriteCharacterRspFlag)
                {
                    count++;
                    if(count == 4) {
                        count = 0;
                        Log.i("发送短数据：", "发送4次失败");
                        break;
                    }
                    BluetoothLeService.writeCharacteristic(WriteCharacteristic);
                    try {
                        Thread.currentThread().sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                bool = true;
                Log.i("回应标志：", WriteCharacterRspFlag.toString());
                WriteCharacterRspFlag = false;
            } else {
                Log.i("写特征值：", "本地写失败");
                bool = false;
            }
        }
        return bool;
    }

    int update_sendImageData()
    {
        byte UPDATE_SEND_PAKET_SIZE	=	(92);//(100)//(112)//(32)//(12)//(14)//(64)//
        byte[] temp = new byte[UPDATE_SEND_PAKET_SIZE+2];
        int imageReadLen = 0;
        int index;

        index = (update_sendSize/UPDATE_SEND_PAKET_SIZE)+1;
        //memcpy(&temp[0], &index, sizeof(U16));
        temp[0] = (byte) (index >> 8 * 0 & 0xFF);
        temp[1] = (byte) (index >> 8 * 1 & 0xFF);

        imageReadLen = update_readImageData(temp, update_sendSize, UPDATE_SEND_PAKET_SIZE);
        if (imageReadLen <= 0)
        {
		/* 升级数据发送完成 */
            return 0;
        }

        comm_send(COMM_TRANS_TYPE_SEND, COMM_CMD_TYPE_UPDATE, temp, imageReadLen+2);

        return imageReadLen;
    }

    /*-------------------------------------------------------------------------
* 函数: update_readImageData
* 说明: 读取image数据
* 参数: ptUpdataInfo
* 返回: 读取大小
-------------------------------------------------------------------------*/
    int update_readImageData(byte pData[], int offset, int len)
    {
        int readLen;
        byte[] senddata=new byte[98];

        if (offset >= filedataLen)
        {
            //readLen = 0;
            return -1;
        }
        else if ((offset+len) > filedataLen)
        {
            readLen = filedataLen - offset;
        }
        else
        {
            readLen = len;
        }
//        try {
//            readLen = fin.read(senddata,offset ,98);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        System.arraycopy(Update_info.image_data,offset,senddata,0,readLen);
        System.arraycopy(senddata , 0, pData, 2 , readLen);

        return readLen;
    }

    final int UPDATE_STEP_SEND_REQUEST	=	0;
    final int UPDATE_STEP_WAIT_REQUEST_RES=	1;

    final int UPDATE_STEP_SEND_IMAGE		=	2;
    final int UPDATE_STEP_WAIT_IMAGE_RES	=	3;

    final int UPDATE_STEP_WAIT_CRC_RES	=	4;
    final int UPDATE_STEP_CRC_RES_RECV 	=	5;

    public int imageIndex = 0,imageNum=0;
    Boolean supportCipher = false;

    void updateReceive_respons(byte[] pdata, int len)
    {
        int index, offset;
        int respons;
        int ret;

        offset = 4;
        //memcpy(&index, &pdata[offset], sizeof(U16));
        index = (pdata[offset] & 0xFF) | (pdata[offset+1] & 0x00FF)<<8 ;
        offset += 2;
        switch (index)
        {
            case UPDATE_REQUEST_ID:
                Log.i("解析升级请求数据....","解析升级请求数据");
                if (len > 3) ret = myNative.update_checkSetFlag(1);
                else ret = myNative.update_checkSetFlag(0);
                if (ret == 0)
                {
                    if (pdata[offset] == UPDATE_REJECT_REASON_HW_ERR)
                    {
				        /* 硬件版本错误 */
                        Log.w("硬件版本错误0....","重新发送升级请求");
                        imageIndex++;
                        if (imageIndex >= imageNum) imageIndex = 0;
                    }
                    ret = myNative.update_getImageInfo(imageIndex, Update_info.ppVer_Str,
                            Update_info.hw_info,
                            Update_info.image_size,
                            Update_info.image_crc,
                            Update_info.image_data);
                    filedataLen = UpdateOpt.byteArrayToInt(Update_info.image_size);
                    Log.i("更换升级文件：=",String.valueOf(imageIndex) +":" + String.valueOf(filedataLen));
                    return;
                }
		        /* 接收升级请求回应 */
                switch(pdata[offset])
                {
                    case UPDATE_REQUST_OK:
			        /* 升级请求被接受 */
                        Log.i("请求被接收....","请求被接收");
                        try {
                            Thread.currentThread().sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        update_step++;
                        if (len > 3)
                        {
                            Log.i("芯片支持OAD0....","芯片支持OAD");
                            supportCipher = true;
                        }
                        else
                        {
                            Log.w("不支持OAD....","");
                            supportCipher = false;
                        }

                        break;
                    case UPDATE_REJECT_REASON_HW_ERR:
			        /* 硬件版本错误 */
                        Log.i("硬件版本错误1....","硬件版本错误");
                        imageIndex++;
                        if (imageIndex >= imageNum) imageIndex = 0;
                        ret = myNative.update_getImageInfo(imageIndex,Update_info.ppVer_Str,
                                Update_info.hw_info,
                                Update_info.image_size,
                                Update_info.image_crc,
                                Update_info.image_data);
                        filedataLen = UpdateOpt.byteArrayToInt(Update_info.image_size);
                        Log.i("更换升级文件：=",String.valueOf(imageIndex) +":" + String.valueOf(filedataLen));
                        break;
                    case UPDATE_REJECT_REASON_SIZE_ERR:
			        /* 升级包大小错误(超过限制) */
                        Log.i("升级包大小错误(超过限制)","超过限制");
                        break;
                }
                break;
            case UPDATE_CRC_RESP_ID:
		    /* 收到CRC校验回应 */
                if (pdata[offset] == 0)
                {
			        /* CRC校验正确 */
                    //mySetRecvInfo("CRC校验正确");
                    Log.i("CRC校验正确","CRC校验正确");
                    update_step = UPDATE_STEP_CRC_RES_RECV;
                    sendMessage(9);
                }
                else
                {
			        /* 校验值错误，重发升级请求，重新升级 */
                    Log.w("校验值错误，重发升级请求，重新升级","校验值错误");
                    update_step = UPDATE_STEP_SEND_REQUEST;
                    update_sendSize = 0;

                }
                break;
            default:
		    /* 升级数据包回应 */
                if(pdata[offset] == 0 && index == (update_sendSize/UPDATE_SEND_PAKET_SIZE)+1 )
                {
			        /* 数据包被正确接收 */
                    update_sendSize += update_sendLen;
                    int persent = (int)Math.floor(100*((double)update_sendSize/filedataLen));
                    myProgress.setProgress(persent);
                    if (update_sendSize >= filedataLen)
                    {
				        /* 数据包发送完成，等待CRC校验结果 */
                        Log.i("数据包发送完成，等待CRC校验结果","等待CRC校验");
                        update_step = UPDATE_STEP_WAIT_CRC_RES;
                        break;
                    }
                }
                else
                {
                    Log.i("数据包接收错误，重发","数据包接收错误");
			        /* 数据包接收错误，重发 */
                }
                update_step--;
                break;
        }

    }

    private String[] province = new String[] { "上海", "北京", "海南" };
    // 单击事件对象的实例
    private ButtonOnClick buttonOnClick = new ButtonOnClick(1);
    // 在单选选项中显示 确定和取消按钮
    //buttonOnClickg变量的数据类型是ButtonOnClick,一个单击事件类
    private void showSingleChoiceButton()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请选择升级文件版本");
        builder.setSingleChoiceItems(province, -1, buttonOnClick);
        builder.setPositiveButton("确定", buttonOnClick);
        builder.setNegativeButton("取消", buttonOnClick);
        builder.show();
    }

    public int index; // 表示选项的索引
    private class ButtonOnClick implements DialogInterface.OnClickListener
    {

        public ButtonOnClick(int listindex)
        {
            index = listindex;
        }

        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            // which表示单击的按钮索引，所有的选项索引都是大于0，按钮索引都是小于0的。
            if (which >= 0)
            {
                //如果单击的是列表项，将当前列表项的索引保存在index中。
                //如果想单击列表项后关闭对话框，可在此处调用dialog.cancel()
                //或是用dialog.dismiss()方法。
                index = which;
            }
            else
            {
                //用户单击的是【确定】按钮
                if (which == DialogInterface.BUTTON_POSITIVE)
                {
                    //显示用户选择的是第几个列表项。
                    Log.w("选择的文件是：",province[index]);
                    final AlertDialog ad = new AlertDialog.Builder(
                            DeviceControlActivity.this).setMessage(
                            "你选择的文件是：" + index + ":" + province[index]).show();
                    sendMessage(11);
                    //五秒钟后自动关闭。
                    Handler hander = new Handler();
                    Runnable runnable = new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            ad.dismiss();
                        }
                    };
                    hander.postDelayed(runnable, 2 * 1000);
                    //TODO
//                    downloadfile(province[index] );
                }
                //用户单击的是【取消】按钮
                else if (which == DialogInterface.BUTTON_NEGATIVE)
                {
                    Toast.makeText(DeviceControlActivity.this, "你没有选择任何文件！",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    Document doc;
    String Url = "http://hycosoft.cc/w16.json";
    List<Map<String, String>> list = new ArrayList<Map<String, String>>();
    protected void load() {
        try {
            doc = Jsoup.parse(new URL("http://192.168.8.135/Images/"), 3000);
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        if(doc == null){
            sendMessage(7);
            return;
        }
        //Elements es = doc.getElementsByClass("a");
        Elements links = doc.select("a[href]"); // 具有 href 属性的链接
        list.clear();
        for (Element e : links) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("title", e.getElementsByTag("a").text());
            map.put("href", "http://192.168.8.135/Images/"
                    + e.getElementsByTag("a").attr("href"));
            if(e.getElementsByTag("a").text().contains(".hyc")) {
                list.add(map);
            }
        }
        province = new String[] { list.get(0).get("title"),list.get(1).get("title") };

    }

    protected void loads() {
        String getdata = HttpUser.getJsonContent(Url);  //请求数据地址
        if(getdata == null){
            sendMessage(7);
            return;
        }
        //JSON对象 转 JSONModel对象
        Root result = JavaBean.getPerson(getdata, Root.class);
        list.clear();
        for (int i=0;i<result.getKey().size();i++) {{
            Map<String, String> map = new HashMap<String, String>();
            map.put("Name", result.getKey().get(i).getName());
            map.put("Url", result.getKey().get(i).getUrl() );
            list.add(map);
            }
        }
        province = new String[result.getKey().size()];
        for(int j=0;j<result.getKey().size();j++)
        {
            province[j] = list.get(j).get("Name");
        }

    }

    /**
     *
     * @Project: Android_MyDownload
     * @Desciption: 读取任意文件，并将文件保存到手机SDCard
     * @Author: LinYiSong
     * @Date: 2011-3-25~2011-3-25
     */
        public void downloadfile(String  urlStr)
        {
            String path="Downloads";
            String fileName="image_W16.hyc";
            OutputStream output=null;
            try {
                /*
                 * 通过URL取得HttpURLConnection
                 * 要网络连接成功，需在AndroidMainfest.xml中进行权限配置
                 * <uses-permission android:name="android.permission.INTERNET" />
                 */
                URL url=new URL( urlStr);
                HttpURLConnection conn=(HttpURLConnection)url.openConnection();
                //获得文件的长度
                int contentLength = conn.getContentLength();
                System.out.println("长度 :"+contentLength);

                //取得inputStream，并将流中的信息写入SDCard
                String SDCard= Environment.getExternalStorageDirectory()+"";
                String pathName=SDCard+"/"+path+"/"+fileName;//文件存储路径

                File file=new File(pathName);
                InputStream input=conn.getInputStream();
                if(file.exists()){
                    System.out.println("exits");
                    file.delete();
                }else{
                    String dir=SDCard+"/"+path;
                    new File(dir).mkdir();//新建文件夹
                    file.createNewFile();//新建文件
                    output=new FileOutputStream(file);
                    //读取大文件
                    byte[] buffer=new byte[1024];
                    //Log.w("文件请求大小",String.valueOf(input.available()));
                    int len;
                    while( (len = input.read(buffer))!=-1 ){
                        output.write(buffer,0,len);
                    }
                    output.flush();
                    input.close();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally{
                try {
                    if(output != null) {
                        output.close();
                        System.out.println("success");
                    }else {
                        System.out.println("fail");
                        sendMessage(11);
                    }
                } catch (IOException e) {
                    System.out.println("fail");
                    e.printStackTrace();
                }
            }
        }


}
