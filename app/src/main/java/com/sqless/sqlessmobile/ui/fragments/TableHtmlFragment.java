package com.sqless.sqlessmobile.ui.fragments;

import android.view.View;
import android.webkit.WebView;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.db.queries.SQLQuery;
import com.sqless.sqlessmobile.db.queries.SQLSelectQuery;
import com.sqless.sqlessmobile.utils.DataTypeUtils;
import com.sqless.sqlessmobile.utils.HTMLDoc;
import com.sqless.sqlessmobile.utils.UIUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

        SQLQuery tableQuery = new SQLSelectQuery(connectionData, sql) {
            @Override
            public void onSuccess(ResultSet rs) throws SQLException {
                HTMLDoc.HTMLDocBuilder builder = new HTMLDoc.HTMLDocBuilder("tablehtml")
                        .withCss("style");

                builder.addHTML("<table>");
                builder.addHTML("<thead>");
                builder.addHTML("<tr>");

                List<String> tableHeaders = getTableHeaders(rs.getMetaData());
                builder.addHTML("<th>").addHTML("</th>"); //numero de filas
                for (String header : tableHeaders) {
                    builder.addHTML("<th>").addHTML(header).addHTML("</th>");
                }
                builder.addHTML("</tr>");
                builder.addHTML("</thead>");
                builder.addHTML("<tbody>");

                int rowCount = 0;
                while (rs.next()) {
                    builder.addHTML("<tr>");
                    builder.addHTML("<td>").addHTML(++rowCount).addHTML("</td>");
                    for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                        builder.addHTML(getHTMLForValue(rs.getMetaData().getColumnTypeName(i), rs, i));
                    }
                    builder.addHTML("</tr>");
                }
                builder.addHTML("</tbody>");
                builder.addHTML("</table>");

                HTMLDoc doc = builder.build();

                UIUtils.invokeOnUIThread(() -> wv.loadDataWithBaseURL(doc.getAssetsFolder(), doc.getHTML(), "text/html", "utf-8", null));
            }

            @Override
            public void onFailure(String errMessage) {
                super.onFailure(errMessage);
            }
        };
        tableQuery.exec();
    }

    public String getHTMLForValue(String columnTypeName, ResultSet rs, int col) throws SQLException {
        if (rs.getString(col) == null) {
            return "<td class=\"null-value\">(Null)</td>";
        }
        if (columnTypeName.equals("BLOB")) {
            return "<td>" + DataTypeUtils.parseBlob(rs.getBlob(col)) + "</td>";
        }

        return "<td>" + rs.getString(col) + "</td>";
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
