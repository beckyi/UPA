package com.example.j.upa.Adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.j.upa.DTO.SideItem;
import com.example.j.upa.R;
import java.util.List;


public class SideAdapter extends ArrayAdapter {

    LayoutInflater inflater = null;
    Context ctxt = null;
    Holder holder = null;
    public SideAdapter(Context context, int resource, List<SideItem> objects) {

        super(context, resource, objects);

        ctxt = context;
        inflater = LayoutInflater.from(context);

    }



    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null)
        {
            convertView = inflater.inflate(R.layout.side_bar_item, null);
            holder = new Holder();
            holder.imgCategory = (ImageView)convertView.findViewById(R.id.imgItem);
            holder.txtCategory = (TextView)convertView.findViewById(R.id.txtItem);
            holder.txtCategory.setGravity(Gravity.CENTER_VERTICAL);
            convertView.setTag(holder);
        }
        holder = (Holder)convertView.getTag();
        SideItem sideItem = (SideItem)getItem(position);
        holder.imgCategory.setImageResource(sideItem.getItemImg());
        holder.txtCategory.setText(sideItem.getItemTxt());
        return convertView;
    }

    class Holder
    {
        ImageView imgCategory;
        TextView txtCategory;
    }
}