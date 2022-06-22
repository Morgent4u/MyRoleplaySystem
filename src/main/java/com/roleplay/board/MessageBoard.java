package com.roleplay.board;

import com.basis.ancestor.Objekt;
import com.basis.main.main;
import com.basis.sys.Sys;
import com.basis.utils.Datei;
import com.roleplay.objects.TextBlock;
import com.roleplay.position.Position;
import com.roleplay.spieler.Spieler;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
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
        of_addMessageOrSound2Board("Roleplay.Money.MoneyDepositFromBank", "&fYou have paid &a%money%€&f to your bank account!");
        of_addMessageOrSound2Board("Roleplay.Money.MoneyWithdrawFromBank", "&fYou have taken &a%money%€&f from your bank account!");
        of_addMessageOrSound2Board("Roleplay.Money.MoneyTransferNotEnoughMoney", "&fYou do not have enough money for this interaction! &f(&e%money%€&f)");

        //  Add Sounds:
        of_addMessageOrSound2Board("Sound.NoPermissions", "block.sand.fall");

        //  Load TextBlocks:
        of_loadTextBlocks();

        return datei.of_save("MessageBoard.of_load();");
    }

    /**
     * This function is used to load predefined TextBlocks.
     */
    private void of_loadTextBlocks()
    {
        TextBlock textBlock = new TextBlock("txt_dataprotection");
        textBlock.of_addMessage2Block("§7═════════════════════════");
        textBlock.of_addMessage2Block("");
        textBlock.of_addMessage2Block("§8[§4§lData Protection§8]");
        textBlock.of_addMessage2Block("");
        textBlock.of_addMessage2Block("§fHello §d%p%§f,");
        textBlock.of_addMessage2Block("§fwelcome to our §6server§f!");
        textBlock.of_addMessage2Block("");
        textBlock.of_addMessage2Block("§fTo secure our server, you need to accept our §edata protection§f.");
        textBlock.of_addMessage2Block("§fWe only need to store the following data:");
        textBlock.of_addMessage2Block("§7-§9 Minecraft name");
        textBlock.of_addMessage2Block("§7-§9 IP-Address");
        textBlock.of_addMessage2Block("§7-§9 UUID");
        textBlock.of_addMessage2Block("§7-§9 Last logout");
        textBlock.of_addMessage2Block("");
        textBlock.of_addInteractiveChatMessage2Block("§a§nAccept our data protection.", "§fClick to §aaccept§f the data protection.", new String[] {"CMD=dataprotection accept"});
        textBlock.of_addMessage2Block("");
        textBlock.of_addInteractiveChatMessage2Block("§c§nDecline our data protection.", "§fClick to §cdecline§f the data protection.", new String[] {"CMD=dataprotection"});
        textBlock.of_addMessage2Block("");
        textBlock.of_addMessage2Block("§7═════════════════════════");
        textBlock.of_save("MessageBoard.of_loadTextBlocks();");

        // DataProtection: Accept
        textBlock = new TextBlock("txt_dataprotection_accepted");
        textBlock.of_addMessage2Block("§7═════════════════════════");
        textBlock.of_addMessage2Block("");
        textBlock.of_addMessage2Block("§8[§4§lData Protection§8]");
        textBlock.of_addMessage2Block("");
        textBlock.of_addMessage2Block("§d%p%§f, you have accepted the");
        textBlock.of_addMessage2Block("§edata protection§f.");
        textBlock.of_addMessage2Block("§fAccept date: §a%dataProtectionDate%");
        textBlock.of_addMessage2Block("");
        textBlock.of_addMessage2Block("§fWe hope you enjoy our server.");
        textBlock.of_addMessage2Block("§fIf you have any questions, feel free to");
        textBlock.of_addMessage2Block("§fask us in our discord server.");
        textBlock.of_addMessage2Block("");
        textBlock.of_addMessage2Block("§5Discord:§a https://discord.gg/QWQWQWQ");
        textBlock.of_addMessage2Block("");
        textBlock.of_addMessage2Block("§7═════════════════════════");
        textBlock.of_save("MessageBoard.of_loadTextBlocks();");

        //  Double IP-Address:
        textBlock = new TextBlock("txt_double_ipaddress");
        textBlock.of_addMessage2Block("§7═════════════════════════");
        textBlock.of_addMessage2Block("");
        textBlock.of_addMessage2Block("§8[§4§lDouble IP-Address§8]");
        textBlock.of_addMessage2Block("");
        textBlock.of_addMessage2Block("§d%p%§f, our system has detect that");
        textBlock.of_addMessage2Block("§fyou are using the same §eIP-Address");
        textBlock.of_addMessage2Block("§fas §c%otherPlayer%" + "§f.");
        textBlock.of_addMessage2Block("");
        textBlock.of_addMessage2Block("§fTo get a §awhitelist§f please contact");
        textBlock.of_addMessage2Block("§four staff members.");
        textBlock.of_addMessage2Block("");
        textBlock.of_addMessage2Block("§5Discord:§a https://discord.gg/QWQWQWQ");
        textBlock.of_addMessage2Block("");
        textBlock.of_addMessage2Block("§7═════════════════════════");
        textBlock.of_addCommandSet2Block(new String[] {"KICK=txt_double_ipaddress"});
        textBlock.of_save("MessageBoard.of_loadTextBlocks();");

        //  Death-Message:
        textBlock = new TextBlock("txt_death_message");
        textBlock.of_addMessage2Block("§7═════════════════════════");
        textBlock.of_addMessage2Block("");
        textBlock.of_addMessage2Block("§8[§4§lYou died!§8]");
        textBlock.of_addMessage2Block("");
        textBlock.of_addMessage2Block("§d%p%§f, you have died!");
        textBlock.of_addMessage2Block("§fYou have been teleported to:");
        textBlock.of_addMessage2Block("§c%pos%");
        textBlock.of_addMessage2Block("");
        textBlock.of_addMessage2Block("§eEnjoy your life! :)");
        textBlock.of_addMessage2Block("");
        textBlock.of_addMessage2Block("§fIf your death was caused by a");
        textBlock.of_addMessage2Block("§fbug, please report it to us.");
        textBlock.of_addMessage2Block("");
        textBlock.of_addMessage2Block("§7═════════════════════════");
        textBlock.of_save("MessageBoard.of_loadTextBlocks();");
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
        message = message.replace("%displayName%", ps.of_getPlayer().getDisplayName());
        message = message.replace("%uuid%", ps.of_getUUID());
        message = message.replace("%moneyATM%", String.valueOf(ps.of_getMoneyATM()));
        message = message.replace("%moneyCash%", String.valueOf(ps.of_getMoneyCash()));
        message = message.replace("%money%", String.valueOf(ps.of_getMoneyDiff()));
        message = message.replace("%jobId%", String.valueOf(ps.of_getJobId()));
        message = message.replace("%rangId%", String.valueOf(ps.of_getRangId()));
        message = message.replace("%id%", String.valueOf(ps.of_getObjectId()));

        //  Check for the DataProtectionDate...
        if(message.contains("%dataProtectionDate%"))
        {
            String dataProtectionDate = main.SPIELERSERVICE.of_getPlayerInternListData(ps, "DataProtection");

            if(dataProtectionDate != null)
            {
                message = message.replace("%dataProtectionDate%", dataProtectionDate);
            }
        }

        //  Check for other player...
        if(ps.of_getDbIdOtherPlayer() != 0)
        {
            Spieler ds = main.SPIELERSERVICE._CONTEXT.of_getPlayerById(ps.of_getDbIdOtherPlayer());

            if(ds != null)
            {
                message = message.replace("%otherPlayer%", ds.of_getPlayer().getName());
            }
        }

        //  Check for the last Position-Object:
        if(ps.of_getPositionId() > 0)
        {
            Position position = main.POSITIONSERVICE._CONTEXT.of_getPositionByObjectId(ps.of_getObjectId());

            if(position != null)
            {
                message = message.replace("%pos%", position.of_getPositionName());
                message = message.replace("%position%", position.of_getPositionName());
            }
        }

        //  Use the PlaceholderAPI if it's enabled...
        if(main.SETTINGS.of_isUsingPlaceholderAPI())
        {
            message = PlaceholderAPI.setPlaceholders(ps.of_getPlayer(), message);
        }

        return message;
    }

    /**
     * This function is used to play a sound to the player.
     * @param soundKey The sound key.
     * @param ps The player instance.
     */
    public void of_playSoundByKey(String soundKey, Spieler ps)
    {
        Player p = ps.of_getPlayer();
        String sound = messages.get(soundKey);

        //  If the sound could be found in the messagesSounds.yml use the defined minecraft-sound.
        if(sound != null)
        {
            sound = sound.toLowerCase();
        }
        //  Otherwise, the sound could be from an CommandSet, so we handle it as a minecraft-sound.
        else
        {
            sound = soundKey.toLowerCase();
        }

        if(!sound.isEmpty())
        {
            p.playSound(p.getLocation(), sound, 1, 1);
        }
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
        String message  = of_translateMessageWithPlayerStats(of_getMessage(messageKey), ps);

        //  Check if for this message is a sound available.
        String[] messageData = messageKey.split("\\.", 1);

        //  If the parameters are correct...
        if(messageData.length >= 2)
        {
            of_playSoundByKey("Sound." + messageData[1], ps);
        }

        return message;
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

    public boolean of_check4MessageId(String msgId)
    {
        return messages.containsKey(msgId);
    }
}
