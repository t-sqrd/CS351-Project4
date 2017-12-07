/**
 * Created by alexschmidt-gonzales on 11/20/17.
 */
public class Protocal {
    String str;


    public String process(String str) {
        if (str != null) {
            System.out.println("House input = " + str);
            System.out.println("Input length = " + str.length());
            System.out.println(".....");
        }
        return str;
    }

}