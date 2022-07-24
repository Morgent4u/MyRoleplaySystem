package com.basis.extern;

import com.basis.ancestor.Objekt;
import com.basis.main.main;
import com.basis.sys.Sys;
import org.apache.commons.lang.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Arrays;
import java.util.Objects;

/**
 * @Recreated 24.07.2022
 * @Created 21.07.2022
 * @Author Nihar
 * @Description
 * This class represents a DataStore.
 * A datastore is used to handle
 * a SQL-ResultSet very easily.
 *
 * You can make several changes to a
 * datastore and update all changes
 * to the database.
 */
public class DataStore extends Objekt
{
    //  Attributes:
    private ResultSet result = null;
    private String[] columns;
    private String[][] values;
    private String tableName;
    private String pkColumn;
    private String sql;

    //  ColumStatus: I = Insert, U = Update, R = Retrieved, D = Delete
    private String[] rowStatus;

    /* ************************************* */
    /* DEFAULT CONSTRUCTOR */
    /* ************************************* */

    public DataStore(@NotNull String name, @NotNull String tableName, @NotNull String primaryKeyColumnName, @NotNull String sql)
    {
        this.tableName = tableName;
        this.pkColumn = primaryKeyColumnName;
        this.sql = sql;

        //  Set a DataStore-name for the debug-/error-method.
        of_setInfo(name);
    }

    /* ************************************* */
    /* MAIN OBJECT - METHODS */
    /* ************************************* */

    /**
     * This method is used to send the given SQL from the constructor
     * to the database-server. This needs to be called before you
     * try to get some values from the DataStore.
     * @return N = Row count of retrieved rows, -1 = Error while retrieving data from database.
     */
    public int of_retrieve()
    {
        result = main.SQL.of_getResultSet(sql, false);

        if(result != null)
        {
            //  We want the column-types so we use the ResultMeta.
            try
            {
                ResultSetMetaData data = result.getMetaData();
                String[] columnTypes = new String[0];
                int columnCount = data.getColumnCount();

                //  Iterate through the column-header.
                for(int dbRow = 1; dbRow <= columnCount; dbRow++)
                {
                    columns = Sys.of_addArrayValue(of_getColumns(), data.getColumnName(dbRow));
                    columnTypes = Sys.of_addArrayValue(columnTypes, data.getColumnTypeName(dbRow));
                }

                //  Initialize the values-array.
                columnCount = columns.length;
                values = new String[columnCount][];

                //  Add the first-row-entry into the DataStore.
                int row = of_addRow();

                for(int i = 0; i < columnCount; i++)
                {
                    of_setItemString(row, columns[i], columnTypes[i]);
                }

                //  Iterate through each row.
                while(result.next())
                {
                    row = of_addRow();

                    for(String columnName : Objects.requireNonNull(of_getColumns()))
                    {
                        Object dbValue = result.getObject(columnName);
                        String value = null;

                        if(dbValue instanceof Integer)
                        {
                            value = String.valueOf((int) dbValue);
                        }
                        else if(dbValue instanceof Double)
                        {
                            value = String.valueOf((double) dbValue);
                        }
                        else
                        {
                            value = String.valueOf(dbValue);
                        }

                        of_setItemString(row, columnName, value);
                    }
                }

                //  We use the of_setItem-Method while we're retrieving data,
                //  so we need to reset the status.
                Arrays.fill(rowStatus, "R");

                return of_getRowCount();
            }
            catch (Exception exception)
            {
                of_sendErrorMessage(exception, "DataStore.of_retrieve();", "There was an error while retrieving data from this DataStore!");
            }
        }

        return -1;
    }

