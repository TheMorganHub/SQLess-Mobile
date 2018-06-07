package com.sqless.sqlessmobile.ui.activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.provider.DocumentFile;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.network.SQLConnectionManager;
import com.sqless.sqlessmobile.sqlobjects.SQLColumn;
import com.sqless.sqlessmobile.sqlobjects.SQLSelectable;
import com.sqless.sqlessmobile.sqlobjects.SQLTable;
import com.sqless.sqlessmobile.ui.FragmentContainer;
import com.sqless.sqlessmobile.ui.FragmentPagerTableDetailsAdapter;
import com.sqless.sqlessmobile.ui.busevents.tabledata.DataEvents;
import com.sqless.sqlessmobile.ui.fragments.ColumnsFragment;
import com.sqless.sqlessmobile.ui.fragments.ExportAsFormatSheetFragment;
import com.sqless.sqlessmobile.utils.DataUtils;
import com.sqless.sqlessmobile.utils.SQLUtils;
import com.sqless.sqlessmobile.utils.UIUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.view.View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION;

public class TableDetailsActivity extends AppCompatActivity implements FragmentContainer {

    private ViewPager viewPager;
    EventBus bus = EventBus.getDefault();
    private AlertDialog activeDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_details);
        bus.register(this);
        // Find the view pager that will allow the user to swipe between fragments
        viewPager = findViewById(R.id.viewpager);

        // Create an adapter that knows which fragment should be shown on each page
        FragmentPagerTableDetailsAdapter adapter = new FragmentPagerTableDetailsAdapter(this, getSupportFragmentManager(), getIntent().getExtras());
        // Set the adapter onto the view pager
        viewPager.setAdapter(adapter);

        if (getIntent().getIntExtra("table_type", -1) == ColumnsFragment.TABLE) {
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    invalidateOptionsMenu();
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
        }

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.btn_export_table:
                exportTableData();
                return true;
            case R.id.btn_add_row:
                requestColumnsToAddRow();
                break;
        }

        return false;
    }

    public void requestColumnsToAddRow() {
        bus.post(new DataEvents.TableColumnsRequestEvent());
    }

    @Subscribe
    public void onTableColumnsResponseEvent(DataEvents.TableColumnsResponseEvent event) {
        SQLTable table = (SQLTable) getIntent().getSerializableExtra("selectable");
        table.setColumns(event.columns);
        showAddRowDialog(event.columns);
    }

    public void showAddRowDialog(List<SQLColumn> columns) {
        if (columns.isEmpty()) {
            UIUtils.showMessageDialog(this, "Agregar fila", "Debe haber al menos una columna en la tabla para agregar una fila.");
            return;
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        View dialogExecutableParameters = LayoutInflater.from(this).inflate(R.layout.dialog_add_row, null);
        dialogBuilder.setView(dialogExecutableParameters);
        dialogBuilder.setTitle("Agregar fila");
        ViewGroup mainLayout = dialogExecutableParameters.findViewById(R.id.lay_new_row);
        mainLayout.setPadding(15, 0, 15, 0);
        for (int i = 0; i < columns.size(); i++) {
            SQLColumn column = columns.get(i);
            View parameterView = LayoutInflater.from(this).inflate(R.layout.new_row_data, null);
            ((TextInputLayout) parameterView.findViewById(R.id.txt_lay_new_row)).setHint(column.getName() + " (" + column.getDatatype() + ")");
            parameterView.findViewById(R.id.txt_lay_new_row).setContentDescription("ColumnData");
            mainLayout.addView(parameterView, i);
        }
        dialogBuilder.setPositiveButton("Agregar", (dialog, which) -> prepareRowData(columns, mainLayout));
        dialogBuilder.setNegativeButton("Cancelar", (dialog, which) -> bus.post(new DataEvents.RefreshDataRequestEvent())); //refrescamos por si el usuario agrego una fila
        dialogBuilder.show();
    }

    public void prepareRowData(List<SQLColumn> columns, ViewGroup rowsDataLayout) {
        if (rowsDataLayout != null) { //el ejecutable tiene parámetros
            Map<String, String> columnDataPairs = new HashMap<>();
            ArrayList<View> outputViews = new ArrayList<>();
            rowsDataLayout.findViewsWithText(outputViews, "ColumnData", FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
            for (int i = 0; i < outputViews.size(); i++) {
                String columnValue = ((TextInputLayout) outputViews.get(i)).getEditText().getText().toString();
                columnDataPairs.put(columns.get(i).getName(), columnValue);
            }
            generateAndExecuteAddRowSQL(columnDataPairs);
        }
    }

    public void generateAndExecuteAddRowSQL(Map<String, String> columnData) {
        SQLTable sqlTable = (SQLTable) getIntent().getSerializableExtra("selectable");
        SQLConnectionManager.ConnectionData connectionData = (SQLConnectionManager.ConnectionData) getIntent().getSerializableExtra("connection_data");
        SQLUtils.insertIntoTable(this, connectionData, sqlTable, columnData,
                () -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this)
                            .setTitle("Agregar fila")
                            .setMessage("La fila se ha agregado con éxito.")
                            .setPositiveButton("Agregar otra", (dialog, which) -> {
                                requestColumnsToAddRow();
                            })
                            .setNeutralButton("Salir", (dialog, which) -> bus.post(new DataEvents.RefreshDataRequestEvent()));
                    activeDialog = builder.show();
                },
                err -> UIUtils.showMessageDialog(this, "Agregar fila", "No se pudo agregar la fila.\nEl servidor respondió con mensaje:\n" + err));
    }

    public void exportTableData() {
        ExportAsFormatSheetFragment exportAsFormat = ExportAsFormatSheetFragment.newInstance();
        exportAsFormat.show(getSupportFragmentManager(), "export_table_data");
    }

    @Subscribe
    public void onUriIsReadyEvent(DataEvents.URIIsReadyEvent event) {
        if (!event.forActivity.equals(getClass().getName())) {
            return;
        }
        DocumentFile file = event.documentFile;
        SQLConnectionManager.ConnectionData connData = (SQLConnectionManager.ConnectionData) getIntent().getSerializableExtra("connection_data");
        SQLSelectable selectable = (SQLSelectable) getIntent().getSerializableExtra("selectable");
        if (event.eventType == DataEvents.URIIsReadyEvent.JSON_EVENT) {
            DataUtils.tableToJSON(this, connData, selectable, file);
        } else if (event.eventType == DataEvents.URIIsReadyEvent.CSV_EVENT) {
            DataUtils.tableToCSV(this, connData, selectable, file);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_table_details, menu);
        menu.findItem(R.id.btn_add_row).setVisible(viewPager.getCurrentItem() == 1);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() != 0) {
            viewPager.setCurrentItem(0, true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
        if (activeDialog != null && activeDialog.isShowing()) {
            activeDialog.dismiss();
        }
    }

    @Override
    public void getTitleFromFragment(String title) {
        setTitle(title);
    }
}
