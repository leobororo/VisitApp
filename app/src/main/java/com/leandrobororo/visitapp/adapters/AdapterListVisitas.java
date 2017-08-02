package com.leandrobororo.visitapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.leandrobororo.visitapp.BaseActivity;
import com.leandrobororo.visitapp.R;
import com.leandrobororo.visitapp.model.Visita;
import com.leandrobororo.visitapp.util.Util;

import java.util.List;

/**
 * Created by leandrobororo on 03/03/17.
 */
public class AdapterListVisitas extends BaseAdapter {

    private final BaseActivity activity;
    private final List<Visita> visitas;
    private final LayoutInflater inflater;

    public AdapterListVisitas(BaseActivity activity, List<Visita> visitas) {
        this.activity = activity;
        this.visitas = visitas;
        this.inflater = activity.getLayoutInflater();
    }

    @Override
    public int getCount() {
        return visitas != null ? visitas.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return visitas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static class ViewHolder {
        public TextView txtNomeGooglePlace;
        public TextView txtDataVisita;
        public TextView txtTimeBoxVisita;
        public ImageView imgGooglePlace;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        final Visita visitaItem = visitas.get(position);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_visita, null);

            holder = new ViewHolder();

            holder.txtNomeGooglePlace = (TextView) convertView.findViewById(R.id.txtNomeGooglePlace);
            holder.txtDataVisita = (TextView) convertView.findViewById(R.id.txtDataVisita);
            holder.txtTimeBoxVisita = (TextView) convertView.findViewById(R.id.txtTimeBoxVisita);
            holder.imgGooglePlace = (ImageView) convertView.findViewById(R.id.imgGooglePlace);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.txtNomeGooglePlace.setText(visitaItem.getNomePlace());
        holder.txtDataVisita.setText(visitaItem.getDataVisita());
        holder.txtTimeBoxVisita.setText(obterDescricaoHorarioVisitaAmigo(visitaItem));

        activity.configurarImagem(holder.imgGooglePlace, visitaItem);

        return convertView;
    }

    public String obterDescricaoHorarioVisitaAmigo(Visita visitaAmigo) {
        return Util.getDescricaoHorarioVisita(visitaAmigo.getHoraInicioVisita(),
                visitaAmigo.getMinutoInicioVisita(),
                visitaAmigo.getHoraFimVisita(),
                visitaAmigo.getMinutoFimVisita());
    }
}
