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
    File file;
    ArrayList<String> sqlStatements = new ArrayList<String>();

    int lowestDbVersion;
    int highestDbVersion;

    int sqlExecutions;
    int sqlErrors;
    int sqlSkipped;
    int sqlStatementSize;

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
        searchDirectory = searchDirectory + "//UPD//2022UPD_" + Sys.of_getPaket() + ".upd";
        file = new File(searchDirectory);
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
                //  FileReader und BufferReader initialisieren!
                FileReader reader = new FileReader(file);
                BufferedReader bufferReader = new BufferedReader(reader);

                StringBuilder sqlStatementsInFile = new StringBuilder();
                String line;
                String flagPattern = "=UPD_VERSION_NUMBER=";

                //  Jede Zeile einlesen...
                while((line = bufferReader.readLine()) != null)
                {
                    if(!line.isEmpty())
                    {
                        //  Wenn ein SQL-Statement gefunden wird oder die UPD-Versions-Nummer, darf
                        //  dieses Statement hinzugefügt werden.
                        if(line.startsWith("-- UPDv=") || !line.startsWith("--"))
                        {
                            //  Wenn es eine UPD-Versions-Nummer ist, eine Markierung setzen um beim auseinander parsen
                            //  zu wissen, was der SQL-Statement Anteil und was der UPD-Versions-Nummern Anteil ist!
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

                //  Nach dem Zusammenbauen der gesamten UPD-File nun die SQL-Statements und die
                //  UPD-Versions-Nummern in der richtigen Reihenfolge auslesen und zur ArrayList hinzufügen.

                String[] sqlStmts = sqlStatementsInFile.toString().split(";");
                int noSqlStatements = 0;

                if(sqlStmts.length > 0)
                {
                    for (String sqlStmt : sqlStmts)
                    {
                        //  Wenn es ein UPD-Version-Kennzeichen ist...
                        if(sqlStmt.contains(flagPattern))
                        {
                            String[] splitSQLParts = sqlStmt.split(flagPattern);

                            //  Es muss die Länge 2 haben, ansonsten wurde ein falsches Flagkennzeichen gesetzt!
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

                //  Anzahl der SQL-Statements setzen...
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
    /* OBJEKT-ANWEISUNGEN */
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
            //  SQL-StatementSize anheben, da das Aktualisieren der DB auch als ein SQL-UPD gewertet wird!
            sqlStatementSize++;
            int currentUPDVersionNumber = -1;
            int executedSQLCounter = 0;
            boolean executeCurrentStatement = false;

            of_sendMessage("========= UPD-Service ("+sqlStatementSize+") =========");

            //  Iterate all sql-statements...
            for (String sql : sqlStatements)
            {
                //  Derzeitiges SQL-Statement ermitteln...
                //  UPD-Version wurde ermittelt.. schauen ob die dazugehörigen
                //  SQL-Statements ausgeführt werden müssen...
                if (sql.startsWith("-- UPDv="))
                {
                    executeCurrentStatement = false;
                    currentUPDVersionNumber = Sys.of_getString2Int(sql.replace("-- UPDv=", "").split("\\.")[3]);

                    if (currentUPDVersionNumber != -1 && currentUPDVersionNumber > lowestDbVersion && currentUPDVersionNumber <= highestDbVersion)
                    {
                        executeCurrentStatement = true;
                    }
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
                //  Skippe current SQL.
                else
                {
                    executedSQLCounter++;
                    of_sendMessage("SQL-Statement skipped: " + executedSQLCounter + "/" + sqlStatementSize);
                    sqlSkipped++;
                }
            }

            //  Datenbank-Version aktualisieren.
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

        //  Aus den eingelesenen SQL-Statements das letzte UPD anhand der UPDVersion, ermitteln.
        for(int i = 0; i < sqlStatements.size(); i++)
        {
            if(sqlStatements.get(i).startsWith("-- UPDv="))
            {
                lastIndex4UpdVVersion = i;
            }
        }

        if(lastIndex4UpdVVersion != -1)
        {
            //  Das Kennzeichen für die UPD-Version entfernen.
            String fileUpdVersion = sqlStatements.get(lastIndex4UpdVVersion).replace("-- UPDv=", "");

            //  Check if the datbase needs to be updated.  If the database version is lower than the plugin version, the database needs to be updated.
            String lastUPDVersion = null;
            String sqlSelect = "SELECT dbVersion FROM "+main.SQL.of_getTableNotation()+"dbversion;";
            String dbVersion = main.SQL.of_getRowValue_suppress(sqlSelect, "dbVersion");

            if(dbVersion != null)
            {
                //	Wenn die DB-Version gleich mit der PL-Verison ist, gibt es kein UPD zum einspielen!
                if(dbVersion.equals(Sys.of_getVersion()))
                {
                    return false;
                }
                //	Letzte UPD-Version...
                else
                {
                    lastUPDVersion = dbVersion;
                }
            }
            //	Grund-Version...
            else
            {
                lastUPDVersion = "22.1.0.00";
            }

            //  Okay UPD muss eingespielt werden, Grenzbereich ermitteln...
            highestDbVersion = Sys.of_getString2Int(Sys.of_getVersion().split("\\.")[3]);
            lowestDbVersion = Sys.of_getString2Int(lastUPDVersion.split("\\.")[3]);

            return lowestDbVersion != -1 && highestDbVersion != -1 && highestDbVersion >= lowestDbVersion;
        }

        return false;
    }
}
