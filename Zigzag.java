import swiftbot.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.time.Duration;

public class Zigzag {
	static SwiftBotAPI swiftBot;
	static int zigzagCount = 0;
	static double largestDisplacement = 0;
	static double smallestDisplacement = Double.MAX_VALUE;
	static String largestDisplacementDetails = "";
	static String smallestDisplacementDetails = "";
	static String logFilePath = "zigzag_log.txt";
	static boolean shouldRestart = true; // Flag to control the loop

	public static void main(String[] args) {
		// Initialise SwiftBotAPI
		if (swiftBot == null) {
			try {
				swiftBot = new SwiftBotAPI();
			} catch (Exception e) {
				System.out.println("I2C disabled! Please enable I2C and try again.");
				System.exit(1);
			}

			// Enable X button to terminate the program
			swiftBot.enableButton(Button.X, () -> {
				System.out.println("X button has been pressed. Terminating program.");
				shouldRestart = false; // Exit the loop
				terminateProgram();
			});

			// Enable Y button to restart the loop
			swiftBot.enableButton(Button.Y, () -> {
				System.out.println("Y button has been pressed. Loop is restarting...");
				shouldRestart = true; // Restart the loop
				mainZigzag(); // Call the mainZigzag function to restart the journey
			});
		}

		// Start the zigzag journey
		mainZigzag();
	}

	// Function to handle the zigzag journey
	private static void mainZigzag() {
		if (shouldRestart) {
			System.out.println("Please scan a QR code to start the zigzag journey. Press the X button on the robot to terminate.");

			// Use the QRCodeDetection class to scan for a QR code
			QRCodeDetection qrScanner = new QRCodeDetection();
			String qrCode = qrScanner.detectQRCode();

			if (qrCode == null) {
				System.out.println("No QR code detected. Please try again.");
				mainZigzag(); // Restart the journey
				return;
			}

			// Process the QR code
			String[] values = qrCode.split(":");
			if (values.length != 2) {
				System.out.println("Invalid QR code format. Please scan again.");
				mainZigzag(); // Restart the journey
				return;
			}

			try {
				double value1 = Double.parseDouble(values[0]);
				int value2 = Integer.parseInt(values[1]);

				if (value1 < 15 || value1 > 85 || value2 < 2 || value2 > 12 || value2 % 2 != 0) {
					System.out.println("Invalid input values. Please ensure Value_1 is between 15 and 85, and Value_2 is an even number between 2 and 12.");
					mainZigzag(); // Restart the journey
					return;
				}

				zigzagCount++;
				double speed = Math.random() * 75 + 25;

				System.out.println("Zigzag journey started with Value_1: " + value1 + " cm, Value_2: " + value2 + ", Speed: " + speed + "%.");

				// Record start time for the first zigzag
				Instant startTime = Instant.now();

				// Perform the first zigzag and record the time taken
				long firstZigzagTime = performFirstZigzag(value1, value2, speed);

				// Perform the 180-degree turn and retrace the path
				performRetrace(value1, value2, speed);

				// Record end time and calculate total duration
				Instant endTime = Instant.now();
				Duration duration = Duration.between(startTime, endTime);
				long totalTimeSeconds = duration.getSeconds(); // Total time in seconds

				double displacement = calculateDisplacement(value1, value2);
				double distanceTraveled = value1 * value2; // Calculate distance travelled

				// Log the journey details
				logJourney(value1, value2, speed, displacement, totalTimeSeconds, firstZigzagTime);

				// Display journey details after completion
				System.out.println("Zigzag journey completed!");
				System.out.println("Distance Traveled: " + distanceTraveled + " cm");
				System.out.println("Displacement: " + displacement + " cm");
				System.out.println("Distance of each section: " + value1 + " cm");
				System.out.println("Number of sections: " + value2);
				System.out.println("Time Taken: " + totalTimeSeconds + " seconds");

				if (displacement > largestDisplacement) {
					largestDisplacement = displacement;
					largestDisplacementDetails = "Value_1: " + value1 + " cm, Value_2: " + value2 + ", Displacement: " + displacement + " cm";
				}
				if (displacement < smallestDisplacement) {
					smallestDisplacement = displacement;
					smallestDisplacementDetails = "Value_1: " + value1 + " cm, Value_2: " + value2 + ", Displacement: " + displacement + " cm";
				}

				System.out.println("Press 'Y' to scan another QR code or 'X' to terminate the program.");


				Scanner scanner = new Scanner(System.in);
				while (true) {
					if (scanner.hasNextLine()) {
						String choice = scanner.nextLine();
						if (choice.equalsIgnoreCase("X")) {
							terminateProgram();
							return; // Exit the program
						} else if (choice.equalsIgnoreCase("Y")) {
							System.out.println("Loop is restarting...");
							mainZigzag(); 
							return;
						} else {
							System.out.println("Invalid input. Press 'Y' to scan another QR code or 'X' to terminate the program.");
						}
					}
				}
			} catch (NumberFormatException e) {
				System.out.println("Invalid QR code format. Please ensure the QR code contains two numeric values separated by a colon.");
				mainZigzag(); 
			}
		}
	}

