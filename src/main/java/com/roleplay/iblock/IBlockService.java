package com.roleplay.iblock;

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
 * This service-class is used to interact with the IBlock-object
 * or -context.
 */
public class IBlockService extends Objekt
{
    //  Attributes:
    public IBlockContext _CONTEXT;
    private ArrayList<String> setupPlayers = new ArrayList<>();

    /* ************************************* */
    /* LOADER */
    /* ************************************* */

    @Override
    public int of_load()
    {
        _CONTEXT = new IBlockContext(Sys.of_getMainFilePath() + "IBlocks//");
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
     * setup-mode of an IBlock.
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
        Sys.of_sendMessage("Loaded IBlocks: " + _CONTEXT.of_getLoadedIBlocks());
    }

    /* ************************************* */
    /* GETTER */
    /* ************************************* */

    /**
     * This function is used to identify an IBlock by its name.
     * @param name The name of the IBlock.
     * @return The IBlock-object or null if not found.
     */
    public IBlock of_getIBlockByName(String name)
    {
        IBlock[] iblocks = _CONTEXT.of_getAllIBlocks();

        if(iblocks != null && iblocks.length > 0)
        {
            for(IBlock iblock : iblocks)
            {
                if(iblock.of_getInfo().equalsIgnoreCase(name))
                {
                    return iblock;
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
     * @return TRUE = IBlock has been found. FALSE = No IBlock has been found.
     */
    public boolean of_check4IBlocks2Execute(Spieler ps, Block block)
    {
        if(of_isInSetup(ps))
        {
            //  Get the powerObject...
            IBlock iblock = null;

            try
            {
                iblock = (IBlock) ps.of_getPowerObject();
            }
            catch (Exception ignored) { }

            if(iblock != null)
            {
                //  Create the IBlock...
                iblock.of_setMaterial(block.getType());
                iblock.of_setLocation(block.getLocation());

                int rc = main.IBLOCKSERVICE._CONTEXT.of_saveIBlock2File(iblock);

                if(rc == 1)
                {
                    // Remove the player from the setup-list.
                    of_removePlayerFromSetup(ps);
                    main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§aCreated IBlock §f" + iblock.of_getInfo() + "§a.");
                    return true;
                }
            }

            main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cAn error occurred while creating the IBlock.");
            of_removePlayerFromSetup(ps);
            return true;
        }

        Location loc = block.getLocation();

        //  Iterate through all defined IBlocks...
        for(IBlock iblock : _CONTEXT.of_getAllIBlocks())
        {
            if(iblock.of_getMaterial() == block.getType())
            {
                if(iblock.of_getLocation().equals(loc))
                {
                    new CommandSet(iblock.of_getCommandSet(), ps).of_executeAllCommands();
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
