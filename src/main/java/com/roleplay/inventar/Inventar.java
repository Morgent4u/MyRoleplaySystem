package com.roleplay.inventar;

import com.basis.ancestor.Objekt;
import com.basis.main.main;
import com.basis.sys.Sys;
import com.roleplay.spieler.Spieler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import java.util.HashMap;

/**
 * @Created 14.04.2022
 * @Author Nihar
 * @Description
 * This class is used to represent a GU-Interface as an inventory for
 * the player.
 */
public class Inventar extends Objekt
{
    //  Integer (invSlot) - Commands to execute.
    HashMap<Integer, String[]> commands = new HashMap<>();
    Inventory inv;
    String invName;

    boolean ib_copyInv;

    /* ************************************* */
    /* EVENT HANDLER */
    /* ************************************* */

    /**
     * This method is used to transfer the openInventoryEvent to a specific inventory-child-instance.
     * So every inventory-child (or inherited-class) can react differently to this event.
     * @param openedInv The inventory from this event.
     * @param ps Player instance
     * @return true if the event was handled.
     */
    public boolean ue_openInventoryEvent(Inventory openedInv, Spieler ps)
    {
        return false;
    }

    /**
     * This method is used to transfer the clickInventoryEvent to a specific inventory-child-instance.
     * So every inventory-child (or inherited-class) can react differently to this event.
     * @param e Event-Instance this is used to get more information for this method.
     * @param openedInv The inventory from this event.
     * @param ps Player instance
     * @return true if the event was handled.
     */
    public boolean ue_itemClickEvent(InventoryClickEvent e, Inventory openedInv, Spieler ps)
    {
        return false;
    }

    /* ************************************* */
    /* OBJEKT-ANWEISUNGEN */
    /* ************************************* */

    /**
     * This method can be overridden in the inherited (child)-class to
     * add specific commands (for the items) to the inventory.
     */
    public void of_defineCommands4Inventory()
    {
        //  This should be overridden in the child-class.
    }

    /* ************************************* */
    /* DEBUG CENTER */
    /* ************************************* */

    @Override
    public void of_sendDebugDetailInformation()
    {
        Sys.of_sendMessage("InventoryName: "+of_getInventarName());
        Sys.of_sendMessage("CopyInv: "+of_isCopyInv());

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
     * @param invSlot The ItemName to interact with.
     * @param cmds The commands to execute.
     * @return true if the command was added.
     */
    public boolean of_addCommands2ItemName(int invSlot, String[] cmds)
    {
        if(!commands.containsKey(invSlot))
        {
            commands.put(invSlot, cmds);
            return true;
        }

        return false;
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

    /* ************************************* */
    /* GETTER */
    /* ************************************* */

    /**
     * This function returns the inventory instance.
     * If this inventory is a copyInv-inventory the inventory will be copied.
     * @return The inventory instance.
     */
    public Inventory of_getInv()
    {
        Inventory localInv = inv;

        //  If the inventory need to be copied...
        if(of_isCopyInv())
        {
            localInv = main.INVENTARSERVICE.of_copyInv(inv, of_getInventarName());
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
     * This function returns the inventory-name.
     * @return The inventory-name.
     */
    public String of_getInventarName()
    {
        return invName;
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
}
