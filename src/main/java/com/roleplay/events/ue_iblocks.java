package com.roleplay.events;

import com.basis.main.main;
import com.roleplay.spieler.Spieler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * @Created 22.05.2022
 * @Author Nihar
 * @Description
 * This event-class is used to listen to the player-interact-events.
 */
public class ue_iblocks implements Listener
{
    /**
     * This event is used to interact with the player when he interacts with the
     * world. For example if he clicks on a block.
     * @param e Event instance.
     */
    @EventHandler
    public void ue_playerInteract4MRS(PlayerInteractEvent e)
    {
        Spieler ps = main.SPIELERSERVICE._CONTEXT.of_getPlayer(e.getPlayer().getName());

        if(ps != null)
        {
            if(e.getClickedBlock() != null && main.SETTINGS.of_isUsingIBlock())
            {
                e.setCancelled(main.IBLOCKSERVICE.of_check4IBlocks2Execute(ps, e.getClickedBlock()));
            }
        }
    }
}
