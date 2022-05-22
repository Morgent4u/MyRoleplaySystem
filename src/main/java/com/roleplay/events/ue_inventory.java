package com.roleplay.events;

import com.basis.main.main;
import com.roleplay.inventar.Inventar;
import com.roleplay.spieler.Spieler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

/**
 * This class is used to handle the inventory events.
 */
public class ue_inventory implements Listener
{
    /**
     * This event is used to interact with the inventory which is opened by the player.
     * The event interactions will be transferred to each registered inventar-object instance.
     * @param e The event which is triggered.
     */
    @EventHandler
    public void ue_inventoryOpen4MRS(InventoryOpenEvent e)
    {
        Spieler ps = main.SPIELERSERVICE._CONTEXT.of_getPlayer(e.getPlayer().getName());

        if(ps != null)
        {
            Inventar inv = main.INVENTARSERVICE._CONTEXT.of_getInv(ps.of_getInvId());

            if(inv != null)
            {
                //  Trigger the open-event for the inventory-child-object.
                inv.ue_openInventoryEvent(e.getInventory(), ps);
            }
        }
    }

    /**
     * This event is used to interact with the clicked inventory item.
     * @param e The event which is triggered.
     */
    @EventHandler
    public void ue_inventoryClick4MRS(InventoryClickEvent e)
    {
        //  Get player instance.
        Spieler ps = main.SPIELERSERVICE._CONTEXT.of_getPlayer(e.getWhoClicked().getName());

        if(ps != null)
        {
            if(e.getClickedInventory() != ps.of_getPlayer().getInventory())
            {
                // Get inventory instance.
                Inventar inv = main.INVENTARSERVICE._CONTEXT.of_getInv(ps.of_getInvId());

                if(inv != null)
                {
                    //  Trigger the click-event for the inventory-child-object.
                    inv.ue_clickInventoryEvent(e.getClickedInventory(), e.getCurrentItem(), e.getSlot(), ps);
                    e.setCancelled(true);
                }
            }
        }
    }
}
