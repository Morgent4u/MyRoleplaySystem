package com.roleplay.plot;

import com.basis.ancestor.Objekt;

/**
 * @Created 24.07.2022
 * @Author Nihar
 * @Description
 * The PlotService is used to interact
 * with the plot-object and the
 * PlotContext-object.
 */
public class PlotService extends Objekt
{
    //  Attributes:
    public PlotContext _CONTEXT;

    /* ************************************* */
    /* LOADER */
    /* ************************************* */

    @Override
    public int of_load()
    {
        _CONTEXT = new PlotContext("PlotContext");
        _CONTEXT.of_load();
        return 1;
    }
}
