package com.sqless.sqlessmobile.ui.busevents.createtable;

import com.sqless.sqlessmobile.sqlobjects.SQLColumn;
import com.sqless.sqlessmobile.sqlobjects.SQLForeignKey;

import java.util.List;

/**
 * Contiene todos los eventos para utilizar con el {@link org.greenrobot.eventbus.EventBus} relacionados a SQLColumns.
 */
public class ColumnEvents {

    public static class ColumnAddedEvent {
        public SQLColumn column;

        public ColumnAddedEvent(SQLColumn column) {
            this.column = column;
        }
    }

    public static class ColumnRequestEvent {

        public ColumnRequestEvent() {
        }
    }

    public static class ColumnRemovedEvent {
        public SQLColumn column;

        public ColumnRemovedEvent(SQLColumn column) {
            this.column = column;
        }
    }

    public static class FKRemovedEvent {
        public SQLForeignKey fk;

        public FKRemovedEvent(SQLForeignKey fk) {
            this.fk = fk;
        }
    }

    public static class ColumnsReceivedEvent {
        public List<SQLColumn> columns;

        public ColumnsReceivedEvent(List<SQLColumn> columns) {
            this.columns = columns;
        }
    }

    public static class FKAddedEvent {
        public SQLForeignKey foreignKey;

        public FKAddedEvent(SQLForeignKey foreignKey) {
            this.foreignKey = foreignKey;
        }
    }
}
