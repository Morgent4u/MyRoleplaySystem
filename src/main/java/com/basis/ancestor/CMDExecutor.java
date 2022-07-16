package com.basis.ancestor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public abstract class CMDExecutor implements CommandExecutor, TabCompleter
{
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args)
    {
        return false;
    }

    /* ************************* */
    /* SEND CMD-HELPER */
    /* ************************* */

    /**
     * This method is used to send the command help-text to the player.
     * @param p Player instance.
     */
    public abstract void of_sendCMDHelperText(Player p);

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        return null;
    }
}
