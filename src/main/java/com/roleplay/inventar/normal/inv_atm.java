package com.roleplay.inventar.normal;

import com.basis.main.main;
import com.basis.sys.Sys;
import com.roleplay.inventar.Inventar;
import org.bukkit.Bukkit;
import org.bukkit.Material;

/**
 * @Created 25.05.2022
 * @Author Nihar
 * @Description
 * This inventory is used to create the atm-inventory for the player.
 */
public class inv_atm extends Inventar
{
    /* ************************************* */
    /* LOADER */
    /* ************************************* */

    /**
     * Will be called by the inventory-service if the
     * file does not exist.
     * @return 1 = OK, -1 = ERROR
     */
    @Override
    public int of_load()
    {
        //  Set default attributes for this inventory...
        of_setInventarName("&8[&4&lATM&8]");
        of_setCopyInv(true);
        of_setInvClassification("MONEY_TRANSFER");
        of_setCloseOnClickEnabled(false);

        inv = Bukkit.createInventory(null, 36, of_getInventarName());

        //  Define items for the inventory...
        //  PayIn...
        String depositText = "&7Deposit selected amount.";
        String defaultDisplayName = "&a+%price%€";

        inv.setItem(0,  main.INVENTARSERVICE.of_createItemStack(Material.EMERALD, defaultDisplayName, new String[] {depositText}, 1));
        inv.setItem(1,  main.INVENTARSERVICE.of_createItemStack(Material.EMERALD, defaultDisplayName, new String[] {depositText}, 1));
        inv.setItem(2,  main.INVENTARSERVICE.of_createItemStack(Material.EMERALD, defaultDisplayName, new String[] {depositText}, 1));
        inv.setItem(3,  main.INVENTARSERVICE.of_createItemStack(Material.EMERALD, defaultDisplayName, new String[] {depositText}, 1));
        inv.setItem(9,  main.INVENTARSERVICE.of_createItemStack(Material.EMERALD, defaultDisplayName, new String[] {depositText}, 1));
        inv.setItem(10, main.INVENTARSERVICE.of_createItemStack(Material.EMERALD, defaultDisplayName, new String[] {depositText}, 1));
        inv.setItem(11, main.INVENTARSERVICE.of_createItemStack(Material.EMERALD, defaultDisplayName, new String[] {depositText}, 1));

        //  Payout...
        String payOffText = "&7Pay out selected amount.";
        defaultDisplayName = "&c-%price%€";
        inv.setItem(5,  main.INVENTARSERVICE.of_createItemStack(Material.EMERALD, defaultDisplayName, new String[] {payOffText}, 1));
        inv.setItem(6,  main.INVENTARSERVICE.of_createItemStack(Material.EMERALD, defaultDisplayName, new String[] {payOffText}, 1));
        inv.setItem(7,  main.INVENTARSERVICE.of_createItemStack(Material.EMERALD, defaultDisplayName, new String[] {payOffText}, 1));
        inv.setItem(8,  main.INVENTARSERVICE.of_createItemStack(Material.EMERALD, defaultDisplayName, new String[] {payOffText}, 1));
        inv.setItem(14, main.INVENTARSERVICE.of_createItemStack(Material.EMERALD, defaultDisplayName, new String[] {payOffText}, 1));
        inv.setItem(15, main.INVENTARSERVICE.of_createItemStack(Material.EMERALD, defaultDisplayName, new String[] {payOffText}, 1));
        inv.setItem(16, main.INVENTARSERVICE.of_createItemStack(Material.EMERALD, defaultDisplayName, new String[] {payOffText}, 1));

        //  Other Itemstacks...
        inv.setItem(27, main.INVENTARSERVICE.of_createItemStack(Material.LEAD, "&aDeposit your cash.", new String[] {"&7Deposit your whole cash amount.", "&e%moneyCash%€"}, 1));
        inv.setItem(35, main.INVENTARSERVICE.of_createItemStack(Material.BARRIER, "&8[&cClose&8]", new String[] {"&7Close inventory."}, 1));
        return 1;
    }

    /* ************************************* */
    /* OBJECT METHODS */
    /* ************************************* */

    @Override
    public void of_defineCommands4Inventory()
    {
        String[] payInCMDSet = new String[] {"TAKE=MONEY_CASH=%price%", "=IF EXECUTED THEN=", "MSGID=Roleplay.Money.MoneyTransferCompleted", "GIVE=MONEY_ATM=%price%", "=ELSE=", "MSGID=Roleplay.Money.MoneyTransferNotEnoughMoney"};
        String[] payOutCMDSet = new String[] {"TAKE=MONEY_ATM=%price%", "=IF EXECUTED THEN=", "MSGID=Roleplay.Money.MoneyTransferCompleted", "GIVE=MONEY_CASH=%price%", "=ELSE=", "MSGID=Roleplay.Money.MoneyTransferNotEnoughMoney"};

        //  PayIn...
        of_addCommands2ItemName(0,  payInCMDSet);
        of_addCommands2ItemName(1,  payInCMDSet);
        of_addCommands2ItemName(2,  payInCMDSet);
        of_addCommands2ItemName(3,  payInCMDSet);
        of_addCommands2ItemName(9,  payInCMDSet);
        of_addCommands2ItemName(10, payInCMDSet);
        of_addCommands2ItemName(11, payInCMDSet);

        //  PayOut...
        of_addCommands2ItemName(5,  payOutCMDSet);
        of_addCommands2ItemName(6,  payOutCMDSet);
        of_addCommands2ItemName(7,  payOutCMDSet);
        of_addCommands2ItemName(8,  payOutCMDSet);
        of_addCommands2ItemName(14, payOutCMDSet);
        of_addCommands2ItemName(15, payOutCMDSet);
        of_addCommands2ItemName(16, payOutCMDSet);

        //  Other...
        of_addCommands2ItemName(27, Sys.of_getReplacedArrayString(payInCMDSet, "%price%", "%moneyCash%"));
        of_addCommands2ItemName(35, new String[] {"CLOSE"});
    }
}
