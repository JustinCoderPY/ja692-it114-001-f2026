package M2;

public class StringIt {
    public static void main(String[] args) {
        String action = new String("heal");

        System.out.println(action == "heal");
        System.out.println(action.equals("heal"));
    }
    
    /*
     * This method adds two integers together.
     * @param a The first integer to add.
     * @param b The second integer to add.
     * @return The sum of the two integers.
     */

    public static int add (int a, int b) {
        return a + b;
    }
}
