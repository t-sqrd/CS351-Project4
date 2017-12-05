import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
/**
 * Created by BeauKujath on 02/12/2017.
 */
public class House
{

    private String userName;
    private HashMap<String, Integer> items = new HashMap<>();


    public House(String userName, HashMap<String, Integer> items)
    {
        this.userName = userName;
        this.items = items;
    }

    public String getUserName(){
        return userName;
    }

    public void addItems(HashMap<String, Integer> newItems){
        Iterator entries = newItems.entrySet().iterator();
        while (entries.hasNext()) {
            Entry thisEntry = (Map.Entry) entries.next();
            String key = (String)thisEntry.getKey();
            Integer price = (Integer)thisEntry.getValue();
            if(!items.containsKey(key)){
                items.put(key, price);
            }
        }
    }

    public Boolean placeBid(String item, Integer bid){
        if(items.containsKey(item)){
            Integer old = items.get(item);
            items.replace(item, old, bid);
            System.out.println("successfully placed new bid");
            return  true;
        }
        return false;
    }


    public String getItemString(){
        String s = "";
        s += "\nItems in " + userName + ":\n";
        int count = 1;
        Iterator entries = items.entrySet().iterator();
        while (entries.hasNext()) {
            Entry thisEntry = (Map.Entry) entries.next();
            String key = (String)thisEntry.getKey();
            Integer price = (Integer)thisEntry.getValue();
            s += count + ".) " + key + " -> " + price + "\n";
            count++;
        }
        return s;
    }
}
