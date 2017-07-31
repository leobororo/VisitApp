package com.leandrobororo.visitapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.leandrobororo.visitapp.AmigosActivity;
import com.leandrobororo.visitapp.R;
import com.leandrobororo.visitapp.model.Amigo;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leandrobororo on 03/03/17.
 */

public class AdapterListAmigos extends BaseAdapter {
    private static final String TEXTO_TIME_BOX = "Entre %02d:%02d e %02d:%02d horas";

    private final AmigosActivity activity;
    private final List<Amigo> amigos;
    private final LayoutInflater inflater;

    public AdapterListAmigos(AmigosActivity activity, List<Amigo> amigos) {
        this.activity = activity;
        this.amigos = amigos;
        this.inflater = activity.getLayoutInflater();
    }

    @Override
    public int getCount() {
        return amigos != null ? amigos.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return amigos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static class ViewHolder {
        public TextView txtNome;
        public TextView txtDataAmigo;
        public TextView txtTimeBoxAmigo;
        public ImageView imgPerfilAmigo;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        final Amigo amigoItem = amigos.get(position);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_amigo, null);

            holder = new ViewHolder();

            holder.txtNome = (TextView) convertView.findViewById(R.id.txtNome);
            holder.txtDataAmigo = (TextView) convertView.findViewById(R.id.txtDataAmigo);
            holder.txtTimeBoxAmigo = (TextView) convertView.findViewById(R.id.txtTimeBoxAmigo);
            holder.imgPerfilAmigo = (ImageView) convertView.findViewById(R.id.imgPerfilAmigo);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.txtNome.setText(amigoItem.getNome());
        holder.txtDataAmigo.setText(amigoItem.getVisita().getDataVisitaFormatada());
        holder.txtTimeBoxAmigo.setText(amigoItem.getVisita().getDescricaoHorarioVisita());

        Picasso.with(activity).load("https://graph.facebook.com/" + amigoItem.getId() + "/picture?type=large").into(holder.imgPerfilAmigo);

        return convertView;
    }

    public List<Amigo> getLista() {
        return amigos == null ? new ArrayList<Amigo>() : amigos;
    }
}