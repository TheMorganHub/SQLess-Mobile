package com.sqless.sqlessmobile.ui.busevents.maplequery;

import com.sqless.sqlessmobile.utils.HTMLDoc;

import java.util.List;

public class RunMapleEvent {

    public static class SQLReadyEvent {
        public String sqlFromMaple;

        public SQLReadyEvent(String sqlFromMaple) {
            this.sqlFromMaple = sqlFromMaple;
        }
    }

    public static class HTMLReadyEvent {
        public List<HTMLDoc> htmlResults;

        public HTMLReadyEvent(List<HTMLDoc> htmlResults) {
            this.htmlResults = htmlResults;
        }
    }

    public static class SQLExceptionEvent {
        public String message;

        public SQLExceptionEvent(String message) {
            this.message = message;
        }
    }

    public static class MapleExceptionEvent {
        public String message;

        public MapleExceptionEvent(String message) {
            this.message = message;
        }
    }
}
