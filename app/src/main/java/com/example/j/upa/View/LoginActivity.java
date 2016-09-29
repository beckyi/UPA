package com.example.j.upa.View;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.j.upa.DAO.Checker;
import com.example.j.upa.DAO.Server;
import com.example.j.upa.R;

import java.net.URLEncoder;

public class LoginActivity extends AppCompatActivity {
    String SERVER_ADDRESS = Server.SERVER_ADDRESS;
    EditText edtId, edtPw;
    TextView txtFind, txtSingup;
    CheckBox cbAuto;
    Button btnLogin;
    String id,pw;
    Server server;
    String tResult;
    SharedPreferences setting;
    SharedPreferences.Editor editor;
    Checker chek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        edtId = (EditText) findViewById(R.id.edtId);
        edtPw = (EditText) findViewById(R.id.edtPassword);
        cbAuto = (CheckBox) findViewById(R.id.cbAuto);
        btnLogin = (Button)findViewById(R.id.btnLogin);
        txtSingup = (TextView)findViewById(R.id.txtSingup);

        chek = new Checker();
        server = new Server();

        setting = getSharedPreferences("setting", 0);
        editor = setting.edit();

        if(setting.getBoolean("auto", false)) {
            id = setting.getString("ID", "");
            Toast.makeText(LoginActivity.this,
                    "로그인 성공", Toast.LENGTH_SHORT).show();
            Intent login_intent = new Intent(LoginActivity.this,HomeActivity.class);
            login_intent.putExtra("pageNum","0");
            startActivity(login_intent);
            finish();
        }
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(chek.Nullcheck(edtId)){
                    Toast.makeText(LoginActivity.this, "아이디를 입력하세요", Toast.LENGTH_SHORT).show();
                }else if(chek.Nullcheck(edtPw)){
                    Toast.makeText(LoginActivity.this, "비밀번호를 입력하세요", Toast.LENGTH_SHORT).show();
                }else{
                    id = edtId.getText().toString();
                    pw = edtPw.getText().toString();
                    try {
                        tResult = server.Connector(SERVER_ADDRESS + "/Login.php?"
                                + "id=" + URLEncoder.encode(id, "UTF-8")
                                + "&password=" + URLEncoder.encode(pw, "UTF-8"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if(tResult.equals("1")){
                        Toast.makeText(LoginActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();
                        editor.putString("ID", id);
                        if(cbAuto.isChecked()){
                            editor.putBoolean("auto", true);

                        }else {
                            editor.putBoolean("auto",false);
                        }
                        editor.commit();
                        Intent login_intent = new Intent(LoginActivity.this,HomeActivity.class);
                        login_intent.putExtra("pageNum","0");
                        startActivity(login_intent);
                        finish();
                    }else if(tResult.equals("2")){
                        Toast.makeText(LoginActivity.this, "아이디와 비밀번호가 일치하지 않습니다",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }else{
                        Toast.makeText(LoginActivity.this, "아이디가 존재하지 않습니다",
                                Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
        txtSingup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent login_intent = new Intent(LoginActivity.this, SingupActivity.class);
                startActivity(login_intent);
            }
        });





    }
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        boolean isKill = intent.getBooleanExtra("KILL_APP", false);
        if (isKill) {
            moveTaskToBack(true);
            finish();
        }

    }

}
