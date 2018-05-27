package com.sqless.sqlessmobile.ui.busevents.tabledata;

import android.support.v4.provider.DocumentFile;

public class DataEvents {
    public static class URIIsReadyEvent {
        public DocumentFile documentFile;
        public static int JSON_EVENT = 921;
        public static int CSV_EVENT = 124;
        public int eventType;

        public URIIsReadyEvent(DocumentFile documentFile, int eventType) {
            this.documentFile = documentFile;
            this.eventType = eventType;
        }
    }
}
