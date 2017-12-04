import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Agent extends Thread {
    public String userName;
    private String initBid;
    private int BANK_PORT = 8080;
    private int CENTRAL_PORT = 8081;
    private String host;
    public volatile boolean bidding = false;
    private volatile boolean creatingAccount = false;
    private ArrayList<String> userInfo = new ArrayList<>();
    public String myAccountNum = "";
    private Encrypt encrypt;



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

            System.out.println("Options : Make Account / View Bidding Houses ");
            System.out.println("To return to main menu type HOME ");

            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            boolean flag = false;
            String ui;

            String request;
            Boolean accountInput = false;
            Boolean clientInput = false;

            while((request = stdin.readLine()) != null){
                System.out.println("Message received: " + request);
                if(accountInput){
                    Message send = new Message();
                    send.username = request;
                    send.newAccount = true;
                    accountInput = false;
                    toBankServer.writeObject(send);
                    System.out.println(((Message)fromBankServer.readObject()).getMessage());
                    System.out.println("\nOptions: View Bidding Houses");
                }
                if (clientInput){
                    System.out.println("client you chose from list: " + request);
                    Message send = new Message();
                    send.message = request;
                    send.selectHouse = true;
                    clientInput = false;
                    toCentralServer.writeObject(send);
                    System.out.println(((Message)fromCentralServer.readObject()).getMessage());
                    System.out.println("Options: Select House / View Bidding Houses");
                }

                if(request.equals("Make Account")){
                    System.out.println("Please Enter Name: ");
                    accountInput = true;

                }
                if(request.equals("View Bidding Houses")){
                    Message send = new Message();
                    send.viewAuctionHouses = true;
                    toCentralServer.writeObject(send);
                    System.out.println(((Message)fromCentralServer.readObject()).getMessage());
                    System.out.println("Options: Select House / View Bidding Houses");
                }

                if(request.equals("Select House")){
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




}
