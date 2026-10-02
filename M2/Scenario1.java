package M2;
// copilot: disable
// @ts-nocheck

public class Scenario1 extends BaseClass {
    private static int[] array1 = {0,1,2,3,4,5,6,7,8,9};   
    private static int[] array2 = {9,8,7,6,5,4,3,2,1,0};
    private static int[] array3 = {0,0,1,1,2,2,3,3,4,4,5,5,6,6,7,7,8,8,9,9};
    private static int[] array4 = {9,9,8,8,7,7,6,6,5,5,4,4,3,3,2,2,1,1,0,0}; 
    private static void printOdds(int[] arr, int arrayNumber){
        // Only make edits between the designated "Start" and "End" comments
        printScenario1ArrayInfo(arr, arrayNumber);
        // This should be solved without Copilot auto-completion, to toggle it, click the Copilot chat bubble at the top of the editor.
        //  Configure inline suggestions to "Disabled Inline Suggestions" (or similar) when writing code for this problem.

        // Challenge 1: From each passed in array, print odd values only in a single line separated by commas and a space after each comma (should not have leading or trailing commas)
        //ja692
        //10/2/2026
        //Solution to this code: We need to create a loop through array and print only odd nums
        // I have to use % to simplify to print out those values
        // I added the first line so that I can prevent the outpouts getting pre-commas

        // Start Solution Edits
        boolean first = true;

        for (int num : arr) {
            if (num % 2 != 0) {
                if(!first) {
                    System.out.print(", ");
                }
           
            System.out.print(num);
            first = false;    
            }


           
        }
        // End Solution Edits
        System.out.println("");
        System.out.println("______________________________________");
    }
    public static void main(String[] args) {
        final String ucid = "mt85"; // <-- change to your UCID
        // no edits below this line
        printHeader(ucid, 1);
        printOdds(array1,1);
        printOdds(array2,2);
        printOdds(array3,3);
        printOdds(array4,4);
        printFooter(ucid, 1);
        
    }
}
