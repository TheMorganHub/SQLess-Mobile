package com.sqless.sqlessmobile.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import androidx.documentfile.provider.DocumentFile;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.ui.busevents.tabledata.DataEvents;

import org.greenrobot.eventbus.EventBus;

public class ExportAsFormatSheetFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    private static final int FILE_CHOOSER_JSON = 42;
    private static final int FILE_CHOOSER_CSV = 522;
    EventBus bus = EventBus.getDefault();

    public static ExportAsFormatSheetFragment newInstance() {
        return new ExportAsFormatSheetFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.export_table_dialog, container, false);
        view.findViewById(R.id.btn_sheet_json).setOnClickListener(this);
        view.findViewById(R.id.btn_sheet_csv).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        startActivityForResult(Intent.createChooser(i, "Elije un directorio"), v.getId() == R.id.btn_sheet_json ? FILE_CHOOSER_JSON : FILE_CHOOSER_CSV);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_CHOOSER_JSON:
                if (data != null) {
                    DocumentFile file = DocumentFile.fromTreeUri(getActivity(), data.getData());
                    bus.post(new DataEvents.URIIsReadyEvent(file, DataEvents.URIIsReadyEvent.JSON_EVENT, getActivity().getClass().getName()));
                    dismiss();
                }
                break;
            case FILE_CHOOSER_CSV:
                if (data != null) {
                    DocumentFile file = DocumentFile.fromTreeUri(getActivity(), data.getData());
                    bus.post(new DataEvents.URIIsReadyEvent(file, DataEvents.URIIsReadyEvent.CSV_EVENT, getActivity().getClass().getName()));
                    dismiss();
                }
                break;
        }
    }
}
