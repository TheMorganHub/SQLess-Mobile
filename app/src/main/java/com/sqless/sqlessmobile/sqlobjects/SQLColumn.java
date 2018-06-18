package com.sqless.sqlessmobile.sqlobjects;

import android.app.Activity;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.network.SQLConnectionManager;
import com.sqless.sqlessmobile.ui.Iconable;
import com.sqless.sqlessmobile.utils.DataTypeUtils;
import com.sqless.sqlessmobile.utils.SQLUtils;

public class SQLColumn extends SQLObject implements Iconable, SQLDroppable, SQLSelectable {

    private String parentName;
    private String datatype;
    private boolean isPK;
    private boolean isFK;
    private boolean nullable;
    private String length;
    /**
     * Numero de decimales
     */
    private String numericScale;
    /**
     * Lo mismo que length pero para números
     */
    private String numericPrecision;
    private boolean autoincrement;
    private String defaultVal;
    private String uncommittedName;
    private boolean isBrandNew;
    private String characterSet;
    private String collation;
    private boolean unsigned;
    private boolean dateTimePrecision;
    private boolean onUpdateCurrentTimeStamp;
    private String enumLikeValues;
    private boolean nullableHasChanged;
    private String firstTimeChangeStatement;

    public SQLColumn(String nombre, String parentName, String datatype) {
        super(nombre);
        this.parentName = parentName;
        this.datatype = datatype;
        uncommittedName = nombre;
        firstTimeChangeStatement = getChangeColumnStatement();
    }

    public SQLColumn(String nombre, String parentName, String datatype, boolean isPK, boolean nullable) {
        super(nombre);
        this.parentName = parentName;
        this.datatype = datatype;
        this.nullable = nullable;
        this.isPK = isPK;
    }

    public SQLColumn(SQLColumn backupCol) {
        super(backupCol.getName());
        this.parentName = backupCol.parentName;
        this.datatype = backupCol.datatype;
        this.isPK = backupCol.isPK;
        this.isFK = backupCol.isFK;
        this.nullable = backupCol.nullable;
        this.length = backupCol.length;
        this.numericScale = backupCol.numericScale;
        this.numericPrecision = backupCol.numericPrecision;
        this.autoincrement = backupCol.autoincrement;
        this.defaultVal = backupCol.defaultVal;
        this.uncommittedName = backupCol.uncommittedName;
        this.characterSet = backupCol.characterSet;
        this.collation = backupCol.collation;
        this.unsigned = backupCol.unsigned;
        this.dateTimePrecision = backupCol.dateTimePrecision;
        this.onUpdateCurrentTimeStamp = backupCol.onUpdateCurrentTimeStamp;
        this.enumLikeValues = backupCol.enumLikeValues;
        this.nullableHasChanged = backupCol.nullableHasChanged;
        firstTimeChangeStatement = getChangeColumnStatement();
    }

    public String getLength() {
        return length;
    }

    public String getNumericScale() {
        return numericScale;
    }

    public String getNumericPrecision() {
        return numericPrecision;
    }

    public boolean isAutoincrement() {
        return autoincrement;
    }

    public String getDefaultVal() {
        return defaultVal;
    }

    public String getUncommittedName() {
        return uncommittedName;
    }

    public String getCharacterSet() {
        return characterSet;
    }

    public String getCollation() {
        return collation;
    }

    public boolean isUnsigned() {
        return unsigned;
    }

    public boolean isDateTimePrecision() {
        return dateTimePrecision;
    }

    public String getEnumLikeValues() {
        return enumLikeValues;
    }

    public void setUncommittedName(String uncommittedName) {
        this.uncommittedName = uncommittedName;
    }

    public String getFirstTimeChangeStatement() {
        return firstTimeChangeStatement;
    }

    public String getDatatype() {
        return datatype;
    }

    public String getDatatypeWithDefaultLength() {
        switch (datatype) {
            case "varchar":
                return "varchar(255)";
            case "decimal":
                return "decimal(10,2)";
        }
        return getDatatype();
    }

    public String getParentName() {
        return parentName;
    }

    public void setIsPK(boolean flag) {
        this.isPK = flag;
    }

    public void setIsFK(boolean flag) {
        this.isFK = flag;
    }

    public boolean isPK() {
        return isPK;
    }

    public void setNumericScale(String numericScale) {
        this.numericScale = numericScale;
    }

    public void setCollation(String collation) {
        this.collation = collation;
    }

    public void setCharacterSet(String characterSet) {
        this.characterSet = characterSet;
    }

    public void setOnUpdateCurrentTimeStamp(boolean flag) {
        this.onUpdateCurrentTimeStamp = flag;
    }

    public void setAutoincrement(boolean autoincrement) {
        this.autoincrement = autoincrement;
    }

    public void setDefaultVal(String defaultVal) {
        this.defaultVal = defaultVal;
    }

