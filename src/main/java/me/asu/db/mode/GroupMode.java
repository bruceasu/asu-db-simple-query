package me.asu.db.mode;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

public class GroupMode implements Runnable {
    private ResultSet         resultSet;
    private ResultSetMetaData metaData;
    private int               columnCount;
    private int               pageSize;

    public GroupMode(ResultSet resultSet,
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
            int maxColWidth = 0;
            for (int i = 0; i < columnCount; i++) {
                final String columnName  = metaData.getColumnName(i + 1);
                final String columnLabel = metaData.getColumnLabel(i + 1);
                if (columnLabel != null && columnLabel.length() > maxColWidth)
                    maxColWidth = columnLabel.length();
                if (columnName != null && columnName.length() > maxColWidth)
                    maxColWidth = columnName.length();
            }
            int cnt = 0;
            while (resultSet.next()) {
                if (cnt > pageSize && cnt % pageSize == 1) {
                    System.out.println("Press any key continue.");
                    System.in.read();
                }
                cnt++;

                System.out.println("RECORD " + cnt + ":");
                for (int i = 0; i < columnCount; i++) {
                    final String columnName  = metaData.getColumnName(i + 1);
                    final String columnLabel = metaData.getColumnLabel(i + 1);
                    System.out.printf("%" + maxColWidth + "s: %s%n",
                                      (columnLabel == null ? columnName : columnLabel),
                                      resultSet.getObject(i + 1));
                }
                System.out.println("-------------------------------------------");

            }
            System.out.printf("Total fetch %d records.%n", cnt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
