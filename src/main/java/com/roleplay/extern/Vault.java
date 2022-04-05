package com.roleplay.extern;

import com.basis.ancestor.Objekt;
import com.basis.main.main;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * @Created 18.10.2021
 * @Author Nihar
 * @Description
 * This class is used to represent vault-objects.
 * In this case the PERMISSIONS-Object is used from the plugin vault.
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
            if(main.SETTINGS.of_isUsingVaultMoneySystem())
            {
                RegisteredServiceProvider<Economy> registerClassEconomy = main.PLUGIN.getServer().getServicesManager().getRegistration(Economy.class);
                assert registerClassEconomy != null;
                ECONOMY = registerClassEconomy.getProvider();
            }

            return 1;
        }
        catch (Exception e)
        {
            of_sendErrorMessage(e, "of_load();", "Error while registering the vault-service to this plugin.");
        }

        return -1;
    }
}
