package com.roleplay.module.deathcmds;

import com.basis.ancestor.Objekt;
import com.basis.main.main;
import com.basis.utils.SimpleFile;
import com.roleplay.objects.CommandSet;
import com.roleplay.spieler.Spieler;
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
public class DeathCmdSet extends Objekt implements Listener
{
    //  Attributes:
    private String[] commandSet;
    private boolean ib_cmdSetOnRespawn;

    /* ************************************* */
    /* LOADER // EVENT-LOADER */
    /* ************************************* */

    /**
     * This function is used to load module specified parameters to this object.
     * @param simpleFile The file-object where the module-settings will be saved/loaded to/from.
     * @return 1 = OK, -1 = Error
     */
    public int of_load(SimpleFile simpleFile)
    {
        //  Load module specified data.
        ib_cmdSetOnRespawn = simpleFile.of_getSetBoolean("OnlyOnRespawn", false);
        commandSet = simpleFile.of_getSetStringArray("CommandSet", new String[] {"CHATCLEAR","POS=respawn_location.yml", "TEXTBLOCK=txt_death_message.yml"});

        if(!simpleFile.of_fileExists())
        {
            simpleFile.of_save("DeathCmdSet.of_load();");
        }

        //  Register this object-class as additional event-listeners.
        return of_loadEvents();
    }

    /**
     * This method is used to register object specified events.
     * @return 1 = OK, -1 = An error occurred.
     */
    private int of_loadEvents()
    {
        String errorMessage = of_validate();

        //  Only continue when no error has been found...
        if(errorMessage == null)
        {
            try
            {
                Bukkit.getPluginManager().registerEvents(this, main.PLUGIN);
                return 1;
            }
            catch (Exception e)
            {
                errorMessage = e.getMessage();
            }
        }

        of_sendErrorMessage(null, "DeathCmdSet.of_load();", "Error while enabling the DeathCommandSet-Module: " + errorMessage);
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
                of_executeCommandSet4Player(p);
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
                of_executeCommandSet4Player(p);
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
            of_executeCommandSet4Player(e.getPlayer());
        }
    }

    /* ************************************* */
    /* OBJECT METHODS */
    /* ************************************* */

    private void of_executeCommandSet4Player(Player p)
    {
        if(p != null)
        {
            Spieler ps = main.SPIELERSERVICE._CONTEXT.of_getPlayer(p.getName());

            if(ps != null)
            {
                new CommandSet(commandSet, ps).of_executeAllCommands();
            }
        }
    }

    @Override
    public String of_validate()
    {
        if(commandSet == null)
        {
            return "The CommandSet is null!";
        }

        if(commandSet.length == 0)
        {
            return "The CommandSet is empty!";
        }

        return null;
    }

    /* ************************************* */
    /* GETTER */
    /* ************************************* */

    public boolean of_isUsingCommandSetOnlyOnRespawn()
    {
        return ib_cmdSetOnRespawn;
    }
}
