package com.roleplay.ifield;

import com.basis.ancestor.Objekt;
import com.basis.main.main;
import com.basis.sys.Sys;
import com.roleplay.objects.CommandSet;
import com.roleplay.spieler.Spieler;
import org.bukkit.Location;
import org.bukkit.block.Block;
import java.util.ArrayList;

/**
 * @Created 22.05.2022
 * @Author Nihar
 * @Description
 * This service-class is used to interact with the IField-object
 * or -context.
 */
public class IFieldService extends Objekt
{
    //  Attributes:
    public IFieldContext _CONTEXT;
    private ArrayList<String> setupPlayers = new ArrayList<>();

    /* ************************************* */
    /* LOADER */
    /* ************************************* */

    @Override
    public int of_load()
    {
        _CONTEXT = new IFieldContext(Sys.of_getMainFilePath() + "IFields//");
        _CONTEXT.of_load();
        return 1;
    }

    /* ************************************* */
    /* OBJECT-METHODS */
    /* ************************************* */

    /**
     * This method is used to add the player to the setup-list.
     * @param ps Player instance.
     */
    public void of_addPlayer2Setup(Spieler ps)
    {
        if(!of_isInSetup(ps))
        {
            setupPlayers.add(ps.of_getName());
        }
    }

    /**
     * This method is used to remove the player from the
     * setup-mode of an iField.
     * @param ps Player instance.
     */
    public void of_removePlayerFromSetup(Spieler ps)
    {
        ps.of_setPowerObject(null);
        setupPlayers.remove(ps.of_getName());
    }

    /* ************************************* */
    /* DEBUG-CENTER */
    /* ************************************* */

    @Override
    public void of_sendDebugDetailInformation()
    {
        Sys.of_sendMessage("Loaded IFields: " + _CONTEXT.of_getLoadedIFields());
    }

    /* ************************************* */
    /* GETTER */
    /* ************************************* */

    /**
     * This function is used to identify a iField by its name.
     * @param name The name of the iField.
     * @return The iField-object or null if not found.
     */
    public IField of_getIFieldByName(String name)
    {
        IField[] ifields = _CONTEXT.of_getAllIFields();

        if(ifields != null && ifields.length > 0)
        {
            for(IField ifield : ifields)
            {
                if(ifield.of_getInfo().equalsIgnoreCase(name))
                {
                    return ifield;
                }
            }
        }

        return null;
    }

    /* ************************************* */
    /* BOOLS */
    /* ************************************* */

    /**
     * This method is used to execute the CommandSet when the given player
     * is interaction with the needed material and is in the given range.
     * @param ps The player which is interacting with the material.
     * @param block The block which has been clicked.
     * @return TRUE = iField has been found. FALSE = No iField has been found.
     */
    public boolean of_check4IFields2Execute(Spieler ps, Block block)
    {
        if(of_isInSetup(ps))
        {
            //  Get the powerObject...
            IField ifield = null;

            try
            {
                ifield = (IField) ps.of_getPowerObject();
            }
            catch (Exception ignored) { }

            if(ifield != null)
            {
                //  Create the IField...
                ifield.of_setMaterial(block.getType());
                ifield.of_setLocation(block.getLocation());

                int rc = main.IFIELDSERVICE._CONTEXT.of_saveIField2File(ifield);

                if(rc == 1)
                {
                    // Remove the player from the setup-list.
                    of_removePlayerFromSetup(ps);
                    main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§aCreated IField §f" + ifield.of_getInfo() + "§a.");
                    return true;
                }
            }

            main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cAn error occurred while creating the IField.");
            of_removePlayerFromSetup(ps);
            return true;
        }

        Location loc = block.getLocation();

        //  Iterate through all defined iFields...
        for(IField iField : _CONTEXT.of_getAllIFields())
        {
            if(iField.of_getMaterial() == block.getType())
            {
                if(iField.of_getLocation().equals(loc))
                {
                    new CommandSet(iField.of_getCommandSet(), ps).of_executeAllCommands();
                    return true;
                }
            }
        }

        return false;
    }

    public boolean of_isInSetup(Spieler ps)
    {
        return setupPlayers.contains(ps.of_getName());
    }
}
