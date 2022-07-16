package com.roleplay.spieler;

import com.basis.ancestor.Objekt;
import com.basis.main.main;
import com.basis.sys.Sys;
import com.basis.utils.Settings;
import com.roleplay.board.MessageBoard;
import com.roleplay.board.ScoreBoard;
import com.roleplay.inventar.Inventar;
import com.roleplay.objects.TextBlock;
import com.roleplay.position.Position;
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

    /**
     * Constructor we need to create the SpielerContext here.
     */
    public SpielerService()
    {
        _CONTEXT = new SpielerContext();
    }

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
        if(Bukkit.getOnlinePlayers().size() > 0)
        {
            for(Player p : Bukkit.getOnlinePlayers())
            {
                _CONTEXT.of_loadPlayer(p);
            }
        }
        //  Close database connection.
        else if(Settings.of_getInstance().of_isUsingMySQL() && main.SQL != null && main.SQL.of_isConnected())
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
        String message = MessageBoard.of_getInstance().of_getMessageWithPlayerStats("General.ErrorMessage", ps);
        message = message.replace("%errorMessage%", errorMessage);
        message = message.replace("%errormessage%", errorMessage);

        ps.of_getPlayer().sendMessage(message);
    }

    /**
     * This function sends the message to the player which is defined when the player has no permissions.
     * @param ps Player instance.
     */
    public void of_sendNoPermissionsMessage(Spieler ps)
    {
        ps.of_getPlayer().sendMessage(MessageBoard.of_getInstance().of_getMessageWithPlayerStats("General.NoPermissions", ps));
    }

    /**
     * This function is used to send a message to the player without using predefined messages.
     * This should only be used if the messages are for the staff members.
     * @param ps Player instance.
     * @param message The message which will be displayed.
     */
    public void of_sendPluginMessage2Player(Spieler ps, String message)
    {
        ps.of_getPlayer().sendMessage(MessageBoard.of_getInstance().of_translateMessage(message));
    }

    /**
     * This function is used to send a message to the player by the given messageId.
     * If the messageId is not valid, the player does not receive a message.
     * @param ps Player instance.
     * @param messageId The messageId which will be displayed.
     */
    public void of_sendMessageByMessageId(Spieler ps, String messageId)
    {
        //  We only send the player a message, if the key/msgId exist.
        if(MessageBoard.of_getInstance().of_check4MessageId(messageId))
        {
            ps.of_getPlayer().sendMessage(MessageBoard.of_getInstance().of_getMessageWithPlayerStats(messageId, ps));
        }
    }

    /**
     * This method is used to send the player to the given position-name or position-id.
     * @param ps Player instance.
     * @param posNameOrId The position-name or position-id which will be used to get the position.
     * @return 1 if successful, -1 if not.
     */
    public int of_sendPlayer2PositionByNameOrId(Spieler ps, String posNameOrId)
    {
        if(Settings.of_getInstance().of_isUsingPosition())
        {
            Position pos = main.POSITIONSERVICE.of_getPositionByAnything(posNameOrId);

            if(pos != null)
            {
                ps.of_setPositionId(pos.of_getObjectId());
                ps.of_getPlayer().teleport(pos.of_getLocation());
                return 1;
            }
        }

        return -1;
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
     * This function is used to check if the player is using
     * a second account. If so, the player will be kicked.
     * @param ps Player instance.
     * @return 1 If kicked (has double ip-address), -1 if not.
     */
    public int of_playerHasDoubleIPAddress(Spieler ps)
    {
        String currentUUID = ps.of_getUUID();
        String otherUUID = main.SQL.of_getRowValue_suppress("SELECT uuid FROM mrs_v_user WHERE ipAddress = '"+ps.of_getPlayerIPAsString()+"';", "uuid");

        if(currentUUID != null && otherUUID != null)
        {
            //  If the UUIDs does not match, we kick the player!
            if(!currentUUID.equals(otherUUID))
            {
                //  Kick the player by the defined text-block-object!
                of_kickPlayerByUsingTextBlock(ps,  new TextBlock("txt_double_ipaddress", ps), "You have already used this IP address.");
                return 1;
            }
        }

        return -1;
    }

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
                            if(Settings.of_getInstance().of_isUsingVaultMoneySystem())
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
                        if(Settings.of_getInstance().of_isUsingVaultMoneySystem())
                        {
                            main.VAULT.ECONOMY.depositPlayer(ps.of_getPlayer(), moneyAmount);
                        }
                        else
                        {
                            ps.of_setMoneyATM(ps.of_getMoneyATM() + moneyAmount);
                        }
                    }
                    break;
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

            //  We need to store the money we have added into the moneyDiff-variable...
            if(lb_continue)
            {
                ps.of_setMoneyDiff(moneyAmount);
            }

            //  Refresh the players scoreboard...
            if(Settings.of_getInstance().of_isUsingScoreboard())
            {
                ScoreBoard.of_getInstance().of_sendScoreboard2Player(ps);
            }

            return lb_continue;
        }

        return false;
    }

    /**
     * This function clears the player's chat.
     * @param ps Player instance.
     */
    public void of_clearChat(Spieler ps)
    {
        for(int i = 0; i < 100; i++)
        {
            ps.of_getPlayer().sendMessage("");
        }
    }


    /**
     * This method is used to kick the given player by using a defined text-block.
     * If no text-block has been defined or an error occurs by the text-block it will use
     * the defined default-message as fall-back!
     * @param ps Player instance.
     * @param text TextBlock object instance.
     * @param defaultMessage4NoTextBlock Default fall-back message if the text-block is not valid!
     */
    public void of_kickPlayerByUsingTextBlock(Spieler ps, TextBlock text, String defaultMessage4NoTextBlock)
    {
        String[] messages = null;

        //  Check if the text-block is valid!
        if(text != null && !text.of_hasAnError())
        {
            messages = text.of_getTranslatedTextBlockLines();
        }

        //  Check if the messages are valid.
        if(messages == null || messages.length == 0)
        {
            messages = new String[] {defaultMessage4NoTextBlock};
        }

        //  Build the kick-message!
        StringBuilder kickMessage = new StringBuilder();

        for(String message : messages)
        {
            kickMessage.append(message).append("\n");
        }

        //  Kick the player...
        ps.of_getPlayer().kickPlayer(kickMessage.toString());
    }

    /**
     * This method is used to kick the player by any database-error.
     * The defined error-message will be used in the defined text-block
     * 'txt_kick_player_db_error'.
     * @param ps Player instance.
     * @param errorMessage The db-error message.
     */
    public void of_kickPlayerByDatabaseError(Spieler ps, String errorMessage)
    {
        TextBlock text = new TextBlock("txt_kick_player_db_error", ps);
        text.of_addPlaceholder2TextBlock("%errorMessage%", errorMessage);
        of_kickPlayerByUsingTextBlock(ps, text, "A general database error occurred. We are sorry that we kicked you!");
    }

    /* ************************************* */
    /* ADDER // SETTER // REMOVER */
    /* ************************************* */

    /**
     * This method is used to update user-data in the table mrs_user_data.
     * @param ps Player instance.
     * @param columnName Column-name in which the value should be updated.
     * @param entryValue The value which should be updated in the given column.
     */
    public void of_addEntry2UserData(Spieler ps, String columnName, Object entryValue)
    {
        String entryValueString = null;

        //  Check for the specified entry-value-type.
        if(entryValue instanceof Double || entryValue instanceof Integer || entryValue.toString().equals(main.SQL.of_getTimeStamp()))
        {
            entryValueString = entryValue.toString();
        }
        else
        {
            entryValueString = "'"+entryValue.toString()+"'";
        }

        main.SQL.of_run_update("UPDATE mrs_user_data SET " + columnName + " = " + entryValueString + " WHERE user = " + ps.of_getObjectId() + ";");
    }

    /* ************************************* */
    /* GETTER */
    /* ************************************* */

    public String of_getEntryFromUserData(Spieler ps, String columnName)
    {
        return main.SQL.of_getRowValue_suppress("SELECT " + columnName + " FROM mrs_user_data WHERE user = " + ps.of_getObjectId() + ";", columnName);
    }

    /* ************************************* */
    /* BOOLS */
    /* ************************************* */

    public boolean of_hasAlreadyAcceptedDataProtection(Spieler ps)
    {
        return of_getEntryFromUserData(ps, "dataProtection_yn").equals("Y");
    }
}