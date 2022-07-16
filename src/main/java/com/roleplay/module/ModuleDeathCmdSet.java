package com.roleplay.module;

import com.basis.main.main;
import com.basis.utils.SimpleFile;
import com.roleplay.ancestor.MRSModule;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 * @Created 21.06.2022
 * @Author Nihar
 * @Description
 * This object is used as a module and will be created if
 * the module: 'DeathCommandSet' has been enabled.
 * With it, you can define the commands that will be executed
 * when a player dies.
 */
public class ModuleDeathCmdSet extends MRSModule implements Listener
{
    //  Attributes:
    private static final ModuleDeathCmdSet instance = new ModuleDeathCmdSet();
    private boolean ib_cmdSetOnRespawn;

    /* ************************************* */
    /* CONSTRUCTOR */
    /* ************************************* */

    private ModuleDeathCmdSet() { }

    /* ************************************* */
    /* LOADER // EVENT-LOADER */
    /* ************************************* */

    /**
     * This function is used to load module specified parameters to this object.
     * @return 1 = OK, -1 = Error
     */
    public int of_load()
    {
        SimpleFile sf = of_getConfig();

        //  Load module specified data.
        ib_cmdSetOnRespawn = sf.of_getSetBoolean("OnlyOnRespawn", false);
        of_setCommandSet(sf.of_getSetStringArray("CommandSet", new String[] {"CHATCLEAR","POS=respawn_location.yml", "TEXTBLOCK=txt_death_message.yml"}));
        return 1;
    }

    /**
     * This method is used to register object specified events.
     * @return 1 = OK, -1 = An error occurred.
     */
    public int of_loadEvents()
    {
        try
        {
            Bukkit.getPluginManager().registerEvents(this, main.PLUGIN);
            return 1;
        }
        catch (Exception ignored) { }

        return -1;
    }

    /* ************************************* */
    /* @EventHandler - Handle some Events */
    /* ************************************* */

    /**
     * This event is used to listen when the player get damage.
     * @param e The event which is triggered.
     */
    @EventHandler
    public void ue_entityDamage4MRS_ModuleDeathCommandSet(EntityDamageEvent e)
    {
        if(e.getEntity() instanceof Player)
        {
            Player p = (Player) e.getEntity();

            //  Only react when the player will be dead, and we need to execute the CommandSet before the respawn (do not respawn!)!
            if(((p.getHealth() - e.getDamage()) <= 0) && !of_isUsingCommandSetOnlyOnRespawn())
            {
                e.setCancelled(true);
                of_executeDefinedCommandSets4Player(main.SPIELERSERVICE._CONTEXT.of_getPlayer(p.getName()));
            }
        }
    }

    /**
     * This Event is used to check if the player dies in fact of another entity.
     * @param e The event which is triggered.
     */
    @EventHandler
    public void ue_entityDamageByEntity4MRS_ModuleDeathCommandSet(EntityDamageByEntityEvent e)
    {
        if(e.getEntity() instanceof Player)
        {
            Player p = (Player) e.getEntity();

            //  Only react when the player will be dead, and we need to execute the CommandSet before the respawn (do not respawn!)!
            if(((p.getHealth() - e.getFinalDamage()) <= 0) && !of_isUsingCommandSetOnlyOnRespawn())
            {
                e.setCancelled(true);
                of_executeDefinedCommandSets4Player(main.SPIELERSERVICE._CONTEXT.of_getPlayer(p.getName()));
            }
        }
    }

    /**
     * This event is used to listen if the player is respawning.
     * @param e The event which is triggered.
     */
    @EventHandler
    public void ue_playerRespawn4MRS_ModuleDeathCommandSet(PlayerRespawnEvent e)
    {
        if(of_isUsingCommandSetOnlyOnRespawn())
        {
            of_executeDefinedCommandSets4Player(main.SPIELERSERVICE._CONTEXT.of_getPlayer(e.getPlayer().getName()));
        }
    }

    /* ************************************* */
    /* OBJECT METHODS */
    /* ************************************* */

    @Override
    public String of_validate()
    {
        if(of_getCommandSets() == null)
        {
            return "The CommandSet is null!";
        }
        else if(of_getCommandSets().length == 0)
        {
            return "The CommandSet is empty!";
        }

        return null;
    }

    /* ************************************* */
    /* GETTER */
    /* ************************************* */

    public static ModuleDeathCmdSet of_getInstance()
    {
        return instance;
    }

    public boolean of_isUsingCommandSetOnlyOnRespawn()
    {
        return ib_cmdSetOnRespawn;
    }
}
