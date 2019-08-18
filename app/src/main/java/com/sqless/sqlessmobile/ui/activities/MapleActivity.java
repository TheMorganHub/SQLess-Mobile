package com.sqless.sqlessmobile.ui.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import androidx.documentfile.provider.DocumentFile;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.ui.FragmentContainer;
import com.sqless.sqlessmobile.ui.FragmentPagerMapleAdapter;
import com.sqless.sqlessmobile.ui.busevents.maplequery.MapleExecutionReadyEvent;
import com.sqless.sqlessmobile.ui.busevents.maplequery.RunMapleEvent;
import com.sqless.sqlessmobile.ui.fragments.AbstractFragment;
import com.sqless.sqlessmobile.utils.DataUtils;
import com.sqless.sqlessmobile.utils.FinalValue;
import com.sqless.sqlessmobile.utils.HTMLDoc;
import com.sqless.sqlessmobile.utils.UIUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapleActivity extends AppCompatActivity implements FragmentContainer {

    EventBus bus = EventBus.getDefault();
    private ViewPager viewPager;
    private List<HTMLDoc> resultsHtml;
    private List<Integer> selectedResults;
    private static final int FILE_CHOOSER_JSON = 42;
    private static final int FILE_CHOOSER_CSV = 522;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maple);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (!bus.isRegistered(this)) {
            bus.register(this);
        }

        FloatingActionButton fab = findViewById(R.id.fab_run_maple);
        viewPager = findViewById(R.id.viewpager);
        FragmentPagerMapleAdapter adapter = new FragmentPagerMapleAdapter(this, getSupportFragmentManager(), getIntent().getExtras());
        viewPager.setAdapter(adapter);
        TabLayout tabLayout = findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        fab.setOnClickListener(view -> adapter.getRegisteredFragment(0).onFabClicked());

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        //el invokeOnUIThread le permite al adapter llenarse con los fragmentos antes de ejecutarse este bloque de código, si no esperamos, el adapter devolverá un fragment null
                        UIUtils.invokeOnUIThreadIfNotDestroyed(MapleActivity.this, () -> {
                            AbstractFragment fragment = adapter.getRegisteredFragment(position);
                            fab.setOnClickListener(view -> fragment.onFabClicked());
                            fab.show();
                        });
                        break;
                    default:
                        fab.hide();
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

    }

    @Subscribe
    public void onMapleExecutionReadyEvent(MapleExecutionReadyEvent event) {
        ViewPager viewPager = findViewById(R.id.viewpager);
        viewPager.setCurrentItem(1, true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_maple, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.btn_maple_manual:
                openMapleManual();
                break;
            case R.id.btn_export_table:
                exportQueryData();
                return true;
        }
        return false;
    }

    public void openMapleManual() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://sqless.000webhostapp.com/maple/docs"));
        startActivity(browserIntent);
    }

    public void exportQueryData() {
        showSelectResultsDialog();
    }

    @Subscribe
    public void onResultCountResponseEvent(RunMapleEvent.ResultResponseEvent event) {
        this.resultsHtml = event.docs;
    }

    public void showSelectResultsDialog() {
        bus.post(new RunMapleEvent.ResultRequestEvent());
        if (resultsHtml == null || resultsHtml.isEmpty()) {
            UIUtils.showMessageDialog(this, "Exportar resultados", "Debe haber al menos un resultado disponible para hacer uso de esta funcionalidad.");
            return;
        }
        selectedResults = new ArrayList<>();
        final String[] items = new String[resultsHtml.size()];
        for (int i = 0; i < items.length; i++) {
            items[i] = "Resultado " + (i + 1);
        }

        if (resultsHtml.size() > 1) {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Resultados a exportar")
                    .setMultiChoiceItems(items, null, (dialogInterface, indexSelected, isChecked) -> {
                        if (isChecked) {
                            selectedResults.add(indexSelected);
                        } else if (selectedResults.contains(indexSelected)) {
                            selectedResults.remove((Integer) indexSelected);
                        }
                    })
                    .setPositiveButton("Siguiente", null)
                    .setNegativeButton("Cancelar", (dialogInterface, id) -> {
                    }).show();
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (!selectedResults.isEmpty()) {
                    dialog.dismiss();
                    showSelectFormatDialog();
                } else {
                    Toast.makeText(MapleActivity.this, "Se debe elegir al menos un resultado a exportar.", Toast.LENGTH_SHORT).show();
                }
            });
        } else { //si hay solo un resultado, vamos directamente al dialogo de elegir formato
            selectedResults.add(0);
            showSelectFormatDialog();
        }
    }

    public void showSelectFormatDialog() {
        String[] items = {"JSON", "CSV"};
        FinalValue<String> selectedItem = new FinalValue<>("JSON");
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Formato a exportar")
                .setSingleChoiceItems(items, 0, (dialogInterface, which) -> selectedItem.set(items[which]))
                .setPositiveButton("OK", (dialogInterface, id) -> showSelectDirectoryDialog(selectedItem.getValue()))
                .setNegativeButton("Cancelar", (dialogInterface, id) -> {
                }).create();
        dialog.show();
    }

    public void showSelectDirectoryDialog(String format) {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        Intent fileChooserIntent = Intent.createChooser(i, "Elije un directorio");
        startActivityForResult(fileChooserIntent, format.equals("JSON") ? FILE_CHOOSER_JSON : FILE_CHOOSER_CSV);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_CHOOSER_JSON:
                if (data != null) {
                    prepareResultsFile(DocumentFile.fromTreeUri(this, data.getData()), FILE_CHOOSER_JSON);
                }
                break;
            case FILE_CHOOSER_CSV:
                if (data != null) {
                    prepareResultsFile(DocumentFile.fromTreeUri(this, data.getData()), FILE_CHOOSER_CSV);
                }
                break;
        }
    }

    public void prepareResultsFile(DocumentFile file, int format) {
        Map<Integer, HTMLDoc> selectedDocs = new HashMap<>();
        for (int i = 0; i < selectedResults.size(); i++) {
            int selectedResult = selectedResults.get(i);
            selectedDocs.put(selectedResult + 1, resultsHtml.get(selectedResult));
        }
        switch (format) {
            case FILE_CHOOSER_JSON:
                DataUtils.htmlTablesToJSON(this, selectedDocs, file);
                break;
            case FILE_CHOOSER_CSV:
                DataUtils.htmlTablesToCSV(this, selectedDocs, file);
                break;
        }
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
        if (bus.isRegistered(this)) {
            bus.unregister(this);
        }
    }

    @Override
    public void getTitleFromFragment(String title) {
        setTitle(title);
    }
}
