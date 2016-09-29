package com.example.j.upa.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.j.upa.DAO.Server;
import com.example.j.upa.R;
import com.example.j.upa.View.ModifyActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by J on 2016-06-08.
 */
public class MypageFragment extends Fragment {
    View view;
    TextView txtUser,txtCarnum,txtPhonenum,txtUPName,txtAddress,txtTime,txtPAname,txtState;
    Button btnEdit;
    Server server;
    String tResult,id,image;
    String SERVER_ADDRESS = Server.SERVER_ADDRESS;
    SharedPreferences setting;
    ImageView imvUser;
    private static final int req = 1;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = (View) inflater.inflate(R.layout.fragment_mypage, container, false);

        setting = getActivity().getSharedPreferences("setting", 0);
        id = setting.getString("ID", "");

        txtUser = (TextView)view.findViewById(R.id.txtUser);
        txtCarnum = (TextView)view.findViewById(R.id.txtCarnum);
        txtPhonenum = (TextView)view.findViewById(R.id.txtPhonenum);
        txtUPName = (TextView)view.findViewById(R.id.txtUPName);
        txtAddress = (TextView)view.findViewById(R.id.txtAddress);
        txtTime = (TextView)view.findViewById(R.id.txtTime);
        txtPAname = (TextView)view.findViewById(R.id.txtPAname);
        txtState = (TextView)view.findViewById(R.id.txtState);
        btnEdit = (Button)view.findViewById(R.id.btnEdit);
        imvUser = (ImageView)view.findViewById(R.id.imvUser);

        txtUser.setText(id);
        server = new Server();
        loadUsr();
        loadUse();
        loadPark();
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent modify_intent = new Intent(getActivity(), ModifyActivity.class);
                getActivity().startActivityForResult(modify_intent, req);
            }
        });
        return view;
    }
    public void loadUsr(){
        try {
            tResult = server.Connector(SERVER_ADDRESS+ "/Loaduser.php?"
                    + "id=" + URLEncoder.encode(id, "UTF-8"));
            JSONArray objects = new JSONArray(tResult);
            JSONObject object = objects.getJSONObject(0);
            txtCarnum.setText(object.getString("carnum"));
            txtPhonenum.setText(object.getString("phonenum"));
            image = object.getString("image");
            imvUser.setImageDrawable(createDrawableFromUrl(SERVER_ADDRESS + "/userImage/" + image));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void loadUse(){
        try {
            tResult = server.Connector(SERVER_ADDRESS + "/Loaduse.php?"
                    + "id=" + URLEncoder.encode(id, "UTF-8"));
            if(tResult.equals("null")){
                txtUPName.setText("사용중인 주차장이 없습니다.");
                txtAddress.setText(" - ");
                txtTime.setText(" - ");
            }else{
                JSONArray objects = new JSONArray(tResult);
                JSONObject object = objects.getJSONObject(0);

                txtUPName.setText(object.getString("name"));
                txtAddress.setText(object.getString("address"));
                txtTime.setText(object.getString("starttime"));
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void loadPark(){
        try {
            tResult = server.Connector(SERVER_ADDRESS + "/Loadmypark.php?"
                    + "id=" + URLEncoder.encode(id, "UTF-8"));
            if(tResult.equals("null")){
                txtPAname.setText("등록하신 주차장이 없습니다.");
                txtState.setText(" - ");
            }else{
                JSONArray objects = new JSONArray(tResult);
                JSONObject object = objects.getJSONObject(0);
                txtPAname.setText(object.getString("name"));
                if(object.getInt("state")==1){
                    txtState.setText("사용 가능");
                }else {
                    txtState.setText("사용 불가");
                }
            }
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
}
