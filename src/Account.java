
import java.util.ArrayList;
import java.util.Random;

public class Account{

    private String clientName;
    private static ArrayList<Integer> usedAccountNumbers = new ArrayList<>();
    private static ArrayList<Integer> usedBankKeys = new ArrayList<>();


    private Integer accountNumber;
    private Integer bankKey;
    private Integer initiaDeposit;


    public Account(String clientName){
        this.clientName = clientName;
        initAccount();
        System.out.println("Account ["+clientName+"] has been created...");

    }
    public String getAccountInfo(){

        return "Name: " + clientName + ", Account Number: " + accountNumber +
                 ", Bank Key: " + bankKey + ", Initial Deposit: $" + initiaDeposit+".00";
    }

    public Integer getKey(){
        return bankKey;
    }


    private Integer makeBankKey() {
        Random rand = new Random();
        Integer key = rand.nextInt(50);
        if(usedBankKeys.contains(key)){
            makeBankKey();
        }

        return key;
    }


    private Integer makeAccountNumber(){
        Random rand = new Random();
        Integer accountNum = rand.nextInt(10000);
        if(usedAccountNumbers.contains(accountNum)){
            makeAccountNumber();
        }
        return accountNum;
    }


    private void initAccount(){
        this.accountNumber = makeAccountNumber();
        this.bankKey = makeBankKey();
        this.initiaDeposit = 5;

    }






}
