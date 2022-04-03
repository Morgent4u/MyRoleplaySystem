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
public class UPDSrv extends Objekt
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
    public UPDSrv(String searchDirectory)
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
                FileReader reader = new FileReader(file);
                BufferedReader bufferReader = new BufferedReader(reader);

                //  SQL-Statement zusammenbauen...
                int noSqlStatement = 0;
                StringBuilder sqlStatementsFullBlock = new StringBuilder();
                String line;

                while((line = bufferReader.readLine()) != null)
                {
                    // Kommentare mit einem UPD-Versions-Kennzeichen direkt hinzufügen...
                    if(!line.isEmpty())
                    {
                        if(line.startsWith("-- UPDv="))
                        {
                            sqlStatements.add(line);
                            noSqlStatement++;
                        }
                        //  SQL-Statement zusammenbauen...
                        else if(!line.startsWith("--"))
                        {
                            sqlStatementsFullBlock.append(line);
                        }
                    }
                }

                //  SQL-Statements auseinander parsen...
                String[] sqlExecuteStatements = sqlStatementsFullBlock.toString().split(";");

                //  Array-Elemente zur ArrayList hinzufügen...
                sqlStatements.addAll(Arrays.asList(sqlExecuteStatements));

                //  Anzahl der SQL-Statements setzen...
                if(sqlStatements.size() > 0 && noSqlStatement < sqlStatements.size())
                {
                    sqlStatementSize = sqlStatements.size() - noSqlStatement;
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
            int currentDbVersionNumber = -1;
            boolean executeCurrentStatement = false;

            of_sendMessage("========= UPD-Service ("+sqlStatementSize+") =========");

            //  Iterate all sql-statements...
            for(int i = 0; i < sqlStatements.size(); i++)
            {
                //  Derzeitiges SQL-Statement ermitteln...
                String sql = sqlStatements.get(i);

                //  UPD-Version wurde ermittelt.. schauen ob die dazugehörigen
                //  SQL-Statements ausgeführt werde müssen...
                if(sql.startsWith("-- UPDv="))
                {
                    executeCurrentStatement = false;
                    currentDbVersionNumber = Sys.of_getString2Int(sql.replace("-- UPDv=", "").split("\\.")[3]);

                    if(currentDbVersionNumber != -1 && currentDbVersionNumber >= lowestDbVersion && currentDbVersionNumber <= highestDbVersion)
                    {
                        executeCurrentStatement = true;
                    }
                }
                //  Execute current SQL.
                else if(executeCurrentStatement)
                {
                    boolean bool = main.SQL.of_run_update_suppress(sql);

                    if(bool)
                    {
                        of_sendMessage("SQL-Statement executed: " + i + "/" + sqlStatementSize);
                        sqlExecutions++;
                    }
                    else
                    {
                        of_sendMessage("SQL-Statement error: " + i + "/" + sqlStatementSize);
                        sqlErrors++;
                    }
                }
                //  Skippe current SQL.
                else
                {
                    of_sendMessage("SQL-Statement skipped: " + i + "/" + sqlStatementSize);
                    sqlSkipped++;
                }
            }

            //  Conclusion anzeigen...
            of_sendMessage("========= UPD-Service: Conclusion =========");
            of_sendMessage("SQL-Statements executed: " + sqlExecutions);
            of_sendMessage("SQL-Statements skipped: " + sqlSkipped);
            of_sendMessage("SQL-Statements errors: " + sqlErrors);
            of_sendMessage("===========================================");
            return 1;
        }

        return 0;
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
                break;
            }
        }

        if(lastIndex4UpdVVersion != -1)
        {
            //  Das Kennzeichen für die UPD-Version entfernen.
            String fileUpdVersion = sqlStatements.get(lastIndex4UpdVVersion).replace("-- UPDv=", "");

            // Die Plugin version stimmt nicht mit dem letzten erstellten UPD überein. Demnach gibt es ein
            // neues UPD. Jetzt nur noch schauen, ob die DB aktuell ist.
            if(!fileUpdVersion.equals(Sys.of_getVersion()))
            {
                //  Es kann sein, dass das Plugin auf 2x Servern läuft und somit die DB
                //  bereits aktualsiert wurde. Aus dem Grund überprüfen wir nun noch die
                //  Version aus der UPD-Datei und anschließend die der DB.

                String lastUPDVersion = null;
                String sqlSelect = "SELECT dbVersion FROM mrs_dbversion;";
                String result = main.SQL.of_getRowValue_suppress(sqlSelect, "dbVersion");

                if(result != null)
                {
                    //	Wenn die DB-Version gleich mit der PL-Verison ist, gibt es kein UPD zum einspielen!
                    if(result.equals(Sys.of_getVersion()))
                    {
                        return false;
                    }
                    //	Letzte UPD-Version...
                    else
                    {
                        lastUPDVersion = result;
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
        }

        return false;
    }
}
