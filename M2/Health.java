package M2;

public class Health {
    public static void main(String[] args) {
        int health = 9000;
        if (health <50) {
            System.out.println("Strong heal");
        } else if (health < 80) {
            System.out.println("Heal");
        } else {
            System.out.println("Health is full");
        }
    }
}
