package M3;

/*
Challenge 2: Simple Slash Command Handler
-----------------------------------------
- Accept user input as slash commands
  - "/greet <name>" → Prints "Hello, <name>!"
  - "/roll <num>d<sides>" → Roll <num> dice with <sides> and returns a single outcome as "Rolled <num>d<sides> and got <result>!"
  - "/echo <message>" → Prints the message back
  - "/quit" → Exits the program
- Commands are case-insensitive
- Print an error for unrecognized commands
- Print errors for invalid command formats (when applicable)
- Capture 3 variations of each command except "/quit"
*/

import java.util.Scanner;

public class SlashCommandHandler extends BaseClass {

    private static String ucid = "ja692"; // <-- change to your UCID

    public static void main(String[] args) {
        printHeader(ucid, 2, "Objective: Implement a simple slash command parser.");

        Scanner scanner = new Scanner(System.in);
        // UCID: ja692
        // Date: 10/09/2026
        //Plan: Needs to understand the specific slash commands, then check to see if it was used, and then put in the correct response or error from those commands. Also needs to handle invalid commands.

        // Can define any variables needed here

        while (true) {
            System.out.print("Enter command: ");
            // get entered text
            String input = scanner.nextLine().trim();

            // check if greet
            //// process greet

            if (input.toLowerCase().startsWith("/greet")){
                String[] parts = input.split ("\\s+", 2);
                if(parts.length != 2 || parts[1].isEmpty()){
                    System.out.println("Error: Please make sure use /greet <name>");
                } else {
                    String name = parts [1];
                    System.out.println("Nice to meet you, " + name  + "!");
                }
            }

            // check if roll
            //// process roll
            //// handle invalid formats
            
            else if (input.toLowerCase().startsWith("/roll")) {
                String[] parts = input.split("\\s+");

                if (parts.length !=2) {
                    System.out.println("Error: Use /roll next time: <num>d<sides>");
                }
                else {
                    try {
                        String[] dice = parts[1].toLowerCase().split("d");
                        int num = Integer.parseInt(dice[0]);
                        int sides = Integer.parseInt(dice[1]);

                        int result = 0;

                        for (int i = 0; i <num; i++) {
                            result = result + (int)(Math.random() * sides) +1; 

                         }
                         System.out.println("Rolled " + num + "d" + sides + " and got " + result + "!");
                    }
                    catch (Exception e) {
                        System.out.println("Error: Use /roll next time: <num>d<sides>");
                    }
                }    

            }
            // check if echo
            //// process echo
            else if (input.equalsIgnoreCase("/echo")) {
                System.out.println("Error: Use the /echo command");
            }

            else if (input.toLowerCase().startsWith("/echo ")){
                String message = input.substring(6).trim();
                System.out.println(message);
            }

            // check if quit
            //// process quit
            
            else if (input.equalsIgnoreCase("/quit")) {
                break;
            }

            // handle invalid commnads
            else {
                System.out.println ("Error: Did not understand command.");
            }

         
        }   // delete this condition/block, it's just here so the sample runs without edits

   
    }

}