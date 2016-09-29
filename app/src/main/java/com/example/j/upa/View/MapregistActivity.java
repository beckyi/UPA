package com.example.j.upa.View;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
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
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.j.upa.DAO.Server;
import com.example.j.upa.R;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;

public class MapregistActivity extends AppCompatActivity implements View.OnClickListener {
    String SERVER_ADDRESS = Server.SERVER_ADDRESS; //서버 주소(php파일이 저장되어있는 경로까지, 절대로 127.0.0.1이나 localhost를 쓰면 안된다!!)
    EditText edtName, edtAddress;
    String address,tResult;
    Server server;
    double latitude,longitude;
    private ImageView imvPark;
    private static final int PICK_FROM_ALBUM = 1;
    private BitmapFactory.Options bitOption;
    private Uri mImageCaptureUri;
    private Bitmap bit;
    private Button btnReg, btnSTime, btnETime;
    private TextView txtSTime, txtETime;
    private int Shour,Sminute,Ehour,Eminute;
    static final int TIME_DIALOG_ID1=0;
    static final int TIME_DIALOG_ID2=1;
    public String uploadFilePath;
    public String uploadFileName;
    String id;
    SharedPreferences setting;

    private int serverResponseCode = 0;
    // 파일을 업로드 하기 위한 변수 선언

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_regist);
        
        server = new Server();
        Intent Register_intent = getIntent();
        latitude = Register_intent.getDoubleExtra("latitude", 0);
        longitude = Register_intent.getDoubleExtra("longitude", 0);
        address=Register_intent.getStringExtra("address");

        setting = getSharedPreferences("setting", 0);
        id = setting.getString("ID", "");

        edtName = (EditText)findViewById(R.id.edtName);
        edtAddress = (EditText)findViewById(R.id.edtAddress);

        imvPark = (ImageView) findViewById(R.id.imvPark);
        imvPark.setOnClickListener(this);

        txtSTime = (TextView)findViewById(R.id.txtSTime);
        btnSTime = (Button)findViewById(R.id.btnSTime);
        btnSTime.setOnClickListener(this);
        txtETime = (TextView)findViewById(R.id.txtETime);
        btnETime = (Button)findViewById(R.id.btnETime);
        btnETime.setOnClickListener(this);

        btnReg = (Button)findViewById(R.id.btnReg);
        btnReg.setOnClickListener(this);
        edtAddress.setText(address);

        final Calendar c = Calendar.getInstance();
        Shour = c.get(Calendar.HOUR_OF_DAY);
        Sminute = c.get(Calendar.MINUTE);
        Ehour = c.get(Calendar.HOUR_OF_DAY);
        Eminute = c.get(Calendar.MINUTE);

        updateDisplay();

    }

    protected Dialog onCreateDialog(int id){
        switch (id){
            case TIME_DIALOG_ID1:
                return new TimePickerDialog(this,STimeSetListener,Shour,Sminute,false);
            case TIME_DIALOG_ID2:
                return new TimePickerDialog(this,ETimeSetListener,Ehour,Eminute,false);
        }
        return null;
    }

    private void updateDisplay(){
        txtSTime.setText(new StringBuilder().append(pad(Shour)).append(":").append(pad(Sminute)));
        txtETime.setText(new StringBuilder().append(pad(Ehour)).append(":").append(pad(Eminute)));
    }

    private static String pad(int c){
        if(c>=10)
            return String.valueOf(c);
        else
            return "0"+String.valueOf(c);
    }

    private TimePickerDialog.OnTimeSetListener STimeSetListener=
            new TimePickerDialog.OnTimeSetListener(){
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    Shour=hourOfDay;
                    Sminute=minute;
                    updateDisplay();
                }
            };
    private TimePickerDialog.OnTimeSetListener ETimeSetListener=
            new TimePickerDialog.OnTimeSetListener(){
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    Ehour=hourOfDay;
                    Eminute=minute;
                    updateDisplay();
                }
            };





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
                imvPark.setBackground(null);
                imvPark.setImageBitmap(bit);
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
            mProgressDialog = new ProgressDialog(MapregistActivity.this);
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
                    dos.writeBytes("newImage");
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
                            Toast.makeText(MapregistActivity.this, "MalformedURLException", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(MapregistActivity.this, "Got Exception : see logcat ", Toast.LENGTH_SHORT).show();
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
    public void onClick(View v){
        switch (v.getId()) {
            case R.id.imvPark:
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType(MediaStore.Images.Media.CONTENT_TYPE);
                i.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, PICK_FROM_ALBUM);
                break;
            case R.id.btnReg:
                runOnUiThread(new Runnable() {

                    public void run() {

                        if (uploadFilePath != null) {

                            UploadImageToServer uploadimagetoserver = new UploadImageToServer();
                            uploadimagetoserver.execute(SERVER_ADDRESS + "/ImageUploadToServer.php");

                            String start = txtSTime.getText().toString();
                            String end = txtETime.getText().toString();
                            String tradeName = edtName.getText().toString();

                            try {
                                tResult = server.Connector(SERVER_ADDRESS + "/MapRegister.php?"
                                        + "id=" + URLEncoder.encode(id, "UTF-8")
                                        + "&latitude=" + latitude
                                        + "&longitude=" + longitude
                                        + "&name=" + URLEncoder.encode(tradeName, "UTF-8")
                                        + "&address=" + URLEncoder.encode(address, "UTF-8")
                                        + "&starttime=" + start
                                        + "&endtime=" + end
                                        + "&image=" + URLEncoder.encode(uploadFileName, "UTF-8"));

                                if (tResult.equals("1")) {
                                    Toast.makeText(MapregistActivity.this,
                                            "DB insert 성공", Toast.LENGTH_SHORT).show();

                                    edtName.setText("");
                                    edtAddress.setText("");
                                    Intent map_intent = new Intent(getApplicationContext(), HomeActivity.class);
                                    map_intent.putExtra("pageNum","0");
                                    startActivity(map_intent);
                                    finish();

                                } else
                                    Toast.makeText(MapregistActivity.this,
                                            "DB insert 실패", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Log.e("Error", e.getMessage());
                            }
                        } else {
                            Toast.makeText(MapregistActivity.this, "You didn't insert any image", Toast.LENGTH_SHORT).show();
                        }



                    }
                });
                break;
            case R.id.btnSTime:
                showDialog(TIME_DIALOG_ID1);
                break;
            case R.id.btnETime:
                showDialog(TIME_DIALOG_ID2);
                break;
        }
    }
}