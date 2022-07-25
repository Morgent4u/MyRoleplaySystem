package com.roleplay.inventar;

import com.basis.ancestor.Objekt;
import com.basis.main.main;
import com.basis.sys.Sys;
import com.roleplay.board.MessageBoard;
import com.roleplay.extended.ExtendedFile;
import com.roleplay.position.Position;
import com.roleplay.spieler.Spieler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
        //  Allow loading own inventories from files.
        _CONTEXT.of_setLoadOwnInventories(true);

        return _CONTEXT.of_load();
    }

    /* ************************************* */
    /* OBJECT - METHODS */
    /* ************************************* */

    /**
     * This method is used to handle the given itemStack-usages.
     * For example if an item should be used as a sell/buy item, we need
     * to check for price-attributes on the given file.
     * @param items The full list of ItemStacks which has been already handled with default-habits.
     * @param invFile The file of the inventory.
     * @param inv The inventory-instance.
     * @param configSection The config-section of the inventory (this should be by default 'Inventory').
     * @return The replacement ItemStack ready to handle it!
     */
    public ItemStack[] of_handleItemStacksFromInventory(ItemStack[] items, ExtendedFile invFile, Inventar inv, String configSection)
    {
        String sourceArea = "InventarService.of_handleItemStacksFromInventory();";

        if(items != null)
        {
            for(int index = 0; index < items.length; index++)
            {
                if(items[index] != null)
                {
                    //  Check if a price has been defined.
                    if(invFile.of_getConfig().isSet(configSection + ".Items." + index + ".Price"))
                    {
                        //  Get the price.
                        double price = invFile.of_getDoubleByKey(configSection + ".Items." + index + ".Price");

                        if(price != -1)
                        {
                            items[index] = of_replaceItemStackAndCommandSetByHandling(items[index], inv, "%price%", String.valueOf(price), index);
                        }
                        //  If the price is not valid!
                        else
                        {
                            Sys.of_debug(sourceArea + " - The price of the item is not valid. Config-key: " + configSection + ".Items." + index + ".Price");
                        }
                    }

                    //  Check if a position has been defined.
                    if(invFile.of_getConfig().isSet(configSection + ".Items." + index + ".Pos"))
                    {
                        //  Get the position name/id.
                        String position = invFile.of_getString(configSection + ".Items." + index + ".Pos");
                        Position pos = main.POSITIONSERVICE.of_getPositionByAnything(position);

                        if(pos != null)
                        {
                            items[index] = of_replaceItemStackAndCommandSetByHandling(items[index], inv, "%pos%", pos.of_getPositionName(), index);
                        }
                        //  If no position could be found!
                        else
                        {
                            Sys.of_debug(sourceArea + " - The given position '"+position+"' does not exist! Config-key: " + configSection + ".Items." + index + ".Pos");
                        }
                    }
                }
            }

            //  After we handled some itemStacks successfully, we check for a inventory-filter...
            if(invFile.of_getConfig().isSet(configSection + ".Filter") && items.length > 0)
            {
                String invFilter = invFile.of_getString(configSection + ".Filter");

                if(invFilter != null && !invFilter.isEmpty())
                {
                    String[] filterFragments = invFilter.split(",");

                    for(String filter : filterFragments)
                    {
                        //  Iterate through all items and remove ItemStacks if it does not match the filter-pattern!
                        for(int i = 0; i < items.length; i++)
                        {
                            ItemStack item = items[i];

                            if(item != null)
                            {
                                //  If the itemStack does not contain the filter, we remove it!
                                if(!of_check4ItemStackWithSpecificPattern(item, filter))
                                {
                                    items[i] = null;
                                }
                            }
                        }
                    }
                }
            }

            return items;
        }

        of_sendErrorMessage(null, sourceArea, "There was an error while handling the ItemStacks! File: " + invFile.of_getFileName());
        return null;
    }

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

    /**
     * This function replaces the lore and displayName with the player stats.
     * @param item The itemStack which should be updated.
     * @param ps The player instance.
     * @return The itemStack with the updated lore and displayName.
     */
    public ItemStack of_replaceItemStackWithPlayerStats(ItemStack item, Spieler ps)
    {
        ItemMeta meta = item.getItemMeta();

        if(meta != null)
        {
            if(meta.hasDisplayName())
            {
                meta.setDisplayName(MessageBoard.of_getInstance().of_translateMessageWithPlayerStats(meta.getDisplayName(), ps));
            }

            if(meta.hasLore())
            {
                List<String> lore = meta.getLore();

                if(lore != null && lore.size() > 0)
                {
                    for(int i = 0; i < lore.size(); i++)
                    {
                        lore.set(i, MessageBoard.of_getInstance().of_translateMessageWithPlayerStats(lore.get(i), ps));
                    }
                }

                meta.setLore(lore);
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * This function is used to replace the display-name or lore-items
     * of an item by searching for a specific pattern and replacing it.
     * @param item The itemStack which should be updated.
     * @param searchValue The searchValue.
     * @param replaceValue The replaceValue.
     * @return The itemStack with the updated display-name or lore-items.
     */
    public ItemStack of_replaceItemStackValues(ItemStack item, String searchValue, String replaceValue)
    {
        ItemMeta meta = item.getItemMeta();

        if(meta != null)
        {
            if(meta.hasDisplayName())
            {
                meta.setDisplayName(meta.getDisplayName().replace(searchValue, replaceValue));
            }

            if(meta.hasLore())
            {
                List<String> lore = meta.getLore();

                if(lore != null && lore.size() > 0)
                {
                    for(int i = 0; i < lore.size(); i++)
                    {
                        lore.set(i, lore.get(i).replace(searchValue, replaceValue));
                    }
                }

                meta.setLore(lore);
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * This method is used to replace all necessary information
     * (itemStack and CommandSet) with the replace-value of the given Inventory-handle.
     * @param item The ItemStack
     * @param inv The given inventory-instance.
     * @param searchValue The search value for example: '%pos%'
     * @param replaceValue The replacement value for example: 'Town 1'
     * @param itemArrayIndex The array-field in which the item has been stored!
     * @return Replaced ItemStack.
     */
    private ItemStack of_replaceItemStackAndCommandSetByHandling(ItemStack item, Inventar inv, String searchValue, String replaceValue, int itemArrayIndex)
    {
        //  Replace the itemStack-Value.
        item = of_replaceItemStackValues(item, searchValue, replaceValue);
        item = of_replaceItemStackValues(item, searchValue.toUpperCase(), replaceValue);

        //  Check for a defined command-set.
        String[] commandSet = inv.of_getCommandsByInvSlot(itemArrayIndex);

        //  Only null-check needed!
        if(commandSet != null)
        {
            commandSet =  Sys.of_getReplacedArrayString(commandSet, searchValue, replaceValue);
            inv.of_updateCommandSet2ItemSlot(itemArrayIndex, Sys.of_getReplacedArrayString(commandSet, searchValue.toUpperCase(), replaceValue));
        }

        return item;
    }

    /**
     * This function converts a itemStack of the type PLAYER_HEAD to a playerHead-ItemStack
     * with the skin of the given playerName.
     * @param item The itemStack of the type PLAYER_HEAD.
     * @param playerName The name of the player.
     * @return The playerHead-ItemStack.
     */
    public ItemStack of_convertPlayerHead2PlayerHeadWithSkin(ItemStack item, String playerName)
    {
        try
        {
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            meta.setOwner(playerName);
            item.setItemMeta(meta);
        }
        catch (Exception ignored){}

        return item;
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

    /* ************************************* */
    /* BOOLS */
    /* ************************************* */

    /**
     * This function checks if the given ItemStack is containing the given pattern.
     * @param item The ItemStack which should be checked.
     * @param pattern The pattern which should be checked.
     * @return True if the ItemStack is containing the pattern, otherwise false.
     */
    public boolean of_check4ItemStackWithSpecificPattern(ItemStack item, String pattern)
    {
        if(item != null && item.hasItemMeta())
        {
            if(Objects.requireNonNull(item.getItemMeta()).hasDisplayName())
            {
                if(item.getItemMeta().getDisplayName().contains(pattern))
                {
                    return true;
                }
                //  Check the lore...
                else
                {
                    if(item.getItemMeta().hasLore())
                    {
                        for(String lore : Objects.requireNonNull(item.getItemMeta().getLore()))
                        {
                            if(lore.contains(pattern))
                            {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * This function is an overload function of of_itemStacksContainsPattern(ItemStack, String);
     * @param items The ItemStack array which should be checked.
     * @param pattern The pattern which should be checked.
     * @return True if the ItemStacks is containing the pattern, otherwise false.
     */
    public boolean of_check4ItemStacksWithSpecificPattern(ItemStack[] items, String pattern)
    {
        for(ItemStack item : items)
        {
            if(of_check4ItemStackWithSpecificPattern(item, pattern))
            {
                return true;
            }
        }

        return false;
    }
}
