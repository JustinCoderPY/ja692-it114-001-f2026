package M2;

public class Precision {
    public static void main(String[] args) {
        float total = 0.1f + 0.2f;
        System.out.println(total);
        System.out.println(total >= 0.3f);
        total = 0.2f + 0.2f;
        System.out.println(total);
        System.out.println(total == 0.4f);
    }
}
