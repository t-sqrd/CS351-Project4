
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by alexschmidt-gonzales on 11/30/17.
 */
public class AuctionHouses extends Thread {

    private int CENTRAL_PORT = 8081;
    private int PORT_NUMBER = 4200;
    private String host = "127.0.0.1";
    private Map<String, Double> items = new HashMap<>();
    private String list;
    private ObjectOutputStream toCentralServer;
    private ObjectInputStream fromCentralServer;
    private Socket centralSocket;
    private String houseName;


    public static String[] items1 = {"Shit , $1.00 ", "Andrews gay ass, $0.25", "MoreShit, $7.00"};

    public static void main(String[] args)
    {
        Scanner in = new Scanner(System.in);
        System.out.println("Enter Name");
        String name = in.nextLine();
        new AuctionHouses(name);
    }

    public AuctionHouses(String name) {


        this.houseName = "House " + name;

        try {


            centralSocket = new Socket(host, CENTRAL_PORT);

            toCentralServer = new ObjectOutputStream(centralSocket.getOutputStream());
            fromCentralServer = new ObjectInputStream(centralSocket.getInputStream());


        }
        catch (IOException e){

        }
        start();
    }


    public void run() {
        try {


            System.out.println("Connecting to Agent " + host + " on port " + CENTRAL_PORT + ".");

//
//            try {
//
//
//                centralSocket = new Socket(host, CENTRAL_PORT);
//
//
//                toCentralServer = new ObjectOutputStream(centralSocket.getOutputStream());
//                fromCentralServer = new ObjectInputStream(centralSocket.getInputStream());
//
//
//            } catch (UnknownHostException e) {
//                System.err.println("Unknown host: " + host);
//                System.exit(1);
//            } catch (IOException e) {
//                System.err.println("Unable to get streams from server in Auction houses");
//                System.exit(1);
//            }




            Message myName = new Message();
            myName.username = houseName.trim();
            myName.newHouse = true;
            sendMessage(myName);

            while(centralSocket.isConnected()) {
                Message request;
                while ((request = (Message) fromCentralServer.readObject()) != null) {

                    System.out.println("In loop");

                    System.out.println(request.message);
                    Message m = new Message();


//
                    if (request.getItems) {
                        System.out.println("entered");
                        Message response = new Message();
                        response.username = request.username;
                        System.out.println(response.username);
                        response.fromHouse = true;
                        response.message = arrayToString(items1);
                        sendMessage(response);

                    }
                    if (request.placeBid) {
                        Message response = new Message();
                        int d = request.index;

                        items1[request.index.intValue()] = " Bought ";
                        response.username = request.username;
                        System.out.println(d);
                        response.fromHouse = true;

                        String temp = "";
                        for(int i = 0; i < items1.length; i++){
                            temp += items1[i];
                        }
                        response.message = temp;
                        sendMessage(response);

                    }

                }
            }
            Message kill = new Message();
            kill.KILL = true;
            kill.HOUSE_LEAVING = true;
            toCentralServer.writeObject(kill);
            toCentralServer.flush();

            System.out.println("HERE IN AUCTION HOUSE");

            fromCentralServer.close();
            toCentralServer.close();
            centralSocket.close();


        }


            catch(IOException e){
                e.printStackTrace();

            }
        catch (ClassNotFoundException e){

        }
        }

    private void sendMessage(Message msg){
        try{
            toCentralServer.writeObject(msg);
            toCentralServer.flush();
        }
        catch (IOException e){

        }
    }

    private String arrayToString(String[] items){
        String temp = "";
        for(int i = 0; i < items.length; i++){
            temp += items[i];
        }
        return temp;
        }
    }








