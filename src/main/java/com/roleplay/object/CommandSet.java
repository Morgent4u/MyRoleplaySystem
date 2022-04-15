package com.roleplay.object;

import com.basis.ancestor.Objekt;
import com.basis.main.main;
import com.basis.sys.Sys;
import com.roleplay.spieler.Spieler;
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
        String errorMessage = of_validate();

        if(errorMessage != null)
        {
            of_sendErrorMessage(null, "CommandSet.of_executeAllCommands();", errorMessage);
            return;
        }

        //  No validation error, continue...
        for(String command : commands)
        {
            String[] commandData = command.split("=");
            String cmd = null;

            //  Check if the command is given.
            if(commandData.length == 2)
            {
                cmd = commandData[1];
            }

            int rc = of_executeCommand(commandData[0].toUpperCase(), cmd);

            if(rc != 1)
            {
                of_sendErrorMessage(null, "CommandSet.of_executeAllCommands();", "Command: '"+command+"' failed!");
                return;
            }
        }

    }

    private int of_executeCommand(String category, String command)
    {
        command = main.MESSAGEBOARD.of_translateMessageWithPlayerStats(command, ps);

        switch (category)
        {
            case "CMD":
                ps.of_getPlayer().performCommand(command);
                return 1;
            case "SCMD":
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                return 1;
            case "DEBUG":
                of_sendDebugInformation("CommandSet.of_executeCommand(); DEBUG");
                return 1;
        }

        return -1;
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

    /* ************************************* */
    /* DEBUG CENTER */
    /* ************************************* */

    @Override
    public void of_sendDebugDetailInformation()
    {
        if(commands != null)
        {
            for(int i = 0; i< commands.length; i++)
            {
                Sys.of_sendMessage(i+": "+commands[i]);
            }
        }
    }
}
