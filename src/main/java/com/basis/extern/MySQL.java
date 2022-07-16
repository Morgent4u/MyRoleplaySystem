package com.basis.extern;

import com.basis.sys.Sys;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

public class MySQL
{
    /*	Angelegt am: 03.02.2021
     * 	Überarbeitet am: 28.11.2021 -> für dieses Projekt :)
     * 	Erstellt von: Nihar
     * 	Beschreibung:
     * 	Verwaltung der DB-Anbindung.
     *
     */

    Connection con;
    Statement stm = null;

    String white = "\u001B[0m";
    String green = "\u001B[32m";

    String instanceName;
    String connectionString;

    String server;
    String dbName;
    String userName;
    String password;

    //	Update-Stuff
    String updateKeyTable;
    String updateKeyColumn;
    String updateTableColumn4Key;
    String tableNotation;

    /* ************************* */
    /* CONSTRUCTOR */
    /* ************************* */

    /**
     * Constructor of the transaction-object.
     * @param instanzName Instance name for example: 'MAIN' or 'FAST SQL'
     *                    This is used to get a difference between multiple transaction-object instances.
     */
    public MySQL(String instanzName)
    {
        this.instanceName = "[MySQL-"+instanzName+"]:";
    }

    /* ************************* */
    /* OBJEKT-ANWEISUNGEN */
    /* ************************* */

    //	Anhand aller Angaben ConnectionString bauen und Verbindung
    //	herstellen.

    /**
     * Creates a database connection to the defined database in the settings.
     * @return 1 = SUCCESS, -1 = ERROR - An error will be displayed in the console!
     */
    public int of_createConnection()
    {
        Sys.of_debug(instanceName + " Try to connect to the following database: " + dbName);

        //	Validierung
        String errorMessage = of_validate();

        if(errorMessage != null)
        {
            Sys.of_sendErrorMessage(null, "MySQL", "of_createConnection(); #10", "Error while validating the settings. "+errorMessage);
            return -1;
        }

        //	Registrierung der JDBC-Driver Klasse
        try
        {
            Class.forName("com.mysql.jdbc.Driver");
        }
        catch (ClassNotFoundException e)
        {
            Sys.of_sendErrorMessage(e, "MySQL", "of_createConnection(); #20", "Error while searching for the Class: com.mysql.jdbc.Driver");
            return -1;
        }

        //	Verbindungsaufbau
        try
        {
            //	Connection-String bauen :)
            connectionString = "jdbc:mysql://"+server+"/"+dbName+"?autoReconnect=true";
            con = DriverManager.getConnection(connectionString, userName, password);
        }
        catch (SQLException e)
        {
            Sys.of_sendErrorMessage(e, "MySQL", "of_createConnection(); #30", "Error while creating the DriverManager instance!");
            return -1;
        }

        //	Verbindung zur Datenbank wurde hergestellt.
        if(of_isConnected())
        {
            //	Debugs...
            Sys.of_debug(instanceName +" Successfully connected to database "+green + dbName + white);
            return 1;
        }

        return -1;
    }

    /**
     * Validation process for this transfer object.
     * This function checks if all necessary attributes has been set.
     * @return EMPTY-String = SUCCESS, NO-EMPTY-String = Error - A message with the error message.
     */
    public String of_validate()
    {
        if(server == null || server.isEmpty())
        {
            return "The server address need to be set in the settings!";
        }

        if(dbName == null ||dbName.isEmpty())
        {
            return "The database name need to be set in the settings!";
        }

        if(userName == null || userName.isEmpty())
        {
            return "The username or logId need to be set in the settings!";
        }

        if(password == null || password.isEmpty())
        {
            return "The password need to be set in the settings!";
        }

        if(updateKeyTable == null || updateKeyColumn == null || updateTableColumn4Key == null)
        {
            return "This is a development error please contact the author. He has forgotten to set the 'updateKeyTable', 'updateKeyColumn' or 'updateTableColumn4Key'!";
        }

        return null;
    }

    /**
     * This function close the database connection.
     */
    public void of_closeConnection()
    {
        if(con != null)
        {
            try
            {
                Sys.of_debug(instanceName +" Successfully disconnected from database "+green + dbName + white);
                con.close();

                if(con.isClosed())
                {
                    con = null;
                }
            }
            catch (Exception ignored)  { }
        }
    }

    /* ************************* */
    /* RUN UPDATE */
    /* ************************* */