	// QRCodeDetection class to handle QR code scanning
	private static class QRCodeDetection {
		public String detectQRCode() {
			String decodedMessage = null;

			while (true) {
				try {
					BufferedImage img = swiftBot.getQRImage();
					decodedMessage = swiftBot.decodeQRImage(img);

					if (decodedMessage != null && !decodedMessage.isEmpty()) {
						System.out.println("SUCCESS: QR code found.");
						System.out.println("Decoded message: " + decodedMessage);
						break; // Exit the loop if a QR code is found
					}
				} catch (Exception e) {
					System.out.println("ERROR: Exception occurred during QR scan.");
					e.printStackTrace();
				}
				try {
					Thread.sleep(500); // Wait 500ms before retrying
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			return decodedMessage;
		}
	}

	private static double calculateDisplacement(double value1, int value2) {
		return value1 * value2 * Math.cos(Math.toRadians(45));
	}

	// Function to perform the first zigzag and record the time taken
	private static long performFirstZigzag(double value1, int value2, double speed) {
		Instant startTime = Instant.now(); // Record start time
		int duration = (int) ((value1*100)*(40/speed));
		try {
			// Perform the forward zigzag path
			for (int i = 0; i < value2; i++) {
				if (i % 2 == 0) {
					swiftBot.fillUnderlights(new int[]{0, 255, 0}); // Green
				} else {
					swiftBot.fillUnderlights(new int[]{0, 0, 255}); // Blue
				}
				swiftBot.move((int) speed, (int) speed, duration);
				swiftBot.stopMove();
				Thread.sleep(1000);

				if (i < value2 - 1) {
					if (i % 2 != 0) {
						ninteyturn("right", (int) speed, 450);
					} else {
						ninteyturn("left", (int) speed, 450);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		Instant endTime = Instant.now(); // Record end time
		return Duration.between(startTime, endTime).getSeconds(); // Return time taken in seconds
	}

	// Function to perform the 180-degree turn and retrace the path
	private static void performRetrace(double value1, int value2, double speed) {
		int duration = (int) ((value1*100)*(40/speed));
		try {
			// Perform a 180-degree turn before retracing the path
			oneEightyTurn((int) speed, 800);

			// Retrace the path
			for (int i = 0; i < value2; i++) {
				if (i % 2 == 0) {
					swiftBot.fillUnderlights(new int[]{0, 255, 0}); // Green
				} else {
					swiftBot.fillUnderlights(new int[]{0, 0, 255}); // Blue
				}
				swiftBot.move((int) speed, (int) speed, duration); 
				swiftBot.stopMove();
				Thread.sleep(1000);

				if (i < value2 - 1) {
					if (i % 2 != 0) {
						ninteyturn("left", (int) speed, 450);
					} else {
						ninteyturn("right", (int) speed, 450);
					}
				}
			}

			swiftBot.disableUnderlights();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void ninteyturn(String direction, int speed, int duration) {
		try {
			if (direction.equalsIgnoreCase("left")) {
				swiftBot.move(-70, 70, duration);
			} else if (direction.equalsIgnoreCase("right")) {
				swiftBot.move(70, -70, duration);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void oneEightyTurn(int speed, int duration) {
		try {
			swiftBot.move(70, -70, duration);
			swiftBot.stopMove();
			Thread.sleep(500);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void logJourney(double value1, int value2, double speed, double displacement, long totalTimeSeconds, long firstZigzagTime) {
		try (FileWriter writer = new FileWriter(logFilePath, true)) {
			double distanceTraveled = value1 * value2; // Calculate distance travelled
			writer.write(
					"Distance per section: " + value1 + " cm, " +
							"Sections: " + value2 + ", " +
							"Speed: " + speed + "%, " +
							"Distance Traveled: " + distanceTraveled + " cm, " +
							"Displacement: " + displacement + " cm, " +
							"Total Time: " + totalTimeSeconds + " seconds, " +
							"First Zigzag Time: " + firstZigzagTime + " seconds\n"
					);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void terminateProgram() {
		System.out.println("Terminating program...");
		System.out.println("Number of zigzag journeys completed: " + zigzagCount);
		System.out.println("Journey with the largest displacement: " + largestDisplacementDetails);
		System.out.println("Journey with the smallest displacement: " + smallestDisplacementDetails);
		System.out.println("Log file stored at: " + logFilePath);
		System.exit(0);
	}
}