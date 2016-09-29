package com.example.j.upa.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.j.upa.DTO.ChildItem;
import com.example.j.upa.DTO.ExpandableItem;
import com.example.j.upa.R;

import java.util.ArrayList;

/**
 * Created by S403 on 2016-06-09.
 */
public class ExpandableAdapter extends BaseExpandableListAdapter {
    private ArrayList<ExpandableItem> groupList = new ArrayList<ExpandableItem>();

    public ExpandableAdapter(){
    }
    @Override
    public int getGroupCount() {
        return groupList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public ExpandableItem getGroup(int groupPosition) {
        return groupList.get(groupPosition);
    }

    @Override
    public ChildItem getChild(int groupPosition, int childPosition) {
        return groupList.get(groupPosition).getChild();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();

        if(convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_history_group, parent, false);
        }
        TextView txtName = (TextView)convertView.findViewById(R.id.txtName);
        TextView txtTime = (TextView)convertView.findViewById(R.id.txtTime);
        ImageView imvExpand = (ImageView)convertView.findViewById(R.id.imvExpand);

        ExpandableItem item = getGroup(groupPosition);

        txtName.setText(item.getName());
        txtTime.setText(item.getTime());
        if(isExpanded){
            imvExpand.setImageResource(R.drawable.history_up);
        }else {

            imvExpand.setImageResource(R.drawable.history_down);
        }
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();
        if(convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_history_child, parent, false);
        }
        TextView txtAddress = (TextView)convertView.findViewById(R.id.txtAddress);
        TextView txtTime = (TextView)convertView.findViewById(R.id.txtTime);
        TextView txtTotal = (TextView)convertView.findViewById(R.id.txtTotal);

        ChildItem item = getChild(groupPosition, childPosition);

        txtAddress.setText(item.getStrAddress());
        txtTime.setText(item.getStrTime());
        txtTotal.setText(item.getStrTotal());

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void addGroup(ExpandableItem temp) {
        groupList.add(temp);
    }


}
