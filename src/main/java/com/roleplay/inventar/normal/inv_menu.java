package com.roleplay.inventar.normal;

import com.basis.main.main;
import com.roleplay.inventar.Inventar;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;

/**
 * @Created 15.04.2022
 * @Author Nihar
 * @Description
 * This inventory is the default menu for every player.
 */
public class inv_menu extends Inventar
{
    /* ************************************* */
    /* LOADER */
    /* ************************************* */

    @Override
    public int of_load()
    {
        of_setInventarName("§8[§4§lMenu§8]");
        of_setCopyInv(true);
        inv = Bukkit.createInventory(null, InventoryType.BREWING, of_getInventarName());

        //  Define items for the inventory.
        inv.setItem(0, main.INVENTARSERVICE.of_createPlayerHead("dummyPlayer", "§8[§4%p%§8]", new String[]{"§fShow some", "§ainformation§f to the player."}, 1));
        return 1;
    }

    /* ************************************* */
    /* OBJECT METHODS */
    /* ************************************* */

    @Override
    public void of_defineCommands4Inventory()
    {
        of_addCommands2ItemName(0, new String[] {"CMD=showinfo"});
    }
}
