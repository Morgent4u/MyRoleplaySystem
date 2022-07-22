package com.basis.extern;

import com.basis.ancestor.Objekt;
import com.basis.main.main;
import com.basis.sys.Sys;
import org.apache.commons.lang.ArrayUtils;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashMap;

/**
 * @Created 21.07.2022
 * @Author Nihar
 * @Description
 * This class represents a DataStore.
 * A datastore is used to handle
 * a ResultSet very easily.
 */
public class DataStore extends Objekt
{
    //  Attributes:
    //  Column-Name, Array of the given type (String, Integer).
    private HashMap<String, String[]> fetchedData;
    private ResultSet resultSet;
    private String sql;

    /* ************************************* */
    /* CONSTRUCTOR */
    /* ************************************* */

    /**
     * Default constructor for the data-store object.
     * @param sql Defined SQL which will be executed when calling
     *            the of_retrieve()-method.
     */
    public DataStore(String name, String sql)
    {
        this.sql = sql;
        of_setInfo(name);
    }

    /* ************************************* */
    /* OBJECT - METHODS */
    /* ************************************* */

    /**
     * This method is used to send the given SQL from the constructor
     * to the database-server. This needs to be called before you
     * try to get some values from the DataStore.
     * @return N = Row count, -1 = Error while retrieving data.
     */
    public int of_retrieve()
    {
        resultSet = main.SQL.of_getResultSet(sql, false);

        if(resultSet != null)
        {
            fetchedData = new HashMap<>();

            try
            {
                //  We need the ResultSetMetaData
                ResultSetMetaData meta = resultSet.getMetaData();
                int columnCount = meta.getColumnCount();

                //  Get all columns by its name!
                for (int i = 1; i <= columnCount; i++ )
                {
                    String columName = meta.getColumnName(i);

                    if(columName != null && !columName.isEmpty())
                    {
                        fetchedData.put(columName, new String[0]);
                    }
                }

                //  Iterate through all rows!
                while(resultSet.next())
                {
                    for(String columnName : fetchedData.keySet())
                    {
                        Object value = resultSet.getObject(columnName);
                        String stringValue = null;

                        if(value instanceof Integer)
                        {
                            stringValue = String.valueOf((int) value);
                        }
                        else if(value instanceof Double)
                        {
                            stringValue = String.valueOf((double) value);
                        }
                        else
                        {
                            stringValue = String.valueOf(value);
                        }

                        fetchedData.put(columnName, Sys.of_addArrayValue(fetchedData.get(columnName), stringValue));
                    }
                }

                //  To save memory.
                resultSet.close();

                //  Return the retrieved row count.
                return of_getRowCount();
            }
            catch (Exception ignored) { }
        }

        return -1;
    }

    /**
     * This method is used to get the rowId by the given searchValue.
     * @param searchColumn The column in which the searchValue can be found!
     * @param searchValue The search value is needed to identify the row.
     * @return N = The rowId, -1 = If no row has been found.
     */
    public int of_findRow(String searchColumn, Object searchValue)
    {
        if(of_validate() == null)
        {
            String stringValue = null;

            if(searchValue instanceof Integer)
            {
                stringValue = String.valueOf((int) searchValue);
            }
            else if(searchValue instanceof Double)
            {
                stringValue = String.valueOf((double) searchValue);
            }
            else
            {
                stringValue = String.valueOf(searchValue);
            }

            if(stringValue != null)
            {
                String[] values = fetchedData.get(searchColumn);

                if(values != null && values.length > 0)
                {
                    for(int row = 0; row < values.length; row++)
                    {
                        if(values[row].equals(stringValue))
                        {
                            return row;
                        }
                    }
                }
            }
        }

        return -1;
    }

    /* ************************************* */
    /* VALIDATE - OBJECT */
    /* ************************************* */

    @Override
    public String of_validate()
    {
        String errorMessage = null;

        if(resultSet == null)
        {
            errorMessage = "You need to call of_retrieve(); before getting values from a data-store!";
            of_sendErrorMessage(null, "DataStore.of_validate();", errorMessage);
        }

        return errorMessage;
    }

    /* ************************************* */
    /* DEBUG CENTER */
    /* ************************************* */

