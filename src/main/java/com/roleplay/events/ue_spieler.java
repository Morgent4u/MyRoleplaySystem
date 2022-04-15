package com.roleplay.events;

import com.basis.main.main;
import com.basis.sys.Sys;
import com.roleplay.spieler.Spieler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @Created 04.04.2022
 * @Author Nihar
 * @Description
 * This event-class listens to player specific casual events.
 */
public class ue_spieler implements Listener
{
    /**
     * This event is used to interact with the player while joining the server.
     * @param e Event instance.
     */
    @EventHandler
    public void ue_playerJoin4MRS(PlayerJoinEvent e)
    {
        //  Load all player specific data from database / file-system and store the instance in the player-context.
        main.SPIELERSERVICE._CONTEXT.of_loadPlayer(e.getPlayer());
    }

    /**
     * This event is used to unload the player and remove the player from the player-context.
     * @param e Event instance.
     */
    @EventHandler
    public void ue_playerQuit4MRS(PlayerQuitEvent e)
    {
        //  Unload all player specific data from the player-context.
        Spieler ps = main.SPIELERSERVICE._CONTEXT.of_getSpieler(e.getPlayer().getName());

        if(ps != null)
        {
            main.SPIELERSERVICE._CONTEXT.of_unloadPlayer(ps);
        }
    }

    /**
     * This Event is used to interact with the player when he switches the item in his hands.
     * @param e Event instance.
     */
    @EventHandler
    public void ue_playerSwapHandItem4MRS(PlayerSwapHandItemsEvent e)
    {
        if(main.SETTINGS.of_isUsingMenuOnSwap())
        {
            Spieler ps = main.SPIELERSERVICE._CONTEXT.of_getSpieler(e.getPlayer().getName());

            if(ps != null)
            {
                main.INVENTARSERVICE.of_openInvById(ps, 1);
            }
        }
    }

}
