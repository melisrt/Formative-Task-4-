
import swiftbot.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;

import javax.imageio.ImageIO;

import java.util.ArrayList;
import java.util.List;

import java.io.IOException;

public class Tunnel_Vision {
	static SwiftBotAPI swiftBot;
	
	private static boolean ypressed = false;
	private static boolean xpressed = false;
	private static boolean apressed = false;
	private static boolean bpressed = false;
	private static long executionStartTime = System.currentTimeMillis();
	private static boolean obstacleDetected = false;
	private static List<Double> tunnelLengths = new ArrayList<>();
    private static List<Double> tunnelIntensity = new ArrayList<>();
    private static List<Double> tunnelDistances = new ArrayList<>();
    private static int tunnelcount = 0; 
    private static long entrytime = 0;        
    private static double previousIntensity = -1;
	private static long programStartTime = System.currentTimeMillis();
	private static long prevExitTime = 0;
	private static double totalTunnelIntensity = 0; // Sum of all intensity values inside a tunnel
	private static int intensityCount = 0; 
	private static double totalDistanceTraveled = 0;
	private static boolean programRunning = false;
	private static boolean inTunnel = false;  
    private static double totalTime = 0;

    private static long movementStartTime = 0;

	private static double intensityThreshold = 10.0; 
	private static int stableThreshold = 3; 


	private static int stableCount = 0;
	
	
	
	public static void main(String[] args) throws InterruptedException, IOException {
		
		try {
			swiftBot = new SwiftBotAPI();
		} catch (Exception e) {
		
			System.out.println("SwiftBot disabled!");
			System.exit(5);
		}
		
		//Displays the initial message to the user and instructs them on what to do
		System.out.println("");
		System.out.println("*******************************************");
		System.out.println("*******************************************");
		System.out.println("");
		System.out.println("Welcome to Tunnel Vision!!!");
		System.out.println("");
		System.out.println("Place the Robot in front of the first Tunnel.");
		System.out.println("");
		System.out.println("Press 'Y' to start the program...");
		System.out.println("");
		System.out.println("*******************************************");
		System.out.println("*******************************************");
		System.out.println("");
		
		buttonPress(swiftBot);
	}
		
	public static void buttonPress(SwiftBotAPI api) {
		
		swiftBot.disableAllButtons();
		
		swiftBot.enableButton(Button.Y,() -> {
			System.out.println( "Y Pressed! Starting program...");
    		ypressed = false;
            programRunning = true;
    		runProgram();
            executionStartTime = System.currentTimeMillis();
			});
		
		swiftBot.enableButton(Button.X,() -> {
				System.out.println("X Pressed...Exiting...");
				terminateProgram(swiftBot);
			});
		
		swiftBot.enableButton(Button.A,() -> {
				System.out.println("Error Invalid Input! An error has occured. That is not a valid input.");
				System.out.println("Press 'Y' to start the program...");
				buttonPress(swiftBot);
			
			});
		swiftBot.enableButton(Button.B,() -> {
			System.out.println("Error Invalid Input! An error has occured. That is not a valid input.");
			System.out.println("Press 'Y' to start the program...");
			buttonPress(swiftBot);
		
		});
	    
	}
	
	
	
