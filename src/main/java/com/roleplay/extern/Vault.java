package com.roleplay.extern;

import com.basis.ancestor.Objekt;
import com.basis.main.main;
import com.basis.utils.Settings;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * @Created 18.10.2021
 * @Author Nihar
 * @Description
 * This class is used to represent vault-objects.
 * In this case the PERMISSIONS-/ECONOMY-Object is used from the plugin vault.
 */

public class Vault extends Objekt
{
    //  Attributes:
    public Permission PERMISSIONS;
    public Economy ECONOMY;

    /* ************************************* */
    /* LOADER */
    /* ************************************* */

    /**
     * This function is used to register all required components for this plugin.
     * @return 1 if everything is registered, 0 if not.
     */
    @Override
    public int of_load()
    {
        try
        {
            //  Register the vault-permissions class to this plugin.
            RegisteredServiceProvider<Permission> registerClassPermission = main.PLUGIN.getServer().getServicesManager().getRegistration(Permission.class);
            assert registerClassPermission != null;
            PERMISSIONS = registerClassPermission.getProvider();

            //  Register the vault-economy class to this plugin.
            if(Settings.of_getInstance().of_isUsingVaultMoneySystem())
            {
                RegisteredServiceProvider<Economy> registerClassEconomy = main.PLUGIN.getServer().getServicesManager().getRegistration(Economy.class);
                assert registerClassEconomy != null;
                ECONOMY = registerClassEconomy.getProvider();
            }

            return 1;
        }
        catch (Exception e)
        {
            String extendedMessage = "";

            //  If the economy system is used this can cause the error. So we add an extended message to the default message
            //  to make sure that the server owner is using an economy system.
            if(Settings.of_getInstance().of_isUsingVaultMoneySystem())
            {
                extendedMessage = "Please check if your server is using an economy plugin.";
            }

            of_sendErrorMessage(e, "of_load();", "Error while registering the vault-service to this plugin. " + extendedMessage);
        }

        return -1;
    }
}
