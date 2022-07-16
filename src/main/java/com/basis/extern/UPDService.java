package com.basis.extern;

import com.basis.ancestor.Objekt;
import com.basis.main.main;
import com.basis.sys.Sys;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * @Created 02.04.2022
 * @Rework  17.07.2022
 * @Author Nihar
 * @Description
 * This java-class is used to represent the object
 * UPDService. The UPD-Service has been implemented as
 * a singleton-pattern.
 *
 * The UPD-Service is used to update the database version
 * and executed the necessary sql-statements for this plugin-version.
 */
public class UPDService extends Objekt
{
    //  Attributes:
    public static final UPDService instance = new UPDService();
    private File file;
    private String[] sqlStatements;
    private String updVersion;
    private int sqlStatementCounter;
    private int updLow;
    private int updHigh;

    /* ************************* */
    /* CONSTRUCTOR // INIT */
    /* ************************* */

    private UPDService() { }

    public void of_init(String directoryPath)
    {
        file = new File(directoryPath + "//UPD//UPD_" + Sys.of_getPaket() +".upd");

        //  Create a template-file with no sql-statements in it.
        if(!file.exists())
        {
            try
            {
                file.createNewFile();
            }
            catch (Exception ignored) { }
        }
    }

    /* ************************* */
    /* LOADER */
    /* ************************* */

    @Override
    public int of_load()
    {
        if(file.exists() && file.length() > 0)
        {
            try
            {
                //  Initialize reader to read the file.
                FileReader reader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(reader);

                //  Create a StringBuilder to collect the SQL-Statements to one large string.
                StringBuilder sqlBuilder = new StringBuilder();
                String versionPattern = "=UPD_VERSION_NUMBER=";
                String currentLine;

                //  Read until the EOF is reached!
                while((currentLine = bufferedReader.readLine()) != null)
                {
                    if(!currentLine.isEmpty())
                    {
                        //  Check for a UPD-Version-Number or SQL-Statements.
                        if(currentLine.startsWith("-- UPDv=") || !currentLine.startsWith("--"))
                        {
                            //  UPD-Version...
                            if(currentLine.startsWith("-- UPDv="))
                            {
                                sqlBuilder.append(currentLine).append(versionPattern);
                            }
                            //  SQL-Statements...
                            else
                            {
                                sqlBuilder.append(currentLine);
                            }
                        }
                    }
                }

                //  SPLIT-SQL-Statements by ';'
                String[] sqlArray = sqlBuilder.toString().split(";");
                int noSQLCounter = 0;

                if(sqlArray.length > 0)
                {
                    //  Iterate through all SQL-Statements...
                    for(String sql : sqlArray)
                    {
                        //  If we find a UPD-Version-Pattern.
                        if(sql.contains(versionPattern))
                        {
                            String[] sqlFragments = sql.split(versionPattern);

                            if(sqlFragments.length == 2)
                            {
                                //  UPD-Version:
                                sqlStatements = Sys.of_addArrayValue(sqlStatements, sqlFragments[0]);
                                noSQLCounter++;

                                //  SQL-Statement.
                                sqlStatements = Sys.of_addArrayValue(sqlStatements, sqlFragments[1] + ";");
                            }
                        }
                        //  SQL-Statement.
                        else
                        {
                            sqlStatements = Sys.of_addArrayValue(sqlStatements, sql + ";");
                        }
                    }
                }

                //  Set the SQL-Statement counter.
                if(sqlStatements.length > 0 && noSQLCounter < sqlStatements.length)
                {
                    sqlStatementCounter = sqlStatements.length - noSQLCounter;
                }

                //  Close the file-reader.
                bufferedReader.close();
                reader.close();
                return 1;
            }
            catch (Exception e)
            {
                of_sendErrorMessage(e, "UPDService.of_load();", "There was an error by reading the following upd-file: " + file.getAbsolutePath());
                return -1;
            }
        }
        else
        {
            Sys.of_debug("UPD-File not found: " + file.getAbsolutePath());
        }

        return -1;
    }

    /* ************************* */
    /* OBJECT - METHODS */
    /* ************************* */

