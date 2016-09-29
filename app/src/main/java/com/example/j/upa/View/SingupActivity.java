package com.example.j.upa.View;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.j.upa.DAO.Checker;
import com.example.j.upa.DAO.Server;
import com.example.j.upa.R;

import java.net.URLEncoder;

/**
 * Created by J on 2016-06-06.
 */
public class SingupActivity extends AppCompatActivity {
    String SERVER_ADDRESS = Server.SERVER_ADDRESS;
    EditText edtId, edtPassword, edtPasswordOk, edtCarnum, edtPhonenum;
    Button btnSingup;
    Server server;
    String tResult;
    Checker chek;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singup);
        edtId = (EditText)findViewById(R.id.edtId);
        edtPassword = (EditText)findViewById(R.id.edtPassword);
        edtPasswordOk = (EditText)findViewById(R.id.edtPasswordOk);
        edtCarnum = (EditText) findViewById(R.id.edtCarnum);
        edtPhonenum = (EditText) findViewById(R.id.edtPhonenum);
        btnSingup = (Button)findViewById(R.id.btnSingup);
        server = new Server();
        chek = new Checker();
        btnSingup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (chek.Nullcheck(edtId)) {
                    toast("아이디를 입력하세요");
                    return;
                } else if (chek.Nullcheck(edtPassword)) {
                    toast("비밀번호를 입력하세요");
                    return;
                } else if (!(edtPassword.getText().toString().equals(edtPasswordOk.getText().toString()))) {
                    toast("비밀번호를 확인하세요");
                    return;
                } else if (chek.Nullcheck(edtCarnum)) {
                    toast("차량번호를 입력하세요");
                    return;
                } else if (chek.Nullcheck(edtPhonenum)) {
                    toast("연락처를 입력하세요");
                    return;
                } else {
                    String id = edtId.getText().toString();
                    String password = edtPassword.getText().toString();
                    String carnum = edtCarnum.getText().toString();
                    String phonenum = edtPhonenum.getText().toString();
                    try {
                        tResult = server.Connector(SERVER_ADDRESS + "/Signup.php?"
                                + "id=" + URLEncoder.encode(id, "UTF-8")
                                + "&password=" + URLEncoder.encode(password, "UTF-8")
                                + "&carnum=" + URLEncoder.encode(carnum, "UTF-8")
                                + "&phonenum=" + URLEncoder.encode(phonenum, "UTF-8"));
                        if (tResult.equals("1")) {
                            toast("회원 가입 성공");
                            finish();
                        } else {
                            toast("회원 가입 실패");
                            return;
                        }
                    } catch (Exception ex) {
                        Log.e("Error", ex.getMessage());
                    }
                }
            }
        });
    }
    public void toast(String text){
        Toast.makeText(SingupActivity.this, text, Toast.LENGTH_SHORT).show();
    }


}
