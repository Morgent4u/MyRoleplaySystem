package com.basis.main;

import com.basis.extern.MySQL;
import com.basis.sys.Sys;
import com.basis.utils.Settings;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.roleplay.board.MessageBoard;
import com.roleplay.board.PermissionBoard;
import com.roleplay.cmds.*;
import com.roleplay.events.ue_inventory;
import com.roleplay.events.ue_spieler;
import com.roleplay.extern.ProtocolLib;
import com.roleplay.extern.Vault;
import com.roleplay.hologram.HologramService;
import com.roleplay.inventar.InventarService;
import com.roleplay.npc.NPCService;
import com.roleplay.spieler.SpielerService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

/**
 * @Created 02.04.2022
 * @Author Nihar
 * @Description
 * This class is an extended version of the JavaPlugin and
 * contains for this plugin important functions and instances.
 *
 * Hint:
 * This function is only used to register Bukkit.Events or Bukkit.Commands.
 * Other important components will be enabled or initializes in the object:
 * SETTINGS.
 *
 * Declared objects in this class will be initialized by the SETTINGS-object!
 *
 */
public class main extends JavaPlugin
{
    //	Instances of the system.
    public static Plugin PLUGIN;

    //  Dependencies:
    public static MySQL SQL;
    public static Vault VAULT;
    public static ProtocolLib PROTOCOLLIB;

    //  Own services/objects:
    public static Settings SETTINGS;
    public static SpielerService SPIELERSERVICE;
    public static InventarService INVENTARSERVICE;
    public static MessageBoard MESSAGEBOARD;
    public static PermissionBoard PERMISSIONBOARD;
    public static NPCService NPCSERVICE;
    public static HologramService HOLOGRAMSERVICE;

    /* ************************* */
    /* ENABLE */
    /* ************************* */

    /**
     * This function is called when the plugin is getting enabled.
     * This function contains a version check which is used to enable the plugin if
     * the 'version.yml' and the plugin-version are compatible.
     * <b>The plugin can be disabled when the connection to the webservice is not given!</b>.
     */
    @Override
    public void onEnable()
    {
        //	Initalize the plugin.
        PLUGIN = this;

        //  Check if the plugin is compatible with the version.
        boolean lb_continue = Sys.of_isSystemVersionCompatible(PLUGIN.getName(), "22.1.0.02", "plugins");

        if(lb_continue)
        {
            boolean useDebugMode = Sys.of_isDebugModeEnabled();

            //  TODO: Fix this problem with the debug-mode!
            //  of_sendDetailDebug while reloading should be stored in the buffer and displayed after reloading!
            Sys.of_setDebugMode(false);

            //  Load the default settings.
            SETTINGS = new Settings(Sys.of_getMainFilePath());
            int rc = SETTINGS.of_load();

            if(rc == 1)
            {
                //  Register some events :)
                Bukkit.getPluginManager().registerEvents(new ue_spieler(), this);
                Bukkit.getPluginManager().registerEvents(new ue_inventory(), this);

                //  Register some commands:
                getCommand("Test").setExecutor(new CMD_Test());
                getCommand("Interaction").setExecutor(new CMD_Interaction());
                getCommand("Dataprotection").setExecutor(new CMD_DataProtection());
                getCommand("Textblock").setExecutor(new CMD_Textblock());
                getCommand("NPC").setExecutor(new CMD_NPC());

                // Initialize own services or dependencies.
                rc = SETTINGS.of_initSystemServices();

                if(rc == 1)
                {
                    //  Loading was successful, so we can send a report to the console.
                    SETTINGS.of_printStatusReport2Console();
                }
                //  If an error occured, we send a hint to the console and disable the plugin.
                else
                {
                    Sys.of_sendWarningMessage("System has been disabled by the plugin. A required function or object is missing!");
                    Bukkit.getPluginManager().disablePlugin(this);
                }
            }
            else
            {
                Sys.of_sendWarningMessage("System has been disabled by 'settings.yml' or no database connection.");
                Bukkit.getPluginManager().disablePlugin(this);
            }

            //  Print all stored messages.
            Sys.of_setDebugMode(useDebugMode);
        }
    }

    /* ************************* */
    /* DISABLE */
    /* ************************* */

    @Override
    public void onDisable()
    {
        //  If the object has been initialized lets unload it.
        if(SETTINGS != null)
        {
            SETTINGS.of_unload();
        }

        //  End.
        Sys.of_sendMessage("This plugin has been coded by Nihar! Thank you for using this plugin! :^)");
    }
}