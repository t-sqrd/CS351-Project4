import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Agent extends Thread{
    public String userName;
    private String initBid;
    String message;
    private int port;
    private String host;
    public volatile boolean bidding = false;


    public static void main(String args[]) {
        //String host = "129.24.112.247"; //work IP
        String host = "127.0.0.1";

        int port = 8081;
        Agent test = new Agent(host, port, "1110", "Alex");

        test.createAccount();
        test.bidding = true;
        test.notify();




    }

    public Agent(String host, int port, String initBid, String userName) {
        userName += initBid;
        this.initBid = initBid;
        this.userName = userName;
        this.host = host;
        this.port = port;
        //start();

    }
    public double placeBid(double bid){





        return 0;
    }
    public void run() {

        while (true) {
            try {
                if (!bidding) {
                    wait();
                }

            } catch (InterruptedException e) {

            }


        }

    }

    public void createAccount() {
        try {
            //String serverHostname = new String("127.0.0.1");
            //String serverHostname = new String("129.24.112.247");

            System.out.println("Connecting to host " + host + " on port " + port + ".");
            Socket echoSocket = null;
            Reader tempIn = new StringReader(userName);
            BufferedReader clientIn = new BufferedReader(tempIn);

            DataOutputStream serverOut = null;
            //OutputStream serverOut = null;
            BufferedReader serverIn = null;
            // DataInputStream serverIn = null;


            try {
                echoSocket = new Socket(host, 8081);


                serverOut = new DataOutputStream(echoSocket.getOutputStream());


                serverIn = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
                //serverIn = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
                //echoSocket = new Socket(serverHostname, 8081);

                //out = new PrintWriter(echoSocket.getOutputStream(), true);

                //serverOut = new DataOutputStream(echoSocket.getOutputStream());
                //in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
                //serverIn = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));


            } catch (UnknownHostException e) {
                System.err.println("Unknown host: " + host);
                System.exit(1);
            } catch (IOException e) {
                System.err.println("Unable to get streams from server");
                System.exit(1);
            }

            /** {@link UnknownHost} object used to read from console */
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));


            //System.out.print("client: ");
            System.out.print("Client: NAME & AMOUNT ");
            //String userInput = stdIn.readLine();
            /** Exit on 'q' char sent */
//                if ("q".equals(clientIn.readLine())) {
//                    //break;
//                }

            String message = clientIn.readLine();
            serverOut.writeBytes(message + '\n');


            String result = serverIn.readLine();


            System.out.println("server: " + result);


            /** Closing all the resources */
//            serverOut.close();
//            serverIn.close();
//            clientIn.close();
//            echoSocket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
