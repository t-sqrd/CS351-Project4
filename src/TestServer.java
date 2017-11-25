

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Random;

public class TestServer extends Thread {
    public static final int PORT_NUMBER = 8081;
    public static HashMap<String, Integer> map = new HashMap<>();
    public volatile boolean isConnected = false;

    protected Socket socket;


    private TestServer(Socket socket) {
        this.socket = socket;
        System.out.println("New client connected from " + socket.getInetAddress().getHostAddress());
        start();
    }

    private void inputData(String name, int ID){
        map.put(name, ID);
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
        if(map.containsKey(name)){
            System.out.println("USER ALREADY EXISTS!");
            return false;

        }
        else{
            System.out.println("User: " + name +", Amount = " + temp);
            map.put(name, temp);
            return true;
        }

    }

    public int createBiddingNum(){
        Random rand = new Random();
        int biddingNum = rand.nextInt(10000);
        return biddingNum;
    }

    public void run() {
        InputStream in = null;
        OutputStream out = null;

        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();

            DataOutputStream out2 = new DataOutputStream(out);

            BufferedReader br = new BufferedReader(new InputStreamReader(in));


            String request = br.readLine();


            String message = "132123";
           //while (isConnected) {

               System.out.println("Message received:" + request);
               if (correctInput(request)) {
                   message = "Account Created" + '\n';
                   out2.write(message.getBytes());
               } else {
                   message = "Account voided" + '\n';
                   out2.write(message.getBytes());
               }

               request += '\n';
               //out.write(request.getBytes());
               out2.write(message.getBytes());

           //}

        }
        catch (IOException ex) {
            System.out.println("Unable to get streams from client");
        }
        finally {
            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("SocketServer Example");
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

