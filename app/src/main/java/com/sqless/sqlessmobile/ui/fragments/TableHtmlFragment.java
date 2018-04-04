package com.sqless.sqlessmobile.ui.fragments;


import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.db.queries.SQLQuery;
import com.sqless.sqlessmobile.db.queries.SQLSelectQuery;
import com.sqless.sqlessmobile.utils.UIUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class TableHtmlFragment extends AbstractFragment {


    public TableHtmlFragment() {
        // Required empty public constructor
    }

    @Override
    protected String getTitle() {
        return connectionData != null ? connectionData.getTableName() : "";
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_table_html;
    }

    @Override
    public void afterCreate() {
        createHTMLTable("SELECT * FROM " + connectionData.getTableName() + " LIMIT 200");
    }

    public void createHTMLTable(String sql) {
        WebView wv = fragmentView.findViewById(R.id.wv_table);
        wv.getSettings().setBuiltInZoomControls(true);
        wv.getSettings().setDisplayZoomControls(false);
        wv.getSettings().setDefaultTextEncodingName("utf-8");
        StringBuilder sb = new StringBuilder();
        sb.append("<HTML><HEAD><LINK href=\"styles.css\" type=\"text/css\" rel=\"stylesheet\"/></HEAD><body>");
        SQLQuery tableQuery = new SQLSelectQuery(connectionData, sql) {
            @Override
            public void onSuccess(ResultSet rs) throws SQLException {
                sb.append("<table>");
                sb.append("<tr>");
                List<String> tableHeaders = getTableHeaders(rs.getMetaData());
                sb.append("<th>").append("</th>"); //numero de filas
                for (String header : tableHeaders) {
                    sb.append("<th>").append(header).append("</th>");
                }
                sb.append("</tr>");

                int rowCount = 0;
                while (rs.next()) {
                    sb.append("<tr>");
                    sb.append("<td class=\"row_num\">").append(++rowCount).append("</td>");
                    for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                        sb.append("<td>").append(rs.getString(i)).append("</td>");
                    }
                    sb.append("</tr>");
                }
                sb.append("</table>");

                UIUtils.invokeOnUIThread(() -> {
                    wv.loadDataWithBaseURL("file:///android_asset/", sb.toString(), "text/html", "utf-8", null);
                });
            }

            @Override
            public void onFailure(String errMessage) {
                Log.e("SQLQuery", errMessage);
            }
        };
        tableQuery.exec();
    }

    public List<String> getTableHeaders(ResultSetMetaData rsmd) throws SQLException {
        List<String> tableHeaders = new ArrayList<>();
        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
            tableHeaders.add(rsmd.getColumnName(i));
        }
        return tableHeaders;
    }

    @Override
    protected void implementListeners(View containerView) {

    }

}
