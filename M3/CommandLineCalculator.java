package M3;

/*
Challenge 1: Command-Line Calculator
------------------------------------
- Accept two numbers and an operator as command-line arguments
- Supports addition (+) and subtraction (-)
- Allow integer and floating-point numbers
- Ensures correct decimal places in output based on input (e.g., 0.1 + 0.2 → 1 decimal place)
- Display an error for invalid inputs or unsupported operators
- Capture 5 variations of tests
*/

public class CommandLineCalculator extends BaseClass {
    private static String ucid = "ja692"; // <-- change to your ucid

    public static void main(String[] args) {
        printHeader(ucid, 1, "Objective: Implement a calculator using command-line arguments.");

        if (args.length != 3) {
            System.out.println("Usage: java M3.CommandLineCalculator <num1> <operator> <num2>");
            printFooter(ucid, 1);
            return;
        }

        try {
            System.out.println("Calculating result...");
            // UCID: ja692
            // Date: 10/09/2026
            //Plan: Need to print out two numbers and an operator from the command line, then use conditional statements for addition or subtraction, and then handle  invalid input.
            String numtext1 = args[0];
            String operator = args[1];
            String numtext2 = args[2];

            // extract the equation (format is <num1> <operator> <num2>)
            double num1 = Double.parseDouble(numtext1);
            double num2 = Double.parseDouble(numtext2);

            double result = 0;

            // check if operator is addition or subtraction
            if (operator.equals("+")) {
                result = num1 + num2;
            }
            else if (operator.equals("-")){
                result = num1 - num2;
            }
            else {
                System.out.println("Error: We can't calculate that for you");
                printFooter(ucid,1);
                return;
            }
            // check the type of each number and choose appropriate parsing
            int decimal1 = 0;
            int dot1 = numtext1.indexOf(".");

            if (dot1 != -1) {
                decimal1 = numtext1.substring(dot1 +1).length();
            }


            int decimal2 = 0;
            int dot2 = numtext2.indexOf(".");

            if (dot2 != -1) {
                decimal2 = numtext2.substring(dot2 +1).length();
            }

            int decimalPlace; 

            if (decimal1 > decimal2){
                decimalPlace = decimal2;
            }
            else {
                decimalPlace = decimal2;
            }



            if (decimal1 > decimal2) {
                decimalPlace = decimal1;
            }
            else {
                decimalPlace = decimal2; 
            }
            // generate the equation result (Important: ensure decimals display as the
            // longest decimal passed)
            // i.e., 0.1 + 0.2 would show as one decimal place (0.3), 0.11 + 0.2 would shows
            // as two (0.31), etc
            System.out.print(numtext1 + " " + operator + " " + numtext2 + " = ");

            System.out.printf("%." + decimalPlace + "f%n", result);


        } catch (Exception e) {
            System.out.println("Invalid input. Please ensure correct format and valid numbers.");
        }

        printFooter(ucid, 1);
    }
}