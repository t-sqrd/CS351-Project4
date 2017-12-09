import java.math.BigInteger;
import java.util.Random;

/**
 * Created by alexschmidt-gonzales on 11/25/17.
 */
public class Encrypt
{

    private BigInteger PRIME_P;
    private BigInteger PRIME_Q;
    private BigInteger PRODUCT_N;
    private BigInteger PHI;
    private BigInteger PRIVATE_KEY;
    private BigInteger PUBLIC_KEY;
    private BigInteger ENCRYTED;

    private String message;
    private String temp = "";


    // the Encrypt class was not implemented
    // but it works correctly to encrypt keys
    public Encrypt()
    {
        init_P_Q();
        makeKey();

    }


    public Encrypt(String message)
    {
        this.message = message;
        init_P_Q();
        makeKey();
    }

    public BigInteger getENCRYTED(String message)
    {
        this.message = message;
        return encryptMessage();

    }


    public BigInteger getPublic()
    {
        return PUBLIC_KEY;
    }

    public BigInteger getPrivate()
    {
        return PRIVATE_KEY;
    }

    private void init_P_Q()
    {
        this.PRIME_P = makePrimes();
        this.PRIME_Q = makePrimes();
        if (PRIME_P.equals(PRIME_Q))
        {
            init_P_Q();
        }

    }

    private void reformatMessage()
    {

        for (int i = 0; i < message.length(); i++)
        {
            temp += Integer.toString(message.codePointAt(i));
        }
    }

    private void makeKey()
    {

        this.PRODUCT_N = PRIME_P.multiply(PRIME_Q);
        this.PHI = (PRIME_P.subtract(BigInteger.ONE)).multiply(PRIME_Q.subtract(BigInteger.ONE));
        this.PUBLIC_KEY = betaCoPrime(PHI);
        this.PRIVATE_KEY = PUBLIC_KEY.modInverse(PHI);
        ;

    }

    private void printValues()
    {
        System.out.println("P = " + PRIME_P);
        System.out.println("Q = " + PRIME_Q);
        System.out.println("Product N = " + PRODUCT_N);
        System.out.println("PHI = " + PHI);
        System.out.println("Private Key = " + PRIVATE_KEY);
        System.out.println("Public Key = " + PUBLIC_KEY);
    }

    public BigInteger encryptMessage()
    {

        BigInteger m = new BigInteger(message);
        return encryptNum(m);
    }


    private BigInteger coPrime(BigInteger r)
    {
        for (long i = 2; i < r.intValueExact(); i++)
        {
            if (r.gcd(BigInteger.valueOf(i)).equals(BigInteger.ONE))
            {
                System.out.println("I = " + i);
                return BigInteger.valueOf(i);
            }
        }
        return r;
    }

    private BigInteger betaCoPrime(BigInteger r)
    {
        BigInteger i;
        for (i = new BigInteger("2");
             i.compareTo(r) < 2;
             i = i.add(BigInteger.ONE))
        {
            if (r.gcd(i).equals(BigInteger.ONE))
            {
                return i;
            }
        }
        return BigInteger.ONE;

    }

    private BigInteger makePrimes()
    {
        Random rand = new Random();
        return BigInteger.probablePrime(16, rand);
    }

    private BigInteger encryptNum(BigInteger number)
    {

        BigInteger encrypted = number.modPow(PUBLIC_KEY, PRODUCT_N);
        ENCRYTED = encrypted;
        System.out.println("Encrypted = " + encrypted);

        return encrypted;
    }

    private BigInteger decryptNum(BigInteger encrypted)
    {

        BigInteger decrypted = encrypted.modPow(PRIVATE_KEY, PRODUCT_N);
        System.out.println("Decrypted = " + decrypted);
        return decrypted;
    }

    public BigInteger getEncrpytedNum()
    {
        return ENCRYTED;
    }

}