    /**
     * This method is used to update all changes from a datastore
     * to the database.
     * @return N = Amount of executed SQls, -1 An error occurred.
     */
    public int of_update()
    {
        //  If the validation-check does not pass!
        if(of_validate() != null)
        {
            return -1;
        }

        String[] columns = of_getColumns();
        int columnCount = Objects.requireNonNull(columns).length;
        int executedSQLs = 0;

        //  Check the RowStatus:
        for(int row = 1; row < rowStatus.length; row++)
        {
            String sql = null;

            switch (rowStatus[row])
            {
                case "U":
                    //  UPDATE table SET column1 = 'value1' WHERE pkColumn = pkValue;
                    StringBuilder sqlBuilder = new StringBuilder();
                    sql = "UPDATE " + of_getTableName() + " SET ";

                    for (int i = 0; i < columnCount; i++)
                    {
                        sqlBuilder.append(columns[i]).append(" = ").append(of_getColumnDataFormat4Value(columns[i], of_getItemString(row, columns[i])));

                        if (i != (columnCount - 1))
                        {
                            sqlBuilder.append(", ");
                        }
                    }

                    //  Build the SQL-Statement.
                    String pkColumn = of_getPrimaryKeyColumn();
                    sql += sqlBuilder + " WHERE " + of_getTableName() + "." + pkColumn + " = " + of_getColumnDataFormat4Value(pkColumn, of_getItemString(row, pkColumn) + ";");
                    break;
                case "I":
                    //  INSERT INTO table( column1, column2, column3 ) VALUES( 'value1', 'value2', 'value3' );
                    StringBuilder tableBuilder = new StringBuilder();
                    StringBuilder valueBuilder = new StringBuilder();
                    sql = "INSERT INTO " + of_getTableName() + " ( ";

                    //  Build the table-list.
                    for (int i = 0; i < columnCount; i++)
                    {
                        tableBuilder.append(columns[i]);
                        valueBuilder.append(of_getColumnDataFormat4Value(columns[i], of_getItemString(row, columns[i])));

                        if (i != (columnCount - 1))
                        {
                            tableBuilder.append(", ");
                            valueBuilder.append(", ");
                        }
                    }

                    //  Add it to the SQL.
                    sql += tableBuilder + " ) VALUES ( " + valueBuilder + " );";
                    break;
                case "D":
                    //  DELETE FROM table WHERE table.pkColumn = pkValue;
                    sql = "DELETE FROM " + of_getTableName() + " WHERE " + of_getTableName() + "." + of_getPrimaryKeyColumn() + " = " + of_getColumnDataFormat4Value(of_getPrimaryKeyColumn(), of_getItemString(row, of_getPrimaryKeyColumn()));
                    break;
            }

            //  Check if we have an SQL-Statement to execute.
            if(sql != null)
            {
                //  Reset the RowStatus of the row.
                rowStatus[row] = "R";

                if(!main.SQL.of_run_update(sql))
                {
                    of_sendErrorMessage(null, "DataStore.of_update();", "There was an error while updating the DataStore to the database!\n Could not execute the following SQL:\n" + sql);
                    return -1;
                }

                //  Increase the number of executed sql-statements.
                executedSQLs++;
            }
        }

        return executedSQLs;
    }

    /* ************************************* */
    /* OBJECT - METHODS */
    /* ************************************* */

    /**
     * This method is used to identify more than one row by the given object-value.
     * @param columnName The column which contains the given object-value.
     * @param value The value to identify a specific row (for example: Id-column)
     * @return An array with stored row-numbers. Null if an error occurs or no rows could be found!
     */
    public int[] of_findRows(String columnName, Object value)
    {
        //  If the validation-check does not pass!
        if(of_validate() != null)
        {
            return null;
        }

        String stringValue = null;
        int columnIndex = ArrayUtils.indexOf(of_getColumns(), columnName);

        //  Parse the given object into the right string-type!
        if(value instanceof Double)
        {
            stringValue = String.valueOf((double) value);
        }
        else if(value instanceof Integer)
        {
            stringValue = String.valueOf((int) value);
        }
        else
        {
            stringValue = String.valueOf(value);
        }

        if(columnIndex != -1)
        {
            String[] tmpValues = values[columnIndex];
            int[] collectedEntries = new int[0];
            int rowCount = tmpValues.length;

            //  We start by 1.
            for(int row = 1; row < rowCount; row++)
            {
                if(tmpValues[row].equals(stringValue))
                {
                    collectedEntries = ArrayUtils.add(collectedEntries, row);
                }
            }

            return collectedEntries;
        }

        return null;
    }

