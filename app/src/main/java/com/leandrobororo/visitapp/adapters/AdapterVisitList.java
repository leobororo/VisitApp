package com.leandrobororo.visitapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.leandrobororo.visitapp.DashboardActivity;
import com.leandrobororo.visitapp.R;
import com.leandrobororo.visitapp.model.Visit;
import com.leandrobororo.visitapp.util.Util;

import java.util.List;

/**
 * Created by leandrobororo on 03/03/17.
 */
public class AdapterVisitList extends BaseAdapter {

    private final DashboardActivity activity;
    private final List<Visit> visits;
    private final LayoutInflater inflater;

    public AdapterVisitList(DashboardActivity activity, List<Visit> visits) {
        this.activity = activity;
        this.visits = visits;
        this.inflater = activity.getLayoutInflater();
    }

    @Override
    public int getCount() {
        return visits != null ? visits.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return visits.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static class ViewHolder {
        public TextView txtPlace;
        public TextView txtDate;
        public TextView txtTimeBox;
        public ImageView imgPlace;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        final Visit visitItem = visits.get(position);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_visit, null);

            holder = new ViewHolder();

            holder.txtPlace = (TextView) convertView.findViewById(R.id.txtPlace);
            holder.txtDate = (TextView) convertView.findViewById(R.id.txtVisitDate);
            holder.txtTimeBox = (TextView) convertView.findViewById(R.id.txtVisitTimeBox);
            holder.imgPlace = (ImageView) convertView.findViewById(R.id.imgVisitPlace);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.txtPlace.setText(visitItem.getPlaceName());
        holder.txtDate.setText(visitItem.getVisitDate());
        holder.txtTimeBox.setText(formatTimeBoxVisit(visitItem));

        activity.configurarImagem(holder.imgPlace, visitItem);

        return convertView;
    }

    public String formatTimeBoxVisit(Visit friendVisit) {
        return Util.formatTimeBox(friendVisit.getStartHour(),
                friendVisit.getStartMinute(),
                friendVisit.getEndHour(),
                friendVisit.getEndMinute());
    }
}
