package com.roleplay.inventar;

import com.basis.ancestor.Objekt;
import com.basis.sys.Sys;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;

/**
 * @Created 14.04.2022
 * @Author Nihar
 * @Description
 * This class is used to load, save or get a defined inventory.
 *
 * This class has been created with the support of the
 * GitHub-CoPilot project.
 *
 */
public class InventarContext extends Objekt
{
    //  InvId - Inventar-Object
    Map<Integer, Inventar> inventories = new HashMap<>();

    /* ************************************* */
    /* LOADER */
    /* ************************************* */

    @Override
    public int of_load()
    {
        //	InventoryId: 1
        //	Description:
        //  First inventory...

        /*
        Example:
        inv_arena_select4invite inv_select4inv = new inv_arena_select4invite();
        Datei datei = new Datei(directory.getPath() + "//" + inv_select4inv.of_getObjektName());
        of_loadInventoryByFile(datei, inv_select4inv);
        */

        return 1;
    }

    /**
     * This function loads an inventory from a file.
     * @param datei The file to load the inventory from.
     * @param inventar The inventory to load.
     * @return 1 if the inventory was loaded successfully. -1 if the inventory was not loaded.
     */
    public int of_loadInventoryByFile(InventarDatei datei, Inventar inventar)
    {
        if(datei.of_fileExists())
        {
            String section = "Inventory";

            //  Get the inventory-name.
            String invName = datei.of_getSetString(section + ".Name", "&cNo Inventory name");
            invName = invName.replace("&", "§");

            //  Get the inventory-size.s
            int invSize = datei.of_getSetInt(section + ".Size", 27);

            if(invSize > 0)
            {
                //  Create an array of ItemStacks.
                ItemStack[] itemStacks = new ItemStack[invSize];

                //  Get the inventory-items.
                for(int i = 0; i < invSize; i++)
                {
                    ItemStack item = datei.of_getItemStackByKey(section + ".Items." + i);

                    if(item != null)
                    {
                        itemStacks[i] = item;

                        //  Check if the item has a defined command-set.
                        String[] commandSet = datei.of_getStringArrayByKey(section + ".Items." + i + ".CommandSet");

                        if(commandSet != null && commandSet.length > 0)
                        {
                            inventar.of_addCommands2ItemName(i, commandSet);
                        }
                    }
                }

                //  Create the inventory and the inventar-instance.
                Inventory inventory = Bukkit.createInventory(null, invSize, invName);
                inventory.setStorageContents(itemStacks);
                inventar.of_setInventarName(invName);
                inventar.of_setInventory(inventory);
            }
            //  An error occurred. No invSlot-size was defined.
            else
            {
                inventar.of_sendErrorMessage(null, "InventarContext.of_loadInventoryByFile();", "No invSlot-size was defined for the file-inventory: " + datei.of_getFileName());
                return -1;
            }
        }
        // When the file does not exist, let the inventar-instance create a predefined inventory which
        // will be saved in the file.
        else
        {
            //  Load the attributes of the inventory.
            inventar.of_load();
            inventar.of_defineCommands4Inventory();

            //  After the inventory is created, save it to the file.
            int rc = of_saveInventory2File(inventar);

            if(rc != 1)
            {
                inventar.of_sendErrorMessage(null, "InventarContext.of_loadInventoryByFile();", "The file-inventory could not be saved: " + datei.of_getFileName());
                return -1;
            }
        }

        return -1;
    }

    /**
     * This function saves an inventory to a file.
     * @param inventar The inventory to save.
     * @return 1 if the inventory was saved successfully. -1 if the inventory was not saved.
     */
    public int of_saveInventory2File(Inventar inventar)
    {
        //  Check if the inventory instance is valid.
        Inventory inventory = inventar.of_getInv();

        if(inventory != null)
        {
            // Get the inventory contents and the inventory name from the inventory instance.
            ItemStack[] items = inventory.getStorageContents();
            String inventoryName = inventar.of_getInventarName();
            String inventoryNameNormalized = Sys.of_getNormalizedString(inventoryName).toLowerCase();
            int invSize = inventory.getSize();

            //  Create a new file.
            InventarDatei datei = new InventarDatei(Sys.of_getMainFilePath() + "//" + inventoryNameNormalized);
            String section = "Inventory";

            datei.of_set(section + ".Name", inventoryName.replace("§", "&"));
            datei.of_set(section + ".Size", invSize);

            if(items.length > 0)
            {
                for(int i = 0; i < items.length; i++)
                {
                    ItemStack item = items[i];

                    if(item != null)
                    {
                        //  Save the item in the file.
                        item = datei.of_getSetItemStack(section + ".Items." + i, item);

                        if(item != null)
                        {
                            //  Check if the item has a defined command-set.
                            String[] commandSet = inventar.of_getCommandsByInvSlot(i);

                            if(commandSet != null && commandSet.length > 0)
                            {
                                datei.of_set(section + ".Items." + i + ".CommandSet", commandSet);
                            }
                        }
                        //  An error occurred.
                        else
                        {
                            inventar.of_sendErrorMessage(null, "InventarContext.of_saveInventory2File();", "The item-stack could not be saved: " + inventoryName + "." + i);
                            return -1;
                        }
                    }
                }
            }

            return datei.of_save("InventarContext.of_saveInventory2File(); File: " + inventoryNameNormalized);
        }

        return -1;
    }
}
