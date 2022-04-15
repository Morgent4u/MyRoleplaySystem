package com.roleplay.spieler;

import com.basis.ancestor.Objekt;
import com.basis.main.main;
import com.roleplay.spieler.Spieler;
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
            Collection<Spieler> players = _CONTEXT.of_getAllSpieler();

            for(Spieler ps : players)
            {
                _CONTEXT.of_savePlayer(ps);
            }
        }
    }

    /* ************************************* */
    /* OBJEKT-ANWEISUNGEN */
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
    public boolean of_editPlayerMoney(Spieler ps, String moneyType, String removeAdd, int moneyAmount)
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
                            ps.of_setMoneyATM(ps.of_getMoneyATM() - moneyAmount);
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
                        ps.of_setMoneyATM(ps.of_getMoneyATM() + moneyAmount);
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
     * This function closes the inventory of the player.
     * @param ps Player instance.
     */
    public void of_closeInventory(Spieler ps)
    {
        ps.of_setInvId(-1);
        ps.of_getPlayer().closeInventory();
    }

}