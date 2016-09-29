package com.example.j.upa.Fragment;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.j.upa.DAO.GPS;
import com.example.j.upa.DAO.Searcher;
import com.example.j.upa.DAO.Server;
import com.example.j.upa.DTO.Item;
import com.example.j.upa.DTO.OnFinishSearchListener;
import com.example.j.upa.R;
import com.example.j.upa.View.InformationActivity;
import com.example.j.upa.View.MapregistActivity;

import net.daum.mf.map.api.CalloutBalloonAdapter;
import net.daum.mf.map.api.CameraUpdateFactory;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPointBounds;
import net.daum.mf.map.api.MapView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class MapFragment extends Fragment implements MapView.MapViewEventListener, MapView.POIItemEventListener {

    String SERVER_ADDRESS = Server.SERVER_ADDRESS;
    String MAPS_API_KEY = Server.DAUM_MAPS_ANDROID_APP_API_KEY;
    private static final String LOG_TAG = "SearchDemoActivity";
    View view;
    private MapView mMapView;
    private EditText edtSearch;
    private ImageView imvSearch, imvGps;
    private HashMap<Integer, Item> mTagItemMap = new HashMap<Integer, Item>();
    private GPS gps;
    private Searcher searcher;
    SharedPreferences setting;
    boolean root;
    double platitude=0.0, plongitude=0.0;

    Calendar calendar = Calendar.getInstance();
    java.util.Date date = calendar.getTime();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        view = (View)inflater.inflate(R.layout.fragment_map, container, false);
        searcher = new Searcher();

        mMapView = (MapView)view.findViewById(R.id.map_view);
        mMapView.setDaumMapApiKey(MAPS_API_KEY);
        mMapView.setMapViewEventListener(this);
        mMapView.setPOIItemEventListener(this);
        mMapView.setCalloutBalloonAdapter(new CustomCalloutBalloonAdapter());
        edtSearch = (EditText)getActivity().findViewById(R.id.edtSearch);
        imvSearch = (ImageView)getActivity().findViewById(R.id.imvSearch);
        imvGps = (ImageView)getActivity().findViewById(R.id.imvGps);

        imvSearch.setOnClickListener(new View.OnClickListener() { // 검색버튼 클릭 이벤트 리스너

            public void onClick(View v) {
                String query = edtSearch.getText().toString();
                if (query == null || query.length() == 0) {
                    showToast("검색어를 입력하세요.");
                    return;
                }
                hideSoftKeyboard();
                searcher.searchKeyword(getActivity(), query, new OnFinishSearchListener() {

                    public void onSuccess(java.util.List<Item> itemList) {
                        mMapView.removeAllPOIItems();
                        showResult(itemList, 0);
                    }

                    public void onFail() {
                        showToast("API_KEY의 제한 트래픽이 초과되었습니다.");
                    }
                });
            }
        });
        imvGps.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String query = "";

                searcher.searchKeyword(getActivity(), query, new OnFinishSearchListener() {

                    public void onSuccess(java.util.List<Item> itemList) {
                        mMapView.removeAllPOIItems();
                        showResult(itemList, 1);
                    }

                    public void onFail() {
                        showToast("API_KEY의 제한 트래픽이 초과되었습니다.");
                    }
                });
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                moveGps();
            }
        });

        moveGps();
        return view;
    }

    class CustomCalloutBalloonAdapter implements CalloutBalloonAdapter {

        private final View mCalloutBalloon;

        public CustomCalloutBalloonAdapter() {
            mCalloutBalloon = LayoutInflater.from(getActivity()).inflate(R.layout.custom_callout_balloon, null);
        }

        public View getCalloutBalloon(MapPOIItem poiItem) {
            if (poiItem == null) return null;
            Item item = mTagItemMap.get(poiItem.getTag());
            if (item == null) return null;
            ImageView imageViewBadge = (ImageView) mCalloutBalloon.findViewById(R.id.badge);
            TextView textViewTitle = (TextView) mCalloutBalloon.findViewById(R.id.title);
            textViewTitle.setText(item.tradeName);
            TextView textViewDesc = (TextView) mCalloutBalloon.findViewById(R.id.desc);
            textViewDesc.setText(item.address);
            imageViewBadge.setImageDrawable(createDrawableFromUrl(SERVER_ADDRESS+"/newImage/"+item.parkimage));
            return mCalloutBalloon;
        }

        public View getPressedCalloutBalloon(MapPOIItem poiItem) {
            return null;
        }

    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edtSearch.getWindowToken(), 0);
    }

    public void onMapViewInitialized(MapView mapView) {
        Log.i(LOG_TAG, "MapView had loaded. Now, MapView APIs could be called safely");

        String query = edtSearch.getText().toString();

        searcher.searchKeyword(getActivity(), query, new OnFinishSearchListener() {

            public void onSuccess(java.util.List<Item> itemList) {
                mMapView.removeAllPOIItems();
                showResult(itemList, 1);
            }

            public void onFail() {
                showToast("API_KEY의 제한 트래픽이 초과되었습니다.");
            }
        });
    }

    private void showToast(final String text) {
                Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }
    private void moveGps(){
        platitude=0.0;
        plongitude=0.0;
        gps = new GPS(getActivity());
        if (gps.isGetLocation()) {
            int i=0;
            while(platitude==0.0) {
                platitude = gps.getLatitude();
                plongitude = gps.getLongitude();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                i++;
                if(i==50){
                    showToast("GPS 위치를 확인 할 수 없습니다.");
                    gps.stopUsingGPS();
                    break;
                }
            }
            if(platitude!=0.0) {
                mMapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(platitude, plongitude), 2, true);
            }
            gps.stopUsingGPS();

        } else {
            // GPS 를 사용할수 없으므로
            gps.showSettingsAlert();
        }
    }
    private void showResult(java.util.List<Item> itemList,int search) {
        MapPointBounds mapPointBounds = new MapPointBounds();

        for (int i = 0; i < itemList.size(); i++) {
            Item item = itemList.get(i);

            MapPOIItem poiItem = new MapPOIItem();
            poiItem.setItemName(item.tradeName);
            poiItem.setTag(i);
            MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(item.latitude, item.longitude);
            poiItem.setMapPoint(mapPoint);
            mapPointBounds.add(mapPoint);
            poiItem.setMarkerType(MapPOIItem.MarkerType.CustomImage);
            if(stringTodate(item.starttime).before(stringTodate(item.endtime))){
                if(stringTodate(item.starttime).before(date))
                {
                    if(stringTodate(item.endtime).after(date)){
                        if(item.devicestate==1){
                            poiItem.setCustomImageResourceId(R.drawable.map_pin_blue);
                        }else{
                            poiItem.setCustomImageResourceId(R.drawable.map_pin_red);
                        }
                    }else{
                        poiItem.setCustomImageResourceId(R.drawable.map_pin_red);
                    }
                }else{
                    poiItem.setCustomImageResourceId(R.drawable.map_pin_red);
                }
            }else{
                if(stringTodate(item.starttime).before(date)){
                    if(stringTodate(item.endtime).before(date)){
                        if(item.devicestate==1){
                            poiItem.setCustomImageResourceId(R.drawable.map_pin_blue);
                        }else{
                            poiItem.setCustomImageResourceId(R.drawable.map_pin_red);
                        }
                    }
                    else{
                        poiItem.setCustomImageResourceId(R.drawable.map_pin_red);
                    }
                }
                else{
                    poiItem.setCustomImageResourceId(R.drawable.map_pin_red);
                }
            }
            poiItem.setCustomSelectedImageResourceId(R.drawable.map_pin_yellow);
            poiItem.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
            poiItem.setCustomImageAutoscale(false);
            poiItem.setCustomImageAnchor(0.5f, 1.0f);

            mMapView.addPOIItem(poiItem);
            mTagItemMap.put(poiItem.getTag(), item);
        }
        if(search==0) {
            mMapView.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds));

            MapPOIItem[] poiItems = mMapView.getPOIItems();
            if (poiItems.length > 0) {
                mMapView.selectPOIItem(poiItems[0], false);
        }
        if(search==1){

            }
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


    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {

        Item item = mTagItemMap.get(mapPOIItem.getTag());
        boolean usefultime;
        if(stringTodate(item.starttime).before(stringTodate(item.endtime))){
            if(stringTodate(item.starttime).before(date))
            {
                if(stringTodate(item.endtime).after(date)){
                    usefultime=true;
                }else{
                    usefultime=false;
                }
            }else{
                usefultime=false;
            }
        }else{
            if(stringTodate(item.starttime).before(date)){
                if(stringTodate(item.endtime).before(date)){
                    usefultime=true;
                }
                else{
                    usefultime=false;
                }
            }
            else{
                usefultime=false;
            }
        }

        Intent intent_Info = new Intent(getActivity(), InformationActivity.class);
        intent_Info.putExtra("master",item.master);
        intent_Info.putExtra("index",item.index);
        intent_Info.putExtra("address",item.address);
        intent_Info.putExtra("starttime",item.starttime);
        intent_Info.putExtra("endtime",item.endtime);
        intent_Info.putExtra("latitude",item.latitude);
        intent_Info.putExtra("longitude",item.longitude);
        intent_Info.putExtra("tradeName",item.tradeName);
        intent_Info.putExtra("devicestate",item.devicestate);
        intent_Info.putExtra("usefultime",usefultime);
        intent_Info.putExtra("parkimage",item.parkimage);
        intent_Info.putExtra("platitude",platitude);
        intent_Info.putExtra("plongitude",plongitude);
        startActivity(intent_Info);
        getActivity().finish();

    }

    @Deprecated
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {
    }


    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {
    }


    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {
    }


    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapCenterPoint) {
    }


    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {
    }


    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

        if(true){

            MapPoint.GeoCoordinate mapPointGeo = mapPoint.getMapPointGeoCoord();
            Intent intent_register = new Intent(getContext(),MapregistActivity.class);
            double latitude = mapPointGeo.latitude;
            double longitude = mapPointGeo.longitude;
            String address = getDetailAddress(latitude, longitude);
            intent_register.putExtra("latitude",latitude);
            intent_register.putExtra("longitude",longitude);
            intent_register.putExtra("address",address);
            startActivity(intent_register);
            getActivity().finish();

        }
        /*
        String url = "daummaps://route?sp=37.537229,127.005515&ep=37.4979502,127.0276368&by=CAR";
        Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(url));
        startActivity(intent);
        */
    }


    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {
    }


    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {
    }


    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {
    }


    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {
    }


    public void onMapViewZoomLevelChanged(MapView mapView, int zoomLevel) {
    }
    public Date stringTodate(String str){
        Date start = null;
        SimpleDateFormat trans = new SimpleDateFormat("hh:mm:ss");
        try{
            start=(Date)trans.parse(str);
            start.setYear(date.getYear());
            start.setMonth(date.getMonth());
            start.setDate(date.getDate());
            return start;
        }catch (ParseException e){
            e.printStackTrace();
        }
        return start;
    }
    public String getDetailAddress(double latitude, double longtitude) {
        String urlStr = "https://apis.daum.net/local/geo/coord2detailaddr?apikey="+MAPS_API_KEY+"&x="+longtitude+"&y="+latitude+"&inputCoordSystem=WGS84";
        String resultStr = "";
        try {
            URL url = new URL(urlStr);
            InputStream is = url.openStream();
            resultStr="edf";
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(is, "UTF-8");
            int eventType = parser.getEventType();
            boolean isStop = false;
            while(eventType != XmlPullParser.END_DOCUMENT && !isStop){
                if(eventType == XmlPullParser.START_TAG){
                    if(parser.getName().trim().equals("old")){
                        parser.next();
                        resultStr = parser.getAttributeValue(0);
                        isStop = true;
                    }
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("ResultStr = " + resultStr);
        return resultStr;
    }
}