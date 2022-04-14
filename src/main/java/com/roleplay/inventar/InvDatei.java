package com.roleplay.inventar;

import com.basis.main.main;
import com.basis.sys.Sys;
import com.basis.utils.Datei;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * @Created 14.04.2022
 * @Author Nihar
 * @Description
 * This class is used to read a file which is meant to be an inventory.
 * It contains several useful methods to get or set inventory-items into the file.
 *
 * This class has been created with the support of the
 * GitHub-CoPilot project.
 *
 */
public class InvDatei extends Datei
{
    /* ************************************* */
    /* CONSTRUCTOR */
    /* ************************************* */

    public InvDatei(String path)
    {
        super(path);
    }

    /* ************************************* */
    /* GETTER-SETTER */
    /* ************************************* */

    /**
     * This function checks if for the configKey is an ItemStack defined and returns it.
     * If it's not it will set the given ItemStack to the configKey and return it also.
     * @param configKey The config key of the itemStack.
     * @param item The itemStack which should be set (as default).
     * @return The itemStack which is set.
     */
    public ItemStack of_getSetItemStack(String configKey, ItemStack item)
    {
        if(cfg != null)
        {
            ItemStack returnItem = of_getItemStackByKey(configKey);

            //  If the itemStack is defined, return it.
            if(returnItem != null)
            {
                return returnItem;
            }

            Sys.of_debug("File: " + of_getFileName() + " | ItemStack is not defined! 'Key="+configKey+"'. Setting it now, you can ignore this message, system is still working :)!");

            // Set the default itemStack to the configKey.
            of_set(configKey+".Material", item.getType().toString());
            of_set(configKey+".Amount", item.getAmount());

            //  Check if a meta is defined and set it to the configKey.
            ItemMeta meta = item.getItemMeta();

            if(meta != null)
            {
                //  Set the item-name to the configKey.
                if(meta.hasDisplayName())
                {
                    of_set(configKey+".DisplayName", meta.getDisplayName().replace("§", "&"));
                }

                //  Set the lore to the configKey.
                if(meta.hasLore())
                {
                    ArrayList<String> lore = (ArrayList<String>) meta.getLore();
                    lore = Sys.of_getReplacedArrayList(lore, "§", "&");

                    of_set(configKey+".Lore", lore);
                }

                //  Set the enchantments to the configKey.
                if(meta.hasEnchants())
                {
                    //  Get the enchantments.
                    ArrayList<String> enchantData = new ArrayList<>();
                    Map<Enchantment, Integer> enchants = meta.getEnchants();

                    //  Loop through the enchantments.
                    for(Enchantment enchant : enchants.keySet())
                    {
                        //  Get the enchantment level and the enchantment name.
                        int enchantLevel = enchants.get(enchant);
                        String enchantmentData = enchant.getName().toString()+";"+enchantLevel;

                        //  Add the enchantment to the enchantData.
                        enchantData.add(enchantmentData);
                    }

                    if(!enchantData.isEmpty())
                    {
                        of_getSetStringArrayList(configKey+".Enchantments", enchantData);
                    }
                }
            }

            return item;
        }

        return null;
    }

    /**
     * This function is used to store an array of ItemStacks into the file.
     * @param configKey The key of the section in the file.
     * @param items The array of ItemStacks.
     * @return 1 if successful, -1 if not.
     */
    public int of_setItemStackAsArray(String configKey, ItemStack[] items)
    {
        if(cfg != null)
        {
            if(items != null && items.length > 0)
            {
                for(int i = 0; i < items.length; i++)
                {
                    ItemStack item = items[i];

                    if(item != null)
                    {
                        ItemStack savedItem = of_getSetItemStack(configKey + "." + i, item);

                        //  Check if the returned itemStack is invalid.
                        if(savedItem == null)
                        {
                            Sys.of_debug( "File: " + of_getFileName() + " | ItemStack is invalid! Key="+configKey + "." + i);
                            return -1;
                        }
                    }
                }
            }
        }

        return 1;
    }


    /* ************************************* */
    /* GETTER */
    /* ************************************* */

