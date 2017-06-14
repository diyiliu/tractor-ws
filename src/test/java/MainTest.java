import com.tiza.util.CommonUtil;
import org.junit.Test;

/**
 * Description: MainTest
 * Author: DIYILIU
 * Update: 2017-05-26 10:28
 */
public class MainTest {


    @Test
    public void test1(){

        byte b0 = 1;
        System.out.println(b0 << 1);

        Integer p =  0x01FF;

        System.out.println(p.byteValue());

    }

    @Test
    public void test2(){

        long sim = 123;

        String s = String.format("%05d", sim);
        System.out.println(s);
    }

    @Test
    public void test3(){

        String s = "64832464309";

        byte[] bytes = CommonUtil.packBCD(Long.parseLong(s), 6);

        System.out.println(CommonUtil.bytesToStr(bytes));
    }

}
