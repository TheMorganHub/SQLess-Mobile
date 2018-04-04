package com.sqless.sqlessmobile.ui.adapters.listview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.sqlobjects.SQLColumn;

import java.util.List;

public class ListViewColumnDetailsAdapter extends ArrayAdapter<SQLColumn> {

    public ListViewColumnDetailsAdapter(@NonNull Context context, @NonNull List<SQLColumn> objects) {
        super(context, R.layout.list_item_column, objects);
    }

    private static class ViewHolder {
        ImageView ivColumnKey;
        TextView tvColumnName;
        TextView tvColumnDatatype;
        TextView tvColumnNullable;
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        SQLColumn item = getItem(position);

        ListViewColumnDetailsAdapter.ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ListViewColumnDetailsAdapter.ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item_column, parent, false);
            viewHolder.ivColumnKey = convertView.findViewById(R.id.iv_column_key);
            viewHolder.tvColumnName = convertView.findViewById(R.id.tv_column_name);
            viewHolder.tvColumnDatatype = convertView.findViewById(R.id.tv_column_datatype);
            viewHolder.tvColumnNullable = convertView.findViewById(R.id.tv_column_nullable);

            convertView.setTag(viewHolder); //Cache guardado en tag
        } else {
            viewHolder = (ListViewColumnDetailsAdapter.ViewHolder) convertView.getTag();
        }

        viewHolder.ivColumnKey.setImageDrawable(getContext().getResources().getDrawable(item.getDrawableRes()));
        viewHolder.tvColumnName.setText(item.getName());
        viewHolder.tvColumnDatatype.setText(item.getDatatype());
        viewHolder.tvColumnNullable.setText("Nullable? " + (item.isNullable() ? "Sí" : "No"));

        return convertView;
    }
}