    /**
     * This method is used to identify a row by the given object-value.
     * @param columnName The column which contains the given object-value.
     * @param value The value to identify a specific row (for example: Id-column)
     * @return N = The row number, -1 = If no row has been found.
     */
    public int of_findRow(String columnName, Object value)
    {
        //  If the validation-check does not pass!
        if(of_validate() != null)
        {
            return -1;
        }

        String stringValue = null;
        int columnIndex = ArrayUtils.indexOf(of_getColumns(), columnName);

        //  Parse the given object into the right string-type!
        if(value instanceof Double)
        {
            stringValue = String.valueOf((double) value);
        }
        else if(value instanceof Integer)
        {
            stringValue = String.valueOf((int) value);
        }
        else
        {
            stringValue = String.valueOf(value);
        }

        if(columnIndex != -1)
        {
            String[] tmpValues = values[columnIndex];
            int rowCount = tmpValues.length;

            //  We start by 1.
            for(int row = 1; row < rowCount; row++)
            {
                if(tmpValues[row].equals(stringValue))
                {
                    return row;
                }
            }
        }

        return -1;
    }

    /* ************************************* */
    /* VALIDATION */
    /* ************************************* */

    @Override
    public String of_validate()
    {
        String errorMessage = null;

        if(result == null)
        {
            errorMessage = "You need to call of_retrieve(); before handling with a DataStore!";
            of_sendErrorMessage(null, "DataStore.of_validate();", errorMessage);
        }

        return errorMessage;
    }

    /* ************************************* */
    /* DEBUG - CENTER */
    /* ************************************* */

    /**
     * This method is used to print a full
     * table view into the terminal/console.
     */
    public void of_sendDebugTableInformation()
    {
        int rowCount = of_getRowCount();
        Sys.of_sendMessage("===== DATA-STORE - START =======");
        of_sendDebugDetailInformation();
        if(rowCount > 0)
        {
            //  Build the column-header.
            StringBuilder columnHeader = new StringBuilder();
            columnHeader.append("Row");

            for(String column : Objects.requireNonNull(of_getColumns()))
            {
                columnHeader.append(" | ").append(column);
            }

            //  Send the column-header.
            Sys.of_sendMessage("================");
            Sys.of_sendMessage(columnHeader.toString());

            //  Iterate through all rows.
            for(int rowIndex = 0; rowIndex < rowCount; rowIndex++)
            {
                StringBuilder rowBuilder = new StringBuilder();
                rowBuilder.append(rowIndex);

                for(int columnIndex = 0; columnIndex < Objects.requireNonNull(of_getColumns()).length; columnIndex++)
                {
                    rowBuilder.append(" | ").append(values[columnIndex] [rowIndex]);
                }

                Sys.of_sendMessage(rowBuilder.toString());
            }
        }
        Sys.of_sendMessage("===== DATA-STORE - END =======");
    }

    @Override
    public void of_sendDebugDetailInformation()
    {
        Sys.of_sendMessage("DataStore-Name: " + of_getInfo());
        Sys.of_sendMessage("Table: " + of_getTableName());
        Sys.of_sendMessage("PK-Column: "+of_getPrimaryKeyColumn());
        Sys.of_sendMessage("SQL: " + of_getSQL());

        //  Need to be handled because of_getRowCount could because a NULL-Pointer Exception!
        try
        {
            Sys.of_sendMessage("Retrieved rows: "+of_getRowCount());
        }
        catch (Exception ignored) { }
    }

    /* ************************************* */
    /* SETTER // ADDER // REMOVER */
    /* ************************************* */

    public void of_setItemString(int row, String columnName, String value)
    {
        //  If the validation-check does not pass!
        if(of_validate() != null)
        {
            return;
        }

        int columnIndex = ArrayUtils.indexOf(of_getColumns(), columnName);

        if(columnIndex != -1)
        {
            String[] tmpValues = values[columnIndex];
            tmpValues[row] = value;
            of_setRowStatus(row, "U");
            values[columnIndex] = tmpValues;
        }
    }

