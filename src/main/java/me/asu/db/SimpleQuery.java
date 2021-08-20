package me.asu.db;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Strings;
import me.asu.db.editor.*;
import me.asu.db.mode.FileMode;
import me.asu.db.mode.GridMode;
import me.asu.db.mode.GroupMode;

import java.beans.PropertyEditor;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public class SimpleQuery implements Runnable {


    private SimpleQueryParams params;

    private Map<Class, PropertyEditor> editors = new HashMap<>();

    public SimpleQuery(SimpleQueryParams params) throws Exception {
        this.params = params;
        initResources();

        Runtime.getRuntime().addShutdownHook(new Thread(this::releaseResources));
    }

    public static void main(String[] args) {
        SimpleQueryParams params = new SimpleQueryParams();
        JCommander        build  = JCommander.newBuilder().addObject(params).build();

        try {

            build.setProgramName("SimpleQuery");
            build.setColumnSize(76);
            build.parse(args);
            checkParams(params, build);


            SimpleQuery executor = new SimpleQuery(params);

            long begin = System.currentTimeMillis();
            executor.run();
            long end = System.currentTimeMillis();
            System.out.printf("Cost %s ms to process. %n", (end - begin));
        } catch (Exception e) {

            if (e instanceof ParameterException) {
                build.usage();
                System.exit(1);
            } else {
                System.exit(2);
            }
        } finally {

        }
    }


    /**
     * 检查参数，特别是某种条件下属于必须的参数。
     *
     * @param params {@link SimpleQueryParams}
     * @param build {@link JCommander}
     */
    private static void checkParams(SimpleQueryParams params,
                                    JCommander build) {
        if (params.help) {
            build.usage();
            System.exit(1);
        }
    }


    void releaseResources() {

    }

    public void closeQuietly(AutoCloseable s) {
        if (s != null) {
            try {
                s.close();
            } catch (Exception throwables) {
            }
        }
    }

    /**
     * 數據源配置
     */
    private void initResources() throws Exception {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // ignore
        }
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            // ignore
        }

        // number
        editors.put(Long.class, new LongEditor());
        editors.put(Integer.class, new IntegerEditor());
        editors.put(Short.class, new ShortEditor());
        editors.put(Byte.class, new ByteEditor());
        editors.put(Double.class, new DoubleEditor());
        editors.put(Float.class, new FloatEditor());
        editors.put(BigDecimal.class, new BigDecimalEditor());
        editors.put(java.util.Date.class, new DateEditor());

    }


    private Connection getConnection() throws SQLException, IOException {
        Properties dbProp = new Properties();
        if (params.dbConfFile != null && Files.isRegularFile(params.dbConfFile.toPath())) {
            dbProp.load(new ByteArrayInputStream(Files.readAllBytes(params.dbConfFile.toPath())));
        }

        // overwrite
        if (!Strings.isStringEmpty(params.jdbcUrl)) {
            dbProp.setProperty("jdbcUrl", params.jdbcUrl);
        }
        if (!Strings.isStringEmpty(params.username)) {
            dbProp.setProperty("username", params.username);
        }
        if (!Strings.isStringEmpty(params.password)) {
            dbProp.setProperty("password", params.password);
        }

        String jdbcUrl  = dbProp.getProperty("jdbcUrl");
        String username = dbProp.getProperty("username");
        String password = dbProp.getProperty("password");

        if (password == null || password.isEmpty()) {
            password = PasswordReader.readPassword("Password:");
        }
        final Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
        if (params.readonly) {
            connection.setReadOnly(true);
        }
        return connection;

    }

    @Override
    public void run() {
        String   sql  = params.sql;
        String[] args = null;
        if (sql == null) {
            if (params.sqlArguments == null) {
                System.err.println("There's no sql script.");
                System.exit(1);
            } else {
                // the first is sql file
                Path sqlPath = Paths.get(params.sqlArguments.get(0));
                if (!Files.isRegularFile(sqlPath)) {
                    System.err.printf("%s is not a sql script file.%n", sqlPath);
                    System.exit(1);
                }

                sql = readSqlFromFile(sqlPath);
                if (params.sqlArguments.size() > 1) {
                    args = params.sqlArguments.subList(1, params.sqlArguments.size())
                                              .toArray(new String[0]);
                }

                if (sql == null) {
                    System.err.println("There's no sql script.");
                    System.exit(1);
                }
            }
        } else {
            if (params.sqlArguments != null && !params.sqlArguments.isEmpty()) {
                args = params.sqlArguments.toArray(new String[0]);
            }
        }
        sql = sql.trim();
        PreparedStatement stat       = null;
        ResultSet         resultSet  = null;
        Connection        connection = null;
        boolean           isQuery;
        try {
            connection = getConnection();
            if ("select".equalsIgnoreCase(sql.substring(0, 6))) {
                stat = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY,
                                                   ResultSet.CONCUR_READ_ONLY);
                stat.setFetchSize(1000);
                isQuery = true;
            } else {
                stat    = connection.prepareStatement(sql);
                isQuery = false;
            }
            setParameters(args, stat);
            if (isQuery) {
                resultSet = stat.executeQuery();
                final ResultSetMetaData metaData    = resultSet.getMetaData();
                final int               columnCount = metaData.getColumnCount();

                final int pageSize = params.pageSize < 1 ? 10 : params.pageSize;
                if (params.output != null) {
                    new FileMode(resultSet, metaData, columnCount, params).run();
                } else if (params.group) {
                    new GroupMode(resultSet, metaData, columnCount, pageSize).run();
                } else {
                    new GridMode(resultSet, metaData, columnCount, pageSize).run();
                }
            } else {
                final int i = stat.executeUpdate();
                System.out.printf("Total %d records updated.%n", i);
            }
            System.out.println("===========================================");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeQuietly(stat);
            closeQuietly(resultSet);
            closeQuietly(connection);
        }
    }


    @SuppressWarnings("unchecked")
    private <T> T getValue(Class<T> cls, String text) {
        final PropertyEditor editor = editors.get(cls);
        if (editor == null) return null;
        editor.setAsText(text);
        return (T) editor.getValue();
    }

    private void setParameters(String[] args, PreparedStatement stat) throws SQLException {
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (arg.contains(":")) {
                    final String substring = arg.substring(arg.indexOf(":") + 1);
                    if (arg.startsWith("dt:")) {
                        java.util.Date d = getValue(java.util.Date.class, substring);
                        if (d != null) {
                            stat.setTimestamp(i + 1, new Timestamp(d.getTime()));
                        } else {
                            stat.setNull(i + 1, JDBCType.TIMESTAMP.getVendorTypeNumber());
                        }
                    } else if (arg.startsWith("d:")) {
                        java.util.Date d = getValue(java.util.Date.class, substring);
                        if (d != null) {
                            stat.setDate(i + 1, new Date(d.getTime()));
                        } else {
                            stat.setNull(i + 1, JDBCType.DATE.getVendorTypeNumber());
                        }
                    } else if (arg.startsWith("t:")) {
                        java.util.Date d = getValue(java.util.Date.class, substring);
                        if (d != null) {
                            stat.setTime(i + 1, new Time(d.getTime()));
                        } else {
                            stat.setNull(i + 1, JDBCType.TIME.getVendorTypeNumber());
                        }
                    } else if (arg.startsWith("ts:")) {
                        if (substring.matches("^\\d+$")) {
                            final long ts = Long.parseLong(substring);
                            stat.setTimestamp(i + 1, new Timestamp(ts));
                        } else {
                            java.util.Date d = getValue(java.util.Date.class, substring);
                            if (d != null) {
                                stat.setTimestamp(i + 1, new Timestamp(d.getTime()));
                            } else {
                                stat.setNull(i + 1, JDBCType.TIMESTAMP.getVendorTypeNumber());
                            }
                        }
                    } else if (arg.startsWith("i:")) {
                        final Integer a = getValue(Integer.class, substring);
                        if (a != null) {
                            stat.setInt(i + 1, a);
                        } else {
                            stat.setNull(i + 1, JDBCType.INTEGER.getVendorTypeNumber());
                        }
                    } else if (arg.startsWith("l:")) {
                        final Long a = getValue(Long.class, substring);
                        if (a != null) {
                            stat.setLong(i + 1, a);
                        } else {
                            stat.setNull(i + 1, JDBCType.BIGINT.getVendorTypeNumber());
                        }
                    } else if (arg.startsWith("s:")) {
                        final Short a = getValue(Short.class, substring);
                        if (a != null) {
                            stat.setShort(i + 1, a);
                        } else {
                            stat.setNull(i + 1, JDBCType.SMALLINT.getVendorTypeNumber());
                        }
                    } else if (arg.startsWith("b:")) {
                        final Byte a = getValue(Byte.class, substring);
                        if (a != null) {
                            stat.setByte(i + 1, a);
                        } else {
                            stat.setNull(i + 1, JDBCType.TINYINT.getVendorTypeNumber());
                        }
                    } else if (arg.startsWith("dbl:")) {
                        final Double a = getValue(Double.class, substring);
                        if (a != null) {
                            stat.setDouble(i + 1, a);
                        } else {
                            stat.setNull(i + 1, JDBCType.DOUBLE.getVendorTypeNumber());
                        }
                    } else if (arg.startsWith("f:")) {
                        final Float a = getValue(Float.class, substring);
                        if (a != null) {
                            stat.setFloat(i + 1, a);
                        } else {
                            stat.setNull(i + 1, JDBCType.FLOAT.getVendorTypeNumber());
                        }
                    } else if (arg.startsWith("bd:")) {
                        final BigDecimal a = getValue(BigDecimal.class, substring);
                        if (a != null) {
                            stat.setBigDecimal(i + 1, a);
                        } else {
                            stat.setNull(i + 1, JDBCType.DECIMAL.getVendorTypeNumber());
                        }
                    } else if (arg.startsWith("str:")) {
                        stat.setString(i + 1, substring);
                    } else {
                        stat.setString(i + 1, arg);
                    }
                } else {
                    stat.setString(i + 1, arg);
                }
            }
        }
    }


    private String readSqlFromFile(Path sqlPath) {
        // read the sql
        final byte[] bytes;
        try {
            bytes = Files.readAllBytes(sqlPath);
            // only support the utf-8 charset.
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }

    }


}

