package com.roleplay.objects;

import com.basis.ancestor.Objekt;
import com.basis.main.main;
import com.basis.sys.Sys;
import com.basis.utils.Datei;
import com.roleplay.spieler.Spieler;
import org.bukkit.entity.Player;
import java.util.ArrayList;

/**
 * @Created 16.04.2022
 * @Author Nihar
 * @Description
 * This class/object is used to send a text block (message) to the player.
 */
public class TextBlock extends Objekt
{
    private ArrayList<String> predefinedMessages = null;
    private ArrayList<String> interactiveChatMessages = null;
    private String[] cmds;
    private String[] placeholders;
    private Datei datei;
    private Spieler ps;

    /* ************************************* */
    /* CONSTRUCTOR */
    /* ************************************* */

    public TextBlock(String fileName, Spieler ps)
    {
        this.datei = new Datei(Sys.of_getMainFilePath() + "//TextBlocks//" + fileName);
        this.ps = ps;
    }

    public TextBlock(String fileName)
    {
        this.datei = new Datei(Sys.of_getMainFilePath() + "//TextBlocks//" + fileName);
    }

    /* ************************************* */
    /* OBJECT METHODS */
    /* ************************************* */

    /**
     * This function sends the predefined messages to the player.
     */
    public void of_sendMessage2Player()
    {
        if(ps != null)
        {
            boolean lb_check4InteractiveChat = of_loadInteractiveChatMessages();
            String[] chatMessages = datei.of_getStringArrayByKey("TextBlock");
            cmds = datei.of_getStringArrayByKey("CommandSet");

            //  Check if a CommandSet is defined and execute it before sending the messages.
            if(cmds != null && cmds.length > 0)
            {
                CommandSet cmdSet = new CommandSet(cmds, ps);
                cmdSet.of_executeAllCommands();

                //  If a kick statement is defined, don't send the messages.
                if(cmdSet.of_commandExists("KICK"))
                {
                    return;
                }
            }

            if(chatMessages != null)
            {
                Player p = ps.of_getPlayer();

                for (String chatMessage : chatMessages)
                {
                    //  Check own replacements...
                    chatMessage = of_translateMessageWithOwnPlaceholder(chatMessage);

                    //  Check for placeholder for the interactiveChat.
                    if(chatMessage.contains("%") && lb_check4InteractiveChat)
                    {
                        boolean lb_found = false;

                        for(String interactiveChatMessage : interactiveChatMessages)
                        {
                            String[] interactiveChatMessageSplit = interactiveChatMessage.split("\\|");

                            //  Check if the message contains a interactiveChatMessage pattern.
                            if(chatMessage.contains(interactiveChatMessageSplit[0]))
                            {
                                main.SPIELERSERVICE.of_sendInteractiveMessage(ps, interactiveChatMessageSplit[1], interactiveChatMessageSplit[2], interactiveChatMessageSplit[3]);
                                lb_found = true;
                                break;
                            }
                        }

                        //  If the interactive Chat has been found and send, then continue with the next message.
                        //  When the message is no interactive Chat message it could be an alternative placeholder.
                        if(lb_found)
                        {
                            ps.of_setTextBlockAttribute(datei.of_getFileName());
                            continue;
                        }
                    }

                    //  Send the message.
                    chatMessage = main.MESSAGEBOARD.of_translateMessageWithPlayerStats(chatMessage, ps);
                    p.sendMessage(chatMessage);
                }
            }
            //  An error occurred.
            else
            {
                main.SPIELERSERVICE.of_sendErrorMessage(ps, "There was an error while loading the text-block file. File: " + datei.of_getFileName());
            }
        }
    }

    /**
     * This function loads the interactiveChatMessages from the file.
     * @return true if the interactiveChatMessages have been loaded.
     */
    private boolean of_loadInteractiveChatMessages()
    {
        String[] configKey = datei.of_getKeySectionsByKey("InteractiveMessage");

        if(configKey != null)
        {
            //  Initialize the interactive chat messages.
            interactiveChatMessages = new ArrayList<>();

            for(String key : configKey)
            {
                //  Check for placeholder for the interactiveChat.
                String text = datei.of_getString("InteractiveMessage." + key + ".Text");
                String hoverText = datei.of_getString("InteractiveMessage." + key + ".HoverText");
                String[] commandSet = datei.of_getStringArrayByKey("InteractiveMessage." + key + ".CommandSet");

                if(text != null && hoverText != null && commandSet != null && commandSet.length > 0)
                {
                    //  Translate all three strings.
                    text = main.MESSAGEBOARD.of_translateMessageWithPlayerStats(text, ps);
                    hoverText = main.MESSAGEBOARD.of_translateMessageWithPlayerStats(hoverText, ps);

                    //  Translate every command...
                    for(int i = 0; i < commandSet.length; i++)
                    {
                        commandSet[i] = main.MESSAGEBOARD.of_translateMessageWithPlayerStats(commandSet[i], ps);
                    }

                    //  Build the CommandSet into a String with a ';' as separator.
                    StringBuilder command = new StringBuilder(commandSet[0]);

                    for(int i = 1; i < commandSet.length; i++)
                    {
                        command.append(";").append(commandSet[i]);
                    }

                    //  Add the message to the interactive chat messages.
                    this.interactiveChatMessages.add(key + "|" + text + "|" + hoverText + "|interaction textblock " + datei.of_getFileName() + " " + command);
                }
            }

            //  If the interactive chat messages are not empty, return true.
            return !interactiveChatMessages.isEmpty();
        }

        return false;
    }

