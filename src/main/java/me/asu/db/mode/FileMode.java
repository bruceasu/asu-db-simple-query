package me.asu.db.mode;

import com.csvreader.CsvWriter;
import me.asu.db.JacksonUtils;
import me.asu.db.SimpleQueryParams;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;

public class FileMode implements Runnable {
    ResultSet         resultSet;
    ResultSetMetaData metaData;
    int               columnCount;
    SimpleQueryParams params;

    public FileMode(ResultSet resultSet,
                    ResultSetMetaData metaData,
                    int columnCount,
                    SimpleQueryParams params) {
        this.resultSet   = resultSet;
        this.metaData    = metaData;
        this.columnCount = columnCount;
        this.params      = params;
    }

    @Override
    public void run() {
        final Path output = params.output;
        final Path parent = output.toAbsolutePath().getParent();
        if (!Files.isDirectory(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        String fileFormat = params.fileFormat;
        if (fileFormat == null) {
            fileFormat = "raw";
        } else {
            fileFormat = fileFormat.toLowerCase();
        }
        if (params.charset == null) {
            params.charset = Charset.defaultCharset().name();
        }

        // acceptable
        Set<String> acceptSet = new HashSet<>();
        acceptSet.add("raw");
        acceptSet.add("csv");
        acceptSet.add("json");
        if (!acceptSet.contains(fileFormat)) {
            throw new RuntimeException("Not support this file format: " + params.fileFormat);
        }
        try {
            switch (fileFormat) {
                case "csv":
                    new CsvOutput().run();
                    break;
                case "json":
                    new JsonOutput().run();
                    break;
                case "raw":
                default:
                    if (params.group) {
                        new GroupOutput().run();
                    } else {
                        new GridOutput().run();
                    }
                    break;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            if (e instanceof RuntimeException) throw (RuntimeException) e;
            throw new RuntimeException(e);
        }
    }


    private String safeString(ResultSet resultSet, String columnName, int i)
    throws SQLException {
        final Object object = resultSet.getObject(i + 1);
        // some field is a long number but defined in decimal.
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


    class JsonOutput implements Runnable {
        BufferedWriter writer;

        public JsonOutput() throws IOException {
            writer = Files.newBufferedWriter(params.output, Charset.forName(params.charset));
        }

        @Override
        public void run() {
            String[] keys = new String[columnCount];
            try {
                for (int i = 0; i < columnCount; i++) {
                    final String columnName  = metaData.getColumnName(i + 1);
                    final String columnLabel = metaData.getColumnLabel(i + 1);
                    keys[i] = ((columnLabel == null || columnLabel.isEmpty()) ? columnName
                                                                              : columnLabel);
                }
                int cnt = 0;

                while (resultSet.next()) {
                    cnt++;
                    Map<String, Object> m = new HashMap<>();
                    for (int i = 0; i < columnCount; i++) {
                        m.put(keys[i], resultSet.getObject(i+1));
                    }
                    String s;
                    if (params.prettyJson) {
                        s = JacksonUtils.serializeForPrint(m);
                    } else {
                        s = JacksonUtils.serialize(m);
                    }
                    writer.write(s);
                    writer.write("\n");
                }
                System.out.printf("Total fetch %d records.%n", cnt);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        // quietly
                    }
                }
            }
        }
    }


    class CsvOutput implements Runnable {
        public void run() {
            try (CsvWriter csvWriter = new CsvWriter(params.output.toString(), ',',
                                                     Charset.forName(params.charset))
            ) {
                for (int i = 0; i < columnCount; i++) {
                    final String columnName  = metaData.getColumnName(i + 1);
                    final String columnLabel = metaData.getColumnLabel(i + 1);
                    csvWriter.write(
                            (columnLabel == null || columnLabel.isEmpty()) ? columnName
                                                                           : columnLabel);
                }
                csvWriter.endRecord();
                int cnt = 0;
                while (resultSet.next()) {
                    cnt++;
                    String[] row = new String[columnCount];
                    for (int i = 0; i < columnCount; i++) {
                        row[i] = safeString(resultSet, metaData.getColumnName(i + 1), i);
                    }
                    csvWriter.writeRecord(row);
                }
                System.out.printf("Total fetch %d records.%n", cnt);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    class GroupOutput implements Runnable {
        BufferedWriter writer;

        public GroupOutput() throws IOException {
            writer = Files.newBufferedWriter(params.output, Charset.forName(params.charset));
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
                    cnt++;

                    writer.write("RECORD " + cnt + ":\n");
                    for (int i = 0; i < columnCount; i++) {
                        final String columnName  = metaData.getColumnName(i + 1);
                        final String columnLabel = metaData.getColumnLabel(i + 1);
                        final String line = String.format("%" + maxColWidth + "s: %s\n",
                                                          (columnLabel == null ? columnName
                                                                               : columnLabel),
                                                          resultSet.getObject(i + 1));
                        writer.write(line);
                    }
                    writer.write("-------------------------------------------\n");

                }
                System.out.printf("Total fetch %d records.%n", cnt);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        // quietly
                    }
                }
            }
        }
    }

    class GridOutput implements Runnable {
        BufferedWriter writer;

        public GridOutput() throws IOException {
            writer = Files.newBufferedWriter(params.output, Charset.forName(params.charset));
        }

        public void run() {
            try {
                int[]          maxColsWidth    = calcColsWidth(metaData, columnCount);
                int            cnt             = 0;
                List<String[]> page            = new ArrayList<String[]>();
                int            currentPageRows = 0;
                while (resultSet.next()) {
                    currentPageRows++;
                    cnt++;
                    String[] row = new String[columnCount];
                    for (int i = 0; i < columnCount; i++) {
                        row[i] = safeString(resultSet, metaData.getColumnName(i + 1), i);
                    }
                    page.add(row);
                }

                if (currentPageRows > 0) {
                    // reset maxColsWidth
                    outputToFile(page, currentPageRows, maxColsWidth, columnCount, metaData);
                    currentPageRows = 0;
                }

                System.out.printf("Total fetch %d records.%n", cnt);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        // quietly
                    }
                }
            }
        }


