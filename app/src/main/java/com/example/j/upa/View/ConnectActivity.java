package com.example.j.upa.View;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.j.upa.DAO.BluetoothSerialClient;
import com.example.j.upa.DAO.Server;
import com.example.j.upa.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Set;

/**
 * Created by J on 2016-06-11.
 */
public class ConnectActivity extends AppCompatActivity {
    TextView txtDate,txtName,txtTime,txtFee,txtTotal,txtCharge;
    Button btnBluetooth,btnUse;
    private LinkedList<BluetoothDevice> mBluetoothDevices = new LinkedList<BluetoothDevice>();
    private ArrayAdapter<String> mDeviceArrayAdapter;
    private BluetoothSerialClient mClient;
    private AlertDialog mDeviceListDialog;
    private ProgressDialog mLoadingDialog;
    Server server;
    String tResult,id;
    String SERVER_ADDRESS = Server.SERVER_ADDRESS;
    SharedPreferences setting;
    String starttime,name;
    int devicestate,index;
    Calendar calendar = Calendar.getInstance();
    java.util.Date date = calendar.getTime();
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        Intent get_intent = getIntent();
        index = get_intent.getIntExtra("index", 0);
        name = get_intent.getStringExtra("name");
        devicestate = get_intent.getIntExtra("devicestate", 0);
        setting = getSharedPreferences("setting", 0);
        id = setting.getString("ID", "");

        server = new Server();
        mClient = BluetoothSerialClient.getInstance();
        txtDate = (TextView)findViewById(R.id.txtDate);
        txtName = (TextView)findViewById(R.id.txtName);
        txtTime = (TextView)findViewById(R.id.txtTime);
        txtFee = (TextView)findViewById(R.id.txtFee);
        txtTotal = (TextView)findViewById(R.id.txtTotal);
        txtCharge = (TextView)findViewById(R.id.txtCharge);
        btnBluetooth = (Button)findViewById(R.id.btnBluetooth);
        btnUse = (Button)findViewById(R.id.btnUse);
        txtDate.setText(calendar.get(Calendar.YEAR)+"-"+(calendar.get(Calendar.MONTH)+1)+"-"+calendar.get(Calendar.DATE));
        txtName.setText(name);
        if(index==0){
            //정보 불러옴
            loadHistroy();
            Log.e("출력",Integer.toString(index));
        }


