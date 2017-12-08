

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class TestServer extends Thread {
    public static final int PORT_NUMBER = 8080;

    public static HashMap<Integer, Map<String, Integer>> IDmap = new HashMap<>();
    public static Map<String, Integer> nameAndBid = new HashMap<>();
    public volatile boolean isConnected = false;
    private String infoMessage = "";
    //private Bank bank;

    protected Socket socket;


    private TestServer(Socket socket) {

        this.socket = socket;
        //bank = new Bank(socket);
        //AuctionCentral server = new AuctionCentral(socket);


        System.out.println("New client connected from " + socket.getInetAddress().getHostAddress());
        start();
    }


    private boolean correctInput(String str){
        String number = "";
        String name = "";
        int temp = 0;
        for(int i = 0; i < str.length(); i++){
            if(Character.isDigit(str.charAt(i))){
                number += str.charAt(i);

            }
            else{
                name += str.charAt(i);
            }
        }
        temp = Integer.parseInt(number);
        //System.out.println(temp);
        if(nameAndBid.containsKey(name)){
            System.out.println("USER ALREADY EXISTS!");
            return false;

        }
        else{

            System.out.println("User: " + name);
            System.out.println("Amount " + temp);
            nameAndBid.put(name, temp);

            return true;
        }

    }

    public int createBiddingNum() {
        Random rand = new Random();
        int biddingNum = rand.nextInt(10000);

        while (IDmap.containsKey(biddingNum)) {
            biddingNum = rand.nextInt(10000);
            System.out.println("NUMBER EXISTS! " + biddingNum);
        }

        return biddingNum;
    }


    public void run(){

        InputStream in = null;
        OutputStream out = null;

        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();

            //DataOutputStream accountInfoOut = new DataOutputStream(out);
            // DataOutputStream serverOut = new DataOutputStream(out);


            ObjectOutputStream clientOut = new ObjectOutputStream(out);

            ObjectInputStream serverIn = new ObjectInputStream(in);

            Message temp;

            while ((temp = (Message)serverIn.readObject()) != null) {
                System.out.println("ENTERED");
                System.out.println(temp.newAccount);
                if (temp.newAccount) {
                    //bank.start();

                }

                clientOut.writeObject(temp);
                clientOut.flush();





//                BufferedReader br = new BufferedReader(new InputStreamReader(in));
//
//                String request;
//                while((request = br.readLine()) != null) {
//
//
//
//
//                    System.out.println("Message received:" + request);
//
//
//                    if(request.equals("Open Account")){
//
//                        bank.start();
//                        createAccount();
//                        break;
//
//                    }
//                    else if(request.equals("Login")){
//
//                        bank.start();
//                        accountLogin();
//                        break;
//
//                    }

                //  serverOut.writeBytes(request + '\n');



//                    if (correctInput(request)) {
////                        infoMessage += biddingNum + '\n';
////                        infoMessage += "Account Created\n";
////                        IDmap.put(id, nameAndBid);
////                        System.out.println("ID: " + id);
////                        accountInfoOut.writeBytes(biddingNum);
//                    } else {
//                        infoMessage += "Account voided\n";
//                    }
//
//                    accountInfoOut.writeBytes(infoMessage);
//                    infoMessage = "";
            }

        }

        catch(ClassNotFoundException e){

        }

        catch (IOException ex) {

            System.out.println("Unable to get streams from client");
//            } finally {
//                try {
//                    in.close();
//                    out.close();
//                    socket.close();
//
//                } catch (IOException ex) {
//                    ex.printStackTrace();
//                }
//            }
        }


    }



    private void createAccount(){

        //bank = new Bank(socket);
    }


    private void accountLogin(){

        //bank = new Bank(socket);
    }


    public static void main(String[] args) {
        System.out.println("Test Server");
        ServerSocket server = null;
        try {
            server = new ServerSocket(PORT_NUMBER);
            while (true) {
                /**
                 * create a new {@link SocketServer} object for each connection
                 * this will allow multiple client connections
                 */
                new TestServer(server.accept());
            }

        } catch (IOException ex) {
            System.out.println("Unable to start server.");
        } finally {
            try {
                if (server != null)
                    server.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
