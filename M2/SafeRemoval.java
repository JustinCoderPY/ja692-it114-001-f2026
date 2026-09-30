package M2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SafeRemoval {
    public static void main(String[] args) {
        ArrayList<String> players = new ArrayList<>(List.of("Ada", "Observer", "Lin"));
        Iterator<String> iterator = players.iterator();
        while (iterator.hasNext()) {
            String player = iterator.next();
            if (player.equals("Observer")) {
                iterator.remove();
            }
        }
        System.out.println(players);
    }
}