        btnUse.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if(devicestate == 0) {
                    //올리는 작업(현재 사용중)
                    sendStringData("F");

                }else {
                    sendStringData("R");

                }
            }
        });


        btnBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDeviceListDialog.show();
            }
        });

        if(mClient == null) {
            Toast.makeText(getApplicationContext(), "Cannot use the Bluetooth device.", Toast.LENGTH_SHORT).show();
            //finish();
        }
        initProgressDialog();
        initDeviceListDialog();
    }

    public void loadHistroy(){
        try {
            tResult = server.Connector(SERVER_ADDRESS + "/Loadhistory.php?"
                    + "id=" + URLEncoder.encode(id, "UTF-8"));
            if(tResult.equals("null")){
                Toast.makeText(getApplicationContext(),"이용 중인 주차장이 없습니다.",Toast.LENGTH_SHORT).show();
                finish();
            }else{
                JSONArray objects = new JSONArray(tResult);
                JSONObject object = objects.getJSONObject(0);
                index = object.getInt("point");
                starttime = object.getString("starttime");
                txtName.setText(object.getString("name"));
                txtTime.setText(starttime);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onPause() {
        mClient.cancelScan(getApplicationContext());
        super.onPause();
    }
    @Override
    protected void onResume() {
        super.onResume();
        enableBluetooth();
    }
    public void sendStringData(String data) {
        byte[] buffer = data.getBytes();
        mBTHandler.write(buffer);
    }
    private void initProgressDialog() {
        mLoadingDialog = new ProgressDialog(this);
        mLoadingDialog.setCancelable(false);
    }
    private void enableBluetooth() {
        BluetoothSerialClient btSet =  mClient;
        btSet.enableBluetooth(this, new BluetoothSerialClient.OnBluetoothEnabledListener() {
            @Override
            public void onBluetoothEnabled(boolean success) {
                if (success) {
                    getPairedDevices();
                } else {
                    finish();
                }
            }
        });
    }
    private void getPairedDevices() {
        Set<BluetoothDevice> devices =  mClient.getPairedDevices();
        for(BluetoothDevice device: devices) {
            addDeviceToArrayAdapter(device);
        }
    }
    private void initDeviceListDialog() {
        mDeviceArrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.devicelist);
        ListView listView = new ListView(getApplicationContext());
        listView.setAdapter(mDeviceArrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item =  (String) parent.getItemAtPosition(position);
                for(BluetoothDevice device : mBluetoothDevices) {
                    if(item.contains(device.getAddress())) {
                        connect(device);
                        mDeviceListDialog.cancel();
                    }
                }
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select bluetooth device");
        builder.setView(listView);
        builder.setPositiveButton("Scan",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        scanDevices();
                    }
                });
        mDeviceListDialog = builder.create();
        mDeviceListDialog.setCanceledOnTouchOutside(false);
    }

    private void scanDevices() {
        BluetoothSerialClient btSet = mClient;
        btSet.scanDevices(getApplicationContext(), new BluetoothSerialClient.OnScanListener() {
            String message = "";

            @Override
            public void onStart() {
                Log.d("Test", "Scan Start.");
                mLoadingDialog.show();
                message = "Scanning....";
                mLoadingDialog.setMessage("Scanning....");
                mLoadingDialog.setCancelable(true);
                mLoadingDialog.setCanceledOnTouchOutside(false);
                mLoadingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        BluetoothSerialClient btSet = mClient;
                        btSet.cancelScan(getApplicationContext());
                    }
                });
            }

            @Override
            public void onFoundDevice(BluetoothDevice bluetoothDevice) {
                addDeviceToArrayAdapter(bluetoothDevice);
                message += "\n" + bluetoothDevice.getName() + "\n" + bluetoothDevice.getAddress();
                mLoadingDialog.setMessage(message);
            }

            @Override
            public void onFinish() {
                Log.d("Test", "Scan finish.");
                message = "";
                mLoadingDialog.cancel();
                mLoadingDialog.setCancelable(false);
                mLoadingDialog.setOnCancelListener(null);
                mDeviceListDialog.show();
            }
        });
    }
    private void addDeviceToArrayAdapter(BluetoothDevice device) {
        if(mBluetoothDevices.contains(device)) {
            mBluetoothDevices.remove(device);
            mDeviceArrayAdapter.remove(device.getName() + "\n" + device.getAddress());
        }
        mBluetoothDevices.add(device);
        mDeviceArrayAdapter.add(device.getName() + "\n" + device.getAddress());
        mDeviceArrayAdapter.notifyDataSetChanged();

    }
    private void connect(BluetoothDevice device) {
        mLoadingDialog.setMessage("Connecting....");
        mLoadingDialog.setCancelable(false);
        mLoadingDialog.show();
        BluetoothSerialClient btSet =  mClient;
        btSet.connect(getApplicationContext(), device, mBTHandler);
    }


    private BluetoothSerialClient.BluetoothStreamingHandler mBTHandler = new BluetoothSerialClient.BluetoothStreamingHandler() {
        ByteBuffer mmByteBuffer = ByteBuffer.allocate(1024);

        @Override
        public void onError(Exception e) {
            mLoadingDialog.cancel();
            showToast("Messgae : Connection error - " + e.toString());
        }

        @Override
        public void onDisconnected() {
            mLoadingDialog.cancel();
            showToast("Messgae : Disconnected.");
        }
        private void showToast(final String text) {
            runOnUiThread(new Runnable() {

                public void run() {
                    Toast.makeText(ConnectActivity.this, text, Toast.LENGTH_SHORT).show();
                }
            });
        }


        @Override
        public void onData(byte[] buffer, int length) {
            if(length == 0) return;
            if(mmByteBuffer.position() + length >= mmByteBuffer.capacity()) {
                ByteBuffer newBuffer = ByteBuffer.allocate(mmByteBuffer.capacity() * 2);
                newBuffer.put(mmByteBuffer.array(), 0,  mmByteBuffer.position());
                mmByteBuffer = newBuffer;
            }
            mmByteBuffer.put(buffer, 0, length);
//            String state = mmByteBuffer.toString();

            String state = new String(mmByteBuffer.array(), 0, mmByteBuffer.position());
            mmByteBuffer.clear();
            if(state.equals("F")){
                runOnUiThread(new Runnable() {

                    public void run() {
                        // TODO Auto-generated method stub
                        try {
                            tResult = server.Connector(SERVER_ADDRESS + "/DeviceUP.php?"
                                    + "index=" + index
                                    + "&useid=" + URLEncoder.encode(id, "UTF-8"));
                            if (tResult.equals("1")) { //result 태그값이 1일때 성공
                                Toast.makeText(ConnectActivity.this,
                                        "금지대 상승 성공", Toast.LENGTH_SHORT).show();
                                Intent map_intent = new Intent(getApplicationContext(), HomeActivity.class);
                                map_intent.putExtra("pageNum","0");
                                startActivity(map_intent);
                                finish();

                            } else //result 태그값이 1이 아닐때 실패
                                Toast.makeText(ConnectActivity.this,
                                        "금지대 상승 실패", Toast.LENGTH_SHORT).show();

                        } catch (Exception e) {
                            Log.e("Error", e.getMessage());
                        }
                    }
                });
            }else if(state.equals("R")){

                runOnUiThread(new Runnable() {
                    public void run() {

                        // TODO Auto-generated method stub

                        try {
                            tResult = server.Connector(SERVER_ADDRESS + "/DeviceDown.php?"
                                    + "index=" + index
                                    + "&useid=" + URLEncoder.encode(id, "UTF-8"));
                            if (tResult.equals("1")) { //result 태그값이 1일때 성공
                                Toast.makeText(ConnectActivity.this,
                                        "금지대 하강 성공", Toast.LENGTH_SHORT).show();
                                Intent map_intent = new Intent(getApplicationContext(), HomeActivity.class);
                                map_intent.putExtra("pageNum","0");
                                startActivity(map_intent);
                                finish();
                            } else //result 태그값이 1이 아닐때 실패
                                Toast.makeText(ConnectActivity.this,
                                        "금지대 하강 실패", Toast.LENGTH_SHORT).show();

                        } catch (Exception e) {
                            Log.e("Error", e.getMessage());
                        }
                    }
                });

            }


        }
        @Override
        public void onConnected() {
            showToast("Messgae : Connected. " + mClient.getConnectedDevice().getName());
            mLoadingDialog.cancel();
        }
    };
    protected void onDestroy() {
        super.onDestroy();
        mClient.claer();
    };


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
