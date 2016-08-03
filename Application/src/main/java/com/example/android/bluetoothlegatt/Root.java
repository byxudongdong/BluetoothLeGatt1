package com.example.android.bluetoothlegatt;

/**
 * Created by doyle on 2016/8/3 0003.
 */
import java.util.ArrayList;
import java.util.List;
public class Root
{
    private List<Key> key;

    public void setKey(List<Key> key){
        this.key = key;
    }
    public List<Key> getKey(){
        return this.key;
    }
}
