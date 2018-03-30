package com.sqless.sqlessmobile.ui.busevents.createtable;

import com.sqless.sqlessmobile.sqlobjects.SQLColumn;

public class ColumnAddedEvent {
    public SQLColumn column;

    public ColumnAddedEvent(SQLColumn column) {
        this.column = column;
    }
}
