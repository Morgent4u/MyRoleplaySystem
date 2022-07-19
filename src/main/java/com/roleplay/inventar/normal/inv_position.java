package com.roleplay.inventar.normal;

import com.basis.main.main;
import com.roleplay.inventar.Inventar;
import org.bukkit.Bukkit;
import org.bukkit.Material;


/**
 * @Created 24.06.2022
 * @Author Nihar
 * @Description
 * This inventory is used to list all defined positions
 * in a GUI. We also list the players in the defined range.
 */
public class inv_position extends Inventar
{
    /* ************************************* */
    /* LOADER */
    /* ************************************* */

    @Override
    public int of_load()
    {
        //  Define some default attributes:
        of_setInventarName("§8[§4§lPosition - List§8]");
        of_setInvClassification("POSITION");
        of_setCloseOnClickEnabled(true);

        inv = Bukkit.createInventory(null, 54, of_getInventarName());
        inv.setItem(0, main.INVENTARSERVICE.of_createItemStack(Material.BIRCH_SIGN, "", new String[] {""}, 1));
        return 1;
    }

    /* ************************************* */
    /* OBJECT METHODS */
    /* ************************************* */

    @Override
    public void of_defineCommands4Inventory()
    {
        of_addCommands2ItemSlot(0, new String[] {"POS=%POS%"});
    }
}
