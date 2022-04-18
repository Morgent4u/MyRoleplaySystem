package com.roleplay.spieler;

import com.basis.ancestor.Objekt;
import com.basis.main.main;
import com.roleplay.inventar.Inventar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.Collection;

/**
 * @Created 21.03.2022
 * @Author Nihar
 * @Description
 * This class contains useful methods or functions
 * in reference to the object: Spieler.
 *
 */
public class SpielerService extends Objekt
{
    //	Attribute
    public SpielerContext _CONTEXT;

    /* ************************************* */
    /* LOADER // UNLOADER */
    /* ************************************* */

    /**
     * This function creates for each online player an object instance of the class Spieler.
     * @return 1 if successful, 0 if not.
     */
    @Override
    public int of_load()
    {
        _CONTEXT = new SpielerContext();

        if(Bukkit.getOnlinePlayers().size() > 0)
        {
            for(Player p : Bukkit.getOnlinePlayers())
            {
                _CONTEXT.of_loadPlayer(p);
            }
        }
        //  Close database connection.
        else if(main.SETTINGS.of_isUsingMySQL() && main.SQL != null && main.SQL.of_isConnected())
        {
            //  The database connection will be established when the player connects to the server!
            main.SQL.of_closeConnection();
        }

        return 1;
    }

    /**
     * Is used to save player data in the database before
     * the server is reloading/stopping.
     */
    @Override
    public void of_unload()
    {
        if(_CONTEXT != null)
        {
            Collection<Spieler> players = _CONTEXT.of_getAllPlayers();

            for(Spieler ps : players)
            {
                _CONTEXT.of_savePlayer(ps);
            }
        }
    }

    /* ************************************* */
    /* SEND METHODS */
    /* ************************************* */

    /**
     * This function sends an interactive-chat message to the player.
     * @param ps Player instance.
     * @param chatText The text which will be displayed in the chat.
     * @param hoverText The text which will be displayed if the cursor hovers over it.
     * @param command The command which will be executed by the player if the user clicks on the message.
     */
    public void of_sendInteractiveMessage(Spieler ps, String chatText, String hoverText, String command)
    {
        //	Interaktive Chat-Nachricht!
        TextComponent tc = new TextComponent();
        tc.setText(chatText);
        tc.setBold(true);
        tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + command));
        tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()));
        ps.of_getPlayer().spigot().sendMessage(tc);
    }

    /**
     * This function sends the message to the player which is defined when an error occurs.
     * @param ps Player instance.
     * @param errorMessage The error message which will be displayed.
     */
    public void of_sendErrorMessage(Spieler ps, String errorMessage)
    {
        //  Send the error message to the player.
        String message = main.MESSAGEBOARD.of_getMessageWithPlayerStats("General.ErrorMessage", ps);
        message = message.replace("%errorMessage%", errorMessage);

        ps.of_getPlayer().sendMessage(message);
    }

    /**
     * This function sends the message to the player which is defined when the player has no permissions.
     * @param ps Player instance.
     */
    public void of_sendNoPermissionsMessage(Spieler ps)
    {
        ps.of_getPlayer().sendMessage(main.MESSAGEBOARD.of_getMessageWithPlayerStats("General.NoPermissions", ps));
    }

    /* ************************************* */
    /* INVENTORY METHODS */
    /* ************************************* */

    /**
     * This function is used to open an inventory for a player.
     * @param ps The player instance.
     * @param invId The id of the inventory.
     */
    public void of_openInvById(Spieler ps, int invId)
    {
        Inventar inv = main.INVENTARSERVICE._CONTEXT.of_getInv(invId);

        if(inv != null)
        {
            ps.of_setInvId(invId);
            ps.of_getPlayer().openInventory(inv.of_getInv(ps));
        }
    }

    /**
     * This function is used to open an inventory by the fileName for the player.
     * This method opens an own created inventory.
     * @param ps The player instance.
     * @param invFileName The fileName of the inventory.
     */
    public void of_openInvByName(Spieler ps, String invFileName)
    {
        Inventar inv = main.INVENTARSERVICE._CONTEXT.of_getInvByName(invFileName);

        if(inv != null)
        {
            ps.of_setInvId(inv.of_getObjectId());
            ps.of_getPlayer().openInventory(inv.of_getInv(ps));
        }
    }

    /**
     * This function closes the inventory of the player.
     * @param ps Player instance.
     */
    public void of_closeInventory(Spieler ps)
    {
        ps.of_setInvId(-1);
        ps.of_getPlayer().closeInventory();
    }

    /* ************************************* */
    /* OBJECT METHODS */
    /* ************************************* */

    /**
     * This function is used to add or remove player atm/cash money.
     * In case that money will be removed from the player and the player does not have enough money,
     * the function will return false.
     * @param ps Player instance.
     * @param moneyType The money-type for example: 'ATM' or 'CASH'.
     * @param removeAdd The operation for example: 'REMOVE' or 'ADD'.
     * @param moneyAmount The amount of money which will be added or removed.
     * @return TRUE if the operation was successful, FALSE if not.
     */
    public boolean of_editPlayerMoney(Spieler ps, String moneyType, String removeAdd, double moneyAmount)
    {
        //  Check if money-amount is given.
        if(moneyAmount > 0)
        {
            boolean lb_continue = true;
            moneyType = moneyType.toLowerCase();
            removeAdd = removeAdd.toLowerCase();

            switch (moneyType)
            {
                case "atm":
                    //  Money-type need to be set to send a message to the player with the money-type.
                    ps.of_setMoneyType(1);

                    if(removeAdd.equals("remove"))
                    {
                        if(ps.of_getMoneyATM() >= moneyAmount)
                        {
                            if(main.SETTINGS.of_isUsingVaultMoneySystem())
                            {
                                main.VAULT.ECONOMY.withdrawPlayer(ps.of_getPlayer(), moneyAmount);
                            }
                            else
                            {
                                ps.of_setMoneyATM(ps.of_getMoneyATM() - moneyAmount);
                            }
                        }
                        //  Set the money difference to the player.
                        else
                        {
                            ps.of_setMoneyDiff(moneyAmount - ps.of_getMoneyATM());
                            lb_continue = false;
                        }
                    }
                    else if(removeAdd.equals("add"))
                    {
                        if(main.SETTINGS.of_isUsingVaultMoneySystem())
                        {
                            main.VAULT.ECONOMY.depositPlayer(ps.of_getPlayer(), moneyAmount);
                        }
                        else
                        {
                            ps.of_setMoneyATM(ps.of_getMoneyATM() + moneyAmount);
                        }
                    }
                case "cash":
                    //  Money-type need to be set to send a message to the player with the money-type.
                    ps.of_setMoneyType(0);

                    if(removeAdd.equals("remove"))
                    {
                        if(ps.of_getMoneyCash() >= moneyAmount)
                        {
                            ps.of_setMoneyCash(ps.of_getMoneyCash() - moneyAmount);
                        }
                        //  Set the money difference to the player.
                        else
                        {
                            ps.of_setMoneyDiff(moneyAmount - ps.of_getMoneyCash());
                            lb_continue = false;
                        }
                    }
                    else if(removeAdd.equals("add"))
                    {
                        ps.of_setMoneyCash(ps.of_getMoneyCash() + moneyAmount);
                    }
            }

            return lb_continue;
        }

        return false;
    }
}