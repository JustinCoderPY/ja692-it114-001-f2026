package M3;

public class StartupArguments {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java M3.StartupArguments NAME ...");
            return;
        }
        for (String name : args) {
            System.out.printf("[%d] = %s\n", i, args[i]);
        }
    }
}
