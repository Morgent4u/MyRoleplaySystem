package com.basis.extern;

import com.basis.ancestor.Objekt;
import com.basis.sys.Sys;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

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
                String line;

                while((line = bufferReader.readLine()) != null)
                {
                    sqlStatements.add(line);
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



    /* ************************* */
    /* BOOLS */
    /* ************************* */

    /**
     * This functions checks if the database needs to be updated.
     * @return TRUE = Update is needed. FALSE = Update is not needed.
     */
    public boolean of_isUPDNeeded()
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

            //  Diese Plugin-Version ist nicht gleich mit der letzten UPD-Version aus der Datei, also sollten wir updaten
            //  können. Jetzt noch DB-Version überprüfen und den Grenzbereich ermitteln...
            if(!fileUpdVersion.equals(Sys.of_getVersion()))
            {
                // TODO: Grenzbereich bestimmten per SQL...
            }
        }

        return false;
    }
}
