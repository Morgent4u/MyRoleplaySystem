package com.roleplay.inventar;

import com.basis.ancestor.Objekt;
import com.basis.main.main;
import com.basis.sys.Sys;
import com.roleplay.extended.ExtendedFile;
import com.roleplay.inventar.normal.inv_atm;
import com.roleplay.inventar.normal.inv_menu;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.io.File;
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
    //  Attributes:
    //  InvId - Inventar-Object
    Map<Integer, Inventar> inventories = new HashMap<>();
    boolean ib_loadOwnInventories;

    /* ************************************* */
    /* LOADER */
    /* ************************************* */

    @Override
    public int of_load()
    {
        //	InventoryId: 1
        //	Description:
        //  This inventory is the default menu for every player.
        of_loadInventoryByFile(new inv_menu(), null);

        //	InventoryId: 2
        //	Description:
        //  This inventory is the atm-inventory for every player.
        of_loadInventoryByFile(new inv_atm(), null);

        //  Load own inventories if its enabled.
        if(of_isUsingOwnInventoriesAllowed())
        {
            //  Load the inventories.
            File directory = new File( Sys.of_getMainFilePath() + "//Inventories//");
            File[] files = directory.listFiles();

            if(files != null && files.length > 0)
            {
                for(File file : files)
                {
                    if(file != null)
                    {
                        //  If the inventory has not been already loaded.
                        if(!of_isFileInventoryAlreadyLoaded(file.getName()))
                        {
                            of_loadInventoryByFile(new Inventar(), file.getName());
                        }
                    }
                }
            }
        }

        return 1;
    }

    /* ************************************* */
    /* OBJECT METHODS */
    /* ************************************* */

    /**
     * This function loads an inventory from a file.
     * If the given file does not exist this method will call the
     * inventar.of_load() function to load all predefined settings
     * for the given inventory-instance.
     * @param inventar The inventory to load.
     */
    public void of_loadInventoryByFile(Inventar inventar, String fileName)
    {
        //  Set the fileName, if it's not set use the inventar-instance name instead.
        if(fileName == null)
        {
            fileName = inventar.of_getInvClassName();
        }

        //  Get the file or create it.
        ExtendedFile invFile = new ExtendedFile(Sys.of_getMainFilePath() + "//Inventories//" + fileName);

        if(invFile.of_fileExists())
        {
            //  Create the inventory-instance.
            Inventory inventory;
            String section = "Inventory";

            //  Get the inventory-name.
            String invName = invFile.of_getSetString(section + ".Name", "&cNo Inventory name");
            invName = invName.replace("&", "§");

            String invType = invFile.of_getSetString(section + ".Type", "CHEST").toUpperCase();
            boolean lb_useInventoryType = !invType.contains("CHEST");
            boolean lb_closeOnClick = invFile.of_getSetBoolean(section + ".CloseOnClick", true);

            //  Get the inventory-size.
            int invSize = invFile.of_getSetInt(section + ".Size", 27);

            if(invSize > 0)
            {
                //  If the inventory-type is not a chest get the inventory-type by the name.
                if(lb_useInventoryType)
                {
                    try
                    {
                        //  Use the given InventoryType
                        InventoryType invTypeEnum = InventoryType.valueOf(invType);
                        inventory = Bukkit.createInventory(null, invTypeEnum, invName);

                        // Get the size of the inventory-type to avoid errors.
                        invSize = inventory.getSize();
                    }
                    //  An error occurred.
                    catch(Exception e)
                    {
                        inventar.of_sendErrorMessage(null, "InventarContext.of_loadInventoryByFile();", "The inventory type '" + invType + "' is not defined.");
                        return;
                    }
                }
                //  Default inventory-type (chest)
                else
                {
                    inventory = Bukkit.createInventory(null, invSize, invName);
                }

                //  Create an array of ItemStacks.
                ItemStack[] itemStacks = new ItemStack[invSize];

                //  Load all ItemStacks...
                for(int i = 0; i < invSize; i++)
                {
                    ItemStack item = invFile.of_getItemStackByKey(section + ".Items." + i);

                    if(item != null)
                    {
                        //  Check for CommandSet and the current item.
                        String[] commandSet = invFile.of_getStringArrayByKey(section + ".Items." + i + ".CommandSet");
                        itemStacks[i] = item;

                        if(commandSet != null && commandSet.length > 0)
                        {
                            //  Store the CommandSet in a local and the inventory-variable!
                            inventar.of_addCommands2ItemSlot(i, commandSet);
                        }
                    }
                }

                //  Handle some Inventory-ItemStacks.
                itemStacks = main.INVENTARSERVICE.of_handleItemStacksFromInventory(itemStacks, invFile, inventar, section);

                if(itemStacks != null)
                {
                    //  Set the ending attributes...
                    inventory.setStorageContents(itemStacks);
                    inventar.of_setInventarName(invName);
                    inventar.of_setInventory(inventory);
                    inventar.of_setCloseOnClickEnabled(lb_closeOnClick);

                    // Check if the inventory need to be a copyInv.
                    if(!inventar.of_isCopyInv())
                    {
                        //  The inventory need to be a copyInv if one of the ItemStacks or the inventory-name contains a placeholder!
                        boolean lb_copyInv = invName.contains("%");

                        if(!lb_copyInv)
                        {
                            lb_copyInv = main.INVENTARSERVICE.of_check4ItemStacksWithSpecificPattern(itemStacks, "%");
                        }

                        //  Set the copyInv state.
                        inventar.of_setCopyInv(lb_copyInv);
                    }

                    //  Set the end-attributes for this object.
                    inventory.setStorageContents(itemStacks);
                    inventar.of_setInventarName(invName);
                    inventar.of_setInventory(inventory);
                    inventar.of_setCloseOnClickEnabled(lb_closeOnClick);
                }
                else
                {
                    inventar.of_sendErrorMessage(null, "InventarContext.of_loadInventoryByFile();", "There was an error while handling the inventory-type! Inventory-File:" + invFile.of_getFileName());
                    return;
                }
            }
            //  An error occurred. No invSlot-size was defined.
            else
            {
                inventar.of_sendErrorMessage(null, "InventarContext.of_loadInventoryByFile();", "No invSlot-size was defined for the Inventory-File: " + invFile.of_getFileName());
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

            //  We load the inventory again...
            of_loadInventoryByFile(inventar, fileName);
            return;
        }

        //  Add the inventory to the inventar-context (inventories).
        inventar.of_setInfo(fileName.toLowerCase().replace(".yml", ""));
        inventar.of_setObjectId(inventories.size() + 1);
        inventories.put(inventar.of_getObjectId(), inventar);
    }

    /**
     * This function saves an inventory to a file.
     * @param inventar The inventory to save.
     * @return 1 if the inventory was saved successfully. -1 if the inventory was not saved.
     */
    private int of_saveInventory2File(ExtendedFile invFile, Inventar inventar)
    {
        //  Check if the inventory instance is valid.
        Inventory inventory = inventar.of_getInv();

        if(inventory != null)
        {
            // Get the inventory contents and the inventory name from the inventory instance.
            ItemStack[] items = inventory.getStorageContents();
            String inventoryName = inventar.of_getInventarName();
            String invClassification = inventar.of_getInvClassification();
            String[] invClassifications = invClassification.split(",");
            int invSize = inventory.getSize();

            //  Store inventory-attributes to the file.
            String section = "Inventory";
            invFile.of_set(section + ".Name", inventoryName.replace("§", "&"));
            invFile.of_set(section + ".Size", invSize);
            invFile.of_set(section + ".Classification", invClassification);
            invFile.of_set(section + ".Type", inventory.getType().toString().toUpperCase());
            invFile.of_set(section + ".CloseOnClick", inventar.of_isClickCloseInv());

            if(items.length > 0)
            {
                boolean lb_moneyTransferInv = ArrayUtils.indexOf(invClassifications, "MONEY_TRANSFER") != -1;
                boolean lb_positionInv = ArrayUtils.indexOf(invClassifications, "POSITION") != -1;

                for(int i = 0; i < items.length; i++)
                {
                    ItemStack item = items[i];

                    if(item != null)
                    {
                        //  Save the item in the file.
                        item = invFile.of_getSetItemStack(section + ".Items." + i, item);

                        //  If the item could be stored we check for inventory-classification
                        if(item != null)
                        {
                            if(lb_moneyTransferInv)
                            {
                                invFile.of_set(section + ".Items." + i + ".UseDefinedList", false);
                                invFile.of_getSetDouble(section + ".Items." + i + ".Price", 999999);
                            }

                            if(lb_positionInv)
                            {
                                invFile.of_getSetString(section + ".Items." + i + ".Pos", "");
                            }

                            //  Check if the item has a defined command-set.
                            String[] commandSet = inventar.of_getCommandsByInvSlot(i);

                            if(commandSet != null && commandSet.length > 0)
                            {
                                invFile.of_set(section + ".Items." + i + ".CommandSet", commandSet);
                            }
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
        Sys.of_sendMessage("Load own inventories: " + of_isUsingOwnInventoriesAllowed());
    }

    /* ************************************* */
    /* SETTER */
    /* ************************************* */

    /**
     * Allows loading own inventories from files.
     * @param bool Allow/Disallow
     */
    public void of_setLoadOwnInventories(boolean bool)
    {
        ib_loadOwnInventories = bool;
    }

    /* ************************************* */
    /* GETTER */
    /* ************************************* */

    /**
     * This function returns the inventory instance for the given id.
     * @param invId The inventory id.
     * @return The inventory instance.
     */
    public Inventar of_getInv(int invId)
    {
       return inventories.get(invId);
    }

    /**
     * This function is used to get an inventory by the file-name.
     * This is used to get own created inventories.
     * @param invName The name of the inventory.
     * @return The inventory instance.
     */
    public Inventar of_getInvByName(String invName)
    {
        invName = invName.replace(".yml","");
        invName = invName.toLowerCase();

        for(Inventar inv : inventories.values())
        {
            if(inv.of_getInfo().equals(invName))
            {
                return inv;
            }
        }

        return null;
    }

    /* ************************************* */
    /* BOOLS */
    /* ************************************* */

    /**
     * Check if for this file is already an inventory is loaded.
     * @param fileName The file name.
     * @return True if the inventory is already loaded.
     */
    public boolean of_isFileInventoryAlreadyLoaded(String fileName)
    {
        fileName = fileName.replace(".yml", "");
        fileName = fileName.toLowerCase();

        for(Inventar inv : inventories.values())
        {
            if(inv != null)
            {
                if(inv.of_getInfo().equals(fileName))
                {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean of_isUsingOwnInventoriesAllowed()
    {
        return ib_loadOwnInventories;
    }
}
