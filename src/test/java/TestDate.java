import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TestDate {
    public static void main(String[] args) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
        String t1 = "2021-07-01T20:10:30+08:00";
        String t2 = "2021-07-01T20:10:30";
        String t3 = "2021-07-01 20:10:30";
        String t4 = "2021-07-01";
        String t5 = "20:10:30";

        final Date parse1 = sdf.parse(t1);
        System.out.println("parse1 = " + parse1);

        final Date parse2 = sdf.parse(t2);
        System.out.println("parse2 = " + parse2);

        final Date parse3 = sdf.parse(t3);
        System.out.println("parse3 = " + parse3);

        final Date parse4 = sdf.parse(t4);
        System.out.println("parse4 = " + parse4);

        final Date parse5 = sdf.parse(t5);
        System.out.println("parse5 = " + parse5);
    }
}
