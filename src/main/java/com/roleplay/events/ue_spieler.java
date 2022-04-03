package com.roleplay.events;

import com.basis.main.main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ue_spieler implements Listener
{
    /**
     * This event is used to interact with the player when he joins the server.
     * @param e Event instance.
     */
    @EventHandler
    public void ue_playerJoin4MRS(PlayerJoinEvent e)
    {
        //  Load all player specific data from database and store the instance in the player-context.
        main.SPIELERSERVICE._CONTEXT.of_loadPlayer(e.getPlayer());
    }
}
