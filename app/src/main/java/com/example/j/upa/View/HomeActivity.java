package com.example.j.upa.View;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.j.upa.Adapter.PageAdapter;
import com.example.j.upa.Adapter.SideAdapter;
import com.example.j.upa.DAO.NonSwipeViewPager;
import com.example.j.upa.DAO.Server;
import com.example.j.upa.DTO.SideItem;
import com.example.j.upa.Fragment.HistoryFragment;
import com.example.j.upa.Fragment.MapFragment;
import com.example.j.upa.Fragment.MypageFragment;
import com.example.j.upa.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by J on 2016-06-01.
 */
public class HomeActivity  extends AppCompatActivity {
    NonSwipeViewPager viewPager;
    PageAdapter pageAdapter;
    Intent intent;
    ImageView imvMenu,imvLogout;
    DrawerLayout dlDrawer;
    ListView listView;
    SideAdapter sideAdapter;
    SharedPreferences setting;
    SharedPreferences.Editor editor;
    String id,tResult,image;
    String SERVER_ADDRESS = Server.SERVER_ADDRESS;
    Server server = new Server();
    private TextView txtTitle,txtCursor,txtId;
    private ImageView imvSearch, imvGps, imvUser;
    private EditText edtSearch;

    private ArrayList<SideItem> arrayList;
    int[] side_icons={R.drawable.sidemenu_map,R.drawable.sidemenu_mypage,R.drawable.sidemenu_history,R.drawable.sidemenu_bluethooth};
    String[] side_txts = {"주차장검색","마이페이지","사용내역","사용중"};
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewPager = (NonSwipeViewPager)findViewById(R.id.viewPager);
        dlDrawer = (DrawerLayout)findViewById(R.id.fragdlDrawer);
        listView = (ListView)findViewById(R.id.listView);
        dlDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        setting = getSharedPreferences("setting", 0);
        id = setting.getString("ID", "");

        txtId = (TextView)findViewById(R.id.txtId);
        edtSearch = (EditText)findViewById(R.id.edtSearch);
        txtTitle = (TextView)findViewById(R.id.txtTitle);
        imvSearch = (ImageView)findViewById(R.id.imvSearch);
        imvGps = (ImageView)findViewById(R.id.imvGps);
        imvUser = (ImageView)findViewById(R.id.imvUser);
        txtCursor = (TextView)findViewById(R.id.txtCursor);
        txtId.setText(id);
        loadUsr();
        imvUser.setImageDrawable(createDrawableFromUrl(SERVER_ADDRESS + "/userImage/" + image));
        intent = getIntent();
        final String strPosition = intent.getStringExtra("pageNum");

        imvMenu = (ImageView)findViewById(R.id.imvMenu);
        imvLogout = (ImageView)findViewById(R.id.imvLogout);
        setupViewpager(viewPager);
        arrayList = new ArrayList<SideItem>();
        sideAdapter = new SideAdapter(getApplicationContext(), R.layout.side_bar_item, arrayList);

        listView.setAdapter(sideAdapter);
        sideInit();
        viewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        imvMenu.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                dlDrawer.openDrawer(GravityCompat.START);

            }
        });
        imvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent logout_intent = new Intent(HomeActivity.this,LoginActivity.class);
                setting = getSharedPreferences("setting", 0);
                editor = setting.edit();
                editor.putString("ID", "");
                editor.putBoolean("auto",false);
                editor.commit();
                startActivity(logout_intent);
                finish();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                switch (position) {
                    case 0:
                        toolbarSet(position);
                        pageAdapter.notifyDataSetChanged();
                        viewPager.setCurrentItem(position);
                        dlDrawer.closeDrawer(GravityCompat.START);
                        break;
                    case 1:
                        toolbarSet(position);
                        pageAdapter.notifyDataSetChanged();
                        viewPager.setCurrentItem(position);
                        dlDrawer.closeDrawer(GravityCompat.START);
                        break;
                    case 2:
                        toolbarSet(position);
                        pageAdapter.notifyDataSetChanged();
                        viewPager.setCurrentItem(position);
                        dlDrawer.closeDrawer(GravityCompat.START);
                        break;
                    case 3:
                        toolbarSet(position);
                        Intent connect_intent = new Intent(HomeActivity.this,ConnectActivity.class);
                        startActivity(connect_intent);
                        break;
                }
            }
        });

        setCurrentPage(strPosition);
    }
    public void setCurrentPage(String str)
    {
        int position = Integer.parseInt(str);
        toolbarSet(position);
        viewPager.setCurrentItem(position);

    }
    public void setupViewpager(ViewPager viewPager)
    {
        pageAdapter = new PageAdapter(getApplicationContext(), getSupportFragmentManager());
        pageAdapter.addFragment(new MapFragment());
        pageAdapter.addFragment(new MypageFragment());
        pageAdapter.addFragment(new HistoryFragment());
        viewPager.setAdapter(pageAdapter);
    }
    public void sideInit()
    {
        for(int i=0;i<side_icons.length;i++)
        {
            SideItem sideItem = new SideItem();
            sideItem.setItemImg(side_icons[i]);
            sideItem.setItemTxt(side_txts[i]);
            arrayList.add(sideItem);
        }

        sideAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        int position = viewPager.getCurrentItem();
        if(position==0){
            if(keyCode== KeyEvent.KEYCODE_BACK){
                AlertDialog.Builder alerDlg = new AlertDialog.Builder(this);
                alerDlg.setMessage("종료 하시겠습니까?");

                alerDlg.setNegativeButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                alerDlg.setPositiveButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog alert = alerDlg.create();
                alert.setTitle("UPA");
                alert.setIcon(R.drawable.ic_launcher);
                alert.show();
            }
        }else {
            toolbarSet(0);
            viewPager.setCurrentItem(0);
            dlDrawer.closeDrawer(GravityCompat.START);
        }

        return false;
    }
    public void toolbarSet(int page){
        if(page==0){
            txtTitle.setVisibility(View.GONE);
            edtSearch.setVisibility(View.VISIBLE);
            imvSearch.setVisibility(View.VISIBLE);
            imvGps.setVisibility(View.VISIBLE);
            txtCursor.setVisibility(View.VISIBLE);
        }else {
            txtTitle.setVisibility(View.VISIBLE);
            edtSearch.setVisibility(View.GONE);
            imvSearch.setVisibility(View.GONE);
            imvGps.setVisibility(View.GONE);
            txtCursor.setVisibility(View.GONE);
        }

    }
    public void loadUsr(){
        try {
            tResult = server.Connector(SERVER_ADDRESS+ "/Loaduser.php?"
                    + "id=" + URLEncoder.encode(id, "UTF-8"));
            JSONArray objects = new JSONArray(tResult);
            JSONObject object = objects.getJSONObject(0);
            image=object.getString("image");
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode==RESULT_OK){
            Log.e("출력", Integer.toString(data.getIntExtra("req", 0)));
            pageAdapter.notifyDataSetChanged();
        }
    }
}
