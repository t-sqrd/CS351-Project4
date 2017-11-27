import java.util.Random;
import java.math.BigInteger;
/**
 * Created by alexschmidt-gonzales on 11/25/17.
 */
public class Encrypt {

    private BigInteger PRIME_P;
    private BigInteger PRIME_Q;
    private BigInteger PRODUCT_N;
    private BigInteger PHI;
    private BigInteger PRIVATE_KEY;
    private BigInteger PUBLIC_KEY;
    private String message;
    private String temp = "";

    public Encrypt(String message){
        this.message = message;
        init_P_Q();
        makeKey();

    }

    private void init_P_Q(){
        PRIME_P = makePrimes();
        PRIME_Q = makePrimes();
        if(PRIME_P.equals(PRIME_Q)){
            init_P_Q();
        }

    }
    private void reformatMessage(){

        for(int i = 0; i < message.length(); i++){
            //temp += Integer.toString(message.codePointAt(i));
             temp += Integer.toString(message.codePointAt(i));



        }
        System.out.println(temp);

    }

    private void makeKey(){

        PRODUCT_N = PRIME_P.multiply(PRIME_Q);
        PHI = (PRIME_P.subtract(BigInteger.ONE)).multiply(PRIME_Q.subtract(BigInteger.ONE));
        //PUBLIC_KEY = coPrime(PHI);
        //PUBLIC_KEY = betaCoPrime(PHI);
        PUBLIC_KEY = new BigInteger("65537");
        PRIVATE_KEY = PUBLIC_KEY.modInverse(PHI);

        reformatMessage();


        BigInteger testEncrypt = new BigInteger(message);
        System.out.println("original num = " + testEncrypt);
        BigInteger temp = encryptNum(testEncrypt);

         BigInteger end = decryptNum(temp);

    }

    private void printValues(){
        System.out.println("P = " + PRIME_P);
        System.out.println("Q = "+ PRIME_Q);
        System.out.println("Product N = " + PRODUCT_N);
        System.out.println("PHI = " + PHI);
        System.out.println("Private Key = " + PRIVATE_KEY);
        System.out.println("Public Key = " + PUBLIC_KEY);
    }


    private BigInteger coPrime(BigInteger r){
        for(long i = 2; i < r.intValueExact(); i++){
            if(r.gcd(BigInteger.valueOf(i)).equals(BigInteger.ONE)){
                System.out.println("I = " + i);
                return BigInteger.valueOf(i);
            }
        }
        return r;
    }

    private BigInteger betaCoPrime(BigInteger r){
        BigInteger i;
        for(i = new BigInteger("2");
            i.compareTo(r) < 2;
            i = i.add(BigInteger.ONE)) {
            if(r.gcd(i).equals(BigInteger.ONE)){
                return i;
            }
        }
        return BigInteger.ONE;

    }

    private BigInteger makePrimes(){
        Random rand = new Random();
        return BigInteger.probablePrime(16*message.length(), rand);
    }

    private BigInteger encryptNum(BigInteger number){

        BigInteger encrypted = number.modPow(PUBLIC_KEY, PRODUCT_N);
        System.out.println("Encrypted = "+ encrypted);
        return encrypted;
    }

    private BigInteger decryptNum(BigInteger encrypted){

        BigInteger decrypted = encrypted.modPow(PRIVATE_KEY, PRODUCT_N);
        System.out.println("Decrypted = " + decrypted);
        return decrypted;
    }

    public static void main(String[] args){
        Encrypt e = new Encrypt("12345678910111213");
        e.printValues();
    }
}
