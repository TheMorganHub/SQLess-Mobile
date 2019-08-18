package com.sqless.sqlessmobile.ui.adapters.listview;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.ui.Iconable;

import java.util.List;

public class ListViewImageAdapter<T> extends ArrayAdapter<T> {

    private Drawable drawable;

    public ListViewImageAdapter(Context context, Drawable drawable, List<T> datos) {
        super(context, R.layout.list_item_image, datos);
        this.drawable = drawable;
    }

    public ListViewImageAdapter(Context context, List<T> datos) {
        super(context, R.layout.list_item_image, datos);
    }

    private static class ViewHolder {
        TextView txtTitle;
        ImageView ivImage;
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        T item = getItem(position);

        //Chequeo si la vista está siendo reusada, de lo contrario, inflo la view
        ListViewImageAdapter.ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ListViewImageAdapter.ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item_image, parent, false);
            viewHolder.txtTitle = convertView.findViewById(R.id.txt_title);
            viewHolder.ivImage = convertView.findViewById(R.id.iv_image);

            convertView.setTag(viewHolder); //Cache guardado en tag
        } else {
            viewHolder = (ListViewImageAdapter.ViewHolder) convertView.getTag();
        }

        String titulo = item != null ? item.toString() : "";
        viewHolder.txtTitle.setText(titulo);
        viewHolder.ivImage.setImageDrawable(drawable == null ? getContext().getResources().getDrawable(((Iconable) item).getDrawableRes()) : drawable);

        return convertView; //Retornar la vista completa para mostrar el pantalla
    }
}
