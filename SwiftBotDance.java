import swiftbot.*; // Import SwiftBot library for controlling the robot
import java.util.*;
import java.io.*;
import java.awt.image.BufferedImage;

public class SwiftBotDance {
	static SwiftBotAPI swiftBot; // SwiftBot API for interacting with the robot
	static List<String> enteredHexNumbers = new ArrayList<>(); // Store all entered hexadecimal values
	static boolean buttonYPressed = false; // Flag to check if 'Y' button is pressed
	static boolean buttonXPressed = false; // Flag to check if 'X' button is pressed

	public static void main(String[] args) throws InterruptedException {
		initialise(); // Initialise SwiftBot API and set up button handlers
		boolean continuePlaying = true; // Control the main loop

		while (continuePlaying) {
			System.out.println("Please scan a QR code containing up to five hexadecimal values, or press 'X' to exit");
			String qrInput = scanQRCode(); // Scan the QR code

			// Wait for valid QR code input
			while (qrInput.isEmpty()) {
				if (buttonXPressed) {
					confirmExit(); // Confirm exit before quitting
				}
				Thread.sleep(100);
				qrInput = scanQRCode(); // Try scanning again
			}

			String[] hexNumbers = qrInput.split(":");
			if (hexNumbers.length > 5) {
				System.out.println("Too many hexadecimal values. Please scan up to five values only, or press 'X' to exit");
				continue;
			}

			List<String> validHexNumbers = new ArrayList<>();
			for (String hex : hexNumbers) {
				if (isValidHex(hex)) {
					validHexNumbers.add(hex.toUpperCase());
				} else {
					System.out.println("Invalid hexadecimal value: " + hex);
				}
			}

			if (!validHexNumbers.isEmpty()) {
				for (String hex : validHexNumbers) {
					executeDanceRoutine(hex);
				}
				enteredHexNumbers.addAll(validHexNumbers);
			} else {
				System.out.println("No valid hexadecimal values detected. Please scan again.");
				continue;
			}

			System.out.println("Press 'Y' to scan another QR code, or 'X' to exit and save data.");
			waitForButtonPress();
			if (buttonXPressed) {
				confirmExit(); // Ask user before exiting
			}
		}
	}

	// Initialise SwiftBot API and configure button handlers
	public static void initialise() {
		try {
			swiftBot = new SwiftBotAPI();
			swiftBot.enableButton(Button.Y, () -> buttonYPressed = true);
			swiftBot.enableButton(Button.X, () -> buttonXPressed = true);
		} catch (Exception e) {
			System.out.println("Initialisation failed. Exiting...");
			System.exit(1);
		}
	}

	// Wait for button press ('Y' or 'X')
	public static void waitForButtonPress() throws InterruptedException {
		buttonYPressed = false;
		buttonXPressed = false;
		while (!buttonYPressed && !buttonXPressed) {
			Thread.sleep(100);
		}
	}

	// Confirm exit when 'X' is pressed
	public static void confirmExit() throws InterruptedException {
		System.out.println("Would you like to exit the program? Yes: Y, No: X");
		buttonXPressed = false;
		buttonYPressed = false;

		while (!buttonYPressed && !buttonXPressed) {
			Thread.sleep(100);
		}

		if (buttonYPressed) {
			exitProgram();
		} else if (buttonXPressed) {
			System.out.println("Continuing program, Please scan a QR code containing up to five hexadecimal values, or press 'X' to exit");
			buttonXPressed = false; // Reset flag so the program doesn't keep repeating the prompt
		}
	}


	// Validate if the hexadecimal value is in the correct range
	public static boolean isValidHex(String hex) {
		return hex.matches("^[0-9A-Fa-f]{1,2}$");
	}

	// Convert hexadecimal to decimal
	public static int hexToDecimal(String hex) {
		int decimal = 0;
		for (char ch : hex.toUpperCase().toCharArray()) {
			decimal = decimal * 16 + (Character.isDigit(ch) ? ch - '0' : ch - 'A' + 10);
		}
		return decimal;
	}

	// Convert hexadecimal to octal
	public static int hexToOctal(String hex) {
		int decimal = hexToDecimal(hex);
		int octal = 0, factor = 1;
		while (decimal > 0) {
			octal += (decimal % 8) * factor;
			decimal /= 8;
			factor *= 10;
		}
		return octal;
	}

	// Convert decimal to binary
	public static String decimalToBinary(int decimal) {
		StringBuilder binary = new StringBuilder();
		while (decimal > 0) {
			binary.insert(0, decimal % 2);
			decimal /= 2;
		}
		return binary.length() > 0 ? binary.toString() : "0";
	}

	// Calculate speed based on octal value
	public static int calculateSpeed(String hex) {
		int octalValue = hexToOctal(hex);
		return octalValue < 50 ? octalValue + 50 : Math.min(octalValue, 100);
	}

	// Execute dance routine
	public static void executeDanceRoutine(String hex) throws InterruptedException {
		int decimal = hexToDecimal(hex);
		int speed = calculateSpeed(hex);
		int duration = hex.length() == 1 ? 1000 : 500;
		setUnderlights(hex);
		String binary = decimalToBinary(decimal);

		System.out.printf("%s, %d, %d, %s, speed = %d, LED colour (red %d, green %d, blue %d)%n",
				hex, hexToOctal(hex), decimal, binary, speed, getRed(hex), getGreen(hex), getBlue(hex));

		performDanceMovement(binary, speed, duration);
		swiftBot.fillUnderlights(new int[]{0, 0, 0});
	}

	// Perform movement based on binary value
	public static void performDanceMovement(String binary, int speed, int duration) throws InterruptedException {
		for (int i = binary.length() - 1; i >= 0; i--) {
			if (buttonXPressed) {
				confirmExit();
			}
			if (binary.charAt(i) == '1') {
				swiftBot.move(speed, speed, duration);
			} else {
				swiftBot.move(-speed, speed, duration);
			}
		}
	}

	// Set LED underlights based on hex value
	public static void setUnderlights(String hex) {
		int[] rgb = {getRed(hex), getGreen(hex), getBlue(hex)};
		swiftBot.fillUnderlights(rgb);
	}

	// Calculate red component
	public static int getRed(String hex) {
		return hexToDecimal(hex);
	}

	// Calculate green component
	public static int getGreen(String hex) {
		return (hexToDecimal(hex) % 80) * 3;
	}

	// Calculate blue component
	public static int getBlue(String hex) {
		return Math.max(getRed(hex), getGreen(hex));
	}

	// Exit the program and save data
	public static void exitProgram() {
		sortAndSaveHexNumbers();
		System.exit(0);
	}

	// Sort and save hex numbers to a file
	public static void sortAndSaveHexNumbers() {
		Collections.sort(enteredHexNumbers);
		try (FileWriter writer = new FileWriter("hex_numbers_log.txt")) {
			for (String hex : enteredHexNumbers) {
				writer.write(hex + "\n");
			}
			System.out.println("Program has been ended. Hex numbers saved to 'hex_numbers_log.txt'.");
		} catch (IOException e) {
			System.out.println("Error writing to file.");
		}
	}

	// Scan QR code and return hex values
	public static String scanQRCode() {
		try {
			BufferedImage img = swiftBot.takeStill(ImageSize.SQUARE_720x720);
			return swiftBot.decodeQRImage(img);
		} catch (Exception e) {
			return "";
		}
	}
}
