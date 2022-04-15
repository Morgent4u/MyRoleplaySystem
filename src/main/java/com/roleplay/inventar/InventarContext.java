package com.roleplay.inventar;

import com.basis.ancestor.Objekt;
import com.basis.main.main;
import com.basis.sys.Sys;
import com.roleplay.inventar.normal.inv_menu;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
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
        //  This inventory is the default menu for every player.
        of_loadInventoryByFile(new inv_menu());

        return 1;
    }

    /* ************************************* */
    /* OBJEKT ANWEISUNGEN */
    /* ************************************* */

    /**
     * This function loads an inventory from a file.
     * @param inventar The inventory to load.
     */
    public void of_loadInventoryByFile(Inventar inventar)
    {
        InventarDatei invFile = new InventarDatei(Sys.of_getMainFilePath() + "//Inventories//" + inventar.of_getInvClassName());

        if(invFile.of_fileExists())
        {
            String section = "Inventory";

            //  Get the inventory-name.
            String invName = invFile.of_getSetString(section + ".Name", "&cNo Inventory name");
            invName = invName.replace("&", "§");

            String invClassification = invFile.of_getSetString(section + ".Classification", "DEFAULT").toUpperCase();
            String invType = invFile.of_getSetString(section + ".Type", "CHEST").toUpperCase();
            boolean useInventoryType = !invType.contains("CHEST");

            //  Get the inventory-size.s
            int invSize = invFile.of_getSetInt(section + ".Size", 27);

            if(invSize > 0)
            {
                //  Create an array of ItemStacks.
                ItemStack[] itemStacks = new ItemStack[invSize];

                //  Get the inventory-items.
                for(int i = 0; i < invSize; i++)
                {
                    ItemStack item = invFile.of_getItemStackByKey(section + ".Items." + i);

                    if(item != null)
                    {
                        itemStacks[i] = item;

                        //  Check if the item has a defined command-set.
                        String[] commandSet = invFile.of_getStringArrayByKey(section + ".Items." + i + ".CommandSet");

                        if(commandSet != null && commandSet.length > 0)
                        {
                            inventar.of_addCommands2ItemName(i, commandSet);
                        }
                    }
                }

                //  Create the inventory and the inventar-instance.
                Inventory inventory;

                if(useInventoryType)
                {
                    try
                    {
                        InventoryType invTypeEnum = InventoryType.valueOf(invType);
                        inventory = Bukkit.createInventory(null, invTypeEnum, invName);
                    }
                    catch(Exception e)
                    {
                        inventar.of_sendErrorMessage(null, "InventarContext.of_loadInventoryByFile();", "The inventory type '" + invType + "' is not defined.");
                        return;
                    }
                }
                else
                {
                    inventory = Bukkit.createInventory(null, invSize, invName);
                }

                // Check if the inventory need to be a copyInv.
                if(!inventar.of_isCopyInv())
                {
                    //  The inventory need to be a copyInv if one of the ItemStacks or the inventory-name contains a placeholder!
                    boolean lb_copyInv = invName.contains("%");

                    if(!lb_copyInv)
                    {
                        lb_copyInv = main.INVENTARSERVICE.of_itemStacksContainsPattern(itemStacks, "%");
                    }

                    //  Set the copyInv state.
                    inventar.of_setCopyInv(lb_copyInv);
                }

                inventory.setStorageContents(itemStacks);
                inventar.of_setInventarName(invName);
                inventar.of_setInventory(inventory);
                inventar.of_setInvClassification(invClassification);
            }
            //  An error occurred. No invSlot-size was defined.
            else
            {
                inventar.of_sendErrorMessage(null, "InventarContext.of_loadInventoryByFile();", "No invSlot-size was defined for the file-inventory: " + invFile.of_getFileName());
                return;
            }
        }
        // When the file does not exist, let the inventar-instance create a predefined inventory which
        // will be saved in the file.
        else
        {
            //  Load the attributes of the inventory.
            inventar.of_load();
            inventar.of_defineCommands4Inventory();

            // Validate the inventory.
            String errorMessage = inventar.of_validate();

            if(errorMessage != null)
            {
                inventar.of_sendErrorMessage(null, "InventarContext.of_loadInventoryByFile();", errorMessage);
                return;
            }

            //  After the inventory is created, save it to the file.
            int rc = of_saveInventory2File(invFile, inventar);

            if(rc != 1)
            {
                inventar.of_sendErrorMessage(null, "InventarContext.of_loadInventoryByFile();", "The file-inventory could not be saved: " + invFile.of_getFileName());
                return;
            }
        }

        //  Add the inventory to the inventar-context (inventories).
        inventar.of_setObjectId(inventories.size() + 1);
        inventories.put(inventar.of_getObjectId(), inventar);
    }

    /**
     * This function saves an inventory to a file.
     * @param inventar The inventory to save.
     * @return 1 if the inventory was saved successfully. -1 if the inventory was not saved.
     */
    public int of_saveInventory2File(InventarDatei invFile, Inventar inventar)
    {
        //  Check if the inventory instance is valid.
        Inventory inventory = inventar.of_getInv();

        if(inventory != null)
        {
            // Get the inventory contents and the inventory name from the inventory instance.
            ItemStack[] items = inventory.getStorageContents();
            String inventoryName = inventar.of_getInventarName();
            int invSize = inventory.getSize();

            String section = "Inventory";
            invFile.of_set(section + ".Name", inventoryName.replace("§", "&"));
            invFile.of_set(section + ".Size", invSize);
            invFile.of_set(section + ".Classification", inventar.of_getInvClassification());
            invFile.of_set(section + ".Type", inventory.getType().toString().toUpperCase());

            if(items.length > 0)
            {
                for(int i = 0; i < items.length; i++)
                {
                    ItemStack item = items[i];

                    if(item != null)
                    {
                        //  Save the item in the file.
                        item = invFile.of_getSetItemStack(section + ".Items." + i, item);

                        if(item != null)
                        {
                            //  Check if the item has a defined command-set.
                            String[] commandSet = inventar.of_getCommandsByInvSlot(i);

                            if(commandSet != null && commandSet.length > 0)
                            {
                                invFile.of_set(section + ".Items." + i + ".CommandSet", commandSet);
                            }
                        }
                        //  An error occurred.
                        else
                        {
                            inventar.of_sendErrorMessage(null, "InventarContext.of_saveInventory2File();", "The item-stack could not be saved on position: " + i);
                            return -1;
                        }
                    }
                }
            }

            return invFile.of_save("InventarContext.of_saveInventory2File(); File: " + invFile.of_getFileName());
        }

        return -1;
    }

    /* ************************************* */
    /* DEBUG CENTER */
    /* ************************************* */

    @Override
    public void of_sendDebugDetailInformation()
    {
        //  Send the debug information.
        Sys.of_sendMessage("Loaded inventories: " + inventories.size());
    }

    /* ************************************* */
    /* GETTER */
    /* ************************************* */

    public Inventar of_getInv(int invId)
    {
       return inventories.get(invId);
    }
}