    public void of_runUPD()
    {
        //  Send information to the console...
        of_sendMessage("Check UPD-File for new updates...");

        //  Check for new database update...
        if(of_isNewUpdateAvailable())
        {
            //  Initialize variables for the upd-process.
            boolean lb_executeSQL = false;
            int currentUPDVersion;
            int executedSQLs = 0;
            int skipSQLs = 0;
            int errorSQLs = 0;
            int triedSQLs = 0;
            sqlStatementCounter++;

            of_sendMessage("...there are new updates available...");
            of_sendMessage("...try to update the db-version to '"+updVersion+"'...");
            of_sendMessage("========= UPD-Service ("+sqlStatementCounter+") =========");

            //  Add the Update-DB-Version Statement to the SQL-Statements.
            sqlStatements = Sys.of_addArrayValue(sqlStatements, "UPDATE "+main.SQL.of_getTableNotation()+"dbversion SET dbVersion = '"+updVersion+"';");

            //  Iterate through all SQL-Statements...
            for(String sql : sqlStatements)
            {
                //  Check for a UPD-Version...
                if(sql.startsWith("-- UPDv="))
                {
                    //  Check if the SQL-Statements to this UPD-Version needs to be executed.
                    currentUPDVersion = of_getUPDNumber(sql.replace("-- UPDv=", ""));
                    lb_executeSQL = ( currentUPDVersion != -1 && currentUPDVersion > updLow && currentUPDVersion <= updHigh );
                }
                //  Execute some SQL-Statements...
                else if(lb_executeSQL)
                {
                    triedSQLs++;

                    //  Execute the given SQl-Statement.
                    if(main.SQL.of_run_update_suppress(sql))
                    {
                        of_sendMessage("SQL-Statement executed: " + triedSQLs + "/" + sqlStatementCounter);
                        executedSQLs++;
                    }
                    else
                    {
                        of_sendMessage("SQL-Statement error: " + triedSQLs + "/" + sqlStatementCounter);
                        errorSQLs++;
                    }
                }
                else
                {
                    triedSQLs++;
                    skipSQLs++;
                    of_sendMessage("SQL-Statement skipped: " + triedSQLs + "/" + sqlStatementCounter);
                }
            }

            //  Display a conclusion about the db-update-process.
            of_sendMessage("========= UPD-Service: Conclusion =========");
            of_sendMessage("SQL-Statements executed: " + executedSQLs);
            of_sendMessage("SQL-Statements skipped: " + skipSQLs);
            of_sendMessage("SQL-Statements errors: " + errorSQLs);
            of_sendMessage("Database version: " + updVersion);
            of_sendMessage("===========================================");
            return;
        }

        //  No updates found.
        of_sendMessage("...no updates found! Database is up to date! :)");
    }

    private void of_sendMessage(String message)
    {
        Sys.of_sendMessage("[UPD-Service]: " + message);
    }

    /* ************************* */
    /* BOOLS */
    /* ************************* */

    /**
     * This method is used to check if some sql-statements
     * in the upd-file needs to be executed to the database.
     * @return TRUE = Database needs to be updated. FALSE = No update needed!
     */
    private boolean of_isNewUpdateAvailable()
    {
        if(sqlStatements.length > 0)
        {
            String updVersionByFile = null;

            //  Identify the latest UPD-Version in the UPD-File.
            for(int i = sqlStatements.length - 1; i >= 0; i--)
            {
                if(sqlStatements[i].startsWith("-- UPDv="))
                {
                    updVersionByFile = sqlStatements[i].replace("-- UPDv=", "");
                    break;
                }
            }

            if(updVersionByFile != null)
            {
                String sqlSelect = "SELECT dbVersion FROM "+ main.SQL.of_getTableNotation()+"dbversion;";
                String updVersionByDb = main.SQL.of_getRowValue_suppress(sqlSelect, "dbVersion");

                if(updVersionByDb != null)
                {
                    //  If the UPD-File-Version and the DB-Version are match, we do not
                    //  need to update the database.
                    if(updVersionByDb.equals(updVersionByFile))
                    {
                        return false;
                    }
                }
                //  Set the base-version.
                else
                {
                    updVersionByDb = "22.1.0.00";
                }

                int updVersionNumberByFile = of_getUPDNumber(updVersionByFile);
                int updVersionNumberByDb = of_getUPDNumber(updVersionByDb);
                int updVersionNumberByPlugin = of_getUPDNumber(Sys.of_getVersion());

                if(updVersionNumberByFile < updVersionNumberByPlugin)
                {
                    //  Store the current upd-version-range. Is needed for the of_runUPD()-method.
                    updHigh = updVersionNumberByFile;
                    updLow = updVersionNumberByDb;
                    updVersion = updVersionByFile;

                    //  Continue if the database is not on the same version as the UPD-File.
                    return updVersionNumberByFile != -1 && updVersionNumberByDb != -1 && updVersionNumberByFile >= updVersionNumberByDb;
                }
                else
                {
                    of_sendErrorMessage(null, "UPDService.of_runUPD();", "The UPD-File is not compatible with this plugin-version!");
                    return false;
                }
            }
        }

        return false;
    }

    /* ************************* */
    /* GETTER */
    /* ************************* */

    public static UPDService of_getInstance()
    {
        return instance;
    }

    private int of_getUPDNumber(String updVersion)
    {
        return Sys.of_getString2Int(updVersion.split("\\.")[3]);
    }
}