    /**
     * This method is used to add a new
     * row to the DataStore.
     * The initial value of each column-value is 'null'.
     * @return N = Row number, -1 = An error occurred.
     */
    public int of_addRow()
    {
        //  If the validation-check does not pass!
        if(of_validate() != null)
        {
            return -1;
        }

        for(int i = 0; i < Objects.requireNonNull(of_getColumns()).length; i++)
        {
            String[] tmpValues = values[i];
            values[i] = Sys.of_addArrayValue(tmpValues, "null");
        }

        //  We need to update the RowStatus.
        int rowCount = of_getRowCount() - 1;
        of_setRowStatus(rowCount, "I");

        return rowCount;
    }

    /**
     * This method is used to set a RowStatus for the given row.
     * R = Retrieved from DB, I = Insert, U = Update, D = Delete
     * @param row The row which RowStatus should be changed.
     * @param rowStatusString Needs to be R, I, U or D.
     */
    private void of_setRowStatus(int row, String rowStatusString)
    {
        if(rowStatus == null || row > ( rowStatus.length - 1 ))
        {
            rowStatus = Sys.of_addArrayValue(rowStatus, rowStatusString);
        }

        //  We only change the RowStatus to UPDATE if it has been retrieved before!
        if(rowStatusString.equals("U"))
        {
            if( rowStatus[row].equals("R"))
            {
                rowStatus[row] = "U";
            }
        }
        //  R, I, D
        else
        {
            rowStatus[row] = rowStatusString;
        }
    }

    /* ************************************* */
    /* GETTER */
    /* ************************************* */

    /**
     * This method is used to get all column-entries from
     * the given column-name.
     * @param columnName The column from which the values should be from.
     * @return A string which contains all values. Null if the column does not exist!
     */
    public String[] of_getColumnEntries(String columnName)
    {
        //  If the validation-check does not pass!
        if(of_validate() != null)
        {
            return null;
        }

        int columnIndex = ArrayUtils.indexOf(of_getColumns(), columnName);

        if(columnIndex != -1)
        {
            return values[columnIndex];
        }

        return null;
    }

    /**
     * This method is used to get a string-value from the given row.
     * @param row The row-number in which the specific value has been stored.
     * @param columnName The column-name where the value is stored.
     * @return A string of the value. Null if the column does not exist!
     */
    public String of_getItemString(int row, String columnName)
    {
        //  If the validation-check does not pass!
        if(of_validate() != null)
        {
            return null;
        }

        int columnIndex = ArrayUtils.indexOf(of_getColumns(), columnName);

        if(columnIndex != -1)
        {
            return values[columnIndex][row];
        }

        return null;
    }

    public int of_getItemInteger(int row, String columnName)
    {
        return Sys.of_getString2Int(of_getItemString(row, columnName));
    }

    private String of_getColumnDataFormat4Value(String columnName, String value)
    {
        //  If the validation-check does not pass!
        if(of_validate() != null)
        {
            return null;
        }

        String columnType = of_getItemString(0, columnName);

        if(columnType == null)
        {
            columnType = "CHAR";
        }

        //  Return the specified column-format.
        switch (columnType)
        {
            case "VARCHAR":
            case "CHAR":
            case "DATETIME":
                if(!value.equals("null"))
                {
                    value = "'"+value+"'";
                    break;
                }
        }

        return value;
    }

    public int of_getRowCount()
    {
        return values[0].length;
    }

    private String[] of_getColumns()
    {
        //  If the validation-check does not pass!
        if(of_validate() != null)
        {
            return null;
        }

        return columns;
    }

    private String of_getTableName()
    {
        return tableName;
    }

    private String of_getPrimaryKeyColumn()
    {
        return pkColumn;
    }

    public String of_getSQL()
    {
        return sql;
    }
}
