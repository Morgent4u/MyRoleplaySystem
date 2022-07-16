package com.basis.extern;

import com.basis.ancestor.Objekt;
import com.basis.main.main;
import com.basis.sys.Sys;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @Created 02.04.2022
 * @Author Nihar
 * @Description
 * This object is used to update the database
 * with this plugin version.
 *
 * This class has been created with the support of the
 * GitHub-CoPilot project.
 *
 */
public class UPDService extends Objekt
{
    //  Attributs:
    private File file;
    private ArrayList<String> sqlStatements = new ArrayList<>();

    private int lowestDbVersion;
    private int highestDbVersion;

    private int sqlExecutions;
    private int sqlErrors;
    private int sqlSkipped;
    private int sqlStatementSize;

    /* ************************* */
    /* CONSTRUCTOR */
    /* ************************* */

    /**
     * Constructor
     * @param searchDirectory File path which will be used to find the upd-file.
     */
    public UPDService(String searchDirectory)
    {
        //  Search path...
        searchDirectory = searchDirectory + "//UPD//UPD_" + Sys.of_getPaket() + ".upd";
        file = new File(searchDirectory);

        if(!file.exists())
        {
            String errorMessage = null;
            boolean fileCreated = false;

            try
            {
                fileCreated = file.createNewFile();
            }
            catch (Exception e)
            {
                errorMessage = e.getMessage();
            }

            //  If the file could not be created, we send a debug-message.
            if(!fileCreated)
            {
                of_sendErrorMessage(null, "UPDService.constructor()", "The UPD-File could not be created!\n" + errorMessage);
            }
        }
    }

    /* ************************* */
    /* LOADER */
    /* ************************* */

    /**
     * This function loads the SQL-Statements from the file.
     * @return 1 = SQL-Statements loaded. -1 = SQL-Statements not loaded.
     */
    @Override
    public int of_load()
    {
        if(file.exists() && file.length() != 0)
        {
            //  Load the file...
            try
            {
                //  Initialize the BufferedReader...
                FileReader reader = new FileReader(file);
                BufferedReader bufferReader = new BufferedReader(reader);

                StringBuilder sqlStatementsInFile = new StringBuilder();
                String line;
                String flagPattern = "=UPD_VERSION_NUMBER=";

                //  We read each line of the current file...
                while((line = bufferReader.readLine()) != null)
                {
                    if(!line.isEmpty())
                    {
                        //  We only add the SQL-Statement if it is a SQL-Statement or the UPD-Version-Number.
                        if(line.startsWith("-- UPDv=") || !line.startsWith("--"))
                        {
                            //  If it's UPD-Version-Number we set a flag to know what is the SQL-Statement and what is the UPD-Version-Number.
                            if(line.startsWith("-- UPDv="))
                            {
                                sqlStatementsInFile.append(line).append(flagPattern);
                            }
                            else
                            {
                                sqlStatementsInFile.append(line);
                            }
                        }
                    }
                }

                //  We split the SQL-Statements and the UPD-Version-Numbers...
                String[] sqlStmts = sqlStatementsInFile.toString().split(";");
                int noSqlStatements = 0;

                if(sqlStmts.length > 0)
                {
                    for (String sqlStmt : sqlStmts)
                    {
                        //  If it's a UPD-Version-Number...
                        if(sqlStmt.contains(flagPattern))
                        {
                            String[] splitSQLParts = sqlStmt.split(flagPattern);

                            //  It needs to be a length of 2, otherwise the wrong flag was set!
                            if(splitSQLParts.length == 2)
                            {
                                //  UPD-Version
                                sqlStatements.add(splitSQLParts[0]);
                                noSqlStatements++;

                                //  SQL-Statement
                                sqlStatements.add(splitSQLParts[1] + ";");
                            }
                        }
                        else
                        {
                            sqlStatements.add(sqlStmt + ";");
                        }
                    }
                }

                //  Set the SQL-Statement size...
                if(sqlStatements.size() > 0 && noSqlStatements < sqlStatements.size())
                {
                    sqlStatementSize = sqlStatements.size() - noSqlStatements;
                }

                //  Close the file...
                bufferReader.close();
                reader.close();
                return 1;
            }
            catch (Exception e)
            {
                Sys.of_sendErrorMessage(e, "UPDSrv", "of_load();", "Error while loading the file.");
            }
        }
        else
        {
            Sys.of_debug("UPD-File not found: " + file.getAbsolutePath());
        }

        return -1;
    }

    /* ************************* */
    /* OBJECT METHODS */
    /* ************************* */

