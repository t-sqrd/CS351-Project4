
import java.util.ArrayList;
import java.util.Random;

public class Account{

    public boolean isRegistered;
    public Integer hasHold;
    private String clientName;
    private static ArrayList<Integer> usedAccountNumbers = new ArrayList<>();
    private static ArrayList<Integer> usedBankKeys = new ArrayList<>();


    private Integer accountNumber;
    private Integer bankKey;
    private Integer initialDeposit;
    private Integer tempBalance;
    private Integer balance;




    public Account(String clientName){
        this.clientName = clientName;
        this.initAccount();
        tempBalance = balance;
        System.out.println("Account ["+clientName+"] has been created...");

    }
    public String getAccountInfo(){

        return "Name: " + clientName + ", Account Number: " + accountNumber +
                 ", Bank Key: " + bankKey + ", Initial Deposit: $" + initialDeposit+".00";
    }

    public Integer getKey(){
        return bankKey;
    }



    public Message placeHoldOnAccount(Message request){

        Integer amount = request.bidAmount;
        boolean isOver = request.isOver;
        boolean WON = request.WON;

        Message response = new Message();
        response.username = clientName;
        if(isOver){
            response.hasFunds = true;
            if(WON) {
                response.funds = balance - tempBalance;
                balance -= tempBalance;
                response.message = "You have won the item! Balance: " + balance;
            }
            else{
                balance += amount;

                response.message = "Auction for this item is over! You were outbidded! Balance: "+ balance;

            }
            return response;
        }
        else{
            if((tempBalance - amount) <= 0){
                response.message = "Insufficient funds. Hold denied";
                System.out.println("HERE");
                return response;
            }

            else {

                balance -= amount;
                response.message = "Hold placed on account. New Amount: " + balance;
                // System.out.println(response.message);
                return response;

            }
        }
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
        this.initialDeposit = 30000;
        this.balance = initialDeposit;

    }






}
