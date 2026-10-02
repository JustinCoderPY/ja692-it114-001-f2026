package M2;
// copilot: disable
// @ts-nocheck

public class Scenario2 extends BaseClass {
    private static double[] array1 = { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6 };
    private static double[] array2 = { 1.0000001, 1.0000002, 1.0000003, 1.0000004, 1.0000005 }; 
    private static double[] array3 = { 1.0 / 3.0, 2.0 / 3.0, 4.0 / 3.0, 8.0 / 3.0,8.0 / 3.0 }; 
    private static double[] array4 = { 1e16, 1.0, -1e16, 2.0, -2.0, 1e-16 };
    private static double[] array5 = { Math.PI, Math.E, Math.sqrt(2), Math.sqrt(3), Math.sqrt(5), Math.log(2),
            Math.log10(3) };

    private static void sumValues(double[] arr, int arrayNumber) {
        // Only make edits between the designated "Start" and "End" comments
        printScenario2ArrayInfo(arr, arrayNumber);
        // This should be solved without Copilot auto-completion, to toggle it, click the Copilot chat bubble at the top of the editor.
        //  Configure inline suggestions to "Disabled Inline Suggestions" (or similar) when writing code for this problem.

        // Challenge 1: Sum all the values of the passed in array and assign to the `total` variable
        // Challenge 2: Have the sum (total) be represented as a number with exactly 2 decimal places (similar to currency), assign to `modifiedTotal` variable
        //ja692
        //10/2/26 
        //Similar to the first one, we to create a loop and then add the value to total
        //Note: All numbers need to be rounded by 2 decimal points

        double total = 0;
        // Start Solution Edits
        // Solve Challenge 1 here

        for (double num : arr) {
            total += num;
        }
       
       
        // Solve Challenge 2 here
        Object modifiedTotal= String.format( "%.2f", total);
        
        // End Solution Edits

        printScenario2Output(total, modifiedTotal);
    }

    public static void main(String[] args) {
        final String ucid = "ja692"; // <-- change to your UCID
        // no edits below this line
        printHeader(ucid, 2);
        sumValues(array1, 1);
        sumValues(array2, 2);
        sumValues(array3, 3);
        sumValues(array4, 4);
        sumValues(array5, 5);
        printFooter(ucid, 2);

    }
}