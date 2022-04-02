package com.basis.main;

import com.basis.extern.MySQL;
import com.basis.extern.UPDSrv;
import com.basis.sys.Sys;
import com.basis.utils.Settings;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

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
    //	Instanz-Variabeln des Systems/Plugins.
    public static Plugin PLUGIN;
    public static MySQL SQL;
    public static Settings SETTINGS;

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
        //	Initialisierungen:
        PLUGIN = this;

        //	Überprüfen ob die Versions-Nummer stimmt...
        boolean lb_continue = Sys.of_isSystemVersionCompatible(PLUGIN.getName(), "22.1.0.01", "plugins");

        if(lb_continue)
        {
            //  Deaktiviere um alle Debug-Nachrichten im Buffer zu haben, da wir reloaden!
            boolean useDebugMode = Sys.of_isDebugModeEnabled();
            Sys.of_setDebugMode(false);

            //  Einstellungen laden...
            SETTINGS = new Settings(Sys.of_getMainFilePath());
            int rc = SETTINGS.of_load();

            if(rc == 1)
            {
                //	Event und Befehle anmelden...

                // Test the UPD-Srv:
                UPDSrv updSrv = new UPDSrv(Sys.of_getMainFilePath());
                updSrv.of_load();

                //  Initalisierungen von Objekten in dieser Klasse via. des SETTINGS-Objekts.
                SETTINGS.of_initSystemServices();

                //	Statusbericht an die Konsole:
                SETTINGS.of_printStatusReport2Console();

                //  Alle gespeicherten Debug-Meldungen ausgeben.
                Sys.of_setDebugMode(useDebugMode);
            }
            else
            {
                Sys.of_sendWarningMessage("System has been disabled by 'settings.yml' or no database connection.");
                Bukkit.getPluginManager().disablePlugin(this);
            }
        }
    }

    /* ************************* */
    /* DISABLE */
    /* ************************* */

    @Override
    public void onDisable()
    {
        //  Entladen der Settings-klasse!
        SETTINGS.of_unload();

        //	Ende.
        Sys.of_sendMessage("This plugin has been coded by Nihar! Thank you for using this plugin! :^)");
    }
}