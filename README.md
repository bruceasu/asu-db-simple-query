# Simple database client.
Because not allow to install database client or other software on a device,
use this simple toolkit to query database.


# Usage
```
Usage: SimpleQuery [options] [The arguments for sql, if no sql script set, the first argument is the script file.]
  Options:
    -d, --jdbc
      The Report server JDBC datasource in HikariConfig format(ex: jdbc.properties) 
      Default: ./jdbc.properties
    -s, --sql
      The sql script
    --readonly
      Do modified the data.
      Default: true
    -g, --group
      Display query result in group.
      Default: false
    -h, --help
      Print this HELP
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