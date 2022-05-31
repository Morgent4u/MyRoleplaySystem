package com.basis.utils;

import com.basis.ancestor.Objekt;
import com.basis.extern.MySQL;
import com.basis.extern.UPDService;
import com.basis.main.main;
import com.basis.sys.Sys;
import com.roleplay.board.MessageBoard;
import com.roleplay.board.PermissionBoard;
import com.roleplay.board.ScoreBoard;
import com.roleplay.extern.ProtocolLib;
import com.roleplay.extern.Vault;
import com.roleplay.hologram.HologramService;
import com.roleplay.ifield.IFieldService;
import com.roleplay.inventar.InventarService;
import com.roleplay.manager.TablistManager;
import com.roleplay.npc.NPCService;
import com.roleplay.spieler.SpielerService;
import org.bukkit.scheduler.BukkitRunnable;

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

    String[] scoreboardLines;
    String[] joinQuitMessage;
    String sectionKey;

    //  Dependencies:
    boolean ib_usePlugin;
    boolean ib_useMySQL;
    boolean ib_useVault;
    boolean ib_usePlaceholderApi;
    boolean ib_useProtocolLib;

    //  Setting-Attributes:
    boolean ib_useMenuOnSwap;
    boolean ib_useScoreboard;
    boolean ib_useTablist;
    boolean ib_useJoinQuitMsg;
    boolean ib_useIField;
    boolean ib_useDataProtection;

    //  Module-Attributes:
    boolean ib_moduleIDCard;

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
            ib_useVaultMoney = datei.of_getSetBoolean(apiSection + "Vault.MoneySystem", false);
            ib_usePlaceholderApi = datei.of_getSetBoolean(apiSection + "PlaceholderAPI.Use", false);
            ib_useProtocolLib = datei.of_getSetBoolean(apiSection + "ProtocolLib.Use", true);

            //  RolePlay-Section:
            String rpSection = sectionKey + ".RolePlay.";

            //  First check for defined modules...
            of_check4Modules(rpSection);

            //  Default-Money-Attributes:
            moneyDefaultATM = datei.of_getSetDouble(rpSection + "StartMoney.ATM", 90000);
            moneyDefaultCash = datei.of_getSetDouble(rpSection + "StartMoney.Cash", 10000);
            ib_useMenuOnSwap = datei.of_getSetBoolean(rpSection + "Menu.UseOnSwap", true);
            ib_useDataProtection = datei.of_getSetBoolean(rpSection + "Check4DataProtection", true);

            //  Load the Scoreboard-Settings:
            ib_useScoreboard = datei.of_getSetBoolean(rpSection + "Scoreboard.Use", true);
            if(of_isUsingScoreboard())
            {
                String[] lines = new String[]{"&c"+Sys.of_getProgramVersion(), "&fThis is a test.", "&fChange me &e:)"};
                scoreboardLines = datei.of_getSetStringArray(rpSection + "Scoreboard.Lines", lines);

                if(scoreboardLines == null || scoreboardLines.length == 0)
                {
                    ib_useScoreboard = false;
                    Sys.of_debug("Deactivated the scoreboard-system because no lines are defined or the entry does not exist!");
                }
            }

            //  Load the tab-list setting (can we use the tab-list?):
            ib_useTablist = datei.of_getSetBoolean(rpSection + "Tablist.Use", true);
            //  Load the tab-list if it is enabled:
            if(of_isUsingTablist())
            {
                //  Load the tab-list.
                main.TABLISTMANAGER = new TablistManager();
                main.TABLISTMANAGER.of_loadPredefinedTeams(datei, rpSection + "Tablist");
            }

            //  Enable or disable the join- and quit-message.
            ib_useJoinQuitMsg = datei.of_getSetBoolean(rpSection + "JoinQuitMsg.Use", true);
            if(of_isUsingJoinAndQuitMessage())
            {
                joinQuitMessage = new String[2];
                joinQuitMessage[0] = datei.of_getSetString(rpSection + "JoinQuitMsg.Join", "&aWelcome to our server, &e%p%&a!");
                joinQuitMessage[1] = datei.of_getSetString(rpSection + "JoinQuitMsg.Quit", "&cGoodbye &e%p%&c!");
            }

            //  Enable or disable iField-System:
            ib_useIField = datei.of_getSetBoolean(rpSection + "IField.Use", true);

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
    /* OBJECT METHODS */
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

        //  Initializes own services.
        if(rc == 1)
        {
            //  First step, load the PermissionsBoard to make sure that
            //  we have permissions for player-stuff.
            main.PERMISSIONBOARD = new PermissionBoard();
            main.PERMISSIONBOARD.of_load();

            //  Load all predefined messages...
            main.MESSAGEBOARD = new MessageBoard();
            main.MESSAGEBOARD.of_load();

            //  Load the player service...
            main.SPIELERSERVICE = new SpielerService();

            //  Load own inventories or predefined ones.
            main.INVENTARSERVICE = new InventarService();
            main.INVENTARSERVICE.of_load();

            //  After loading all needed services we load the ProtocolLib-Specific Listeners.
            if(of_isUsingProtocolLib() && main.PROTOCOLLIB != null)
            {
                //  Create the NPCService if ProtoclLib has been loaded.
                main.NPCSERVICE = new NPCService();
                main.NPCSERVICE.of_load();

                //  Load the ProtocolLib-Specific Listeners.
                main.PROTOCOLLIB.ue_addSpecificPacketListeners2ProtocolLibManager();
            }

            //  Load the Hologram-Service.
            main.HOLOGRAMSERVICE = new HologramService();
            main.HOLOGRAMSERVICE.of_load();

            //  Create the scoreBoard for each online-player if it's enabled!
            if(of_isUsingScoreboard())
            {
                main.SCOREBOARD = new ScoreBoard(scoreboardLines);
            }

            if(of_isUsingIField())
            {
                main.IFIELDSERVICE = new IFieldService();
                main.IFIELDSERVICE.of_load();
            }

            //  We need to invoke the postInitSystemServices...
            of_postInitSystemServices();

            return 1;
        }

        return -1;
    }

    /**
     * This post function needs to be called 1 second after the reload process.
     */
    private void of_postInitSystemServices()
    {
        //  We use a RunnableTask to run the following code after 1 second.
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                //  We load the player service...
                main.SPIELERSERVICE.of_load();

                //  Load all NPCs for each player.
                if(of_isUsingProtocolLib() && main.PROTOCOLLIB != null && main.NPCSERVICE != null)
                {
                    main.NPCSERVICE.of_showAllNPCs2AllOnlinePlayers();
                }

                //  Load the scoreboard to all players...
                if(of_isUsingScoreboard())
                {
                    main.SCOREBOARD.of_loadScoreboard2AllPlayers();
                }

                //  We create for every player the tab-list.
                if(of_isUsingTablist())
                {
                    main.TABLISTMANAGER.of_createOrUpdateTablist4AllPlayers();
                }
            }

        }.runTaskLater(main.PLUGIN, 20);
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
     * This method is used to initialize some modules which can be disabled or enabled by the settings.
     * @param configSection The config section which contains the settings.
     */
    private void of_check4Modules(String configSection)
    {
        //  Define some attributes for the module-system:
        String moduleFolder = Sys.of_getMainFilePath() + "Modules//";
        configSection = configSection + "Module";

        //  Check for the idCard-Module:
        ib_moduleIDCard = datei.of_getSetBoolean(configSection + ".IDCard.Use", true);

        if(of_isUsingModuleIDCard())
        {
            //  Create the idCard-Module-Folder.
            Datei module = new Datei(moduleFolder + "//IDCard//IDCard.yml");

            if(!module.of_fileExists())
            {
                module.of_set("Test", "test");
                module.of_save("Settings.of_check4Modules(); IDCard.yml");
            }
        }
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
        //  We don't show the debug-mode because we deactivated it in the reload-process.
        // Sys.of_sendMessage("DebugMode-Enabled: "+Sys.of_isDebugModeEnabled());
        Sys.of_sendMessage("MySQL-Enabled: "+of_isUsingMySQL());
        if(main.SQL != null)
        {
            Sys.of_sendMessage("MySQL-Connected: "+main.SQL.of_isConnected());
        }
        Sys.of_sendMessage("Vault-Enabled: "+of_isUsingVault());
        Sys.of_sendMessage("Vault-MoneySystem: "+of_isUsingVaultMoneySystem());
        Sys.of_sendMessage("PlaceholderAPI-Enabled: "+of_isUsingPlaceholderAPI());
        Sys.of_sendMessage("ProtocolLib-Enabled: "+of_isUsingProtocolLib());
        Sys.of_sendMessage(blue+"[*] Permissions-board:"+white);
        main.PERMISSIONBOARD.of_sendDebugDetailInformation();
        Sys.of_sendMessage(blue+"[*] Message-/Sound-board:"+white);
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
        if(of_isUsingIField())
        {
            Sys.of_sendMessage(blue+"[*] IFields:"+white);
            main.IFIELDSERVICE.of_sendDebugDetailInformation();
        }
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

    public void of_setUseProtocolLib(boolean bool)
    {
        ib_useProtocolLib = bool;
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

    /**
     * This function returns the join message for the player.
     * @return The join message.
     */
    public String of_getJoinMessage()
    {
        if(joinQuitMessage != null && joinQuitMessage.length > 0)
        {
            return joinQuitMessage[0];
        }

        return "";
    }

    /**
     * This function returns the quit message for the player.
     * @return The quit message.
     */
    public String of_getQuitMessage()
    {
        if(joinQuitMessage != null && joinQuitMessage.length > 0)
        {
            return joinQuitMessage[1];
        }

        return "";
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

    public boolean of_isUsingScoreboard()
    {
        return ib_useScoreboard;
    }

    public boolean of_isUsingTablist()
    {
        return ib_useTablist;
    }

    public boolean of_isUsingJoinAndQuitMessage()
    {
        return ib_useJoinQuitMsg;
    }

    public boolean of_isUsingModuleIDCard()
    {
        return ib_moduleIDCard;
    }

    public boolean of_isUsingIField()
    {
        return ib_useIField;
    }

    public boolean of_isUsingDataProtection()
    {
        return ib_useDataProtection;
    }
}