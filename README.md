# Simple database client.
Because not allow to install database client or other software on a device,
use this simple toolkit to query database.


# Usage
```
Usage: SimpleQuery [options] [The arguments of sql, if no sql script set, 
      the first argument is the script file.]
  Options:
    -h, --help
      Print this HELP
    -c, --config
      The Report server JDBC datasource in HikariConfig format(ex: 
      jdbc.properties) 
      Default: D:\workspace\tools\asu-db-simple-query\jdbc.properties
    -u, --username
      The user name of connection, overwrite the config file settings.
    -p, --password
      The password of connection, overwrite the config file settings.
    -j, --jdbc-url
      The jdbcUrl of connection, overwrite the config file settings.
    -s, --sql
      The sql script
    --readonly
      Do modified the data.
      Default: true
    -g, --group
      Display query result in group.
      Default: false
    --output-format-pretty-json
      Pretty the output if output to a json file.
      Default: false
    -o, --output-file
      write the result to file.
    -e, --output-charset
      The output file charset, default is dependent on system/file.encoding 
      setting. 
      Default: UTF-8
    -f, --output-format
      The output file format, support raw, csv and json.
      Default: raw
    --page-size
      Display page size of query.
      Default: 10


Process finished with exit code 1


```

## sql parameter type support
  - dt： datetime/timestamp
  - d: date
  - t: time
  - ts: timestamp
  - i: integer 32bit with sign
  - l: long/big integer 64bit with sign
  - s: short integer 16bit with sign
  - b: byte integer 8bit with sign
  - dbl: double number  64bit with sign
  - f: float number 32bit with sign
  - bd: big decimal  number, no limit
  - str: string
  - nothing means string