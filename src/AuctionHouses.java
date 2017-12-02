
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alexschmidt-gonzales on 11/30/17.
 */
public class AuctionHouses extends Thread {

    private int CENTRAL_PORT = 4200;
    private int PORT_NUMBER = 4200;
    private String host = "127.0.0.1";
    private Map<String, Double> items = new HashMap<>();
    private String list;

    private String[] items1 = {"Shit , $1.00 \n", "Andrews gay ass, $0.25\n", "MoreShit, $7.00\n"};

    public static void main(String[] args) {
        new AuctionHouses();
    }

    public AuctionHouses(){
        init();
    }



    private String makeList() {

        String l = "";
        for (int i = 0; i < items1.length; i++) {
            l += items1[i];
            System.out.println(l);
        }
        System.out.println(l);
        return l;
    }

    public void init() {
        try {


            System.out.println("Connecting to Agent " + host + " on port " + CENTRAL_PORT + ".");


            Socket centralSocket = null;

            ObjectOutputStream toCentralServer = null;
            ObjectInputStream fromCentralServer = null;


            try {


                centralSocket = new Socket(host, CENTRAL_PORT);

                toCentralServer = new ObjectOutputStream(centralSocket.getOutputStream());
                fromCentralServer = new ObjectInputStream(centralSocket.getInputStream());


            } catch (UnknownHostException e) {
                System.err.println("Unknown host: " + host);
                System.exit(1);
            } catch (IOException e) {
                System.err.println("Unable to get streams from server in Auction houses");
                System.exit(1);
            }


            Message request;
            while ((request = (Message) fromCentralServer.readObject()) != null) {

                System.out.println("In loop");

//
                if (request.askForList) {
                Message response = new Message();
                response.message = "yeettt";
                response.fromHouse = true;
                toCentralServer.writeObject(response);
                }
            }


                //}





                fromCentralServer.close();
                toCentralServer.close();
                centralSocket.close();




        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}







