package com.example.android.bluetoothlegatt;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

//DatabaseHelper作为一个访问SQLite的助手类，提供两个方面的功能，
//第一，getReadableDatabase(),getWritableDatabase()可以获得SQLiteDatabse对象，通过该对象可以对数据库进行操作
//第二，提供了onCreate()和onUpgrade()两个回调函数，允许我们在创建和升级数据库时，进行自己的操作

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    //在SQLiteOepnHelper的子类当中，必须有该构造函数
    /**
     *  context:上下文对象
     *  name:数据库名
     */
    public DatabaseHelper(Context context, String name, CursorFactory factory,
                          int version) {
        //必须通过super调用父类当中的构造函数
        super(context, name, factory, version);
        // TODO Auto-generated constructor stub
    }
    public DatabaseHelper(Context context,String name){
        this(context,name,VERSION);
    }
    public DatabaseHelper(Context context,String name,int version){
        this(context, name,null,version);
    }

    //该函数是在第一次创建数据库的时候执行,实际上是在第一次得到SQLiteDatabse对象的时候，才会调用这个方法
    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        System.out.println("create a Database");
        //execSQL函数用于执行SQL语句
        //db.execSQL("create table user(id int,name varchar(20))");
        //建表
//        db.execSQL("create table if not exists userTb (" +
//                "_id integer primary key," +
//                "name text not null,age integer not null," +
//                "sex text not null)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        System.out.println("update a Database");
    }

    public void add_data()
    {

    }

    /**
     * 批量插入(插入1W条数据耗时：1365ms)
     * @param openHelper
     * @param list
     * @return
     */
//    public static boolean insertBySql(SQLiteOpenHelper openHelper,
//            List<RemoteAppInfo> list) {
//        if (null == openHelper || null == list || list.size() <= 0) {
//            return false;
//        }
//        SQLiteDatabase db = null;
//        try {
//            db = openHelper.getWritableDatabase();
//            String sql = "insert into " + RemoteDBHelper.TABLE_APP_REMOTE + "("
//                    + RemoteDBHelper.COL_PKG_NAME + ","// 包名
//                    + RemoteDBHelper.COL_USER_ACCOUNT + ","// 账号
//                    + RemoteDBHelper.COL_APP_SOURCE + ","// 来源
//                    + RemoteDBHelper.COL_SOURCE_UNIQUE + ","// PC mac 地址
//                    + RemoteDBHelper.COL_MOBILE_UNIQUE + ","// 手机唯一标识
//                    + RemoteDBHelper.COL_IMEI + ","// 手机IMEI
//                    + RemoteDBHelper.COL_INSTALL_STATUS + ","// 安装状态
//                    + RemoteDBHelper.COL_TRANSFER_RESULT + ","// 传输状态
//                    + RemoteDBHelper.COL_REMOTE_RECORD_ID // 唯一标识
//                    + ") " + "values(?,?,?,?,?,?,?,?,?)";
//            SQLiteStatement stat = db.compileStatement(sql);
//            db.beginTransaction();
//            for (RemoteAppInfo remoteAppInfo : list) {
//                stat.bindString(1, remoteAppInfo.getPkgName());
//                stat.bindString(2, remoteAppInfo.getAccount());
//                stat.bindLong(3, remoteAppInfo.getFrom());
//                stat.bindString(4, remoteAppInfo.getFromDeviceMd5());
//                stat.bindString(5, remoteAppInfo.getMoblieMd5());
//                stat.bindString(6, remoteAppInfo.getImei());
//                stat.bindLong(7, remoteAppInfo.getInstallStatus());
//                stat.bindLong(8, remoteAppInfo.getTransferResult());
//                stat.bindString(9, remoteAppInfo.getRecordId());
//                long result = stat.executeInsert();
//                if (result < 0) {
//                    return false;
//                }
//            }
//            db.setTransactionSuccessful();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        } finally {
//            try {
//                if (null != db) {
//                    db.endTransaction();
//                    db.close();
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        return true;
//    }

}