package com.roleplay.inventar;

import com.basis.ancestor.Objekt;
import com.basis.main.main;
import com.basis.sys.Sys;
import com.roleplay.board.MessageBoard;
import com.roleplay.objects.CommandSet;
import com.roleplay.spieler.Spieler;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;

/**
 * @Created 14.04.2022
 * @Author Nihar
 * @Description
 * This class is used to represent a GU-Interface as an inventory for
 * the player.
 *
 * Current Classifactions:
 * - DEFAULT
 * - MONEY_TRANSFER
 * - TEMPLATE_ITEM
 *
 */
public class Inventar extends Objekt
{
    //  Integer (invSlot) - Commands to execute.
    public Inventory inv;

    HashMap<Integer, String[]> commands = new HashMap<>();
    String invName;
    String invClassification = "DEFAULT";

    boolean ib_clickCloseInv = true;
    boolean ib_copyInv;

    /* ************************************* */
    /* EVENT HANDLER */
    /* ************************************* */

    /**
     * This event is used to modify the itemStacks in the given inventory.
     * @param openedInv The inventory from this event.
     * @param ps Player instance
     */
    public void ue_openInventoryEvent(Inventory openedInv, Spieler ps)
    {
        //  If this is a copyInv inventory we need to replace the given placeholder in the itemStacks.
        if(of_isCopyInv())
        {
            ItemStack[] items = openedInv.getStorageContents();

            for(int i = 0; i < items.length; i++)
            {
                if(items[i] != null)
                {
                    items[i] = main.INVENTARSERVICE.of_replaceItemStackWithPlayerStats(items[i], ps);

                    //  Check if the item is a player head.
                    if(items[i].getType() == Material.PLAYER_HEAD)
                    {
                        items[i] = main.INVENTARSERVICE.of_convertPlayerHead2PlayerHeadWithSkin(items[i], ps.of_getPlayer().getName());
                    }
                }
            }

            openedInv.setStorageContents(items);
        }
    }

    /**
     * This method is used to execute the commands for the inventory-slot.
     * This method also transfers the event to the child-class so that it can be
     * overridden in the child-class.
     * @param localInv The inventory from this event.
     * @param clickedItem The item from this event.
     * @param clickedSlot The item-slot from this event.
     * @param ps Player instance
     */
    public void ue_clickInventoryEvent(Inventory localInv, ItemStack clickedItem, int clickedSlot, Spieler ps)
    {
        String[] cmds = commands.get(clickedSlot);

        boolean lb_openInv = false;

        if(cmds != null && cmds.length > 0)
        {
            //  Execute all given commands.
            CommandSet cmdSet = new CommandSet(cmds, ps);
            lb_openInv = cmdSet.of_commandExists("OPEN");
            cmdSet.of_executeAllCommands();
        }

        //  Closing after executing all commands.
        //  If the CommandSet opens an inventory we do not close the current inventory.
        if(of_isClickCloseInv() && !lb_openInv)
        {
            main.SPIELERSERVICE.of_closeInventory(ps);
        }
    }

    /* ************************************* */
    /* OBJEKT-ANWEISUNGEN */
    /* ************************************* */

    /**
     * This function needs to be overridden in the child class so the
     * inventory has a predefined gui. This function will be called
     * if no inventory-file exist!
     * @return 1 if the inventory is created, -1 if not.
     */
    @Override
    public int of_load()
    {
        return super.of_load();
    }

    /**
     * This method can be overridden in the inherited (child)-class to
     * add specific commands (for the items) to the inventory.
     */
    public void of_defineCommands4Inventory()
    {
        //  This should be overridden in the child-class.
    }

    @Override
    public String of_validate()
    {
        if(invName == null)
        {
            return "Inventory-Name is not set.";
        }

        if(commands.size() == 0)
        {
            return "No commands defined.";
        }

        return null;
    }

    /* ************************************* */
    /* DEBUG CENTER */
    /* ************************************* */

    @Override
    public void of_sendDebugDetailInformation()
    {
        Sys.of_sendMessage("Inv-Id: " + of_getObjectId());
        Sys.of_sendMessage("Inv-Name: "+of_getInventarName());
        Sys.of_sendMessage("Inv-Classification: " + of_getInvClassification());
        Sys.of_sendMessage("Inv-Copy: "+of_isCopyInv());

        //  Print all commands into the console.
        if(commands != null && commands.size() > 0)
        {
            Sys.of_sendMessage("Commands: "+commands.size());

            for(int invSlot : commands.keySet())
            {
                //  Get the command-set.
                String[] cmds = commands.get(invSlot);

                if(cmds != null && cmds.length > 0)
                {
                    //  Print the command-set to this console.
                    for (String cmd : cmds)
                    {
                        Sys.of_sendMessage("Command: " + cmd + " | InvSlot: " + invSlot);
                    }
                }
            }
        }
    }