    /**
     * This function executes a sql-statement. For example:
     * 'UPDATE user SET user.name = 'Test' WHERE user.userId = 1;'
     * @param sql_insert_update The sql-statement which will be executed.
     * @param bool Displays an error message.
     * @return TRUE = SUCCESS, FALSE = ERROR - Error message only appears when the parameter 'bool' is true!
     */
    private boolean of_run_update(String sql_insert_update, boolean bool)
    {
        try
        {
            if(stm == null || stm.isClosed())
            {
                stm = con.createStatement();
            }

            stm.executeUpdate(sql_insert_update);
            return true;
        }
        catch (SQLException e)
        {
            if(bool)
            {
                Sys.of_sendErrorMessage(null, "MySQL", "of_run_update("+ true +");", sql_insert_update);
            }
        }

        return false;
    }

    /**
     * Overload function of of_run_update();
     * This function is using by default display error message.
     * @param sql_insert_update The sql-statement which will be executed.
     * @return TRUE = SUCCESS, FALSE = ERROR - <b>Message will be displayed in the console!</b>.
     */
    public boolean of_run_update(String sql_insert_update)
    {
        return of_run_update(sql_insert_update, true);
    }

    /**
     * Overload function of of_run_update();
     * This function is using by default don't display error message.
     * @param sql_insert_update The sql-statement which will be executed.
     * @return TRUE = SUCCESS, FALSE = ERROR - <b>No Message will be displayed in the console!</b>.
     */
    public boolean of_run_update_suppress(String sql_insert_update)
    {
        return of_run_update(sql_insert_update, false);
    }

    /* ************************* */
    /* GET ONE ROW VALUE */
    /* ************************* */

    /**
     * This function is used to retrieve a one value/column from the database.
     * @param sql_select_query SQL-statement which will be executed.
     * @param column_name Column name from which the value should be from.
     * @param bool Display an error message into the console.
     * @return Value from the column which is specified in the sql-statement and the parameter column_name.
     */
    private String of_getRowValue(String sql_select_query, String column_name, boolean bool)
    {
        ResultSet rs = null;

        try
        {
            if(stm == null || stm.isClosed())
            {
                stm = con.createStatement();
            }

            rs = stm.executeQuery(sql_select_query);
            rs.next();

            return rs.getString(column_name);
        }
        catch (SQLException e)
        {
            if(bool)
            {
                Sys.of_sendErrorMessage(null, "MySQL", "of_getRowValue("+ true +");", sql_select_query);
            }
        }

        return null;
    }

    /**
     * Overload for of_getRowValue(String sqlStatement, String column_name, boolean displayErrorMessage)
     * <b>This function sends by default an error message into the console!</b>.
     * @param sql_select_query SQL-Statement which will be executed.
     * @param column_name Column name from which the value should be from.
     * @return Value from the column which is specified in the sql-statement and the parameter column_name.
     */
    public String of_getRowValue(String sql_select_query, String column_name)
    {
        return of_getRowValue(sql_select_query, column_name, true);
    }

    /**
     * Overload for of_getRowValue(String sqlStatement, String column_name, boolean displayErrorMessage)
     * <b>This function sends by default no error message into the console!</b>.
     * @param sql_select_query SQL-Statement which will be executed.
     * @param column_name Column name from which the value should be from.
     * @return Value from the column which is specified in the sql-statement and the parameter column_name.
     */
    public String of_getRowValue_suppress(String sql_select_query, String column_name)
    {
        return of_getRowValue(sql_select_query, column_name, false);
    }

    /* ************************* */
    /* GET A RESULT SET */
    /* ************************* */

    /**
     * This function returns a ResultSet. This can be used for execute a multirow select-statement.
     * For example:
     * ResultSet result = MySQL.of_getResultSet("SELECT * FROM user;", false, false);
     * @param sql_select_query_rows SQL-Statement which will be executed.
     * @param bool Display error message
     * @param result_next This should be FALSE when you're using a multirow SELECT-Statement. This can be TRUE
     *                    if you know that your result will be one row, and you want to get data from more COLUMN-Data than one!
     * @return Returns a ResultSet for external using!
     */
    private ResultSet of_getResultSet(String sql_select_query_rows, boolean bool, boolean result_next)
    {
        ResultSet rs = null;

        try
        {
            if(stm == null || stm.isClosed())
            {
                stm = con.createStatement();
            }

            rs = stm.executeQuery(sql_select_query_rows);

            if(result_next)
            {
                if(rs.next())
                {
                    return rs;
                }
            }

            return rs;
        }
        catch (SQLException e)
        {
            if(bool)
            {
                Sys.of_sendErrorMessage(null, "MySQL", "of_getResultSet("+ true +");", sql_select_query_rows);
            }
        }

        return null;
    }

