package com.roleplay.ancestor;

import com.basis.ancestor.Objekt;
import com.basis.utils.Settings;
import com.basis.utils.SimpleFile;
import com.roleplay.objects.CommandSet;
import com.roleplay.spieler.Spieler;

/**
 * @Created 11.07.2022
 * @Author Nihar
 * @Description
 * This abstract class is used to represent default-settings
 * for any module.
 */
public class MRSModule extends Objekt
{
    //  Attributes:
    private SimpleFile sf;
    private String[] commandSets;
    private String configKey;
    private boolean ib_enabled;

    /* ************************************* */
    /* CONSTRUCTOR */
    /* ************************************* */

    public void of_init(String instanceName, SimpleFile simpleFile, String configKey)
    {
        this.sf = simpleFile;
        this.configKey = configKey;
        of_setInfo(instanceName);
        of_preLoad();
    }

    /* ************************************* */
    /* LOADER // EVENT-LOADER */
    /* ************************************* */

    /**
     * This method is used to be called before
     * the of_load()-method in which the module-differences
     * can be defined. So this method is used to load the
     * basic-stuff for any module.
     */
    private void of_preLoad()
    {
        ib_enabled = Settings.of_getInstance().of_getSettingsFile().of_getSetBoolean(of_getConfigKey() + "." + of_getInfo() +".Use", true);

        if(ib_enabled)
        {
            //  We call the load-method of the child-class.
            int rc = of_load();

            if(rc == 1)
            {
                //  Save the current settings into the file.
                sf.of_save(of_getInfo() + ".of_preLoad()");

                // Call preLoadEvent-method to load events which has
                // been defined in the child-class.
                of_preLoadEvents();
            }
        }
    }

    /**
     * This function is used to register events to the plugin.
     * @return 1 = OK, -1 = Error while registering.
     */
    public int of_loadEvents()
    {
        return 1;
    }

    private void of_preLoadEvents()
    {
        String errorMessage = of_validate();

        if(errorMessage == null)
        {
            if(of_loadEvents() != 1)
            {
                of_sendErrorMessage(null, of_getInfo() + ".of_preLoadEvents();", "Error while registering events to this plugin.");
            }
        }
        else
        {
            of_sendErrorMessage(null, of_getInfo() + ".of_preLoadEvents();", "Error while validating the MRS-Module. Validation-message:\n" + errorMessage);
        }
    }

    /* ************************************* */
    /* METHOD - OBJECTS */
    /* ************************************* */

    @Override
    public String of_validate()
    {
        return null;
    }

    /**
     * This method is used to execute defined CommandSets for this
     * module.
     * @param ps Player instance.
     */
    public void of_executeDefinedCommandSets4Player(Spieler ps)
    {
        if(ps != null)
        {
            if(commandSets != null && commandSets.length > 0)
            {
                new CommandSet(commandSets, ps).of_executeAllCommands();
            }
        }
    }

    /* ************************************* */
    /* SETTER */
    /* ************************************* */

    public void of_setCommandSet(String[] commandSets)
    {
        this.commandSets = commandSets;
    }

    /* ************************************* */
    /* GETTER */
    /* ************************************* */

    public boolean of_isEnabled()
    {
        return ib_enabled && !of_hasAnError();
    }

    public SimpleFile of_getConfig()
    {
        return sf;
    }

    public String[] of_getCommandSets()
    {
        return commandSets;
    }

    public String of_getConfigKey()
    {
        return configKey;
    }
}