    /**
     * This method is used to print a full
     * table view into the terminal/console.
     */
    public void of_sendDebugTableInformation()
    {
        Sys.of_sendMessage("===== DATA-STORE - START =======");
        Sys.of_sendMessage("Name: " + of_getInfo());
        Sys.of_sendMessage("Has an error: " + of_hasAnError());
        int rowCount = of_getRowCount();
        Sys.of_sendMessage("Retrieved rows: "+rowCount);

        if(rowCount > 0)
        {
            //  Build the column-header.
            StringBuilder columnHeader = new StringBuilder();
            columnHeader.append("Row");

            for(String column : fetchedData.keySet())
            {
                columnHeader.append(" | ").append(column);
            }

            Sys.of_sendMessage(columnHeader.toString());

            //  Iterate through all columns/rows.
            for(int i = 0, row = 1; i < rowCount; i++, row++)
            {
                StringBuilder rowBuilder = new StringBuilder();
                rowBuilder.append(row);

                for(String column : fetchedData.keySet())
                {
                    rowBuilder.append(" | ").append(fetchedData.get(column) [i]);
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
        Sys.of_sendMessage("SQL: " + of_getSQL());
    }

    /* ************************************* */
    /* GETTER */
    /* ************************************* */

    /**
     * This method is used to collect all row-ids of column entries
     * which are equal to the searchValue.
     * @param columnName The column-name of the SQL-Select.
     * @param searchValue The collect criteria.
     * @return Null or an integer-array with the row-ids.
     */
    public int[] of_findRows(String columnName, Object searchValue)
    {
        if(of_validate() == null)
        {
            if(columnName != null && searchValue != null)
            {
                //  Parse the given object into the right type.
                String stringValue = null;

                if(searchValue instanceof Integer)
                {
                    stringValue = String.valueOf((int) searchValue);
                }
                else if(searchValue instanceof Double)
                {
                    stringValue = String.valueOf((double) searchValue);
                }
                else
                {
                    stringValue = String.valueOf(searchValue);
                }

                //  Get all column-values.
                String[] values = fetchedData.get(columnName);

                if(values != null && values.length > 0)
                {
                    int[] collectedEntries = new int[0];

                    for(int row = 0; row < values.length; row++)
                    {
                        if(values[row].equals(stringValue))
                        {
                            collectedEntries = ArrayUtils.add(collectedEntries, row);
                        }
                    }

                    return collectedEntries;
                }
            }
        }

        return null;
    }

    /**
     * This method is used to get the string-value
     * from the given column and row.
     * @param row The row in which the value should be.
     * @param columnName The column in which the value should be.
     * @return The value of the type String, or NULL if no value could be found.
     */
    public String of_getItemString(int row, String columnName)
    {
        if(of_validate() == null && row != -1)
        {
            String[] values = fetchedData.get(columnName);

            if(values != null && row < values.length)
            {
                return values[row];
            }
        }

        return null;
    }

    /**
     * This method is used to get the integer-value
     * from the given column and row.
     * @param row The row in which the value should be.
     * @param columnName The column in which the value should be.
     * @return The value of the type Integer, or -1 if no value could be found.
     */
    public int of_getItemInteger(int row, String columnName)
    {
        String integerValue = of_getItemString(row, columnName);

        if(integerValue != null)
        {
            return Sys.of_getString2Int(integerValue);
        }

        return -1;
    }

    /**
     * This method is used to get the string-value
     * from the given column and row.
     * @param row The row in which the value should be.
     * @param columnName The column in which the value should be.
     * @return The value of the type Double, or -1 if no value could be found.
     */
    public double of_getItemDouble(int row, String columnName)
    {
        String doubleValue = of_getItemString(row, columnName);
        double tmpDouble = -1;

        if(doubleValue != null)
        {
            try
            {
                tmpDouble = Double.parseDouble(doubleValue);
            }
            catch (Exception ignored) { }
        }

        return tmpDouble;
    }

    public ResultSet of_getResultSet()
    {
        return resultSet;
    }

    public String of_getSQL()
    {
        return sql;
    }

    public int of_getRowCount()
    {
        if(of_validate() == null)
        {
            String[] keyColumns = fetchedData.keySet().toArray(new String[0]);

            if(keyColumns.length > 0)
            {
                //  Get the Array-String with all row-values and return the size/length!
                return fetchedData.get(keyColumns[0]).length;
            }
        }

        return -1;
    }
}
