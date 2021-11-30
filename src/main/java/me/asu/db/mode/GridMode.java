package me.asu.db.mode;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;

public class GridMode implements Runnable {
    private ResultSet         resultSet;
    private ResultSetMetaData metaData;
    private int               columnCount;
    private int               pageSize;

    public GridMode(ResultSet resultSet,
                    ResultSetMetaData metaData,
                    int columnCount,
                    int pageSize) {
        this.resultSet   = resultSet;
        this.metaData    = metaData;
        this.columnCount = columnCount;
        this.pageSize    = pageSize;

    }

    public void run() {
        try {
            int[]      maxColsWidth    = calcColsWidth(metaData, columnCount);
            int        cnt             = 0;
            String[][] page            = new String[pageSize][columnCount];
            int        currentPageRows = 0;
            while (resultSet.next()) {
                if (cnt > pageSize && cnt % pageSize == 1) {
                    System.out.println("Press any key continue.");
                    System.in.read();
                }
                currentPageRows++;
                String[] row = page[cnt % pageSize];
                for (int i = 0; i < columnCount; i++) {
                    row[i] = safeString(resultSet, metaData.getColumnName(i + 1), i);
                }
                cnt++;
                if (currentPageRows == pageSize) {
                    printPage(page, currentPageRows, maxColsWidth, columnCount, metaData);
                    currentPageRows = 0;

                }

            }
            if (currentPageRows > 0) {
                // reset maxColsWidth
                printPage(page, currentPageRows, maxColsWidth, columnCount, metaData);
                currentPageRows = 0;
            }

            System.out.printf("Total fetch %d records.%n", cnt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printPage(String[][] page,
                           int currentPageRows,
                           int[] maxColsWidth,
                           int columnCount, ResultSetMetaData metaData) throws SQLException {
        // reset maxColsWidth
        resetMaxColsWidth(maxColsWidth, columnCount, page, currentPageRows);
        printGridRowLine(columnCount, maxColsWidth);
        printGridHeader(metaData, columnCount, maxColsWidth);
        printGridRowLine(columnCount, maxColsWidth);

        for (int i = 0; i < currentPageRows; i++) {
            final String[] curRow = page[i];
            for (int j = 0; j < columnCount; j++) {
                final String format = "|%" + maxColsWidth[j] + "s";
                System.out.printf(format, curRow[j]);
            }
            System.out.println("|");
        }
        printGridRowLine(columnCount, maxColsWidth);
    }

    private void resetMaxColsWidth(int[] maxColsWidth,
                                   int columnCount,
                                   String[][] page,
                                   int currentPageRows) {
        for (int i = 0; i < columnCount; i++) {
            int m = maxColsWidth[i];
            for (int j = 0; j < currentPageRows; j++) {
                final String s      = page[j][i];
                final int    length = s==null ? 0 :s.length();
                if (length > m) m = length;
            }
            maxColsWidth[i] = m;
        }
//        for (int i = 0; i < columnCount; i++) {
//            System.out.printf("<%d>", maxColsWidth[i]);
//        }
//        System.out.println();
    }


    private String safeString(ResultSet resultSet, String columnName, int i) throws SQLException {
        final Object object = resultSet.getObject(i + 1);
        boolean isIdCol = columnName != null &&
                (columnName.endsWith("id") || columnName.endsWith("ID"));
        boolean isAccount = columnName != null
                && (columnName.equalsIgnoreCase("account")
                || columnName.equalsIgnoreCase("account_no"));
        if (object instanceof Number) {
            boolean integer = (object instanceof Integer) || (object instanceof Byte)
                    || (object instanceof Short) || (object instanceof Long)
                    || (object instanceof BigInteger);
            if (integer) {
                if (isIdCol || isAccount) {
                    DecimalFormat df = new DecimalFormat("#");
                    return df.format(object);
                } else {
                    DecimalFormat df = new DecimalFormat("#,##0");
                    return df.format(object);
                }
            }
            boolean floatType = (object instanceof Double) || (object instanceof Float)
                    || (object instanceof BigDecimal);
            if (floatType) {
                if (isIdCol || isAccount) {
                    DecimalFormat df = new DecimalFormat("#");
                    return df.format(object);
                } else {
                    DecimalFormat df = new DecimalFormat("#,##0.######");
                    return df.format(object);
                }
            }
        }
        return object == null ? "<null>" : object.toString();
    }


    private int[] calcColsWidth(ResultSetMetaData metaData, int columnCount) throws SQLException {
        int[] maxColsWidth = new int[columnCount];
        for (int i = 0; i < columnCount; i++) {
            final String columnName  = metaData.getColumnName(i + 1);
            final String columnLabel = metaData.getColumnLabel(i + 1);
            int          m           = 0;
            if (columnLabel != null && columnLabel.length() > m)
                m = columnLabel.length();
            if (columnName != null && columnName.length() > m)
                m = columnName.length();
            maxColsWidth[i] = m;
        }
        return maxColsWidth;
    }


    private void printGridHeader(ResultSetMetaData metaData, int columnCount, int[] maxColsWidth)
    throws SQLException {
        for (int i = 0; i < columnCount; i++) {
            final String columnName  = metaData.getColumnName(i + 1);
            final String columnLabel = metaData.getColumnLabel(i + 1);
            int          m           = maxColsWidth[i];
            System.out.printf("|%-" + m + "s", (columnLabel == null ? columnName : columnLabel));
        }
        System.out.println("|");
    }

    private void printGridRowLine(int columnCount, int[] maxColsWidth) {
        for (int i = 0; i < columnCount; i++) {
            System.out.printf("+");
            for (int j = 0; j < maxColsWidth[i]; j++) {
                System.out.printf("-");
            }
        }
        System.out.println("+");
    }

}