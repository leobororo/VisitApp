package com.leandrobororo.visitapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.leandrobororo.visitapp.FriendsActivity;
import com.leandrobororo.visitapp.R;
import com.leandrobororo.visitapp.model.Friend;
import com.leandrobororo.visitapp.model.Visit;
import com.leandrobororo.visitapp.util.Util;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by leandrobororo on 03/03/17.
 */
public class AdapterFriendsList extends BaseAdapter {

    private final FriendsActivity activity;
    private final List<Friend> friends;
    private final LayoutInflater inflater;

    public AdapterFriendsList(FriendsActivity activity, List<Friend> friends) {
        this.activity = activity;
        this.friends = friends;
        this.inflater = activity.getLayoutInflater();
    }

    @Override
    public int getCount() {
        return friends != null ? friends.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return friends.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static class ViewHolder {
        public TextView txtName;
        public TextView txtDate;
        public TextView txtTimeBox;
        public ImageView imgProfile;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        final Friend friendItem = friends.get(position);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_friend, null);

            holder = new ViewHolder();

            holder.txtName = (TextView) convertView.findViewById(R.id.txtName);
            holder.txtDate = (TextView) convertView.findViewById(R.id.txtDate);
            holder.txtTimeBox = (TextView) convertView.findViewById(R.id.txtTimeBox);
            holder.imgProfile = (ImageView) convertView.findViewById(R.id.imgProfile);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Visit friendVisit = friendItem.getVisit();

        holder.txtName.setText(friendItem.getName());
        holder.txtDate.setText(friendVisit.getVisitDate());
        holder.txtTimeBox.setText(formatTimeBoxVisit(friendVisit));

        Picasso.with(activity).load("https://graph.facebook.com/" + friendItem.getId() + "/picture?type=large").into(holder.imgProfile);

        return convertView;
    }

    public String formatTimeBoxVisit(Visit friendVisit) {
        return Util.formatTimeBox(friendVisit.getStartHour(),
                friendVisit.getStartMinute(),
                friendVisit.getEndHour(),
                friendVisit.getEndMinute());
    }
}
