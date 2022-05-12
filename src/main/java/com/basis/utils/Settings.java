package com.basis.utils;

import com.basis.ancestor.Objekt;
import com.basis.extern.MySQL;
import com.basis.extern.UPDService;
import com.basis.main.main;
import com.basis.sys.Sys;
import com.roleplay.board.MessageBoard;
import com.roleplay.board.PermissionBoard;
import com.roleplay.extern.ProtocolLib;
import com.roleplay.extern.Vault;
import com.roleplay.hologram.HologramService;
import com.roleplay.inventar.InventarService;
import com.roleplay.npc.NPCService;
import com.roleplay.spieler.SpielerService;

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

    //  Dependencies:
    boolean ib_usePlugin;
    boolean ib_useMySQL;
    boolean ib_useVault;
    boolean ib_usePlaceholderApi;
    boolean ib_useProtocolLib;

    //  Setting-Attributes:
    boolean ib_useMenuOnSwap;

    //  Money-Attributes:
    boolean ib_useVaultMoney;
    double moneyDefaultATM = 0;
    double moneyDefaultCash = 0;

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

        int rc;

        //	Settings:
        ib_usePlugin = datei.of_getSetBoolean(sectionKey + ".Enabled", true);

        if(ib_usePlugin)
        {
            //  Einstellungen einlesen...
            String apiSection = sectionKey + ".API.";
            ib_useVaultMoney = datei.of_getSetBoolean(apiSection + "Vault.MoneySystem", true);
            ib_usePlaceholderApi = datei.of_getSetBoolean(apiSection + "PlaceholderAPI.Use", false);
            ib_useProtocolLib = datei.of_getSetBoolean(apiSection + "ProtocolLib.Use", true);

            //  Money-Section:
            String rpSection = sectionKey + ".RolePlay.";
            moneyDefaultATM = datei.of_getSetDouble(rpSection + "StartMoney.ATM", 90000);
            moneyDefaultCash = datei.of_getSetDouble(rpSection + "StartMoney.Cash", 10000);
            ib_useMenuOnSwap = datei.of_getSetBoolean(rpSection + "Menu.UseOnSwap", true);

            //	MySQL-Attribute einlesen:

            String externalSection = sectionKey + ".External.";
            ib_useMySQL = datei.of_getSetBoolean(externalSection + "MySQL.Use", false);
            String hostName = datei.of_getSetString(externalSection + "MySQL.Host", "localhost");
            String database = datei.of_getSetString(externalSection + "MySQL.Database", "database");
            String username = datei.of_getSetString(externalSection + "MySQL.Username", "user");
            String password = datei.of_getSetString(externalSection + "MySQL.Password", "pwd");

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
                main.SQL.of_setUpdateKeyTableAndColumns("mrs_key", "lastKey", "tableName");

                //	Verbindung zur DB herstellen und ggf. UPDSrv ansprechen!
                rc = main.SQL.of_createConnection();

                //	DB-Zugriff bzw. Verbindung in die Settings.yml schreiben...
                if(rc == 1)
                {
                    datei.of_set(sectionKey + ".MySQL.Status", Sys.of_getTimeStamp(true) + " - Connected.");

                    //  Überprüfen ob es ein neues UPD gibt, welches eingespielt werden muss...
                    UPDService updSrv = new UPDService(Sys.of_getMainFilePath());

                    //  1 = UPD-File geladen und gefunden. -1 = keine UPD-File gefunden.
                    int updSrvRc = updSrv.of_load();

                    //  Wenn ein UPD-File gefunden wurde, dann...
                    if(updSrvRc == 1)
                    {
                        updSrv.of_sendMessage("Search for database updates...");

                        //  Wenn eine neue UPD-Version gefunden wurde...
                        if(updSrv.of_isNewUpdateAvailable())
                        {
                            //  Neues Update vorhanden!
                            updSrvRc = updSrv.of_runUPD();

                            if(updSrvRc != 1)
                            {
                                updSrv.of_sendMessage("Error while updating database! No sql-statements found! (Is this okay?)");
                            }
                        }
                        else
                        {
                            //  Kein neues Update vorhanden!
                            updSrv.of_sendMessage("No new update available. Your database is up to date.");
                        }
                    }
                }
                else
                {
                    of_setUseMySQL(false);
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
    /* UNLOADER */
    /* ************************* */

    /**
     * This function destroys objects in the correct order
     * which are defined in the main.java class.
     */
    @Override
    public void of_unload()
    {
        //  If ProtocolLib and NPCs are loaded/enabled then remove the NPCs from the player.
        if(of_isUsingProtocolLib() && main.PROTOCOLLIB != null && main.NPCSERVICE != null && main.NPCSERVICE._CONTEXT.of_getLoadedNPCsSize() > 0)
        {
            main.NPCSERVICE.of_removeAllNPCsFromAllOnlinePlayers();
        }

        if(main.SPIELERSERVICE != null)
        {
            main.SPIELERSERVICE.of_unload();
        }

        if(main.HOLOGRAMSERVICE != null)
        {
            main.HOLOGRAMSERVICE.of_unload();
        }

        //  Nach dem SpielerService etc.
        if(main.SQL != null && of_isUsingMySQL() && main.SQL.of_isConnected())
        {
            main.SQL.of_closeConnection();
        }
    }

    /* ************************* */
    /* OBJEKT-ANWEISUNGEN */
    /* ************************* */

    /**
     * This function initializes objects which are defined in the
     * main.java class.
     * @return 1 = success, -1 = failure
     */
    public int of_initSystemServices()
    {
        //  Check for the required components before registering own services.
        int rc = of_checkExternComponents();

        if(rc == 1)
        {
            //  Initializes own services.
            main.SPIELERSERVICE = new SpielerService();
            main.SPIELERSERVICE.of_load();

            //  Load own inventories or predefined ones.
            main.INVENTARSERVICE = new InventarService();
            main.INVENTARSERVICE.of_load();

            //  Load the message-board.
            main.MESSAGEBOARD = new MessageBoard();
            main.MESSAGEBOARD.of_load();

            //  Load the permissions-board.
            main.PERMISSIONBOARD = new PermissionBoard();
            main.PERMISSIONBOARD.of_load();

            //  After loading all needed services we load the ProtocolLib-Specific Listeners.
            if(of_isUsingProtocolLib() && main.PROTOCOLLIB != null)
            {
                //  Create the NPCService if ProtoclLib has been loaded.
                main.NPCSERVICE = new NPCService();
                main.NPCSERVICE.of_load();

                //  Load all NPCs for each player.
                main.NPCSERVICE.of_showAllNPCs2AllOnlinePlayers();

                //  Load the ProtocolLib-Specific Listeners.
                main.PROTOCOLLIB.ue_addSpecificPacketListeners2ProtocolLibManager();
            }

            //  Load the Hologram-Service.
            main.HOLOGRAMSERVICE = new HologramService();
            main.HOLOGRAMSERVICE.of_load();

            return 1;
        }

        return -1;
    }

    /**
     * This function checks while start up if
     * required or soft depends plugins are on this server.
     * @return 1 = success, 0 = failure
     */
    public int of_checkExternComponents()
    {
        //  If vault is not on the server then stop the process.
        ib_useVault = Sys.of_check4SpecificPluginOnServer("Vault");

        if(of_isUsingVault())
        {
            // Initialize Vault:
            main.VAULT = new Vault();
            main.VAULT.of_load();

            //  If no error occurred we can check for the soft dependencies.
            if(!main.VAULT.of_hasAnError())
            {
                //  Check for soft depends on plugins.
                if(of_isUsingPlaceholderAPI())
                {
                    ib_usePlaceholderApi = Sys.of_check4SpecificPluginOnServer("PlaceholderAPI");
                }

                if(of_isUsingProtocolLib())
                {
                    ib_useProtocolLib = Sys.of_check4SpecificPluginOnServer("ProtocolLib");

                    // If protocolLib is on the server we can initialize the protocolLib object.
                    if(of_isUsingProtocolLib())
                    {
                        main.PROTOCOLLIB = new ProtocolLib();
                        int rc = main.PROTOCOLLIB.of_load();

                        //  If an error occurred while registering the protocolLib object, we're going to deactivate it.
                        if(rc != 1)
                        {
                            main.PROTOCOLLIB = null;
                            ib_useProtocolLib = false;
                        }
                    }
                }

                return 1;
            }
        }

        return -1;
    }

    /**
     * This function sends a status report to the console after
     * successfully loading all objects or required methods for the plugin.
     */
    public void of_printStatusReport2Console()
    {
        //	Farbcodes
        String red = "\u001B[31m";
        String white = "\u001B[0m";
        String green = "\u001B[32m";
        String yellow = "\u001B[33m";
        String purple = "\u001B[35m";
        String blue = "\u001B[36m";

        Sys.of_sendMessage("========================================================");
        if(Sys.of_isHotfix())
        {
            Sys.of_sendMessage(red+"[Hotfix: "+green+Sys.of_getPaket()+" "+yellow+"v"+Sys.of_getVersion()+red+"]"+white);
        }
        else
        {
            Sys.of_sendMessage(red+"["+green+Sys.of_getPaket()+" "+yellow+"v"+Sys.of_getVersion()+red+"]"+white);
        }
        Sys.of_sendMessage("Developed by:");
        Sys.of_sendMessage(purple+"Nihar"+white);
        Sys.of_sendMessage(blue+"[*] Settings:"+white);
        Sys.of_sendMessage("Plugin-Enabled: "+of_isUsingPlugin());
        Sys.of_sendMessage("MySQL-Enabled: "+of_isUsingMySQL());
        if(main.SQL != null)
        {
            Sys.of_sendMessage("MySQL-Connected: "+main.SQL.of_isConnected());
        }
        Sys.of_sendMessage("Vault-Enabled: "+of_isUsingVault());
        Sys.of_sendMessage("Vault-MoneySystem: "+of_isUsingVaultMoneySystem());
        Sys.of_sendMessage("PlaceholderAPI-Enabled: "+of_isUsingPlaceholderAPI());
        Sys.of_sendMessage("ProtocolLib-Enabled: "+of_isUsingProtocolLib());
        Sys.of_sendMessage(blue+"[*] Permission board:"+white);
        main.PERMISSIONBOARD.of_sendDebugDetailInformation();
        Sys.of_sendMessage(blue+"[*] Message-/Soundboard:"+white);
        main.MESSAGEBOARD.of_sendDebugDetailInformation();
        Sys.of_sendMessage(blue+"[*] Inventories:"+white);
        main.INVENTARSERVICE._CONTEXT.of_sendDebugDetailInformation();
        //  The NPCService can be null if ProtocolLib has been disabled.
        if(main.NPCSERVICE != null)
        {
            Sys.of_sendMessage(blue+"[*] NPCs:"+white);
            main.NPCSERVICE._CONTEXT.of_sendDebugDetailInformation();
        }
        Sys.of_sendMessage(blue+"[*] Holograms:"+white);
        main.HOLOGRAMSERVICE._CONTEXT.of_sendDebugDetailInformation();
        Sys.of_sendMessage("========================================================");
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

    public double of_getDefaultMoneyATM()
    {
        return moneyDefaultATM;
    }

    public double of_getDefaultMoneyCash()
    {
        return moneyDefaultCash;
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

    public boolean of_isUsingVault()
    {
        return ib_useVault;
    }

    public boolean of_isUsingPlaceholderAPI()
    {
        return ib_usePlaceholderApi;
    }

    public boolean of_isUsingVaultMoneySystem()
    {
        return ib_useVaultMoney;
    }

    public boolean of_isUsingMenuOnSwap()
    {
        return ib_useMenuOnSwap;
    }

    public boolean of_isUsingProtocolLib()
    {
        return ib_useProtocolLib;
    }
}