package com.rprescott.combatloganalyzer.utils;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class ResultsPrinter {

    private static final int TOTAL_LINE_LENGTH = 100;

    /**
     * Displays the provided Title and Header Row to the System Output. <br/>
     * Example: <br/>
     * 
     * +--------------------------------------------------------------------------------------------------+
     * <br/>
     * | <title> | <br/>
     * +--------------------------------------------------------------------------------------------------+
     * <br/>
     * | Column 1 | Column 2 | Column 3 | <br/>
     * +--------------------------------------------------------------------------------------------------+
     * <br/>
     * 
     * 
     * @param title
     *            The title of the table to display.
     * @param columnNames
     *            The list of columns to display.
     */
    public static void displayTitleAndHeaderRow(String title, List<String> columnNames) {
        String headerSurrounder = new StringBuilder().append("+").append(StringUtils.repeat("-", TOTAL_LINE_LENGTH - 2)).append("+").toString();
        String titleRow = new StringBuilder().append("|").append(StringUtils.center(title, TOTAL_LINE_LENGTH - 2)).append("|").toString();
        StringBuilder headerRow = new StringBuilder().append("|");
        for (String columnName : columnNames) {
            headerRow.append(StringUtils.center(columnName, TOTAL_LINE_LENGTH / columnNames.size() - 1));
            headerRow.append("|");
        }

        System.out.println();
        System.out.println(headerSurrounder);
        System.out.println(titleRow);
        System.out.println(headerSurrounder);
        System.out.println(headerRow);
        System.out.println(headerSurrounder);
    }

    /**
     * Displays a single row of data. Each element in the provided List must be a separate column. <br/>
     * Example:<br/>
     * <br/>
     * <b>ResultsPrinter.displayDataRow(Arrays.asList("Geology", "8", "3");</b> <br/>
     * <br/>
     * 
     * | Geology | 8 | 3 |
     * 
     * 
     * @param data
     *            The data to display where each element in the list must be a separate column.
     */
    public static void displayDataRow(List<String> data) {
        StringBuilder dataRow = new StringBuilder().append("|");
        for (String entry : data) {
            dataRow.append(StringUtils.center(entry, TOTAL_LINE_LENGTH / data.size() - 1));
            dataRow.append("|");
        }
        System.out.println(dataRow);
    }

    public static void displayHeaderSurrounder() {
        String headerSurrounder = new StringBuilder().append("+").append(StringUtils.repeat("-", TOTAL_LINE_LENGTH - 2)).append("+").toString();
        System.out.println(headerSurrounder);
    }

}
