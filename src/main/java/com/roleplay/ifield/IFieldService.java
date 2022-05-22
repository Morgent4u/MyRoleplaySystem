package com.roleplay.ifield;

import com.basis.ancestor.Objekt;
import com.basis.sys.Sys;
import com.roleplay.objects.CommandSet;
import com.roleplay.spieler.Spieler;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

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
     * @param material The material.
     */
    public void of_check4IFields2Execute(Spieler ps, Material material)
    {
        Location loc = ps.of_getPlayer().getLocation();

        for(IField iField : _CONTEXT.of_getAllIFields())
        {
            if(iField.of_getMaterial() == material)
            {
                if(iField.of_getLocation().distance(loc) <= iField.of_getRange())
                {
                    new CommandSet(iField.of_getCommandSet(), ps).of_executeAllCommands();
                }
            }
        }
    }
}