        private int[] calcColsWidth(ResultSetMetaData metaData, int columnCount)
        throws SQLException {
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

        private void printGridHeader(ResultSetMetaData metaData,
                                     int columnCount,
                                     int[] maxColsWidth)
        throws SQLException, IOException {
            for (int i = 0; i < columnCount; i++) {
                final String columnName  = metaData.getColumnName(i + 1);
                final String columnLabel = metaData.getColumnLabel(i + 1);
                int          m           = maxColsWidth[i];
                writer.write(String.format("|%-" + m + "s",
                                           (columnLabel == null ? columnName : columnLabel)));
            }
            writer.write("|\n");
        }

        private void printGridRowLine(int columnCount, int[] maxColsWidth) throws IOException {
            for (int i = 0; i < columnCount; i++) {
                writer.write("+");
                for (int j = 0; j < maxColsWidth[i]; j++) {
                    writer.write("-");
                }
            }
            writer.write("+\n");
        }

        private void outputToFile(List<String[]> page,
                                  int currentPageRows,
                                  int[] maxColsWidth,
                                  int columnCount, ResultSetMetaData metaData)
        throws SQLException, IOException {
            // reset maxColsWidth
            resetMaxColsWidth(maxColsWidth, columnCount, page, currentPageRows);
            printGridRowLine(columnCount, maxColsWidth);
            printGridHeader(metaData, columnCount, maxColsWidth);
            printGridRowLine(columnCount, maxColsWidth);

            for (int i = 0; i < currentPageRows; i++) {
                final String[] curRow = page.get(i);
                for (int j = 0; j < columnCount; j++) {
                    final String format = "|%" + maxColsWidth[j] + "s";
                    writer.write(String.format(format, curRow[j]));
                }
                writer.write("|\n");
            }
            printGridRowLine(columnCount, maxColsWidth);
        }

        private void resetMaxColsWidth(int[] maxColsWidth,
                                       int columnCount,
                                       List<String[]> page,
                                       int currentPageRows) {
            for (int i = 0; i < columnCount; i++) {
                int m = maxColsWidth[i];
                for (int j = 0; j < currentPageRows; j++) {
                    final String s      = page.get(j)[i];
                    final int    length = s.length();
                    if (length > m) m = length;
                }
                maxColsWidth[i] = m;
            }
        }
    }

}