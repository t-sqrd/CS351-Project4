
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Created by alexschmidt-gonzales on 11/30/17.
 */
public class AuctionHouses extends Thread {

    private int CENTRAL_PORT = 8081;
    private int PORT_NUMBER = 4200;
    private String host = "127.0.0.1";
    private static String[] items = new String[1000];
    private static double[] prices = new double[1000];
    private static boolean read;
    private ObjectOutputStream toCentralServer;
    private ObjectInputStream fromCentralServer;
    private Socket centralSocket;
    private String houseName;
    private BufferedReader reader;
    private static String[] myItems = new String[3];
    private static double[] myPrices = new double[3];

    public static void main(String[] args) {
        new AuctionHouses();
    }

    private void addItems() {
        reader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("itemPack/items.txt")));
        try {
            String str;
            double dbl;
            int counter = 0;
            while ((str = reader.readLine()) != null) {
                String[] vals = str.split(";");
                str = vals[0];
                dbl = Double.parseDouble(vals[1]);
                items[counter] = str;
                prices[counter] = dbl;
                counter++;
            }
            Random rand = new Random();
            for (int i = 0; i < myItems.length; i++) {
                int z = rand.nextInt(counter);
                myItems[i] = items[z];
                myPrices[i] = prices[z];
            }
        } catch (IOException e) {
            System.out.println("File not found.");
        }
    }

    public AuctionHouses() {

        System.out.println("Connecting to Agent " + host + " on port " + CENTRAL_PORT + ".");

        if (!read) {
            addItems();
            read = true;
        }

        try {


            centralSocket = new Socket(host, CENTRAL_PORT);

            this.houseName = "House #" + centralSocket.getLocalPort();

            toCentralServer = new ObjectOutputStream(centralSocket.getOutputStream());
            fromCentralServer = new ObjectInputStream(centralSocket.getInputStream());


        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + host);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Unable to get streams from server in Auction houses");
            System.exit(1);
        }
        start();

    }

    public void run() {
        try {


            System.out.println("Connecting to Agent " + host + " on port " + CENTRAL_PORT + ".");

            Message myName = new Message();
            myName.username = houseName.trim();
            myName.newHouse = true;
            sendMessage(myName);
            while (centralSocket.isConnected()) {
                Message request, response;
                while ((request = (Message) fromCentralServer.readObject()) != null) {
//
                    if (request.getItems) {
                        response = new Message();
                        response.username = request.username;
                        response.fromHouse = true;
                        response.isItems = true;
                        response.items = new String[3];
                        for (int i = 0; i < 3; i++) {
                            response.makeStringArray(myItems[i]);
                        }
                        response.prices = new double[3];
                        for (int i = 0; i < 3; i++) {
                            response.makeDoubleArray(myPrices[i]);
                        }
                        sendMessage(response);
                    }
                    if (request.placeBid) {
                        response = new Message();
                        response.username = request.username;
                        response.fromHouse = true;
                        response.placeBid = true;
                        if (myPrices[request.index] < request.bidAmount) {
                            myPrices[request.index] = request.bidAmount;
                        } else {
                            response.invalidBid = true;
                        }
                        response.items = new String[3];
                        for (int i = 0; i < 3; i++) {
                            response.makeStringArray(myItems[i]);
                        }
                        response.prices = new double[3];
                        for (int i = 0; i < 3; i++) {
                            response.makeDoubleArray(myPrices[i]);
                        }
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


        } catch (Exception e) {
            e.printStackTrace();

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
}







