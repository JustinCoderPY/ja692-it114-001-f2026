package M2;

public class LoopShapes {
    public static void main(String[] args) {
        // while loop: check before each iteration
        int round = 1;
        while (round <= 3) {
            System.out.println(round);
            round++;
        }
        // do-while loop: check after one iteration
        int attempt = 1;
        do {
            System.out.println(attempt);
            attempt++;
        } while (attempt <= 3);
        // indexed for loop: count known turns
        for (int turn = 1; turn <= 3; turn++) {
            System.out.println(turn);
        }
        // enhanced for (foreach) loop: visit each player
        String[] players = { "Ada", "Lin" };
        for (String player : players) {
            System.out.println(player);
        }
    }
}
