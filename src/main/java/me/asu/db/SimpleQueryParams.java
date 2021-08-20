package me.asu.db;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.PathConverter;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

public class SimpleQueryParams {


    @Parameter(names = {"-h", "--help"},
               description = "Print this HELP",
               help = true,
               order = 0)
    public boolean help = false;


    @Parameter(names = {"-c", "--config"},
               description = "The Report server JDBC datasource in HikariConfig format(ex: jdbc.properties)",
               order = 1
    )
    public File dbConfFile = new File(System.getProperty("user.dir"), "jdbc.properties");

    @Parameter(names={"-u", "--username"},
               description = "The user name of connection, overwrite the config file settings.",
               order = 1)
    public String username;

    @Parameter(names={"-p", "--password"},
               description = "The password of connection, overwrite the config file settings.",
               order = 1)
    public String password;

    @Parameter(names={"-j", "--jdbc-url"},
               description = "The jdbcUrl of connection, overwrite the config file settings.",
               order = 1)
    public String jdbcUrl;

    @Parameter(names = {"-s", "--sql"},
               description = "The sql script",
               order = 2)
    public String sql;

    @Parameter(names = {"--readonly"},
               description = "Do modified the data.",
               order = 3, arity = 1)
    public boolean readonly = true;

    @Parameter(names = {"-g", "--group"},
               description = "Display query result in group.",
               order = 4)
    public boolean group = false;

    @Parameter(names = {"-o", "--output-file"},
               description = "write the result to file.",
               converter = PathConverter.class,
               order = 5)
    public Path output;

    @Parameter(names = {"-e","--output-charset"},
               description = "The output file charset, default is dependent on system/file.encoding setting.",
               converter = PathConverter.class,
               order = 5)
    public String charset = Charset.defaultCharset().name();

    @Parameter(names = {"-f","--output-format"},
               description = "The output file format, support raw, csv and json.",
               order = 5)
    public String fileFormat = "raw";

    @Parameter(names = {"--output-format-pretty-json"},
               description = "Pretty the output if output to a json file.",
               order = 5)
    public boolean prettyJson = false;


    @Parameter(names = {"--integer-format"},
               description = "The Integer output format.",
               order = 6)
    public String integerFormat = "#,##0";

    @Parameter(names = {"--decimal-format"},
               description = "The Decimal output format.",
               order = 6)
    public String floatFormat = "#,##0.######";
    @Parameter(names = {"--page-size"}, description = "Display page size of query.")
    public int pageSize = 10;

    @Parameter(
            description = "[The arguments of sql, if no sql script set, the first argument is the script file.]")
    List<String> sqlArguments;
}
