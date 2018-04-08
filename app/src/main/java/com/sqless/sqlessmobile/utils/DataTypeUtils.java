package com.sqless.sqlessmobile.utils;

import java.sql.Blob;
import java.sql.SQLException;

public class DataTypeUtils {

    public static String parseBlob(Blob blob) throws SQLException {
        return blob != null ? "BLOB (" + String.format("%.2f", (float) blob.length() / 1024) + " KB)" : null;
    }
}
