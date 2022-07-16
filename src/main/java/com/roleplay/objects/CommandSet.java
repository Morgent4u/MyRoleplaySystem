package com.roleplay.objects;

import com.basis.ancestor.Objekt;
import com.basis.main.main;
import com.basis.sys.Sys;
import com.roleplay.board.MessageBoard;
import com.roleplay.board.PermissionBoard;
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
    String[] replacements;
    Spieler ps;

    int executed = 0;

    /* ************************************* */
    /* CONSTRUCTOR */
    /* ************************************* */

    /**
     * Constructor for the CommandSet
     * @param cmds The commands which should be executed by the given player.
     * @param ps The player which should execute the commands or interactions.
     */
    public CommandSet(String[] cmds, Spieler ps)
    {
        commands = new String[cmds.length];

        //  Need to be copied to avoid problems with the java-pointer.
        System.arraycopy(cmds, 0, commands, 0, cmds.length);
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

        //  Replace all commands with player stats...
        for(int i = 0; i < size; i++)
        {
            commands[i] = MessageBoard.of_getInstance().of_translateMessageWithPlayerStats(commands[i], ps);
        }

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

                //  We need to set the executed-indicator to 0 to avoid the underlying else-block.
                executed = 0;

                //  Continue will increase the i-value by 1.
                continue;
            }
            //  If the previous command was a permissions-check and failed, we do not continue!
            else if(executed == -3)
            {
                return;
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
     * @return 1 if the command was executed successfully, -1 if not, -2 if the command is not defined., -3 If the permissions-check failed.
     */
    private int of_executeCommand(String category, String command)
    {
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
            case "PLAYSOUND":
                MessageBoard.of_getInstance().of_playSoundByKey(command, ps);
                return 1;
            case "POS":
            case "POSITION":
                return main.SPIELERSERVICE.of_sendPlayer2PositionByNameOrId(ps, command);
            case "NOTHING":
                return 1;
            case "INPUT":
                return of_executeCommand4PlayerInput(command);
            case "GIVE":
                return of_executeCommand4MoneySystem(command, "add");
            case "TAKE":
                return of_executeCommand4MoneySystem(command, "remove");
            case "PERM":
                return PermissionBoard.of_getInstance().of_hasPermissionsByDefault(ps, command) ? 1 : -3;
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
     * This Method is used to replace all commands in an CommandSet
     * when some replacements have been defined.
     */
    public void of_replaceAllCommandsWithDefinedReplacements()
    {
        if(of_hasReplacementValues())
        {
            //  Iterate through all replacements...
            for(String replacement : replacements)
            {
                String[] replaceParameters = replacement.split("=");

                //  Iterate trough all commands...
                if(replaceParameters != null && replaceParameters.length == 2)
                {
                    for(int i = 0; i < commands.length; i++)
                    {
                        //  Replace the commands with the replacement-values.
                        commands[i] = commands[i].replace(replaceParameters[0], replaceParameters[1]);
                    }
                }
            }
        }
    }

    /* ************************************* */
    /* COMMAND-SET METHODS */
    /* ************************************* */

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
                ps.of_setMoneyDiff(0);
                rc = ( main.SPIELERSERVICE.of_editPlayerMoney(ps, moneyType, removeAdd, money) ) ? 1 : -1;
            }
            catch (Exception ignored) { /* If an error occurs the returnCode will be -2 */ }
        }

        if(rc == -2)
        {
            of_sendErrorMessage(null, "CommandSet.of_executeCommand();", "Money-Type or Money-Value is not defined!");
        }

        return rc;
    }

    /**
     * This method is used to get the player-input.
     * We store the player-input in the given placeholder.
     * For example: 'INPUT=%value1%'
     * So we store the player-chat-message into the variable %value1%
     * @param command The current command.
     * @return 1 = OK, -1 = An error occurs.
     */
    private int of_executeCommand4PlayerInput(String command)
    {
        //  Example:
        //  0       1       2       3       4
        //  test    test2   test3   INPUT   test5

        //  First we need to identify the placeholder.
        if(command.contains("%"))
        {
            int arrayIndex = 0;

            //  We need to identify the INPUT-Statement.
            for(String cmd : commands)
            {
                if(cmd.startsWith("INPUT="))
                {
                    //  We set the placeholder here, %PLACEHOLDER% for example.
                    commands[arrayIndex] = command;
                    break;
                }

                arrayIndex++;
            }

            //  Create an array with the rest of the commandSet-Statements.
            String[] commandSet = new String[0];
            int arraySize = commands.length - arrayIndex;

            //	ArrayCopy :)
            System.arraycopy(commands, arrayIndex, commandSet, 0, arraySize);

            //  Print some debug stuff...
            for(int i = 0; i < commandSet.length; i++)
            {
                Sys.of_debug("CMD-SET-DEBUG: " + i + " - " + commandSet[i]);
            }

            ps.of_setPowerObject(commandSet);
            ps.of_setWaiting4Input(true);
        }
        else
        {
            Sys.of_debug("There was an error while executing the following INPUT-Statement: " + command + "'. No placeholder found!");
        }

        return -1;
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
    /* ADDER */
    /* ************************************* */

    /**
     * This method is used to add to the current CommandSet
     * defined placeholder and replace values.
     * @param placeholder The placeholder.
     * @param replacement The replace-value for the placeholder.
     */
    public void of_addReplaceValue2CommandSet(String placeholder, String replacement)
    {
        if(placeholder != null && replacement != null)
        {
            replacements = Sys.of_addArrayValue(replacements, placeholder + "=" + replacement);
        }
    }


    /* ************************************* */
    /* GETTER */
    /* ************************************* */

    public String[] of_getCommandSets()
    {
        return commands;
    }

    /* ************************************* */
    /* BOOLS */
    /* ************************************* */

    /**
     * This function is used to iterate through all defined Commands and
     * find the CommandSet with the given CommandSet-Start-parameter.
     * For example: CMD or SCMD.
     * @param command The command which should be searched.
     * @return True if the command was found, false if not.
     */
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

    private boolean of_hasReplacementValues()
    {
        return ( replacements != null && replacements.length > 0);
    }

}
