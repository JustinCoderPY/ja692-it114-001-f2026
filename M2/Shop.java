package M2;

public class Shop {
    public static void main(String[] args) {
        int wallet = 30;
        
        if (wallet >= 1) {
            System.out.println("You can get something");
        } else if (wallet >= 15) {
            System.out.println("You got the best item");
        } else {
            System.out.println("You broke");
        }
    }
}
