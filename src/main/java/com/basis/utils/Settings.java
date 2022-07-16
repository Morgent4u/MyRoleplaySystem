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
import com.roleplay.iblock.IBlockService;
import com.roleplay.inventar.InventarService;
import com.roleplay.manager.TablistManager;
import com.roleplay.module.ModuleDeathCmdSet;
import com.roleplay.module.ModuleIdCard;
import com.roleplay.npc.NPCService;
import com.roleplay.position.PositionService;
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
    //  We use singleton-pattern to get the instance of the settings-object.
    private static final Settings instance = new Settings();

    //	Attribute:
    SimpleFile datei;

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
    boolean ib_useIBlock;
    boolean ib_useDataProtection;
    boolean ib_usePosition;

    //  MRSModule-Attributes:
    boolean ib_moduleIDCard;
    boolean ib_moduleDeathCommandSet;

    //  Money-Attributes:
    boolean ib_useVaultMoney;
    double moneyDefaultATM = 0;
    double moneyDefaultCash = 0;

    /* ************************* */
    /* CONSTRUCTOR */
    /* ************************* */

    /**
     * Constructor
     */
    private Settings()
    {
        datei = new SimpleFile(Sys.of_getMainFilePath()+"settings.yml");
        sectionKey = Sys.of_getPaket();
    }

    /* ************************* */
    /* LOADER // UNLOADER */
    /* ************************* */

    /**
     * This function is called when the plugin is getting enabled.
     * @return 1 = OK, 0 = Plugin has been disabled, -1 = An error occurred.
     */
    @Override
    public int of_load()
    {
        int rc;

        //	Settings:
        ib_usePlugin = datei.of_getSetBoolean(sectionKey + ".Enabled", true);

        if(ib_usePlugin)
        {
            //  We're going to read the settings...
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
                TablistManager.of_getInstance().of_loadPredefinedTeams(datei, rpSection + "Tablist");
                TablistManager.of_getInstance().of_loadPredefinedTeams(datei, rpSection + "Tablist");
            }

            //  Enable or disable the join- and quit-message.
            ib_useJoinQuitMsg = datei.of_getSetBoolean(rpSection + "JoinQuitMsg.Use", true);
            if(of_isUsingJoinAndQuitMessage())
            {
                joinQuitMessage = new String[2];
                joinQuitMessage[0] = datei.of_getSetString(rpSection + "JoinQuitMsg.Join", "&aWelcome to our server, &e%p%&a!");
                joinQuitMessage[1] = datei.of_getSetString(rpSection + "JoinQuitMsg.Quit", "&cGoodbye &e%p%&c!");
            }

            //  Enable or disable iBlock-System:
            ib_useIBlock = datei.of_getSetBoolean(rpSection + "IBlock.Use", true);

            //  Enable or disable the Position-System:
            ib_usePosition = datei.of_getSetBoolean(rpSection + "Position.Use", true);

            if(of_isUsingPosition())
            {
                main.POSITIONSERVICE = new PositionService();
                main.POSITIONSERVICE.of_load();
            }

            //  Reading the SQL-Settings:
            String externalSection = sectionKey + ".External.";
            ib_useMySQL = datei.of_getSetBoolean(externalSection + "MySQL.Use", false);
            String hostName = datei.of_getSetString(externalSection + "MySQL.Host", "localhost");
            String database = datei.of_getSetString(externalSection + "MySQL.Database", "database");
            String username = datei.of_getSetString(externalSection + "MySQL.Username", "user");
            String password = datei.of_getSetString(externalSection + "MySQL.Password", "pwd");

            //  Save the current settings.
            datei.of_save("Settings.of_load();");

            //	If MySQL can be used we create an object-instance for it.
            if(ib_useMySQL)
            {
                //	Create one SQL-instance.
                main.SQL = new MySQL("Main");

                //	Set the connection-parameters.
                main.SQL.of_setServer(hostName);
                main.SQL.of_setDbName(database);
                main.SQL.of_setUserName(username);
                main.SQL.of_setPassword(password);
                main.SQL.of_setUpdateKeyTableAndColumns("mrs_key", "lastKey", "tableName");

                //	Connect to the database and check for database updates by using the UPD-Service.
                rc = main.SQL.of_createConnection();

                //	If we could connect to the database we can start the UPD-Service.
                if(rc == 1)
                {
                    datei.of_set(sectionKey + ".MySQL.Status", Sys.of_getTimeStamp(true) + " - Connected.");

                    //  Check if the database is up-to-date.
                    UPDService updSrv = new UPDService(Sys.of_getMainFilePath());

                    //  1 = UPD-File could be found. -1 = No UPD-File could be found.
                    int updSrvRc = updSrv.of_load();

                    if(updSrvRc == 1)
                    {
                        updSrv.of_sendMessage("Search for database updates...");

                        //  If a new UPD-Version is available...
                        if(updSrv.of_isNewUpdateAvailable())
                        {
                            //  Update the database.
                            updSrvRc = updSrv.of_runUPD();

                            if(updSrvRc != 1)
                            {
                                updSrv.of_sendMessage("Error while updating database! No sql-statements found! (Is this okay?)");
                            }
                        }
                        else
                        {
                            //  No new UPD-Version available.
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
        if(of_isUsingProtocolLib() && main.PROTOCOLLIB != null && main.NPCSERVICE != null && main.NPCSERVICE._CONTEXT.of_getLoadedObjects() > 0)
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
            PermissionBoard.of_getInstance().of_load();

            //  Load all predefined messages...
            MessageBoard.of_getInstance().of_load();

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
                ScoreBoard.of_getInstance().of_load(scoreboardLines);
            }

            if(of_isUsingIBlock())
            {
                main.IBLOCKSERVICE = new IBlockService();
                main.IBLOCKSERVICE.of_load();
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
                    main.NPCSERVICE.of_removeAllNPCsFromAllOnlinePlayers();
                    main.NPCSERVICE.of_showAllNPCs2AllOnlinePlayers();
                }

                //  Load the scoreboard to all players...
                if(of_isUsingScoreboard())
                {
                    ScoreBoard.of_getInstance().of_loadScoreboard2AllPlayers();
                }

                //  We create for every player the tab-list.
                if(of_isUsingTablist())
                {
                    TablistManager.of_getInstance().of_createOrUpdateTablist4AllPlayers();
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
        configSection = configSection + "MRSModule";

        //  Check for the ModuleIdCard-MRSModule:
        ModuleIdCard.of_getInstance().of_init("ModuleIDCard", new SimpleFile(moduleFolder + "//IDCard//IDCard.yml"), configSection);
        ib_moduleIDCard = ModuleIdCard.of_getInstance().of_isEnabled();

        //  Check for the death-CommandSet-MRSModule:
        ModuleDeathCmdSet.of_getInstance().of_init("DeathCommandSet", new SimpleFile(moduleFolder + "//DeathCommandSet//DeathCommandSet.yml"), configSection);
        ib_moduleDeathCommandSet = ModuleDeathCmdSet.of_getInstance().of_isEnabled();
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
        PermissionBoard.of_getInstance().of_sendDebugDetailInformation();
        Sys.of_sendMessage(blue+"[*] Message-/Sound-board:"+white);
        MessageBoard.of_getInstance().of_sendDebugDetailInformation();
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
        if(of_isUsingIBlock())
        {
            Sys.of_sendMessage(blue+"[*] IBlock:"+white);
            main.IBLOCKSERVICE.of_sendDebugDetailInformation();
        }
        if(of_isUsingPosition())
        {
            Sys.of_sendMessage(blue+"[*] Position:"+white);
            main.POSITIONSERVICE.of_sendDebugDetailInformation();
        }
        Sys.of_sendMessage("========================================================");
    }

    /* ******************************* */
    /* SETTER // ADDER // REMOVER */
    /* ******************************* */

    public void of_setPlugin(boolean bool)
    {
        ib_usePlugin = bool;

        //	Speicherung in der SimpleFile...
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

    public static Settings of_getInstance()
    {
        return instance;
    }

    public SimpleFile of_getSettingsFile()
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

    public boolean of_isUsingIBlock()
    {
        return ib_useIBlock;
    }

    public boolean of_isUsingDataProtection()
    {
        return ib_useDataProtection;
    }

    public boolean of_isUsingPosition()
    {
        return ib_usePosition;
    }

    public boolean of_isUsingModuleDeathCommandSet()
    {
        return ib_moduleDeathCommandSet;
    }
}