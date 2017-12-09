import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class Item extends Thread {
    private String name;
    private double price;
    public boolean timerStarted, died;
    private bidTimer timer;
    private String topBidder;
    private Thread house;

    public Item(String name, double price, Thread house) {
        this.name = name;
        this.price = price;
        this.house = house;
        timer = new bidTimer(house);
    }

    public boolean placeBid(double bid, String user) {
        if (bid > price) {
            price = bid;
            if(!timerStarted) {
                timerStarted = true;
                timer.start();
                died = true;
            }
            topBidder = user;
            return true;
        } else {
            return false;
        }
    }

    public String returnName() {
        return name;
    }

    public double returnPrice() {
        return price;
    }

    public String returnBidder(){
        return topBidder;
    }

    public int returnTime() {
        if(timer != null && timer.isAlive()){
            return timer.returnTime();
        } else if(died) {
            return -5;
        } else {
            return -1;
        }
    }

    public class bidTimer extends Thread {
        private int steps = 5;
        Thread house;

        public bidTimer(Thread house) {
            this.house = house;
        }

        public int returnTime(){
            return steps;
        }

        @Override
        public void run() {
            while (steps>=0) {
                try {
                    steps--;
                    sleep(1000);
                } catch (Exception e) {

                }
            }
        }
    }
}
