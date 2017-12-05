import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * Created by BeauKujath on 30/11/2017.
 */
public class Items
{
    private HashMap<String, House> houses = new HashMap<String, House>(); // Maps registered id of house to object
    private static Items single = null;

    private Items()
    {
        System.out.println("creating item list");
    }

    // static method to create instance of Singleton class
    public static Items getInstance()
    {
        if (single== null)
            single = new Items();

        return single;
    }

    public String addItems(String name, HashMap<String, Integer> items){
        String result = "";
        if(hasHouse(name)){
            getHouse(name).addItems(items);
            result += "Successfully added items to " + name;
        } else {
            result += "Sorry that house does not exist.";
        }
        return result;
    }

    public String getItems(String name){
        String s = "";
        if(hasHouse(name)){
            s = getHouse(name).getItemString();
        } else {
            s = "Sorry that house does not exist.";
        }
        return s;
    }


    public void addHouse(String userName, House h){
        houses.put(userName, h);
    }


    private Boolean hasHouse(String user){
        if(houses.containsKey(user)){
            return true;
        }
        return false;
    }

    public House getHouse(String user){
        return houses.get(user);
    }

    public Boolean placeBid(String house, String item, Integer bid){
        House h = houses.get(house);
        return h.placeBid(item, bid);
    }


    public String getClientString(){
        String s = "The Houses: \n";
        Iterator entries = houses.entrySet().iterator();
        while (entries.hasNext()) {
            Entry thisEntry = (Entry) entries.next();
            String key = (String)thisEntry.getKey();
            //House value = (House)thisEntry.getValue();
            s += "- " + key + "\n";
        }
        if(houses.size() == 0 ){
            s = "No houses have been registered with Auction Central yet.\n";
        }
        return s;
    }

}
