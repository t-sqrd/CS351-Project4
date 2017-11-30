import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by alexschmidt-gonzales on 11/28/17.
 */
public class Server2 extends Thread {
    Socket socket;

    public static final int PORT_NUMBER = 8081;

    public Server2(Socket socket) {
        this.socket = socket;
        start();

    }



    public void run() {
        InputStream in = null;
        OutputStream out = null;

        ObjectOutputStream toBank = null;
        ObjectInputStream fromBank = null;

        try {

            in = socket.getInputStream();
            out = socket.getOutputStream();

            toBank = new ObjectOutputStream(out);
            fromBank = new ObjectInputStream(in);

            Message request;
            System.out.println("Entered");
            //input = (Message) agentInfo.readObject();
            Message response;
           while ((request = (Message)fromBank.readObject()) != null) {

               if(request.HOME) break;
               if(request.viewAuctionHouses){
                   response = new Message();
                   response.message = "Fuck off...";
                   toBank.writeObject(response);
                   toBank.flush();
               }

            }

            System.out.println("Exited");
        }



        catch (IOException e) {

            System.out.println("Unable to get streams from client in Server 2");
        }

        catch (ClassNotFoundException e) {
        }

        finally {
            try {

                fromBank.close();
                toBank.close();
                in.close();
                out.close();
                socket.close();

            }
            catch (IOException ex) {

                ex.printStackTrace();
            }
        }
    }



    public static void main(String[] args) {
        System.out.println("Starting Server2");
        ServerSocket server = null;
        try {
            server = new ServerSocket(PORT_NUMBER);
          //  while (true) {
                /**
                 * create a new {@link SocketServer} object for each connection
                 * this will allow multiple client connections
                 */
                new Server2(server.accept());
           // }

        } catch (IOException ex) {
            System.out.println("Unable to start Server2.");
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
