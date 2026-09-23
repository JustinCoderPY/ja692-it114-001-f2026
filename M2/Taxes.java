package M2;

public class Taxes {
    public static void main(String[] args) {
        int taxes = 0;
        int income = 2;

        switch (income) {
            case 1:
                taxes++;
                break;
            case 2:
                taxes += 2;
                break;
            case 3:
                taxes= taxes +3;
                break;
            default:
                break;
        }
        System.out.println("You have:" + taxes);
    }
}