package com.sqless.sqlessmobile.utils;

import java.sql.Blob;
import java.sql.SQLException;

public class DataTypeUtils {

    public static String parseBlob(Blob blob) throws SQLException {
        return blob != null ? "BLOB (" + String.format("%.2f", (float) blob.length() / 1024) + " KB)" : null;
    }

    public static boolean dataTypeIsNumeric(String dataType) {
        return dataTypeIsInteger(dataType) || dataTypeIsDecimal(dataType);
    }

    public static boolean dataTypeIsInteger(String dataType) {
        return dataType.equals("tinyint") || dataType.equals("smallint")
                || dataType.equals("mediumint") || dataType.equals("int")
                || dataType.equals("bigint");
    }

    public static boolean dataTypeIsDecimal(String dataType) {
        return dataType.equals("float") || dataType.equals("decimal") || dataType.equals("double") || dataType.equals("numeric");
    }

    public static boolean dataTypeIsTimeBased(String dataType) {
        return dataType.equals("date") || dataType.equals("datetime")
                || dataType.equals("time") || dataType.equals("timestamp");
    }

    public static boolean dataTypeIsStringBased(String dataType) {
        return dataType.equals("varchar") || dataType.equals("enum") || dataType.equals("set") || dataType.equals("text");
    }

    public static boolean dataTypeCanBeUnsigned(String dataType) {
        return dataType.equals("tinyint") || dataType.equals("smallint") || dataType.equals("mediumint");
    }
}
