import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by alexschmidt-gonzales on 11/28/17.
 */
public class AuctionCentral extends Thread {

    Socket agentSocket;
    Socket houseSocket;

    public static final int CENTER_PORT = 8081;
    public static final int HOUSE_PORT =  4200;
    public final int HOUSE_PORT2 = 4201;
    public String host = "127.0.0.1";
    private String list;
    public static int count = 0;


    public AuctionCentral(Socket socket, Socket houseSocket) {
       this.agentSocket = socket;
        this.houseSocket = houseSocket;
        start();

    }

    public void serverConnector(Socket socket){

    }

    public void run() {


        ObjectOutputStream toHouse = null;
        ObjectInputStream fromHouse = null;

        ObjectOutputStream toAgent = null;
        ObjectInputStream fromAgent = null;

        try {



            toAgent = new ObjectOutputStream(agentSocket.getOutputStream());
            fromAgent = new ObjectInputStream(agentSocket.getInputStream());

            toHouse = new ObjectOutputStream(houseSocket.getOutputStream());
            fromHouse = new ObjectInputStream(houseSocket.getInputStream());



            Message request;


            while ((request = (Message)fromAgent.readObject()) != null ) {
                System.out.println("In Auction Central Server...");

                if(request.viewAuctionHouses){
                    Message response = new Message();
                    response.askForList = true;

                    toHouse.writeObject(response);
                    response.message = ((Message) fromHouse.readObject()).message;
                    toAgent.writeObject(response);
                    toHouse.reset();

                }

            }

        }

        catch (IOException e) {

            System.out.println("Unable to get streams from client in Server 2");
        }

        catch (ClassNotFoundException e) {
        }

        finally {
            try {

                fromHouse.close();
                toHouse.close();
                fromAgent.close();
                toAgent.close();
                agentSocket.close();
                houseSocket.close();

            }
            catch (IOException ex) {

                ex.printStackTrace();
            }
        }
    }



    public static void main(String[] args) {
        System.out.println("Starting Auction Central...");
        ServerSocket fromAgent = null;
        ServerSocket fromHouse = null;
        try {
            fromAgent = new ServerSocket(CENTER_PORT);
            fromHouse = new ServerSocket(HOUSE_PORT);

            while (true) {

                new AuctionCentral(fromAgent.accept(), fromHouse.accept());
                count++;
                System.out.println(count);

            }


        }

        catch (IOException ex) {
            System.out.println("Unable to start Auction Central.");
        }
        finally {
            try {
                if (fromAgent != null) fromAgent.close();
                if(fromHouse != null) fromHouse.close();
            }

            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
