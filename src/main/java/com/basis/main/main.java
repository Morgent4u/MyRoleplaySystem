package com.basis.main;

import com.basis.extern.MySQL;
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



                //	Als letztes die Instanz-Variabeln der Services, Objekte initialisieren.
                of_initSystemServices();

                //	Statusbericht an die Konsole:
                of_printStatusReport2Console();

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
        //	Ende.
        Sys.of_sendMessage("This plugin has been coded by Nihar! Thank you for using this plugin! :^)");
    }

    /* ************************* */
    /* OBJEKT-ANWEISUNGEN */
    /* ************************* */

    /**
     * This function initialize objects which are required for this plugin!
     */
    private static void of_initSystemServices()
    {
        //	Im Anschluss schauen, ob noch andere Komponenten gefordert sind und ob diese zur Verfügung stehen.
        of_checkExternComponents();
    }

    /**
     * This function checks if external components are registered.
     * For example: if the SETTINGS-object is using the PlaceholderAPI this function
     * checks if the plugin is on the server.
     */
    private static void of_checkExternComponents()
    {
        //  TODO: Implement this function!
    }

    /* ************************* */
    /* SONSTIGES */
    /* ************************* */

    /**
     * This function sends a status report to the console after
     * successfully loading all objects or required methods for the plugin.
     */
    private static void of_printStatusReport2Console()
    {
        //	Farbcodes
        String red = "\u001B[31m";
        String white = "\u001B[0m";
        String green = "\u001B[32m";
        String yellow = "\u001B[33m";
        String purple = "\u001B[35m";
        String blue = "\u001B[36m";

        Sys.of_sendMessage("┏╋━━━━━━━━◥◣◆◢◤━━━━━━━━╋");
        if(Sys.of_isHotfix())
        {
            Sys.of_sendMessage(red+"[Hotfix: "+green+Sys.of_getPaket()+" "+yellow+"v"+Sys.of_getVersion()+red+"]"+white);
        }
        else
        {
            Sys.of_sendMessage(red+"["+green+Sys.of_getPaket()+" "+yellow+"v"+Sys.of_getVersion()+red+"]"+white);
        }
        Sys.of_sendMessage("Developed by:");
        Sys.of_sendMessage("»"+purple+" Nihar"+white);
        Sys.of_sendMessage(blue+"▶ Settings:"+white);
        SETTINGS.of_sendDebugDetailInformation();
        Sys.of_sendMessage("┗╋━━━━━━━━◥◣◆◢◤━━━━━━━━╋┛");
    }
}