package com.example.j.upa.Fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.example.j.upa.Adapter.ExpandableAdapter;
import com.example.j.upa.DAO.Server;
import com.example.j.upa.DTO.ChildItem;
import com.example.j.upa.DTO.ExpandableItem;
import com.example.j.upa.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by S403 on 2016-06-09.
 */
public class HistoryFragment extends Fragment {
    View view;
    ExpandableListView elvHistory;
    ArrayList<ExpandableItem> groupList = null;
    ArrayList<ArrayList<ChildItem>> childList = null;
    ArrayList<ChildItem> childListContent = null;
    ExpandableAdapter adapter=null;
    Server server = new Server();
    String tResult, id;
    String SERVER_ADDRESS = Server.SERVER_ADDRESS;
    SharedPreferences setting;
    private static final int req = 1;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = (View) inflater.inflate(R.layout.fragment_history, container, false);
        elvHistory = (ExpandableListView)view.findViewById(R.id.elvHistory);
        setting = getActivity().getSharedPreferences("setting", 0);
        id = setting.getString("ID", "");
        groupList = new ArrayList<ExpandableItem>();

        adapter = new ExpandableAdapter();
        elvHistory.setAdapter(adapter);
        initGroup();
        return view;
    }

    public void initGroup()
    {
        try {
            Log.e("출력",SERVER_ADDRESS + "/Loadhistorylist.php?"
                    + "id=" + URLEncoder.encode(id, "UTF-8"));
            tResult = server.Connector(SERVER_ADDRESS + "/Loadhistorylist.php?"
                    + "id=" + URLEncoder.encode(id, "UTF-8"));
            if(tResult.equals("null")){
                Toast.makeText(getActivity(), "이용 내역이 없습니다.", Toast.LENGTH_SHORT).show();
            }else{
                JSONArray objects = new JSONArray(tResult);
                for (int i = 0; i < objects.length(); i++) {
                    JSONObject object = objects.getJSONObject(i);
                    ExpandableItem gitem = new ExpandableItem();
                    gitem.setName(object.getString("name"));
                    gitem.setTime(object.getString("starttime"));
                    ChildItem citem = new ChildItem();
                    citem.setStrAddress(object.getString("address"));
                    citem.setStrTime(object.getString("starttime"));
                    citem.setStrTotal(object.getString("endtime"));
                    gitem.setChild(citem);
                    adapter.addGroup(gitem);
                }
                adapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
