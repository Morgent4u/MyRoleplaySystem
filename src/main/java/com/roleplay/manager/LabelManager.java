package com.roleplay.manager;

import com.basis.ancestor.Objekt;
import com.basis.extern.DataStore;
import org.apache.commons.lang3.ArrayUtils;

/**
 * @Created 22.07.2022
 * @Author Nihar
 * @Description
 * The LabelManager is used to
 * manage labels. You can get
 * labels by the labelEnum or
 * the labelId.
 */
public class LabelManager extends Objekt
{
    //  Attributes:
    private static final LabelManager instance = new LabelManager();
    private DataStore dataStoreLabel;
    private DataStore dataStoreLabelEnums;

    /* ************************************* */
    /* CONSTRUCTOR // LOADER */
    /* ************************************* */

    private LabelManager() { }

    @Override
    public int of_load()
    {
        dataStoreLabel = new DataStore("DataStore4Labels", "SELECT label, label_enum, text, flag, sort FROM mrs_label ORDER BY sort;");

        //  Create a DataStore for the labels.
        if(dataStoreLabel.of_retrieve() == -1)
        {
            of_sendErrorMessage(null, "LabelManager.of_load();", "There was an error while retrieving data from the DataStore: " + dataStoreLabel.of_getInfo());
            return -1;
        }

        //  Create a DataStore for the enumLabels.
        dataStoreLabelEnums = new DataStore("DataStore4LabelEnums", "SELECT label_enum, text, flag FROM mrs_label_enum;");

        if(dataStoreLabelEnums.of_retrieve() == -1)
        {
            of_sendErrorMessage(null, "LabelManager.of_load();", "There was an error while retrieving data from the DataStore: " + dataStoreLabelEnums.of_getInfo());
            return -1;
        }

        return 1;
    }

    /* ************************************* */
    /* GETTER */
    /* ************************************* */

    /**
     * This method is used to get defined labels from
     * the database by using the labelEnumFlag (mrs_label_enum.flag).
     * @param labelEnumFlag The labelEnum-flag.
     * @return A string-array which contains all defined labels for the given category or NULL.
     */
    public String[] of_getLabelsByEnumFlag(String labelEnumFlag)
    {
        int row = dataStoreLabelEnums.of_findRow("flag", labelEnumFlag);
        int labelEnumId = dataStoreLabelEnums.of_getItemInteger(row, "label_enum");

        if(labelEnumId != -1)
        {
            //  Get the rows which are using the given labelEnumFlag.
            int[] rows = dataStoreLabel.of_findRows("label_enum", labelEnumId);
            dataStoreLabel.of_sendDebugTableInformation();

            if(rows != null && rows.length > 0)
            {
                String[] collectedLabels = new String[0];

                for (int rowIndex : rows)
                {
                    String label = dataStoreLabel.of_getItemString(rowIndex, "text");

                    if (label != null)
                    {
                        collectedLabels = ArrayUtils.add(collectedLabels, label);
                    }
                }

                return collectedLabels;
            }
        }

        return null;
    }

    public String of_getLabelById(int labelId)
    {
        return dataStoreLabel.of_getItemString(dataStoreLabel.of_findRow("label", labelId), "label");
    }

    public static LabelManager of_getInstance()
    {
        return instance;
    }
}
