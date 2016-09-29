package com.example.j.upa.View;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.j.upa.DAO.Checker;
import com.example.j.upa.DAO.Server;
import com.example.j.upa.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by J on 2016-06-08.
 */
public class ModifyActivity extends AppCompatActivity{
    TextView txtName;
    EditText edtPasswd, edtrePasswd, edtCarnum, edtPhone;
    ImageView imvUser;
    Button btnModify;
    Server server;
    Checker chek;
    private Uri mImageCaptureUri;
    private Bitmap bit;
    String tResult,id;
    String SERVER_ADDRESS = Server.SERVER_ADDRESS;
    SharedPreferences setting;
    private static final int PICK_FROM_ALBUM = 1;
    public String uploadFilePath;
    public String uploadFileName;
    private int serverResponseCode = 0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify);
        server = new Server();
        chek = new Checker();
        setting = getSharedPreferences("setting", 0);
        id = setting.getString("ID", "");
        txtName = (TextView)findViewById(R.id.txtName);
        edtPasswd = (EditText)findViewById(R.id.edtPasswd);
        edtrePasswd = (EditText)findViewById(R.id.edtrePasswd);
        edtCarnum = (EditText)findViewById(R.id.edtCarnum);
        edtPhone = (EditText)findViewById(R.id.edtPhone);
        btnModify = (Button)findViewById(R.id.btnModify);
        imvUser = (ImageView)findViewById(R.id.imvUser);

        txtName.setText(id);
        loadUsr();

        imvUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType(MediaStore.Images.Media.CONTENT_TYPE);
                i.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, PICK_FROM_ALBUM);
            }
        });
        btnModify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runOnUiThread(new Runnable() {

                    public void run() {

                        if (uploadFilePath != null) {

                            UploadImageToServer uploadimagetoserver = new UploadImageToServer();
                            uploadimagetoserver.execute(SERVER_ADDRESS + "/ImageUploadToServer.php");
                            try {
                                server.Connector(SERVER_ADDRESS + "/ModifyImage.php?"
                                        + "id=" + URLEncoder.encode(id, "UTF-8")
                                        + "&image=" + URLEncoder.encode(uploadFileName, "UTF-8"));
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                if(chek.Nullcheck(edtPasswd)){
                    toast("비밀번호를 입력하세요");
                } else if (!(edtPasswd.getText().toString().equals(edtrePasswd.getText().toString()))) {
                    toast("비밀번호를 확인하세요");
                }else if(chek.Nullcheck(edtPhone)){
                    toast("연락처를 입력하세요");
                }else if(chek.Nullcheck(edtCarnum)){
                    toast("차량번호를 입력하세요");
                }else {
                    String password = edtPasswd.getText().toString();
                    String carnum = edtCarnum.getText().toString();
                    String phonenum = edtPhone.getText().toString();
                    try {
                        tResult = server.Connector(SERVER_ADDRESS + "/Modify.php?"
                                + "id=" + URLEncoder.encode(id, "UTF-8")
                                + "&password=" + URLEncoder.encode(password, "UTF-8")
                                + "&carnum=" + URLEncoder.encode(carnum, "UTF-8")
                                + "&phonenum=" + URLEncoder.encode(phonenum, "UTF-8"));
                        if (tResult.equals("1")) {
                            toast("회원 정보 수정");
                            Intent modify_intent = new Intent();
                            modify_intent.putExtra("req",2);
                            setResult(RESULT_OK,modify_intent);
                            finish();
                        } else {
                            toast("수정 실패");
                            return;
                        }
                    } catch (Exception ex) {
                        Log.e("Error", ex.getMessage());
                    }
                }

            }
        });

    }
    public void loadUsr(){
        try {
            tResult = server.Connector(SERVER_ADDRESS+ "/Loaduser.php?"
                    + "id=" + URLEncoder.encode(id, "UTF-8"));
            JSONArray objects = new JSONArray(tResult);
            JSONObject object = objects.getJSONObject(0);
            edtCarnum.setText(object.getString("carnum"));
            edtPhone.setText(object.getString("phonenum"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void toast(String text){
        Toast.makeText(ModifyActivity.this, text, Toast.LENGTH_SHORT).show();
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(resultCode != RESULT_OK)
        {
            return;
        }

        switch(requestCode)
        {
            case PICK_FROM_ALBUM:
            {
                mImageCaptureUri = data.getData();
                String path = getPath(mImageCaptureUri);
                String name = getName(mImageCaptureUri);
                uploadFilePath = path;
                uploadFileName = name;
                bit = BitmapFactory.decodeFile(uploadFilePath);
                imvUser.setBackground(null);
                imvUser.setImageBitmap(bit);
                break;
            }

        }
    }
    private String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
    private String getName(Uri uri) {
        String[] projection = {MediaStore.Images.ImageColumns.DISPLAY_NAME};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DISPLAY_NAME);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
    private class UploadImageToServer extends AsyncTask<String, String, String> {
        ProgressDialog mProgressDialog;
        String fileName = uploadFilePath;
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 10240 * 10240;
        File sourceFile = new File(uploadFilePath);

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(ModifyActivity.this);
            mProgressDialog.setTitle("Loading...");
            mProgressDialog.setMessage("Image uploading...");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... serverUrl) {
            if (!sourceFile.isFile()) {
                runOnUiThread(new Runnable() {
                    public void run() {
                    }
                });
                return null;
            } else {
                try {
                    // open a URL connection to the Servlet
                    FileInputStream fileInputStream = new FileInputStream(sourceFile);
                    URL url = new URL(serverUrl[0]);

                    // Open a HTTP  connection to  the URL
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true); // Allow Inputs
                    conn.setDoOutput(true); // Allow Outputs
                    conn.setUseCaches(false); // Don't use a Cached Copy
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    conn.setRequestProperty("uploaded_file", fileName);

                    dos = new DataOutputStream(conn.getOutputStream());

                    // 사용자 이름으로 폴더를 생성하기 위해 사용자 이름을 서버로 전송한다.
                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"data1\"" + lineEnd);
                    dos.writeBytes(lineEnd);
                    dos.writeBytes("userImage");
                    dos.writeBytes(lineEnd);

                    // 이미지 전송
                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\"; filename=\"" + fileName + "\"" + lineEnd);
                    dos.writeBytes(lineEnd);

                    // create a buffer of  maximum size
                    bytesAvailable = fileInputStream.available();

                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    // read file and write it into form...
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {
                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    }

                    // send multipart form data necesssary after file data...
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    // Responses from the server (code and message)
                    serverResponseCode = conn.getResponseCode();
                    String serverResponseMessage = conn.getResponseMessage();


                    if (serverResponseCode == 200) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                            }
                        });
                    }
                    //close the streams //
                    fileInputStream.close();
                    dos.flush();
                    dos.close();
                    fileInputStream.close();
                    conn.disconnect();

                } catch (MalformedURLException ex) {
                    ex.printStackTrace();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(ModifyActivity.this, "MalformedURLException", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(ModifyActivity.this, "Got Exception : see logcat ", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                return null;
            } // End else block
        }

        @Override
        protected void onPostExecute(String s) {
        }
    }

}
