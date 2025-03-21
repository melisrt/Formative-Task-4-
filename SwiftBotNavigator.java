package task_8_finalswiftbotnavigator;

import swiftbot.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SwiftBotNavigator {
    static SwiftBotAPI swiftBot;  
    static List<String> commandHistory = new ArrayList<>();  
    private static boolean scanAttempted = false;  // NOTE TO SELF; Flag to track QR code scan attempt

    public static void main(String[] args) throws InterruptedException {
        try {
            swiftBot = new SwiftBotAPI(); 
        } catch (Exception e) {
            System.out.println("I2C disabled! Run: sudo raspi-config nonint do_i2c 0");
            System.exit(5);
        }

        // NOTE TO SELF; Close program when X button is pressed
        swiftBot.enableButton(Button.X, () -> {
            System.out.println("Thank you for using Raabia's QR Code Navigation system, please come again! Goodbye!");
            System.exit(0);
        });

        System.out.println("\u001B[35mWelcome to Raabia's SwiftBot QR Code Navigation system... please input a QR Code!\nIf you would like to exit please press X on your SwiftBot.\u001B[0m");

        long startTime = System.currentTimeMillis();  // NOTE TO SELF; Store the start time
        boolean firstAttempt = true;  // NOTE TO SELF; Track first attempt

        while (true) {
            // NOTE TO SELF; Attempt to scan a QR code
            String command = scanQRCode();  

            if (command != null) {
                scanAttempted = true;  
                // NOTE TO SELF; Only print execution message if valid QR code 
                System.out.println("\u001B[35mExecuting: " + command + "\u001B[0m");
                executeCommands(command); 
                firstAttempt = true;  //NOTE TO SELF;  Reset first attempt flag after a successful scan
            } else if (scanAttempted && !firstAttempt) {
                // NOTE TO SELF; If QR code was not recognised and a scan attempt was made, print message
                System.out.println("\u001B[35mThe QR code has failed, please try again.\u001B[0m");
                scanAttempted = false;  // NOTE TO SELF; Reset scan attempt after showing failure message
            }

            // NOTE TO SELF; Check if 60 seconds passed since the start
            if (!firstAttempt && (System.currentTimeMillis() - startTime) >= 60000) {
                System.out.println("\u001B[35mNo QR code detected. Retrying in 60 seconds...\u001B[0m");
                Thread.sleep(60000);  // NOTE TO SELF; Pause 60 seconds before retrying
            }
            firstAttempt = false;  // NOTE TO SELF; Set the first attempt flag to false after the first attempt
        }
    }

    // NOTE TO SELF; Error handling section
    public static String scanQRCode() {
        try {
            BufferedImage img = swiftBot.getQRImage();
            if (img == null) {
                return null;  // NOTE TO SELF; Return null if no image is captured
            }

            // NOTE TO SELF; Create a copy of the image to prevent ImageIO error showing up
            BufferedImage copy = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
            copy.getGraphics().drawImage(img, 0, 0, null);

            String decodedMessage = swiftBot.decodeQRImage(copy);

            if (decodedMessage.isEmpty()) {
                return null;  // NOTE TO SELF; Return null if no QR code is detected
            }

            // NOTE TO SELF; Check for spaces between commas and values
            if (decodedMessage.contains(" ,") || decodedMessage.contains(", ")) {
                System.out.println("\u001B[35mPlease use a valid QR code (no spaces between values and commas).\u001B[0m");
                return null;
            }

            // NOTE TO SELF; Check if more than 10 commands
            String[] commands = decodedMessage.split(";");
            if (commands.length > 10) {
                System.out.println("\u001B[35mYou can't have more than 10 commands, please try again with fewer commands.\u001B[0m");
                return null;
            }

            return decodedMessage;
        } catch (Exception e) {
            System.out.println("QR Code scan failed. General error.");
            e.printStackTrace();
            return null;
        }
    }

    // NOTE TO SELF; Execute commands from QR code
    public static void executeCommands(String input) throws InterruptedException {
        String[] commands = input.split(";");
        for (String cmd : commands) {
            if (cmd.startsWith("F") || cmd.startsWith("B") || cmd.startsWith("R") || cmd.startsWith("L")) {
                String[] parts = cmd.split(",");
                if (parts.length != 3) {
                    System.out.println("\u001B[35mInvalid command format (must be direction,speed,duration): " + cmd + "\u001B[0m");
                    continue;
                }
                String direction = parts[0];
                int speed = Integer.parseInt(parts[1]);
                int duration = Integer.parseInt(parts[2]);

                if (speed > 100 || duration > 6) {
                    System.out.println("\u001B[35mInvalid speed/duration: " + cmd + "\u001B[0m");
                    continue;
                }

                moveSwiftBot(direction, speed, duration);  
                commandHistory.add(cmd);  
            } else if (cmd.startsWith("T")) {
                try {
                    int steps = Integer.parseInt(cmd.split(",")[1]);
                    retraceSteps(steps);  
                } catch (NumberFormatException e) {
                    System.out.println("\u001B[35mInvalid 'T' command format (must be T,steps): " + cmd + "\u001B[0m");
                }
            } else if (cmd.equals("W")) {
                writeLog();  
                // NOTE TO SELF; Tell the user how to access the log file
                System.out.println("\u001B[35mThe log has been saved. You can access the log text file at the location: swiftbot_log.txt\u001B[0m");
            } else {
                System.out.println("\u001B[35mUnsupported command type: " + cmd + "\u001B[0m");
            }
        }
    }

    // NOTE TO SELF; Move SwiftBot
    public static void moveSwiftBot(String direction, int speed, int duration) throws InterruptedException {
        switch (direction) {
            case "F":
                swiftBot.move(speed, speed, duration * 1000);
                break;
            case "B":
                swiftBot.move(-speed, -speed, duration * 1000);
                break;
            case "R":
                swiftBot.move(speed, -speed, duration * 1000);
                break;
            case "L":
                swiftBot.move(-speed, speed, duration * 1000);
                break;
        }
    }

    //NOTE TO SELF; Retrace the last steps number of commands
    public static void retraceSteps(int steps) throws InterruptedException {
        if (steps > commandHistory.size()) {
            System.out.println("\u001B[35mCannot retrace " + steps + " steps. Only " + commandHistory.size() + " available.\u001B[0m");
            return;
        }
        for (int i = commandHistory.size() - steps; i < commandHistory.size(); i++) {
            String cmd = commandHistory.get(i);
            if (cmd.startsWith("F") || cmd.startsWith("B") || cmd.startsWith("R") || cmd.startsWith("L")) {
                String[] parts = cmd.split(",");
                String direction = parts[0];
                int speed = Integer.parseInt(parts[1]);
                int duration = Integer.parseInt(parts[2]);
                moveSwiftBot(direction, speed, duration);
            }
        }
    }

    // NOTE TO SELF; Write the command history to log file
    public static void writeLog() {
        try {
            String filePath = "swiftbot_log.txt";
            FileWriter writer = new FileWriter(filePath, true); 
            writer.write("Commands executed: \n");
            for (String cmd : commandHistory) {
                writer.write(cmd + "\n");
            }
            String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            writer.write("Logged at: " + currentTime + "\n\n");
            writer.close();
        } catch (IOException e) {
            System.out.println("Error writing to log file.");
            e.printStackTrace();
        }
    }
}










