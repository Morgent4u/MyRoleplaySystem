package com.roleplay.objects;

import com.basis.ancestor.Objekt;
import com.basis.main.main;
import com.basis.sys.Sys;
import com.roleplay.spieler.Spieler;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;

/**
 * @Created 15.04.2022
 * @Author Nihar
 * @Description
 * This class is used to execute defined commands.
 * The commands which should be executed are defined in the config.
 * For using a CommandSet you need to use the specific patterns.
 */
public class CommandSet extends Objekt
{
    //  Attributes:
    String[] commands;
    Spieler ps;

    int executed = 0;

    /* ************************************* */
    /* CONSTRUCTOR */
    /* ************************************* */

    /**
     * Constructor for the CommandSet
     * @param commands The commands which should be executed by the given player.
     * @param ps The player which should execute the commands or interactions.
     */
    public CommandSet(String[] commands, Spieler ps)
    {
        this.commands = commands;
        this.ps = ps;
    }

    /* ************************************* */
    /* OBJECT METHODS */
    /* ************************************* */

    /**
     * This function is used to execute all given Commands which are defined in this CommandSet.
     */
    public void of_executeAllCommands()
    {
        //  First validate this CommandSet:
        String errorMessage = of_validate();

        if(errorMessage != null)
        {
            of_sendErrorMessage(null, "CommandSet.of_executeAllCommands();", errorMessage);
            return;
        }

        //  Get the current-size stored in a variable, it will be used later...
        int size = commands.length;

        //  Iterate through all commands:
        for(int i = 0; i < size; i++)
        {
            String command = commands[i];

            if(command.equals("=IF EXECUTED THEN="))
            {
                //  Search for the ELSE-Statement...
                int index = ArrayUtils.indexOf( commands, "=ELSE=");

                //  Validate the index-value...
                if(index != -1)
                {
                    if(size <= index)
                    {
                        index = -1;
                    }
                }

                //  If no ELSE-Statement could be found, we stop here.
                if(index == -1 && executed == -1)
                {
                    return;
                }

                //  If the previous command was executed,
                //  we only execute the commands between the IF- and ELSE-Statement.
                if(executed == 1)
                {
                    if(index != -1)
                    {
                        size = index;
                    }
                }
                //  If the previous command was not executed,
                //  we only execute the commands after ELSE-Statement.
                else
                {
                    i = index;
                }

                //  Continue will increase the i-value by 1.
                continue;
            }

            //  Get the commandData from the current command.
            String[] commandData = command.split("=", 2);

            //  This need to be an empty string.
            String cmd = "";

            //  Check if additional data is given...
            if(commandData.length == 2)
            {
                cmd = commandData[1];
            }

            //  Execute the command with the given data.
            executed = of_executeCommand(commandData[0].toUpperCase(), cmd);

            //  If an error occurred, stop executing the commands.
            if(executed == -2)
            {
                of_sendErrorMessage(null, "CommandSet.of_executeAllCommands();", "Command: '"+command+"' failed!");
                return;
            }
        }
    }

    /**
     * This function is used to execute the given command.
     * @param category The category of the command (CMD, SCMD, CLOSE e.g.)
     * @param command Specific parameters which are used for the command.
     * @return 1 if the command was executed successfully, -1 if not, -2 if the command is not defined.
     */
    private int of_executeCommand(String category, String command)
    {
        //  Replace all player placeholder if command is given!
        if(!command.isEmpty())
        {
            command = main.MESSAGEBOARD.of_translateMessageWithPlayerStats(command, ps);
        }

        switch (category)
        {
            case "CMD":
                ps.of_getPlayer().performCommand(command);
                return 1;
            case "SCMD":
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                return 1;
            case "CLOSE":
                main.SPIELERSERVICE.of_closeInventory(ps);
                return 1;
            case "OPEN":
                main.SPIELERSERVICE.of_openInvByName(ps, command);
                return 1;
            case "TEXTBLOCK":
                new TextBlock(command, ps).of_sendMessage2Player();
                return 1;
            case "CHATCLEAR":
                main.SPIELERSERVICE.of_clearChat(ps);
                return 1;
            case "DEBUG":
                of_sendDebugInformation("CommandSet.of_executeCommand(); DEBUG");
                return 1;
            case "MSGID":
                main.SPIELERSERVICE.of_sendMessageByMessageId(ps, command);
                return 1;
            case "GIVE":
                return of_executeCommand4MoneySystem(command, "add");
            case "TAKE":
                return of_executeCommand4MoneySystem(command, "remove");
        }

        return -2;
    }

    @Override
    public String of_validate()
    {
        if(commands.length == 0)
        {
            return "Commands is empty";
        }

        //  Check if the player instance is valid.
        if(ps == null)
        {
            return "Player is null";
        }

        return null;
    }

    /**
     * This function is used to handle the money-give and money-take commands.
     * @param command The command which should be executed.
     * @param removeAdd The command which should be executed.
     * @return 1 if the command was executed successfully, -1 if not, -2 if the command-format is not valid.
     */
    private int of_executeCommand4MoneySystem(String command, String removeAdd)
    {
        //  Get more command-information...
        String[] commandData = command.split("=");
        int rc = -2;

        if(commandData.length == 2)
        {
            try
            {
                //  Get the money-amount and the money-type.
                String moneyType = commandData[0].split("_")[1];
                double money = Double.parseDouble(commandData[1]);

                //  Check if the player has the needed money...
                rc = ( main.SPIELERSERVICE.of_editPlayerMoney(ps, moneyType, removeAdd, money) ) ? 1 : -1;
            }
            catch (Exception ignored) { /* If an error occurred the returnCode will be -2 */ }
        }

        if(rc == -2)
        {
            of_sendErrorMessage(null, "CommandSet.of_executeCommand();", "Money-Type or Money-Value is not defined!");
        }

        return rc;
    }

    /* ************************************* */
    /* DEBUG CENTER */
    /* ************************************* */

    @Override
    public void of_sendDebugDetailInformation()
    {
        Sys.of_sendMessage("Player available: "+ (ps != null));

        if(ps != null)
        {
            Sys.of_sendMessage("Player name: "+ps.of_getPlayer().getName());
        }

        if(commands != null)
        {
            Sys.of_sendMessage("Loaded commands: "+commands.length);

            for(int i = 0; i< commands.length; i++)
            {
                Sys.of_sendMessage(i+": "+commands[i]);
            }
        }
    }

    /* ************************************* */
    /* BOOLS */
    /* ************************************* */

    public boolean of_commandExists(String command)
    {
        command = command.toUpperCase();

        if(commands != null && commands.length > 0)
        {
            for(String cmd : commands)
            {
                if(cmd.toUpperCase().split("=")[0].contains(command))
                {
                    return true;
                }
            }
        }

        return false;
    }
}