    /**
     * Returns an ItemStack from the given config-key.
     * It also checks if enchantments are set in the file and adds them to the ItemStack.
     * @param configKey The config-key to get the ItemStack from.
     * @return The ItemStack.
     */
    public ItemStack of_getItemStackByKey(String configKey)
    {
        if(cfg != null)
        {
            if(cfg.getString(configKey) != null)
            {
                //  Get the material or type of the itemStack.
                Material material = Material.getMaterial(of_getSetString(configKey+".Material", "STONE").toUpperCase());

                if(material != null)
                {
                    // The amount of this itemStack.
                    int amount = of_getIntByKey(configKey+".Amount");

                    if(amount <= 0)
                    {
                        amount = 1;
                    }

                    //  The itemStack itself.
                    ItemStack item = new ItemStack(material, amount);
                    ItemMeta meta = item.getItemMeta();

                    if(meta != null)
                    {
                        //  Set default attributs.
                        meta = main.INVENTARSERVICE.of_setDefaultAttributes2ItemMeta(meta);

                        //  Set the displayName.
                        String displayName = of_getSetString(configKey+".DisplayName", "&cNo DisplayName").replace("&", "§");
                        meta.setDisplayName(displayName);

                        //  Get the lore...
                        if(of_getString(configKey+".Lore") != null)
                        {
                            String[] lore = of_getStringArrayByKey(configKey+".Lore");

                            if(lore != null && lore.length > 0)
                            {
                                lore = Sys.of_getReplacedArrayString(lore, "&", "§");
                                meta.setLore(Arrays.asList(lore));
                            }
                        }

                        //  Check for enchantments...
                        if(of_getString(configKey+".Enchantments") != null)
                        {
                            String[] enchants = of_getStringArrayByKey(configKey+".Enchantments");

                            if(enchants != null && enchants.length > 0)
                            {
                                //  Add the enchantments to the ItemStack.
                                for(String enchant : enchants)
                                {
                                    //  Search for specific attributes...
                                    String[] enchantData = enchant.split(";");

                                    if(enchantData.length > 0)
                                    {
                                        //  Get the enchantment name and level from the attributes.
                                        String enchantmentName = enchantData[0];
                                        int enchantLevel = Sys.of_getString2Int(enchantData[1]);

                                        if(enchantLevel > 0)
                                        {
                                            // Create the enchantment by the given name and level and add it to the itemStack.
                                            Enchantment enchantment = Enchantment.getByName(enchantmentName);

                                            if(enchantment != null)
                                            {
                                                meta.addEnchant(enchantment, enchantLevel, true);
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        //  Finally, set the current created meta to the itemStack and return it.
                        item.setItemMeta(meta);

                        return item;
                    }
                }
            }
        }

        return null;
    }

    /**
     * This function is equal to of_getItemStacks2ArrayByKey(String) but you can set
     * the amount of the itemstacks which should be return.
     * @param configKey The config key of the itemstack.
     * @param arraySize The size of the array which should be return.
     * @return The array of itemstacks.
     */
    public ItemStack[] of_getItemStacks2ArrayByKey(String configKey, int arraySize)
    {
        if(cfg != null)
        {
            //  Get all ItemStacks which are defined for this configKey.
            ItemStack[] items = of_getItemStacks2ArrayByKey(configKey);
            ItemStack[] items4Return = new ItemStack[arraySize];

            //  Only return the amount which is set
            if(items != null && items.length <= arraySize)
            {
                System.arraycopy(items, 0, items4Return, 0, arraySize);
                return items4Return;
            }
        }

        return null;
    }

    /**
     * This function returns an array of ItemStack by a configKey.
     * @param configKey The configKey of the file for the ItemStacks.
     * @return An array of ItemStack.
     */
    public ItemStack[] of_getItemStacks2ArrayByKey(String configKey)
    {
        ItemStack[] invContent = null;

        try
        {
            invContent = ((List<ItemStack>) (cfg.get(configKey))).toArray(new ItemStack[0]);;
        }
        catch (Exception ignored) { }

        return invContent;
    }
}
