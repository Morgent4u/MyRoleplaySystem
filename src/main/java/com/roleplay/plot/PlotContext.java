package com.roleplay.plot;

import com.basis.ancestor.ObjektContext;
import com.basis.extern.DataStore;
import com.basis.sys.Sys;
import org.bukkit.Location;
import java.util.Objects;

/**
 * @Created 24.07.2022
 * @Author Nihar
 * @Description
 * This PlotContext is used to store
 * or delete MRS-Plots from the database.
 * It's also used to make changes at a mrs-plot and update
 * these changes to the database.
 */
public class PlotContext extends ObjektContext
{
    //  Attributes:
    private DataStore dataStore = null;

    /* ************************************* */
    /* CONSTRUCTOR // LOADER */
    /* ************************************* */

    /**
     * Use a default-constructor to set
     * the ContextObject-Name.
     * @param instanceName The instance-name of this context.
     */
    public PlotContext(String instanceName)
    {
        super(instanceName);
    }

    @Override
    public int of_load()
    {
        String sqlSelect = "SELECT plot, user, label, wg_region, price, sellPrice, text, location FROM mrs_plot;";
        dataStore = new DataStore("DataStore4Plots", "mrs_plot", "plot", sqlSelect);

        if(dataStore.of_retrieve() == -1)
        {
            of_sendErrorMessage(null, "PlotContext.of_load();", "There was an error while retrieving data from the database.");
            return -1;
        }

        return 1;
    }

    /* ************************************* */
    /* OBJECT METHODS */
    /* ************************************* */

    /**
     * This method is used to store a new plot into the database.
     * @param plot The plot object which should be stored into the database.
     * @return 1 = OK, -1 = An error occurred., -2 = The object does not pass the validation!
     */
    public int of_createNewPlot(Plot plot)
    {
        if(plot != null)
        {
            //  Check the validation...
            if(plot.of_validate() == null)
            {
                int enumId = plot.of_getLabelEnum();
                int row = dataStore.of_addRow();

                if(row != -1)
                {
                    //  Generate a database-location entry.
                    Location loc = plot.of_getLocation();
                    String location = "WORLD=" + Objects.requireNonNull(loc.getWorld()).getName() + ";X=" + Sys.of_getRoundedDouble(loc.getX(), 2) + ";Y=" + Sys.of_getRoundedDouble(loc.getY(), 2) + ";Z="+Sys.of_getRoundedDouble(loc.getZ(), 2)+";YAW="+loc.getYaw();

                    //  Store the data into the DataStore and update it to the database.
                    dataStore.of_setItemString(row, "label", String.valueOf(plot.of_getLabelEnum()));
                    dataStore.of_setItemString(row, "text", plot.of_getInfo());
                    dataStore.of_setItemString(row, "wg_region", plot.of_getWorldGuardRegion());
                    dataStore.of_setItemString(row, "price", Double.toString(plot.of_getPrice()));
                    dataStore.of_setItemString(row, "location", location);

                    return ( dataStore.of_update(row) == 1 ) ? 1 : -1;
                }
            }
            //  Validation error.
            else
            {
                return -2;
            }
        }

        //  Invalid Plot-Object.
        return -1;
    }
}