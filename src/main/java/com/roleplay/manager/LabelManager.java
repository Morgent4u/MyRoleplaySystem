package com.roleplay.manager;

import com.basis.ancestor.Objekt;
import com.basis.extern.DataStore;
import com.basis.main.main;
import com.basis.sys.Sys;
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
        //  Create a DataStore for the labels.
        String sqlSelect = "SELECT label, label_enum, text, flag, sort FROM mrs_label ORDER BY sort;";
        dataStoreLabel = new DataStore("DataStore4Labels", "mrs_label", "label", sqlSelect);

        if(dataStoreLabel.of_retrieve() == -1)
        {
            of_sendErrorMessage(null, "LabelManager.of_load();", "There was an error while retrieving data from the DataStore: " + dataStoreLabel.of_getInfo());
            return -1;
        }

        //  Create a DataStore for the enumLabels.
        sqlSelect = "SELECT label_enum, text, flag FROM mrs_label_enum;";
        dataStoreLabelEnums = new DataStore("DataStore4LabelEnums", "mrs_label_enum", "label_enum", sqlSelect);

        if(dataStoreLabelEnums.of_retrieve() == -1)
        {
            of_sendErrorMessage(null, "LabelManager.of_load();", "There was an error while retrieving data from the DataStore: " + dataStoreLabelEnums.of_getInfo());
            return -1;
        }

        return 1;
    }

    /* ************************************* */
    /* OBJECT - METHODS */
    /* ************************************* */

    /**
     * This method is used to create a new label-category.
     * A label-category is used to group labels.
     * @param categoryName The category-name.
     * @return 1 = OK, 0 = Category already exists, -1 = An error occurred.
     */
    public int of_createNewLabelEnum(String categoryName)
    {
        //  Check if the category already exists.
        categoryName = categoryName.replace("§", "&");
        String flagText = Sys.of_getNormalizedString(categoryName).trim().toLowerCase();
        int rowId = dataStoreLabelEnums.of_findRow("flag", flagText);

        if(rowId == -1)
        {
            //  Create a new primary-key for the enum-table.
            int enumId = main.SQL.of_updateKey("mrs_label_enum");

            if(enumId != -1)
            {
                //  Update the dataStore with the new entry.
                rowId = dataStoreLabelEnums.of_addRow();

                if(rowId != -1)
                {
                    dataStoreLabelEnums.of_setItemString(rowId, "label_enum", String.valueOf(enumId));
                    dataStoreLabelEnums.of_setItemString(rowId, "text", categoryName);
                    dataStoreLabelEnums.of_setItemString(rowId, "flag", flagText);

                    //  Execute a DataStore-Update.
                    return ( dataStoreLabelEnums.of_update() > 0) ? 1 : -1;
                }
            }
            //  Error while creating a new primary key.
            else
            {
                return -1;
            }
        }

        return 0;
    }

    /**
     * This method is used to create a new label.
     * A label needs to have a defined label-category.
     * @param enumId The label-enum category number.
     * @param labelText The label-name.
     * @return 1 = OK, 0 = The label-category does not exist, -1 = An error occurred.
     */
    public int of_createNewLabel(int enumId, String labelText)
    {
        //  Check if the enumId exist!
        labelText = labelText.replace("§", "&");
        int row = dataStoreLabelEnums.of_findRow("label_enum", enumId);

        if(row != -1)
        {
            //  Create a new primary-key for the label-table.
            int labelId = main.SQL.of_updateKey("mrs_label");

            if(labelId != -1)
            {
                row = dataStoreLabel.of_addRow();

                if(row != -1)
                {
                    dataStoreLabel.of_setItemString(row, "label", String.valueOf(labelId));
                    dataStoreLabel.of_setItemString(row, "label_enum", String.valueOf(enumId));
                    dataStoreLabel.of_setItemString(row, "text", labelText);

                    //  Execute a DataStore-Update.
                    return ( dataStoreLabel.of_update() > 0) ? 1 : -1;
                }
                //  If an add-row error occurs.
                else
                {
                    return -2;
                }
            }
            //  Error while creating a new primary key.
            else
            {
                return -1;
            }
        }

        return 0;
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

    public DataStore of_getDataStore4LabelEnums()
    {
        return dataStoreLabelEnums;
    }

    public DataStore of_getDataStore4Labels()
    {
        return dataStoreLabel;
    }

    public static LabelManager of_getInstance()
    {
        return instance;
    }
}
