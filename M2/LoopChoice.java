package M2;

public class LoopChoice {
    public static void main(String[] args) {
        String[] players = { "Ada", "Observer", "Lin" };

        // Indexed for loop: count three rounds
        for (int round = 1; round <= 3; round++) {
            // Enhanced for loop: visit every player
            for (String player : players) {
                if (player.equals("Observer")) {
                    continue;
                }
                System.out.println("Round " + round + ": " + player);
            }
        }
    }
}
