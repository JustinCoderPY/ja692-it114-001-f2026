package M2;

public class InfiniteLoop {
    public static void main(String[] args) {
        int count = 1;
        while (count <= 3) {
            System.out.println(count);
            // Missing count++ means the condition stays true.
        }
    }
}