    public void setDateTimePrecision(boolean dateTimePrecision) {
        this.dateTimePrecision = dateTimePrecision;
    }

    public void setUnsigned(boolean unsigned, boolean evaluateChanges) {
        this.unsigned = unsigned;
    }

    public String getDataPrecision() {
        if (length != null && length.equals("-1")) {
            return "(255)";
        }
        if (DataTypeUtils.dataTypeIsDecimal(datatype)) {
            return "(" + numericPrecision + "," + numericScale + ")";
        }

        if (numericPrecision != null) {
            return "(" + numericPrecision + ")";
        }

        if (length != null && DataTypeUtils.dataTypeIsStringBased(datatype)) {
            return "(" + length + ")";
        }

        return "";
    }

    @Override
    public int getDrawableRes() {
        return isPK ? R.drawable.ic_primary_key_24dp : isFK ? R.drawable.ic_foreign_key_24dp : R.drawable.ic_column_24dp;
    }

    public void setNumericPrecision(String numericPrecision) {
        this.numericPrecision = numericPrecision;
    }

    public void setLength(String length) {
        if (DataTypeUtils.dataTypeIsStringBased(datatype)) {
            int intLength = Integer.parseInt(length);
            this.length = datatype.equals("text") ? intLength + "" : intLength > 255 ? "255" : length;
        } else {
            setNumericPrecision(length);
        }
    }

    public void setDatatype(Activity context, SQLConnectionManager.ConnectionData connectionData, String datatype) {
        this.datatype = datatype;
        length = datatype.equals("varchar") ? "255" : null;

        if (DataTypeUtils.dataTypeIsStringBased(datatype)) {
            if (collation == null || characterSet == null) {
                SQLUtils.getDbCollationAndCharSetName(context, connectionData, map -> {
                    collation = map.get("collation");
                    characterSet = map.get("charset");
                });
            }
        }

        numericPrecision = DataTypeUtils.dataTypeIsNumeric(datatype) ? "10" : null;
        numericScale = DataTypeUtils.dataTypeIsDecimal(datatype) ? "2" : null;
        dateTimePrecision = DataTypeUtils.dataTypeIsTimeBased(datatype);
        unsigned = DataTypeUtils.dataTypeCanBeUnsigned(datatype);
        onUpdateCurrentTimeStamp = datatype.equals("timestamp");

        if (autoincrement && !DataTypeUtils.dataTypeIsNumeric(datatype)) {
            autoincrement = false;
        }
    }

    public void setNullable(boolean flag) {
        this.nullable = flag;
    }

    public boolean isNullable() {
        return nullable;
    }

    public String getCreateLine() {
        return "`" + getName() + "` " + getDatatypeWithDefaultLength() + " " + (isNullable() ? "NULL" : "NOT NULL");
    }

    public String getEnumLikeValues(boolean includeDataType) {
        if (enumLikeValues == null) {
            return includeDataType ? datatype + "()" : "";
        }
        return includeDataType ? datatype + "(" + enumLikeValues + ")" : enumLikeValues;
    }

    public void setEnumLikeValues(String enumLikeValues) {
        this.enumLikeValues = enumLikeValues;
    }

    @Override
    public String toString() {
        return getName() + " (" + datatype + ")";
    }

    @Override
    public String getDropStatement() {
        return "ALTER TABLE `" + parentName + "` DROP COLUMN `" + getName() + "`";
    }

    public String getChangeColumnStatement() {
        return "ALTER TABLE `" + parentName + "` "
                + ((!getName().equals(uncommittedName) ? "CHANGE COLUMN `" + getName() + "` `" + uncommittedName + "` " : "MODIFY COLUMN `" + getName() + "` ")
                + (getDatatype().equals("enum") || datatype.equals("set") ? getEnumLikeValues(true) : datatype + getDataPrecision())
                + (unsigned ? " UNSIGNED" : "")
                + " " + (DataTypeUtils.dataTypeIsStringBased(datatype) ? "CHARACTER SET " + characterSet + " " + "COLLATE " + collation : "")
                + " " + (nullable ? "NULL" : "NOT NULL")
                + " " + (autoincrement ? "AUTO_INCREMENT " : "")
                + (defaultVal == null || defaultVal.isEmpty() ? "" : "DEFAULT "
                + (DataTypeUtils.dataTypeIsStringBased(datatype) ? "'" + defaultVal + "'" : DataTypeUtils.dataTypeIsTimeBased(datatype) && defaultVal.startsWith("CURRENT") ? defaultVal : "'" + defaultVal + "'"))
                + (onUpdateCurrentTimeStamp ? " ON UPDATE CURRENT_TIMESTAMP" : ""));
    }

    @Override
    public String getSelectStatement(int limit) {
        return "SELECT `" + getName() + "` FROM `"
                + getParentName() + "`" + (limit == SQLSelectable.ALL ? "" : " LIMIT " + limit);
    }
}
