package com.roleplay.inventar;

import com.basis.ancestor.Objekt;
import com.basis.main.main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Created 14.04.2022
 * @Author Nihar
 * @Description
 * This class contains several methods to manage the inventar-object or
 * a default inventory.
 */
public class InventarService extends Objekt
{
    public InventarContext _CONTEXT;

    /* ************************************* */
    /* CONSTRUCTOR // LOADER */
    /* ************************************* */

    public InventarService()
    {
        _CONTEXT = new InventarContext();
    }

    @Override
    public int of_load()
    {
        return _CONTEXT.of_load();
    }

    /* ************************************* */
    /* OBJEKT - ANWEISUNGEN */
    /* ************************************* */

    /**
     * This function is used to create an itemStack by the given attributes.
     * @param material The material of the itemStack.
     * @param displayName The displayName of the itemStack.
     * @param arrayLore The lore of the itemStack.
     * @param amount The amount of the itemStack.
     * @return The itemStack.
     */
    public ItemStack of_createItemStack(Material material, String displayName, String[] arrayLore, int amount)
    {
        //  Create the item.
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if(meta != null)
        {
            //  Set the meta attributes.
            meta.setDisplayName(displayName);
            meta.setUnbreakable(true);
            meta.setUnbreakable(true);
            meta = of_setDefaultAttributes2ItemMeta(meta);

            //  Set the lore.
            if(arrayLore != null && arrayLore.length > 0)
            {
                List<String> lore = Arrays.asList(arrayLore);
                meta.setLore(lore);
            }

            if(amount <= 0)
            {
                amount = 1;
            }

            item.setAmount(amount);
            item.setItemMeta(meta);

            return item;
        }

        return null;
    }

    /**
     * This function creates a playerhead-ItemStack from the given playerName.
     * @param playerName The name of the player.
     * @param displayName The displayName of the playerhead.
     * @param itemLore The lore of the playerhead.
     * @param amount The amount of the playerhead.
     * @return The playerhead-ItemStack.
     */
    public ItemStack of_createPlayerHead(String playerName, String displayName, String[] itemLore, int amount)
    {
        //  Create the playerhead.
        ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1, (short) SkullType.PLAYER.ordinal());
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwner(playerName);

        //  Set the displayName.
        meta.setDisplayName(displayName);
        meta.setUnbreakable(true);

        // Set default attributs.
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_PLACED_ON);
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);

        //  Set the lore.
        if(itemLore != null && itemLore.length > 0)
        {
            List<String> lore = Arrays.asList(itemLore);
            meta.setLore(lore);
        }

        //  Set the meta and the amount.
        item.setAmount(amount);
        item.setItemMeta(meta);

        return item;
    }

    /**
     * This function is used to copy the content of the inventory to a new one and return it.
     * @param inv The inventory which should be copied.
     * @param invTitle The title of the new inventory.
     * @return The new inventory.
     */
    public Inventory of_copyInv(Inventory inv, String invTitle)
    {
        Inventory tmpInv;

        //	Sicherstellen, dass das Inv vom Typ Chest ist, da sonst SIZE nicht
        //	funktioniert.
        if(inv.getType() == InventoryType.CHEST)
        {
            tmpInv = Bukkit.createInventory(null, inv.getSize(), invTitle);
        }
        else
        {
            //	Inventar könnte ein Braustand sein, also dann lieber direkt mit
            //	dem InventoryType arbeiten.
            tmpInv = Bukkit.createInventory(null, inv.getType(), invTitle);
        }

        //	Inhalt wird übertragen.
        tmpInv.setContents(inv.getContents());

        return tmpInv;
    }

    /* ************************************* */
    /* SETTER */
    /* ************************************* */

    /**
     * This function sets default attributes to the ItemMeta.
     * @param meta The ItemMeta which should be set.
     * @return The ItemMeta with the default attributes.
     */
    public ItemMeta of_setDefaultAttributes2ItemMeta(ItemMeta meta)
    {
        if(meta != null)
        {
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
            meta.addItemFlags(ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_PLACED_ON);
            meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);

            return meta;
        }

        return null;
    }
}