    @Override
    public int of_save(String invoker)
    {
        if(!datei.of_fileExists())
        {
            String errorMessage = of_validate();

            if(errorMessage != null)
            {
                of_sendErrorMessage(null, "TextBlock.of_save(String); Invoker: " + invoker, errorMessage);
                return -1;
            }

            // Save the predefined messages.
            datei.of_getSetStringArrayList("TextBlock", this.predefinedMessages);

            // Save the interactive chat messages.
            for (String interactiveChatMessage : this.interactiveChatMessages)
            {
                String[] iChatData = interactiveChatMessage.split("\\|");

                if(iChatData.length == 4)
                {
                    String configKey = "InteractiveMessage." + iChatData[0];
                    datei.of_set(configKey + ".Text", iChatData[1]);
                    datei.of_set(configKey + ".HoverText", iChatData[2]);
                    String[] commands = iChatData[3].split(";");

                    if(commands.length > 0)
                    {
                        datei.of_getSetStringArray(configKey + ".CommandSet", commands);
                    }
                }
            }

            //  Set the CommandSet if it is not empty.
            if(cmds != null && cmds.length > 0)
            {
                datei.of_getSetStringArray("CommandSet", cmds);
            }

            return datei.of_save("TextBlock.of_save(String); Invoker: " + invoker);
        }

        return 0;
    }

    @Override
    public String of_validate()
    {
        if(predefinedMessages == null || predefinedMessages.size() == 0)
        {
            return "TextBlock: Predefined messages are null/or empty.";
        }

        return null;
    }

    /**
     * This function translates the message with own specified replacements.
     * @param translateMessage The message to translate.
     * @return The translated message.
     */
    private String of_translateMessageWithOwnPlaceholder(String translateMessage)
    {
        // Check own placeholders...
        if(placeholders != null && placeholders.length > 0)
        {
            //  Check own placeholders...
            for (String placeholder : placeholders)
            {
                String[] placeholderSplit = placeholder.split("=");

                if(placeholderSplit.length == 2)
                {
                    return translateMessage.replace( placeholderSplit[0], placeholderSplit[1]);
                }
            }
        }

        return translateMessage;
    }

    /* ************************************* */
    /* ADDER // SETTER */
    /* ************************************* */

    /**
     * This method adds a message to the textblock. This is used to
     * create a predefined text block.
     * @param message The message to add.
     */
    public void of_addMessage2Block(String message)
    {
        // Initialize the array list if it is null
        of_initialize();

        //  Default translation...
        message = message.replace("§", "&");
        this.predefinedMessages.add(message);
    }

    /**
     * This function adds a placeholder with a specific id to the text block.
     * The specific id is used to predefine the interactive chat message.
     * @param displayMessage The message that will be displayed to the player.
     * @param hoverText The message that will be displayed when the player hovers over the message.
     * @param cmdSet CommandSet which should be executed when the player clicks on the message.
     */
    public void of_addInteractiveChatMessage2Block(String displayMessage, String hoverText, String[] cmdSet)
    {
        // Initialize the array list if it is null.
        of_initialize();

        // Add the message to the array list.
        // This format is used for the saving process.
        String iChatId = "InteractiveChatNo" + ( this.interactiveChatMessages.size() + 1 );
        of_addMessage2Block("%" + iChatId + "%" );

        //  Default translation...
        displayMessage = displayMessage.replace("§", "&");
        hoverText = hoverText.replace("§", "&");

        //  Build the CommandSet into a String with a ';' as separator.
        StringBuilder command = new StringBuilder();

        if(cmdSet != null && cmdSet.length > 0)
        {
            for (String cmd : cmdSet)
            {
                command.append(cmd).append(";");
            }
        }

        //  Add the interactive chat message to the array list.
        this.interactiveChatMessages.add(iChatId + "|" + displayMessage + "|" + hoverText + "|" + command);
    }

    /**
     * This function is used to add a specific placeholder with the replacementValue.
     * @param placeholder The placeholder to replace.
     * @param replacementValue The value that will be used to replace the placeholder.
     */
    public void of_addPlaceholder2TextBlock(String placeholder, String replacementValue)
    {
        placeholders = Sys.of_addArrayValue(placeholders, placeholder + "=" + replacementValue);
    }

    /**
     * This function is used to define a CommandSet for the text block.
     * After getting the messages the CommandSet will be executed.
     * @param commandSet The CommandSet to set.
     */
    public void of_addCommandSet2Block(String[] commandSet)
    {
        cmds = commandSet;
    }

    /* ************************************* */
    /* OTHERS */
    /* ************************************* */

    /**
     * This function initializes the both array lists if they are null.
     */
    private void of_initialize()
    {
        //  Initialize the array list if it is null
        if(this.interactiveChatMessages == null)
            this.interactiveChatMessages = new ArrayList<>();

        //  Initialize the array list if it is null
        if(this.predefinedMessages == null)
            this.predefinedMessages = new ArrayList<>();
    }

    /* ************************************* */
    /* GETTER */
    /* ************************************* */

    /**
     * Returns only translated messages with the player stats.
     * @return The translated messages.
     */
    public String[] of_getTranslatedTextBlockLines()
    {
        String[] chatMessages = datei.of_getStringArrayByKey("TextBlock");

        if(chatMessages != null && chatMessages.length > 0)
        {
            for(int i = 0; i < chatMessages.length; i++)
            {
                //  Check own placeholder...
                chatMessages[i] = of_translateMessageWithOwnPlaceholder(chatMessages[i]);

                if(ps == null)
                {
                    chatMessages[i] = main.MESSAGEBOARD.of_translateMessage(chatMessages[i]);
                }
                //  If the player is given, translate the stats :)
                else
                {
                    chatMessages[i] = main.MESSAGEBOARD.of_translateMessageWithPlayerStats(chatMessages[i], ps);
                }
            }

            return chatMessages;
        }

        return null;
    }
}
