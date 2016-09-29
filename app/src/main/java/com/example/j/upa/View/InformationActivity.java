package com.example.j.upa.View;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.j.upa.DAO.Server;
import com.example.j.upa.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by J on 2016-06-08.
 */
public class InformationActivity extends AppCompatActivity {
    String SERVER_ADDRESS = Server.SERVER_ADDRESS;
    double longitude,latitude,plongitude,platitude;
    String address,name,master,starttime,endtime,parkimage;
    int index;
    private int devicestate;
    ImageView imvPark;
    Button btnUse,btnNavi;
    TextView txtName,txtAddress,txtID,txtFee,txtState,txtTime;
    boolean usefultime;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        final Intent intent_get = getIntent();
        master = intent_get.getStringExtra("master");
        index = intent_get.getIntExtra("index", 0);
        address = intent_get.getStringExtra("address");
        name = intent_get.getStringExtra("tradeName");
        longitude = intent_get.getDoubleExtra("longitude", 0);
        latitude = intent_get.getDoubleExtra("latitude", 0);
        plongitude = intent_get.getDoubleExtra("plongitude",0);
        platitude = intent_get.getDoubleExtra("platitude", 0);
        starttime = intent_get.getStringExtra("starttime");
        endtime = intent_get.getStringExtra("endtime");
        devicestate = intent_get.getIntExtra("devicestate", 0);
        usefultime=intent_get.getBooleanExtra("usefultime", false);
        parkimage = intent_get.getStringExtra("parkimage");

        txtID = (TextView)findViewById(R.id.txtID);
        txtAddress = (TextView) findViewById(R.id.txtAddress);
        txtName = (TextView) findViewById(R.id.txtName);
        txtTime = (TextView) findViewById(R.id.txtTime);
        txtState = (TextView) findViewById(R.id.txtState);
        imvPark=(ImageView) findViewById(R.id.imvPark);
        btnUse = (Button)findViewById(R.id.btnUse);
        btnNavi = (Button)findViewById(R.id.btnNavi);

        imvPark.setImageDrawable(createDrawableFromUrl(SERVER_ADDRESS + "/newImage/" + parkimage));
        txtAddress.setText(address);
        txtID.setText(master);
        txtName.setText(name);
        txtTime.setText(starttime.substring(0, 5) + "~" + endtime.substring(0, 5));

        if (usefultime==true && devicestate ==1) {   //주차 가능시간이고 장비가 올라간상태
            txtState.setText("주차가능");

        }else
            txtState.setText("주차불가"); //주차 불가능시간이고 장비가 내려간상태

        btnUse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(devicestate==1){
                    Intent use_intent = new Intent(InformationActivity.this,ConnectActivity.class);
                    use_intent.putExtra("index",index);
                    use_intent.putExtra("name",name);
                    use_intent.putExtra("devicestate",devicestate);
                    startActivity(use_intent);
                    finish();

                }else {
                    Toast.makeText(getApplicationContext(),"주차가 불가능 합니다.",Toast.LENGTH_SHORT).show();
                }

            }
        });
        btnNavi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "daummaps://route?sp=" +
                        platitude+","+plongitude +
                        "&ep=" +
                        latitude+","+longitude +
                        "&by=CAR";
                Intent navi_intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(navi_intent);
            }
        });
    }

    private Drawable createDrawableFromUrl(String url) {
        try {
            InputStream is = (InputStream) this.fetch(url);
            Drawable d = Drawable.createFromStream(is, "src");
            return d;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    private Object fetch(String address) throws MalformedURLException,IOException {
        URL url = new URL(address);
        Object content = url.getContent();
        return content;
    }
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode==KeyEvent.KEYCODE_BACK){
            Intent map_intent = new Intent(getApplicationContext(), HomeActivity.class);
            map_intent.putExtra("pageNum","0");
            startActivity(map_intent);
            finish();
        }
        return false;
    }
}
