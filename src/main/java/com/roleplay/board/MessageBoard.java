package com.roleplay.board;

import com.basis.ancestor.Objekt;
import com.basis.main.main;
import com.basis.sys.Sys;
import com.basis.utils.Datei;
import com.roleplay.spieler.Spieler;
import me.clip.placeholderapi.PlaceholderAPI;

import java.util.HashMap;
import java.util.Map;

/**
 * @Created 15.04.2022
 * @Author Nihar
 * @Description
 * This object/class is used to create predefined messages which can be
 * edited in a config-file.
 */
public class MessageBoard extends Objekt
{
    //  Attributse:
    //  MessageKey - Message
    Map<String, String> messages = new HashMap<>();
    Datei datei;
    String prefix;

    boolean ib_usePrefix;

    /* ************************* */
    /* CONSTRUCTOR // LOADER */
    /* ************************* */

    /**
     * Constructor for the MessageBoard.
     */
    public MessageBoard()
    {
        datei = new Datei(Sys.of_getMainFilePath() + "//Others//messagesSounds.yml");
    }

    /**
     * Load all messages/sounds from the file: 'messagesSounds.yml'.
     * @return 1 if success, -1 if failed
     */
    @Override
    public int of_load()
    {
        prefix = datei.of_getSetString("Settings.Prefix", "&8[&aMyRoleplay&fSystem&8]&f:&7");
        prefix = prefix.replace("&", "§");
        ib_usePrefix = datei.of_getSetBoolean("Settings.AlwaysPrefix", true);

        //  Add Messages:
        //  General:
        of_addMessageOrSound2Board("General.ErrorMessage", "&fHey &a%p%&f an error occurred! Error: &c%errorMessage%");
        of_addMessageOrSound2Board("General.NoPermissions", "&cYou do not have permissions to do that!");
        of_addMessageOrSound2Board("General.PlayerIsNotOnline", "&fThe player &c%otherPlayer%&f, you are looking for is not online!");

        // Roleplay:
        // ....

        //  Add Sounds:
        of_addMessageOrSound2Board("Sound.NoPermissions", "block.sand.fall");

        return datei.of_save("MessageBoard.of_load();");
    }

    /* ************************* */
    /* OBJECT METHODS */
    /* ************************* */

    /**
     * Is used to replace the given message with color codes and the prefix.
     * @param message The message which should be replaced.
     * @return The replaced message.
     */
    public String of_translateMessage(String message)
    {
        //  Replacements...
        message = message.replace("&", "§");
        message = message.replace("%prefix%", prefix);

        //  If prefix is needed...
        if(of_isUsingAlwaysPrefix())
        {
            message = prefix + " " + message;
        }

        return message;
    }

    /**
     * This method is used to replace the given message with the player stats.
     * @param message The message which should be replaced.
     * @param ps The player instance.
     * @return The replaced message.
     */
    public String of_translateMessageWithPlayerStats(String message, Spieler ps)
    {
        //  Default translation... do not call of_translateMessage(String) here!
        message = message.replace("&", "§");
        message = message.replace("%prefix%", prefix);

        //  Player Stats:
        message = message.replace("%p%", ps.of_getPlayer().getName());
        message = message.replace("%uuid%", ps.of_getUUID());
        message = message.replace("%moneyATM%", String.valueOf(ps.of_getMoneyATM()));
        message = message.replace("%moneyCash%", String.valueOf(ps.of_getMoneyCash()));
        message = message.replace("%jobId%", String.valueOf(ps.of_getJobId()));
        message = message.replace("%rangId%", String.valueOf(ps.of_getRangId()));
        message = message.replace("%id%", String.valueOf(ps.of_getObjectId()));

        //  Use the PlaceholderAPI if it's enabled...
        if(main.SETTINGS.of_isUsingPlaceholderAPI())
        {
            message = PlaceholderAPI.setPlaceholders(ps.of_getPlayer(), message);
        }

        return message;
    }

    /* ************************* */
    /* DEBUG CENTER */
    /* ************************* */

    @Override
    public void of_sendDebugDetailInformation()
    {
        Sys.of_sendMessage("Loaded messages/sounds: " + messages.size());
    }

    /* **************************** */
    /* ADDER // SETTER // REMOVER */
    /* *************************** */

    /**
     * This function adds the given configKey and message to the file if it doesn't exist.
     * If it exists it will be loaded and add to the message board.
     * @param configKey The configKey of the message.
     * @param messageSound The message/sound which should be added.
     */
    private void of_addMessageOrSound2Board(String configKey, String messageSound)
    {
        //  Add or load the specific message/sound from the file. After this add it to the messages.
        messageSound = datei.of_getSetString(configKey, messageSound);
        messages.put(configKey, messageSound);
    }

    /* ************************* */
    /* GETTER */
    /* ************************* */

    /**
     * This function gets a message by the messageKey and replaces the message placeholders with
     * the player stats.
     * @param messageKey The messageKey of the message.
     * @param ps The player instance.
     * @return The message.
     */
    public String of_getMessageWithPlayerStats(String messageKey, Spieler ps)
    {
        return of_translateMessageWithPlayerStats(of_getMessage(messageKey), ps);
    }

    /**
     * This function returns the message which is defined for the given messageKey.
     * @param messageKey The messageKey of the message.
     * @return The message.
     */
    public String of_getMessage(String messageKey)
    {
        String message = messages.get(messageKey);

        if(message == null)
        {
            message = "This message is not defined in the config-file! MessageKey: " + messageKey;
        }

        return of_translateMessage(message);
    }

    /* ************************* */
    /* BOOLS */
    /* ************************* */

    public boolean of_isUsingAlwaysPrefix()
    {
        return ib_usePrefix;
    }
}
