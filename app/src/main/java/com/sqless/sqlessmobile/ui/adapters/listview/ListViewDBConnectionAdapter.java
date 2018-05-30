package com.sqless.sqlessmobile.ui.adapters.listview;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sqless.sqlessmobile.R;

import java.util.List;

public class ListViewDBConnectionAdapter<T extends Subtitulado> extends ArrayAdapter<T> {

    public ListViewDBConnectionAdapter(Activity context, List<T> datos) {
        super(context, R.layout.list_item_subtitulado_image, datos);
    }

    private static class ViewHolder {
        TextView txtTitle;
        TextView txtSubtitle;
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Subtitulado item = getItem(position);

        //Chequeo si la vista está siendo reusada, de lo contrario, inflo la view
        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item_subtitulado_image, parent, false);
            viewHolder.txtTitle = convertView.findViewById(R.id.txtTitle);
            viewHolder.txtSubtitle = convertView.findViewById(R.id.txtSubtitle);

            convertView.setTag(viewHolder); //Cache guardado en tag
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        String titulo = item.getTitulo() == null || item.getTitulo().isEmpty() ? item.getTitulo(parent.getContext()) : item.getTitulo();
        String subtitulo = item.getSubtitulo() == null || item.getSubtitulo().isEmpty() ? item.getSubtitulo(parent.getContext()) : item.getSubtitulo();
        viewHolder.txtTitle.setText(titulo);
        viewHolder.txtSubtitle.setText(subtitulo);

        return convertView; //Retornar la vista completa para mostrar el pantalla
    }
}