    /**
     * This function is used to run all required sql-statements for this
     * plugin version.
     * @return 1 = OK. 0 = Nothing to do.
     */
    public int of_runUPD()
    {
        if(sqlStatements.size() != 0)
        {
            //  We need to increase the SQL-StatementSize, because the update of the DB is also a SQL-UPD!
            sqlStatementSize++;
            int currentUPDVersionNumber = -1;
            int executedSQLCounter = 0;
            boolean executeCurrentStatement = false;

            of_sendMessage("========= UPD-Service ("+sqlStatementSize+") =========");

            //  Iterate through all sql-statements...
            for (String sql : sqlStatements)
            {
                //  If it's a UPD-Version-Number...
                if (sql.startsWith("-- UPDv="))
                {
                    //  We check if we can execute the following SQL-Statements which are defined for this UPD-Version...
                    currentUPDVersionNumber = Sys.of_getString2Int(sql.replace("-- UPDv=", "").split("\\.")[3]);
                    executeCurrentStatement = currentUPDVersionNumber != -1 && currentUPDVersionNumber > lowestDbVersion && currentUPDVersionNumber <= highestDbVersion;
                }
                //  Execute current SQL.
                else if (executeCurrentStatement)
                {
                    executedSQLCounter++;
                    boolean bool = main.SQL.of_run_update_suppress(sql);

                    if (bool)
                    {
                        of_sendMessage("SQL-Statement executed: " + executedSQLCounter + "/" + sqlStatementSize);
                        sqlExecutions++;
                    }
                    else
                    {
                        of_sendMessage("SQL-Statement error: " + executedSQLCounter + "/" + sqlStatementSize);
                        sqlErrors++;
                    }
                }
                //  We skip the current SQL.
                else
                {
                    executedSQLCounter++;
                    of_sendMessage("SQL-Statement skipped: " + executedSQLCounter + "/" + sqlStatementSize);
                    sqlSkipped++;
                }
            }

            //  We update the database-version.
            executedSQLCounter++;
            of_updateDatabaseVersion();
            of_sendMessage("SQL-Statement executed: " + executedSQLCounter + "/" + sqlStatementSize);

            //  Conclusion anzeigen...
            of_sendMessage("========= UPD-Service: Conclusion =========");
            of_sendMessage("SQL-Statements executed: " + sqlExecutions);
            of_sendMessage("SQL-Statements skipped: " + sqlSkipped);
            of_sendMessage("SQL-Statements errors: " + sqlErrors);
            of_sendMessage("Database version: " + Sys.of_getVersion());
            of_sendMessage("===========================================");
            return 1;
        }

        return 0;
    }

    private void of_updateDatabaseVersion()
    {
        //  Update the database version...
        String sqlUpdate = "UPDATE "+main.SQL.of_getTableNotation()+"dbversion SET dbVersion = '"+Sys.of_getVersion()+"';";
        main.SQL.of_run_update(sqlUpdate);
        sqlExecutions++;
    }

    public void of_sendMessage(String message)
    {
        Sys.of_sendMessage("[UPD-Service]: " + message);
    }

    /* ************************* */
    /* BOOLS */
    /* ************************* */

    /**
     * This functions checks if the database needs to be updated.
     * @return TRUE = Update is needed. FALSE = Update is not needed.
     */
    public boolean of_isNewUpdateAvailable()
    {
        int lastIndex4UpdVVersion = -1;

        //  We need to find the last UPD-Version-Number in the SQL-Statements.
        for(int i = 0; i < sqlStatements.size(); i++)
        {
            if(sqlStatements.get(i).startsWith("-- UPDv="))
            {
                lastIndex4UpdVVersion = i;
            }
        }

        if(lastIndex4UpdVVersion != -1)
        {
            //  We remove the flag for the UPD-Version.
            String fileUpdVersion = sqlStatements.get(lastIndex4UpdVVersion).replace("-- UPDv=", "");

            //  Check if the database needs to be updated.  If the database version is lower than the plugin version, the database needs to be updated.
            String lastUPDVersion = null;
            String sqlSelect = "SELECT dbVersion FROM "+main.SQL.of_getTableNotation()+"dbversion;";
            String dbVersion = main.SQL.of_getRowValue_suppress(sqlSelect, "dbVersion");

            if(dbVersion != null)
            {
                //  If the database version is equal to the plugin version, there is no UPD to run!
                if(dbVersion.equals(Sys.of_getVersion()))
                {
                    return false;
                }
                //  Last UPD-Version...
                else
                {
                    lastUPDVersion = dbVersion;
                }
            }
            //  Base version.
            else
            {
                lastUPDVersion = "22.1.0.00";
            }

            //  We need to identify the range of the UPD-SQL-Statements.
            highestDbVersion = Sys.of_getString2Int(Sys.of_getVersion().split("\\.")[3]);
            lowestDbVersion = Sys.of_getString2Int(lastUPDVersion.split("\\.")[3]);

            return lowestDbVersion != -1 && highestDbVersion != -1 && highestDbVersion >= lowestDbVersion;
        }

        return false;
    }
}
