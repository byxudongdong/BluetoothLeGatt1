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
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static Object object = new Object();
    Thread mthread;
    Boolean updateFlag = false;
    String newtime;
    public Button upDateButton;
    public EditText updateState;

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
//                byte[] bytes = {0x33,0x34,0x36,0x34,0x39};
//                Boolean bool = writecharacteristic.setValue(bytes);
//                BluetoothLeService.writeCharacteristic( writecharacteristic);
//                if(bool) {
//                    PrintLog.printHexString("写特征值：", writecharacteristic.getValue());
//                }else{
//                    Log.i("写特征值：", "写失败");
//                }
//                //读数据的服务和characteristic
               //readMnotyGattService = mBluetoothLeService.getSupportedGattService(UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb"));
               // readCharacteristic = readMnotyGattService.getCharacteristic(UUID.fromString("0000ffe4-0000-1000-8000-00805f9b34fb"));
            }
            //显示数据
            else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                //将数据显示在mDataField上
                //Log.i("显示接受数据","将接受数据显示在mDataField上");
                byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                //System.out.println("接收到data----" + data);
                PrintLog.printHexString("接收到data----",data);
                displayData(PrintLog.returnHexString(data));
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }

            else if(BluetoothLeService.EXTRA_DATA.equals(action))
            {
                Log.i("显示EXTRA_DATA","EXTRA_DATA");
            }
            else if(BluetoothLeService.WRITE_STATUS.equals(action))
            {
                WriteCharacterRspFlag = true;
                Log.i("写数据结果","回应成功");
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

        upDateButton = (Button)findViewById(R.id.updateButton);
        updateState = (EditText) findViewById(R.id.updateState);

        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        upDateButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Log.i("开始升级", "button onClick");
                upDateButton.setClickable(false);
                updateFlag = true;
                mthread =new Thread(sendData );
                mthread.start();
            }
        });
    }

    public final int UpdateStepSendRequst = 0,
                    UpdateStepWaitRequestRes = 1,
                    UpdateStepSendImage = 2,
                    UpdateStepWaitImageRes = 3,
                    UpdateStepWaitCRCRes = 4,
                    UpdateStepCRCResRecv = 5;
    public int sendLen=0,filedataLen=0,updateIdex= 0,update_step = UpdateStepSendRequst;
    public long startTime=0,consumingTime=0;  //開始時間
    FileInputStream fin = null;
    byte [] buffer = null;
    String fileName = "classes.dex";
    Boolean receiveDataFlag = false;
    public static Boolean WriteCharacterRspFlag = false;

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

//    		int hour = t.hour; // 0-23
//    		int minute = t.minute;
//    		int second = t.second;
            newtime = String.valueOf(year)
                    +"-"+String.format("%02d",month)
                    +"-"+String.format("%02d",date);

            //读SD中的文件

            try{
                String filePath = UpdateOpt.getSdCardPath() + "/classes.dex";
                fin = new FileInputStream(filePath);
                filedataLen = fin.available();
                buffer = new byte[98];

            } catch(Exception e){
                e.printStackTrace();
            }

            while(updateFlag)
            {
                byte[] bytes = UpdateOpt.wakeupData;        //写入发送数据
                Boolean bool = UpdateOpt.WriteComm( writecharacteristic, bytes, bytes.length);
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
                //发送唤醒
                if(update_step == 0)
                {
                    UpdateOpt.WriteComm(writecharacteristic, bytes, bytes.length);
                    try{
                        Thread.sleep(50);
                    }catch( InterruptedException e){
                        Log.i("等待延时：", "wait...");
                    }
                }
                update_step = update_Switch();
                try {
                    sendLen = fin.read(buffer,updateIdex * 98 ,98);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(sendLen > 0)
                {
                    UpdateOpt.WriteComm(writecharacteristic, buffer, sendLen);
                }
                if(update_step == 5)
                {
                    //升级完成后
                    updateFlag = false;
                    break;
                }

                updateFlag = false;
            }

            try {
                fin.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Message message = new Message();
            message.what = 0;
            handler.sendMessage(message);

            //Log.i("使能按键：", "wait...");
            upDateButton.setClickable(true);
        }
    };

    public int update_Switch()
    {
        int res = -1;
        startTime = System.currentTimeMillis();  //開始時間

        switch (update_step)
        {
            case UpdateStepSendRequst:
                //发送升级请求
                sendMessage( 2 );
                update_sendUpdateReq();
                //update_sendSize = 0;
                update_step++;
                consumingTime = startTime;
                break;
            case UpdateStepSendImage:
                sendMessage( 3 );
                break;
            case UpdateStepWaitRequestRes:

                break;
            case UpdateStepWaitImageRes:

                break;
            case UpdateStepWaitCRCRes:

                break;
            case UpdateStepCRCResRecv:

                break;
        }

        return res;
    }

    final Handler handler=new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch(msg.what){
                case 0:
                    Toast.makeText(getApplicationContext(), "升级成功！！！", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    //Toast.makeText(getApplicationContext(), "收到回应", Toast.LENGTH_SHORT).show();
                    receiveDataFlag = true;
                    updateState.setText("收到回应");

                    synchronized(object)
                    {
                        Log.i("解锁通知：", "wait...");
                        object.notify(); // 恢复线程
                    }

                    break;
                case 2:
                    updateState.setText("发送升级请求");
                    break;
            }
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
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
        }
        return super.onOptionsItemSelected(item);
    }

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
    public static byte[] readFileSdcardFile(String fileName) throws IOException{
        String res="";
        byte [] buffer = null;
        try{
            FileInputStream fin = new FileInputStream(UpdateOpt.getSdCardPath() + fileName);
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
        temp[len] = (byte)0xFD;
        temp[len+1] = (byte)0xFF;
        len += 2;
        //memcpy(&temp[len], &tUpdate_info.hw_info, 4);
        len += 4;
        //memcpy(&temp[len], &tUpdate_info.image_size, 4);
        len += 4;
        //memcpy(&temp[len], &tUpdate_info.image_crc, 4);
        len += 4;

        //comm_send(COMM_TRANS_TYPE_SEND, COMM_CMD_TYPE_UPDATE, &temp[1], len-1);//只为唤醒目标机
        //Delay(50);
        //comm_send(COMM_TRANS_TYPE_SEND, COMM_CMD_TYPE_UPDATE, temp, len);
    }
}
