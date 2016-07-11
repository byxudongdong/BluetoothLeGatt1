package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;

/**
 * Created by byxdd on 2016/7/4 0004.
 */
public class ProgressTest extends Activity {

    private Button btn_go = null;
    private MyProgress myProgress = null;
    private Handler mHandler;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);
        findView();
        setParam();
        addListener();

        mHandler =  new Handler(new Handler.Callback() {

            @Override
            public boolean handleMessage(Message msg) {
                // TODO Auto-generated method stub
                myProgress.setProgress(msg.what);
                return false;
            }
        });

    }

    private void findView(){
        btn_go = (Button) findViewById(R.id.updateButton);
        myProgress = (MyProgress) findViewById(R.id.pgsBar);
    }

    private void setParam(){
        btn_go.setText("开始");
    }
    private void addListener(){
        btn_go.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        for(int i = 0;i <=50; i++){
                            mHandler.sendEmptyMessage(i * 2);
                            try {
                                Thread.sleep(80);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            }
        });
    }
}
