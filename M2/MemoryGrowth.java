package M2;

import java.util.ArrayList;
import java.util.List;

public class MemoryGrowth {
    public static void main(String[] args) {
        // Do not run as-is: the stopping boundary grows
        List<Integer> ints = new ArrayList<>();
        ints.add(1);

        for (int i = 0; i < ints.size(); i++) {
            System.out.println(i);
            ints.add(i);
        }

        System.out.println("Final size: " + ints.size());
    }
}
