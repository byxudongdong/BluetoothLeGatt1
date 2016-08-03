package com.example.android.bluetoothlegatt;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * Created by doyle on 2016/8/2 0002.
 */
//异步获取信息
class Loadhtml extends AsyncTask<String, String, String>
{
    DatabaseHelper helper;
    SQLiteDatabase usedatabase;
    ProgressDialog bar;
    Document doc;
    @Override
    protected String doInBackground(String... params) {
        // TODO Auto-generated method stub
        try {
            doc = Jsoup.connect(Constans.NetAddress).timeout(5000).post();
            Document content = Jsoup.parse(doc.toString());
            Elements divs = content.select("#siteNav");
            Document divcontions = Jsoup.parse(divs.toString());
            Elements element = divcontions.getElementsByTag("li");
            Log.d("element", element.toString());
            for(Element links : element)
            {
                String title = links.getElementsByTag("a").text();

                String link   = links.select("a").attr("href").replace("/", "").trim();
                String url  = Constans.NetAddress+link;
                ContentValues values = new ContentValues();
                values.put("Title", title);
                values.put("Url", url);

                String sql = "insert into files (Title,Url)values(?,?)";
                //创建一个SQLiteHelper对象

                helper = new DatabaseHelper(null, "usedatabase" + ".db");
                //使用getWritableDatabase()或getReadableDatabase()方法获得SQLiteDatabase对象
                usedatabase = helper.getWritableDatabase();
                //usedatabase.execSQL(sql, new Object[] { stu.name, stu.age });

                usedatabase.insert("Cach",null, values);
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        // TODO Auto-generated method stub
        super.onPostExecute(result);
//            Log.d("doc", doc.toString().trim());
        bar.dismiss();
//        ListItemAdapter adapter = new ListItemAdapter(context, usedatabase.getlist());
//        listmenu.setAdapter(adapter);
    }

    @Override
    protected void onPreExecute() {
        // TODO Auto-generated method stub
        super.onPreExecute();

//        bar = new ProgressDialog(context);
        bar.setMessage("正在加载数据····");
        bar.setIndeterminate(false);
        bar.setCancelable(false);
        bar.show();
    }

}