    /* ************************************* */
    /* SETTER */
    /* ************************************* */

    /**
     * This function is used to add dynamically a command to the inventory.
     * It will be called it the inventory-file has not been created yet.
     * @param invSlot The inventory-slot on which the item has been set!
     * @param cmds The command to execute.
     */
    public void of_addCommands2ItemSlot(int invSlot, String[] cmds)
    {
        if(!commands.containsKey(invSlot))
        {
            commands.put(invSlot, cmds);
        }
    }

    /**
     * This method is used to override/update the current CommandSet
     * which has been set on the given InvSlot!
     * @param invSlot The inventory-slot which the item has been set!
     * @param cmds The command to execute.
     */
    public void of_updateCommandSet2ItemSlot(int invSlot, String[] cmds)
    {
        commands.put(invSlot, cmds);
    }

    /**
     * This function sets the inventory instance to the given one.
     * @param inv The inventory instance.
     */
    public void of_setInventory(Inventory inv)
    {
        this.inv = inv;
    }

    /**
     * This function sets the inventory-name.
     * @param inventarName The inventory-name.
     */
    public void of_setInventarName(String inventarName)
    {
        this.invName = inventarName;
    }

    /**
     * This function sets the inventory as a copyInv.
     * A copyInv is used to get by calling the function of_getInv();
     * o copy of the inventory-instance. With this feature its possible to
     * show every player a different inventory because the inventory will be copied!
     * @param bool true if the inventory should be a copyInv.
     */
    public void of_setCopyInv(boolean bool)
    {
        ib_copyInv = bool;
    }

    /**
     * This function sets the inventory-classification-type.
     * With the inventory-classification-type an inventory can be handled differently.
     * @param invClassification The inventory-classification-type.
     */
    public void of_setInvClassification(String invClassification)
    {
        this.invClassification = invClassification.toUpperCase();
    }

    public void of_setCloseOnClickEnabled(boolean bool)
    {
        this.ib_clickCloseInv = bool;
    }

    /* ************************************* */
    /* GETTER */
    /* ************************************* */

    /**
     * This function returns the inventory instance.
     * If this inventory is a copyInv-inventory the inventory will be copied.
     * @return The inventory instance.
     */
    public Inventory of_getInv(Spieler ps)
    {
        Inventory localInv = inv;

        //  If the inventory need to be copied...
        if(of_isCopyInv())
        {
            localInv = main.INVENTARSERVICE.of_copyInv(inv, MessageBoard.of_getInstance().of_translateMessageWithPlayerStats(of_getInventarName(), ps));
        }

        return localInv;
    }

    /**
     * This function should be used in the events to get the specific commands for the item
     * which is defined for this slot.
     * @param invSlot The slot of the item.
     * @return The commands for the item (invSlot).
     */
    public String[] of_getCommandsByInvSlot(int invSlot)
    {
        String[] cmds = commands.get(invSlot);

        if(cmds != null && cmds.length > 0)
        {
            return cmds;
        }

        return null;
    }

    /**
     * This function returns the inventory without checking anything.
     * @return The inventory instance.
     */
    public Inventory of_getInv()
    {
        return inv;
    }

    /**
     * This function returns the inventory-name.
     * @return The inventory-name.
     */
    public String of_getInventarName()
    {
        return invName;
    }

    public String of_getInvClassification()
    {
        return invClassification;
    }

    public String of_getInvClassName()
    {
        return getClass().getSimpleName();
    }

    /* ************************************* */
    /* BOOLS */
    /* ************************************* */

    /**
     * This function returns true if the inventory is a copyInv.
     * For more information about the copyInv read the information for the
     * of_setCopyInv().
     * @return true if the inventory is a copyInv.
     */
    public boolean of_isCopyInv()
    {
        return ib_copyInv;
    }

    /**
     * If this option is enabled after clicking on an item in the inventory, the inventory
     * will be closed.
     * @return true if the inventory should be closed after clicking on an item.
     */
    public boolean of_isClickCloseInv()
    {
        return ib_clickCloseInv;
    }

}
