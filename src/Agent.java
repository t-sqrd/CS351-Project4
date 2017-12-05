import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Agent extends Thread {
    public String userName;
    private String initBid;
    private int BANK_PORT = 8080;
    private int CENTRAL_PORT = 8081;
    private String accountName = "";
    private Integer accountNum;
    private String host;
    public volatile boolean bidding = false;
    private volatile boolean creatingAccount = false;
    private ArrayList<String> userInfo = new ArrayList<>();
    public String myAccountNum = "";

    private Encrypt encrypt;
    private Boolean registered = false;
    private Boolean canBid = false;
    private String housePicked = "";




    public static void main(String args[]) {


        String host = "127.0.0.1";
//        System.out.println("Please enter IP address ");
//        Scanner scanner = new Scanner(System.in);
//        String t = scanner.next();
//        host = t;
        //String host = "129.24.112.247"; //work IP

        int port = 8080;
        new Agent(host, "1110", "Alex");

    }

    public Agent(String host, String initBid, String userName) {

        this.encrypt = new Encrypt();

        userInput();


    }

    public double placeBid(double bid) {


        return 0;
    }

    private void changeSocket() {


    }



    private void userInput() {

        try {

            creatingAccount = true;
            System.out.println("Connecting to host " + host + " on port " + BANK_PORT + ".");
            Socket bankSocket = null;
            Socket centralSocket = null;
            //Reader tempIn = new StringReader(userName);
            BufferedReader in = null;


            //serverOut sends message to server (name, amount, initial bid)


            ObjectOutputStream toBankServer = null;
            ObjectInputStream fromBankServer = null;

            ObjectOutputStream toCentralServer = null;
            ObjectInputStream fromCentralServer = null;

            try {

                bankSocket = new Socket(host, BANK_PORT);
                centralSocket = new Socket(host, CENTRAL_PORT);
                //serverOut = new DataOutputStream(echoSocket.getOutputStream());
                //in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
                toBankServer = new ObjectOutputStream(bankSocket.getOutputStream());
                fromBankServer = new ObjectInputStream(bankSocket.getInputStream());

                toCentralServer = new ObjectOutputStream(centralSocket.getOutputStream());
                fromCentralServer = new ObjectInputStream(centralSocket.getInputStream());


            } catch (UnknownHostException e) {
                System.err.println("Unknown host: " + host);
                System.exit(1);
            } catch (IOException e) {
                System.err.println("Unable to get streams from server in Agent");
                System.exit(1);
            }

            System.out.println("To return to main menu type HOME ");
            System.out.println("Options : Make Account (m) / View Bidding Houses (v)");

            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

            String request;
            Boolean accountInput = false;
            Boolean clientInput = false;
            Boolean bidInput = false;

            while((request = stdin.readLine()) != null){
                //System.out.println("Message sent: " + request);


                if(accountInput){
                    Message send = new Message();
                    send.username = request;
                    accountName = request;
                    send.newAccount = true;
                    accountInput = false;
                    toBankServer.writeObject(send);
                    Message m = (Message)fromBankServer.readObject();
                    accountNum = m.accountNum;
                    System.out.println(m.getMessage());
                    printOptions();
                }
                if (clientInput){
                    System.out.println("client you chose from list: " + request);
                    housePicked = request;
                    Message send = new Message();
                    send.message = request;
                    send.selectHouse = true;
                    clientInput = false;
                    canBid = true;
                    toCentralServer.writeObject(send);
                    System.out.println(((Message)fromCentralServer.readObject()).getMessage());
                    printOptions();
                }
                if (bidInput){
                    System.out.println("you chose item: " + request);
                    Message send = new Message();
                    send.message = request;
                    send.username = housePicked;
                    send.bid = 10;
                    send.placeBid = true;
                    bidInput = false;
                    toCentralServer.writeObject(send);
                    Message response = (Message)fromCentralServer.readObject();
                    System.out.println(response.getMessage());
                    System.out.println("should i tell the bank to bid? " + response.placeBid);
                    Message bankSend = new Message();
                    bankSend.message = "Hey we gonna put a hold on some cash of " + send.bid;
                    bankSend.placeBid = true;
                    bankSend.bid = 10;
                    bankSend.username = accountName;
                    bankSend.accountNum = accountNum;
                    toBankServer.writeObject(bankSend);
                    Message bankResponse = (Message)fromBankServer.readObject();
                    System.out.println(bankResponse.getMessage());
                    printOptions();
                }
                if(request.equals("Make Account") || request.equals("m")){
                    System.out.println("Please Enter Name: ");
                    accountInput = true;
                }
                if(request.equals("Register with Auction Central") || request.equals("r")){
                    System.out.println("Registering with auction central...");
                    registered = true;
                    Message send = new Message();
                    //send.message = accountKey;
                    send.username = accountName;
                    send.addAgent = true;
                    toCentralServer.writeObject(send);
                    System.out.println(((Message)fromCentralServer.readObject()).getMessage());
                    //System.out.println("Options: Select House (s) / View Bidding Houses (v)");
                    printOptions();
                }
                if(request.equals("View Bidding Houses") || request.equals("v")){
                    Message send = new Message();
                    send.viewAuctionHouses = true;
                    toCentralServer.writeObject(send);
                    System.out.println(((Message)fromCentralServer.readObject()).getMessage());
                    //System.out.println("Options: Select House (s) / View Bidding Houses (v)");
                    printOptions();
                }

                if(request.equals("Place Bid") || request.equals("p")){
                    System.out.println("Select number of item to place bid on");
                    bidInput = true;
                }
                if(request.equals("Select House") || request.equals("s")){
                    System.out.println("Choose a house from the list");
                    clientInput = true;
                }
                toBankServer.flush();
                toCentralServer.flush();
            }


            System.out.print("House: NAME & AMOUNT ");

            //System.out.println("Your account number is " + accountNum);


            /** Closing all the resources */
            toBankServer.close();
            fromBankServer.close();
            toCentralServer.close();
            fromCentralServer.close();

            in.close();
            bankSocket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printOptions(){
        if(accountName.equals("")){
            System.out.println("Options: View Bidding Houses (v) / Make Account (m) ");
        }else if(!registered){
            System.out.println("Options: View Bidding Houses (v) / Register with Auction Central (r)");
        } else if(canBid){
            System.out.println("Options: View Bidding Houses (v) / Place Bid (p)");
        }else {
            System.out.println("Options: View Bidding Houses (v) / Select House (s)");
        }
    }



}
