package com.roleplay.inventar;

import com.basis.ancestor.Objekt;
import com.basis.sys.Sys;
import com.roleplay.board.MessageBoard;
import com.roleplay.extended.ExtendedFile;
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
    /* OBJEKT - ANWEISUNGEN */
    /* ************************************* */

    /**
     * This function has been implemented to structure the sourcecode from the InventarContext
     * to the InventarService.
     *
     * @param invFile The given inventory-file.
     * @param section The given inventory--file-section.
     * @param item The current itemStack.
     * @param arrayIndex The current arrayIndex of the item which has been loaded.
     * @param invClassification The inv-classification.
     * @param commandSet Defined CommandSets.
     * @return 1 = OK, -1 = ERROR, -2 = NO Error but the ItemStacks[]-Array has been filled by this function.
     */
    public Object[] of_handleInventoryClassification4ItemStack(Inventory inv, ExtendedFile invFile, String section, ItemStack item, int arrayIndex, String invClassification, String[] commandSet)
    {
        //  We need to handle the inventory-classification. The return is an object-array,
        //  Array-Index:
        //  0 => integer => 1 = OK, -1 Error, -2 Break here
        //  1 => item => modified-item-stack (for example replace some placeholder in the item-stack).
        //  2 => CommandSet => modified-command-set (for example replace some placeholder).
        //  3 => ItemStacks[] => If we break here, the itemStacks-Array has been filled in the Inventory-Class-Handle.
        //  4 => CommandSets[] => Is used to set to the defined ItemStacks which has been set by the handle-function, the right commandSet.

        Object[] errorObject = new Object[] {-1, null, null, null, null};

        //  Check for the inventory classification.
        if(invClassification.equals("MONEY_TRANSFER"))
        {
            if(invFile.of_getConfig().isSet(section + ".Items." + arrayIndex + ".Price"))
            {
                double price = invFile.of_getDoubleByKey(section + ".Items." + arrayIndex + ".Price");

                if(price != -1)
                {
                    item = of_replaceItemStackValues(item, "%price%", String.valueOf(price));

                    //  Iterate through the CommandSet and check if the placeholder is in the command.
                    if(commandSet != null)
                    {
                        for(int j = 0; j < commandSet.length; j++)
                        {
                            if(commandSet[j].contains("%price%"))
                            {
                                commandSet[j] = commandSet[j].replace("%price%", String.valueOf(price));
                            }
                        }
                    }

                    //  Return the needed objects in the correct order.
                    return new Object[] {1, item, commandSet, null, null};
                }
                //  If the price is not valid.
                else
                {
                    Sys.of_debug("InventarService.of_handleInventoryClassification4ItemStack();  - The price of the item is not valid. Config-key: " + section + ".Items." + arrayIndex + ".Price");
                }
            }
        }
        else if(invClassification.startsWith("TEMPLATE_ITEM"))
        {
            String dataSource = invClassification.replace("TEMPLATE_ITEM", "");

            //  TODO: Allgemeinesn Dienst erstellten, welcher jede Datensource annehmen kann.
            //  Hier soll folgende Funktion hilfreich sein.
            //  Sys.of_getStringWithoutPlaceholder()

            return new Object[] {-2, item, commandSet, null, null};
        }

        //  Send the default...
        return new Object[] {1, null, null, null, null};
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
