package com.basis.utils;

import com.basis.ancestor.Objekt;
import com.basis.extern.MySQL;
import com.basis.main.main;
import com.basis.sys.Sys;

/**
 * @Created 22.03.2022
 * @Author Nihar
 * @Description
 * This object represents the settings.yml.
 * All settings which are made in the settings.yml will be
 * defined in this settings-object.
 * This object also creates the settings.yml if it does not exist.
 *
 */
public class Settings extends Objekt
{
    //	Attribute:
    Datei datei;

    String sectionKey;

    boolean ib_usePlugin;
    boolean ib_useMySQL;

    /* ************************* */
    /* CONSTRUCTOR */
    /* ************************* */

    /**
     * Constructor
     * @param directoryPath This is the path where the settings.yml is stored.
     */
    public Settings(String directoryPath)
    {
        datei = new Datei(directoryPath+"settings.yml");
        sectionKey = Sys.of_getPaket();
    }

    /* ************************* */
    /* LOADER // UNLOADER */
    /* ************************* */

    @Override
    public int of_load()
    {
        //	RC:
        //	 1: OK
        //	 0: Nicht aktivieren.
        //	-1: Fehler

        int rc = -1;

        //	Settings:
        ib_usePlugin = datei.of_getSetBoolean(sectionKey + ".Enabled", true);

        if(ib_usePlugin)
        {
            //  Einstellungen einlesen...


            //	MySQL-Attribute einlesen:
            ib_useMySQL = datei.of_getSetBoolean(sectionKey+".MySQL.Use", false);
            String hostName = datei.of_getSetString(sectionKey + ".MySQL.Host", "localhost");
            String database = datei.of_getSetString(sectionKey + ".MySQL.Database", "database");
            String username = datei.of_getSetString(sectionKey + ".MySQL.Username", "user");
            String password = datei.of_getSetString(sectionKey + ".MySQL.Password", "pwd");

            //  Speichern der Einstellungen.
            datei.of_save("Settings.of_load();");

            //	Wenn MySQL verwendet werden soll, Instanz an der Main-Klasse initialisieren.
            if(ib_useMySQL)
            {
                //	SQL-Instanz erzeugen!
                main.SQL = new MySQL("Main");

                //	Attribute zur DB setzen und Verbindung herstellen!
                main.SQL.of_setServer(hostName);
                main.SQL.of_setDbName(database);
                main.SQL.of_setUserName(username);
                main.SQL.of_setPassword(password);
                main.SQL.of_setUpdateKeyTableAndColumns("mrs_keys", "lastkey", "tablename");

                //	Verbindung zur DB herstellen und ggf. UPDSrv ansprechen!
                rc = main.SQL.of_createConnection();

                //	DB-Zugriff bzw. Verbindung in die Settings.yml schreiben...
                if(rc == 1)
                {
                    datei.of_set(sectionKey + ".MySQL.Status", Sys.of_getTimeStamp(true) + " - Connected.");
                }
                else
                {
                    datei.of_set(sectionKey + ".MySQL.Status", Sys.of_getTimeStamp(true) + " - No connection.");
                }

                datei.of_save("Settings.of_load();");
            }
            else
            {
                rc = 1;
            }
        }
        else
        {
            rc = 0;
        }

        return rc;
    }

    /* ************************* */
    /* DEBUG - CENTER */
    /* ************************* */

    @Override
    public void of_sendDebugDetailInformation()
    {
        Sys.of_sendMessage("Plugin-Enabled: "+of_isUsingPlugin());
        Sys.of_sendMessage("MySQL-Enabled: "+of_isUsingMySQL());
        if(main.SQL != null)
        {
            Sys.of_sendMessage("MySQL-Connected: "+main.SQL.of_isConnected());
        }

    }

    /* ******************************* */
    /* SETTER // ADDER // REMOVER */
    /* ******************************* */

    public void of_setPlugin(boolean bool)
    {
        ib_usePlugin = bool;

        //	Speicherung in der Datei...
        datei.of_set(sectionKey + ".Enabled", ib_usePlugin);
        datei.of_save("Settings.of_setPlugin(boolean)");
    }

    public void of_setUseMySQL(boolean bool)
    {
        ib_useMySQL = bool;
    }

    /* ************************* */
    /* GETTER */
    /* ************************* */

    public Datei of_getSettingsFile()
    {
        return datei;
    }

    /* ************************* */
    /* BOOLS */
    /* ************************* */

    public boolean of_isUsingPlugin()
    {
        return ib_usePlugin;
    }

    public boolean of_isUsingMySQL()
    {
        return ib_useMySQL;
    }
}