    /**
     * Overload: This function returns a ResultSet. This can be used for execute a multirow select-statement.
     * For example:
     * ResultSet result = MySQL.of_getResultSet("SELECT * FROM user;", false, false);
     * <b>This function sends by default an error message to the console!</b>
     * @param sql_select_query_rows SQL-Statement which will be executed.
     * @param result_next This should be FALSE when you're using a multirow SELECT-Statement. This can be TRUE
     *                    if you know that your result will be one row, and you want to get data from more COLUMN-Data than one!
     * @return Returns a ResultSet for external using!
     */
    public ResultSet of_getResultSet(String sql_select_query_rows, boolean result_next)
    {
        return of_getResultSet(sql_select_query_rows, true, result_next);
    }

    /**
     * Overload: This function returns a ResultSet. This can be used for execute a multirow select-statement.
     * For example:
     * ResultSet result = MySQL.of_getResultSet("SELECT * FROM user;", false, false);
     * <b>This function sends by default no error message to the console!</b>
     * @param sql_select_query_rows SQL-Statement which will be executed.
     * @param result_next This should be FALSE when you're using a multirow SELECT-Statement. This can be TRUE
     *                    if you know that your result will be one row, and you want to get data from more COLUMN-Data than one!
     * @return Returns a ResultSet for external using!
     */
    public ResultSet of_getResultSet_suppress(String sql_select_query_rows, boolean result_next)
    {
        return of_getResultSet(sql_select_query_rows, false, result_next);
    }

    /**
     * This function is used to get a primary-key for a table by using an external key-column-control table.
     * For this function your code must have already executed following function: of_setUpdateKeyTableAndColumns(String, String, String);
     * @param tableName Table name from which table you want to get the primary-key.
     * @return Primary-key id.
     */
    public int of_updateKey(String tableName)
    {
        //	For this method the updateKey-Strings need to be set!
        if(!updateKeyTable.isEmpty() && !updateKeyColumn.isEmpty() && !updateTableColumn4Key.isEmpty() && tableNotation != null)
        {
            //  Get the current-key value.
            String sqlSelect = "SELECT " + updateKeyColumn + " FROM " + updateKeyTable + " WHERE " + updateTableColumn4Key + " = '"+tableName+"';";
            int key = -1;

            try
            {
                key = Integer.parseInt(Objects.requireNonNull(of_getRowValue(sqlSelect, updateKeyColumn, false)));
            }
            catch (Exception ignored) { }

            //  Error while getting the key-value, does the table has been inserted to the key-table?
            if(key == -1)
            {
                Sys.of_sendErrorMessage(null, "MySQL", "of_updateKey("+tableName+");", "This is a SQL-problem might be the table entry doesn't exist! SQL: "+sqlSelect);
                return -1;
            }

            //  Increase the key-value and update the key-control table.
            key++;
            String sqlUpdate = "UPDATE "+updateKeyTable+" SET " + updateKeyColumn + " = " + key + " WHERE " + updateTableColumn4Key + " = '" + tableName + "';";

            //  If an error occurred we need to return -1 to avoid a primary-key problem!
            if(!of_run_update(sqlUpdate))
            {
                key = -1;
            }

            return key;
        }

        return -1;
    }

    /* ****************************** */
    /* SETTER // REMOVER // ADDER */
    /* ****************************** */

    public void of_setServer(String server)
    {
        this.server = server;
    }

    public void of_setDbName(String database)
    {
        this.dbName = database;
    }

    public void of_setUserName(String username)
    {
        this.userName = username;
    }

    public void of_setPassword(String password)
    {
        this.password = password;
    }

    /**
     * <b>This function needs to be executed after initialising this object!</b>
     * @param updateKeyTable Name of the table which controls the primary-keys for example: '%_keys'
     * @param updateKeyColumn The column which contains the key for example: 'lastkey'
     * @param updateTableColumn4Key The column which contains the table name for example: 'tablename'
     */
    public void of_setUpdateKeyTableAndColumns(String updateKeyTable, String updateKeyColumn, String updateTableColumn4Key)
    {
        this.updateKeyTable = updateKeyTable;
        this.updateKeyColumn = updateKeyColumn;
        this.updateTableColumn4Key = updateTableColumn4Key;

        //  Notation raus filtern und setzen (wird z.B. für den UPD-Service benötigt, sowie den Table-Update!
        tableNotation = updateKeyTable.split("_")[0] + "_";
    }

    /* ****************************** */
    /* GETTER */
    /* ****************************** */

    public String of_getTableNotation()
    {
        return tableNotation;
    }

    public String of_getTimeStamp()
    {
        return "CURRENT_TIMESTAMP()";
    }

    /* ****************************** */
    /* BOOLS */
    /* ****************************** */

    /**
     * This function checks if this transaction-object is still connected to the database.
     * @return TRUE = Connected, FALSE = No connection to the database!
     */
    public boolean of_isConnected()
    {
        boolean lb_value = false;

        if(con != null)
        {
            try
            {
                lb_value = !con.isClosed();
            }
            catch (Exception ignored) { }
        }

        return lb_value;
    }
}