	public static void runProgram() {

		// Sets the under lights to red
		swiftBot.fillUnderlights(new int[] { 255, 0, 0 });
        movementStartTime = System.currentTimeMillis();

		//Need to disable button Y so that you can press button X to exit.
		swiftBot.disableAllButtons();
		swiftBot.enableButton(Button.X, () -> {xpressed = true;
		System.out.println("X Pressed...Exiting...");
		terminateProgram(swiftBot);});

		
		try {
            if(programRunning == true) {
                int	leftWheelVelocity = 50;
                int rightWheelVelocity = 50;
                
                swiftBot.startMove(leftWheelVelocity, rightWheelVelocity);
                
                

               
               
            }
			//Initializes the velocity of both wheels (the swiftbot will move at this speed)
            int imagecount = 0;
			
			while (programRunning) {
                double distanceTemp = (System.currentTimeMillis() - movementStartTime) / 1000.0;
                totalDistanceTraveled = distanceTemp * 23;

			double distance = swiftBot.useUltrasound();
			
			if (distance <= 40.0) {
			System.out.println("Stopping. An obstacle is detecled at " + distance + " cm.");
			obstacleDetected = true;
			swiftBot.stopMove();
            totalTime = (System.currentTimeMillis() - executionStartTime) / 1000.0;
			
			BufferedImage objectimage = swiftBot.takeStill(ImageSize.SQUARE_240x240);

			if (objectimage == null) {
				System.out.println("ERROR: Object_Image is null");
				System.exit(5);
			} else {
				
				ImageIO.write(objectimage, "png", new File("/data/home/pi/images/objectImage.png"));

				System.out.println("Image of object is captured ");
			
			}
			
			
			
			terminateProgram(swiftBot);
			break;
			
		}
		
			BufferedImage image = swiftBot.takeStill(ImageSize.SQUARE_240x240);
            Thread.sleep(1000);

			if (image == null) {
				System.out.println("ERROR: Image is null");
				System.exit(5);
			} else {
				
				String filename = "Image." + imagecount + ".png";
				
				ImageIO.write(image, "png", new File("/data/home/pi/images/" + filename));

			
			 
	            
	        processImage(image, imagecount);

	
				
				imagecount++;
			}
				
		} 

	}
	
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("ERROR: Error while moving");
			System.exit(5);
		}
		

	} 
	
	
	public static void processImage(BufferedImage image, int imagecount) {
		
	try {
		File imageFile = new File("/data/home/pi/images/Image." + imagecount + ".png");
        image = ImageIO.read(imageFile);

           
           int width = image.getWidth();
           int height = image.getHeight();

           double totalIntensity = 0;
           int pixelCount = width * height;
           double maxIntensity = 0;
           double minIntensity = 255;
           
           int[][] pixelMatrix = new int[height][width];

         
           for (int y = 0; y < height; y++) {
               for (int x = 0; x < width; x++) {
                   pixelMatrix[y][x] = image.getRGB(x, y); 
                   int pixel = image.getRGB(x, y);
                   
                   int red = (pixel >> 16) & 0xFF;
                   int green = (pixel >> 8) & 0xFF;
                   int blue = pixel & 0xFF;

                 
                   double intensity = 0.299 * red + 0.587 * green + 0.114 * blue;
               
                   if (intensity > maxIntensity) maxIntensity = intensity;
                   if (intensity < minIntensity) minIntensity = intensity;

                   // Sum for average calculation
                   totalIntensity += intensity;
                   
                  
               }
           }
           
           double averageIntensity = totalIntensity / pixelCount;
          
           
         
    
           calculateDistance(swiftBot, averageIntensity);
           
	}catch (IOException e) {
        System.out.println("Error loading image: " + e.getMessage());
    }
	

}
	
			 
	public static void calculateDistance(SwiftBotAPI api, double averageIntensity) {
		
	
	
        int speed = 23;
  
        
        
      
     
    
        
        if(!inTunnel && averageIntensity <= previousIntensity * 0.5){
        	inTunnel = true;
        	entrytime = System.currentTimeMillis();
        	totalTunnelIntensity = 0;
            intensityCount = 0; 
            
            tunnelcount++;
            
            System.out.println("Entered Tunnel " + tunnelcount);
        }
            
        	if (inTunnel) {
        	    totalTunnelIntensity += averageIntensity;
        	    intensityCount++;
        	}
        	
        	

            
           
         
         
        if (inTunnel && averageIntensity > 100) {
            inTunnel = false;
        	 long exitTime = System.currentTimeMillis();
             double timeInside = (exitTime - entrytime) / 1000.0; 
             double tunnelDistance = speed * timeInside;
        	double averageTunnelIntensity = totalTunnelIntensity / intensityCount;
        	tunnelLengths.add(tunnelDistance);
        	tunnelIntensity.add(averageTunnelIntensity);
        	
       System.out.println("Exited Tunnel " + (tunnelcount));

  
       if (prevExitTime != 0) {  
           
    	   double timeBetweenTunnels = (entrytime - prevExitTime) / 1000.0;
           double distanceBetweenTunnels = speed * timeBetweenTunnels;
           tunnelDistances.add(distanceBetweenTunnels);
          
           
        }
       
      prevExitTime = exitTime;
        
        } 
        
        if (previousIntensity != -1) {  
            // Calculate the absolute difference between the current and previous intensity
            double change = Math.abs(averageIntensity - previousIntensity);

            // If the change is smaller than the threshold, count it as stable
            if (change < intensityThreshold) {
                if(inTunnel == false){
                    stableCount++;
                    System.out.println("stable count:" + stableCount);
                }

            } else {
                stableCount = 0; // Reset if a significant change occurs
            }

            // If the intensity hasn't changed significantly for `stableThreshold` number of images, terminate
            if (stableCount >= stableThreshold) {
                System.out.println("No significant light change detected for " + stableThreshold + " images. Terminating program.");
                terminateProgram(swiftBot);
                return;
            }
        }

        previousIntensity = averageIntensity;
        return;
	}
	
	public static void terminateProgram(SwiftBotAPI api) {
		
		programRunning = false;
		swiftBot.stopMove();
        totalTime = (System.currentTimeMillis() - executionStartTime) / 1000.0;
        System.currentTimeMillis();
		swiftBot.disableUnderlights();
		System.out.println( "Program is being terminated. ");
		System.out.println("");
		System.out.println( "Do you want to view the log? Press 'Y' for yes or 'X' for no. ");
		
		
		swiftBot.disableAllButtons();
		
		swiftBot.enableButton(Button.Y,() -> {
			System.out.println("Button Y Pressed");
		logYexecution(swiftBot);
			});
		
		swiftBot.enableButton(Button.X, () -> {
		 	  System.out.println("Button X Pressed");
		 	 logXexecution(swiftBot);
		     });
		
		swiftBot.enableButton(Button.A, () -> {
		 	  System.out.println("Error! Invalid input. Button A Pressed");
		 	  terminateProgram(swiftBot);
		     });
		
		swiftBot.enableButton(Button.B, () -> {
		 	  System.out.println("Error! Invalid input. Button B Pressed");
		 	  terminateProgram(swiftBot);
		     });
	}
	
	public static void logXexecution(SwiftBotAPI api) {
		long endTime = System.currentTimeMillis();
       
		System.out.println("");
		System.out.println("Log of Tunnel Vision:");
		System.out.println("");
		
        try (FileWriter writer = new FileWriter("/data/home/pi/log.txt")) {
        	
        	//number of tunnels
        	writer.write("Number of Tunnels Passed: " + tunnelcount + "\n");
     
        	
        	//Length of Each tunnel
            for (int i = 0; i < tunnelcount; i++) {
            	writer.write("Tunnel " + (i+1) + " Length: " + tunnelLengths.get(i) + " cm\n");
         
            }
            
           // Average Intensity in each tunnel
            for (int i = 0; i < tunnelcount; i++) {
            	writer.write("Average Intensity in Tunnel " + (i+1) + ": " + tunnelIntensity.get(i) + "\n");
             
            }
            
            //Distance between each tunnel
            for (int i = 0; i < tunnelcount; i++) {
                if (i < tunnelDistances.size()) {
                	writer.write("Distance between Tunnel " + (i+1) + " and Tunnel " + (i + 2) + ": " + tunnelDistances.get(i) + " cm\n");
                   
                }
            }
            
            //Total distance traveled
            writer.write("Total Distance Traveled: " + totalDistanceTraveled + " cm\n");
        
        	
            //Execution time
            writer.write("Total Execution Time: " + totalTime + " seconds\n");
            System.out.println("Total Execution Time: " + totalTime + " seconds\n");
             
            //Obstacle detected and its saved file
             writer.write("Obstacle Detected: " + (obstacleDetected ? "Yes" : "No") + "\n");
             writer.write("Obstacle Image saved at: /data/home/pi/images/objectImage.png" + "\n");
            
             
             //Log path
             writer.write("Log saved at /data/home/pi/log.txt" + "\n");
             System.out.println("Log saved at /data/home/pi/log.txt" + "\n");
             
        
        } catch (IOException e) {
            System.out.println("Error saving log: " + e.getMessage());
        }
        System.exit(5);
       
	}
	
	
	public static void logYexecution(SwiftBotAPI api) {
		
		System.out.println("");
		System.out.println("Log of Tunnel Vision:");
		System.out.println("");
		
		long endTime = System.currentTimeMillis();

        try (FileWriter writer = new FileWriter("/data/home/pi/log.txt")) {
        	
        	//number of tunnels
        	writer.write("Number of Tunnels Passed: " + tunnelcount + "\n");
        	System.out.println("Number of Tunnels Passed: " + tunnelcount + "\n");
        	
        	//Length of Each tunnel
            for (int i = 0; i < tunnelcount; i++) {
            	writer.write("Tunnel " + (i+1) + " Length: " + tunnelLengths.get(i) + " cm\n");
            	System.out.println("Tunnel " + (i+1) + " Length: " + tunnelLengths.get(i) + " cm\n");
            }
            
           // Average Intensity in each tunnel
            for (int i = 0; i < tunnelcount; i++) {
            	writer.write("Average Intensity in Tunnel " + (i+1) + ": " + tunnelIntensity.get(i) + "\n");
                System.out.println("Average Intensity in Tunnel " + (i+1) + ": " + tunnelIntensity.get(i) + "\n");
            }
            
            //Distance between each tunnel
            for (int i = 0; i < tunnelcount; i++) {
                if (i < tunnelDistances.size()) {
                	writer.write("Distance between Tunnel " + (i+1) + " and Tunnel " + (i + 2) + ": " + tunnelDistances.get(i) + " cm\n");
                    System.out.println("Distance between Tunnel " + (i+1) + " and Tunnel " + (i + 2) + ": " + tunnelDistances.get(i) + " cm\n");
                }
            }
            
            //Total distance traveled
            writer.write("Total Distance Traveled: " + totalDistanceTraveled + " cm\n");
            System.out.println("Total Distance Traveled: " + totalDistanceTraveled + " cm\n");
        	
            //Execution time
            writer.write("Total Execution Time: " + totalTime + " seconds\n");
            System.out.println("Total Execution Time: " + totalTime + " seconds\n");
             
            //Obstacle detected and its saved file
             writer.write("Obstacle Detected: " + (obstacleDetected ? "Yes" : "No") + "\n");
             writer.write("Obstacle Image saved at: /data/home/pi/images/objectImage.png" + "\n");
             System.out.println("Obstacle Detected: " + (obstacleDetected ? "Yes" : "No") + "\n");
             System.out.println("Obstacle Image saved at: /data/home/pi/images/objectImage.png" + "\n");
             
             //Log path
             writer.write("Log saved at /data/home/pi/log.txt" + "\n");
             System.out.println("Log saved at /data/home/pi/log.txt" + "\n");
             
        
        } catch (IOException e) {
            System.out.println("Error saving log: " + e.getMessage());
        }
        
        System.exit(5);
    }

	
}
	









