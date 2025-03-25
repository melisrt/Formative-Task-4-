import java.util.Scanner;
import java.io.IOException;

public class MainMenu {
    // ANSI escape codes for colors
    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String BLUE = "\u001B[34m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";
    private static final String CYAN = "\u001B[36m";
    private static final String BOLD = "\u001B[1m";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Clear screen with ANSI escape code
        System.out.print("\033[H\033[2J");
        System.out.flush();

        System.out.println(BOLD + BLUE + "|----------------------------------|");
        System.out.println("|      SWIFTBOT TASKS SELECTOR     |");
        System.out.println("|----------------------------------|" + RESET);
        System.out.println();
        
        System.out.println(BOLD + "Available Tasks:" + RESET);
        System.out.println(CYAN + "----------------------------------" + RESET);
        
        System.out.println(GREEN + " [1]" + RESET + " Search for Light");
        System.out.println(GREEN + " [2]" + RESET + " SwiftBot Dance");
        System.out.println(GREEN + " [3]" + RESET + " Mastermind");
        System.out.println(GREEN + " [4]" + RESET + " Draw Shapes");
        System.out.println(GREEN + " [5]" + RESET + " Tunnel Vision");
        System.out.println(GREEN + " [6]" + RESET + " Detect Object");
        System.out.println(GREEN + " [7]" + RESET + " Traffic Lights");
        System.out.println(GREEN + " [8]" + RESET + " Navigate");
        System.out.println(GREEN + " [9]" + RESET + " Snakes and Ladders");
        System.out.println(GREEN + "[10]" + RESET + " Zigzag");
        System.out.println(RED + " [0]" + RESET + " Exit Program");
        
        System.out.println(CYAN + "----------------------------------" + RESET);
        System.out.print(BOLD + "Enter your choice (0-10): " + RESET);

        try {
            int choice = scanner.nextInt();
            
            if (choice == 0) {
                System.out.println(YELLOW + "\nExiting program. Goodbye!" + RESET);
                return;
            }

            System.out.println(BOLD + GREEN + "\n▶ Starting Task " + choice + "...\n" + RESET);

            try {
                switch (choice) {
                    case 1:
                        AlviSearchForLight.main(new String[]{});
                        break;
                    case 2:
                        SwiftBotDance.main(new String[]{});
                        break;
                    case 3:
                        SwiftbotMastermind.main(new String[]{});
                        break;
                    case 4:
                        Data.main(new String[]{});
                        break;
                    case 5:
                        Tunnel_Vision.main(new String[]{});
                        break;
                    case 6:
                        DetectObject.main(new String[]{});
                        break;
                    case 7:
                        MainFlow.main(new String[]{});
                        break;
                    case 8:
                        SwiftBotNavigator.main(new String[]{});
                        break;
                    case 9:
                        SnakesandLadders.main(new String[]{});
                        break;
                    case 10:
                        Zigzag.main(new String[]{});
                        break;
                    default:
                        System.out.println(RED + "Invalid choice. Please enter a number between 0 and 10." + RESET);
                        break;
                }

            } catch (InterruptedException ex) {
                System.out.println(RED + "Error: " + ex.getMessage() + RESET);
            } catch (IOException ex) {
                System.out.println(RED + "Error: " + ex.getMessage() + RESET);
            } catch (Exception ex) {
                System.out.println(RED + "Error running task: " + ex.getMessage() + RESET);
            }

        } catch (Exception e) {
            System.out.println(RED + "\nInvalid input. Please enter a number between 0 and 10." + RESET);
        } finally {
            scanner.close();
        }
    }
}
