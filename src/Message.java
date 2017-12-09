/**
 * Created by alexschmidt-gonzales on 11/28/17.
 */

import java.io.Serializable;

public class Message implements Serializable
{
    private static final long serialVersionUID = -5399605122490343339L;

    String username;
    boolean newAccount;
    volatile boolean KILL;
    boolean HOUSE_LEAVING;

    boolean askForList, getItems, houseList, isItems;
    boolean fromHouse, register, isOver, hasFunds;
    boolean selectHouse, WON, notification;
    boolean placeBid, verify, isMember, fromBank;
    boolean newHouse, placeHold, toUser, invalidBid;
    Integer biddingKey, bankKey, index, bidAmount, funds;
    String message, selectedHouse;
    String[] items;
    String[] houses;
    double[] prices;
    int[] timeLeft;

    int counterD = 0;
    int counterS = 0;
    int counterT = 0;


    // Adds a price to the prices array
    public void makeDoubleArray(double price)
    {
        prices[counterD] = price;
        counterD++;
    }

    // adds an item to the items array
    public void makeStringArray(String item)
    {
        items[counterS] = item;
        counterS++;
    }

    // Adds the time left to the timer array
    public void makeTimerArray(int timer)
    {
        timeLeft[counterT] = timer;
        counterT++;
    }


}
