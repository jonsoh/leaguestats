import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class LeagueStats
{
	private static Gson gson = new Gson();
	private static String configFile = "config.json";
	private static LSConfig config;
	
	public static void main(String[] args)
	{
		// Load our configuration file
		try
		{
			JsonReader jsonReader = new JsonReader(new FileReader(configFile));
			config = gson.fromJson(jsonReader, LSConfig.class);
			jsonReader.close();
		}
		catch (FileNotFoundException e)
		{
			// Just create an empty configuration object
			config = new LSConfig();
		}
		catch (IOException e)
		{
			// Well, we tried to close our stream...
		}
		
		// Print a welcome message
		System.out.println("Welcome to LeagueStats");
		String apiKey = config.getApiKey();
		if (apiKey != null)
		{
			System.out.println("Using API Key: " + apiKey + ". To change, use config");
		}
		else
		{
			System.out.println("No API Key specified, specify one using config");
		}
		printCommands();

		Scanner scanner = new Scanner(System.in);
		
		// Main run loop
		while (true)
		{
			String command = scanner.nextLine();
			
			// Configuration
			if (command.equalsIgnoreCase("config"))
			{
				apiKey = config.getApiKey();
				if (apiKey != null)
				{
					System.out.print("Enter new API key (currently " + apiKey + "): ");
				}
				else
				{
					System.out.print("Enter new API key (blank to cancel): ");
				}
				
				String newApiKey = scanner.nextLine();
				if (newApiKey.isEmpty())
				{
					System.out.println("No key entered, using existing API key");
				}
				else
				{
					config.setApiKey(newApiKey);
					boolean success = saveConfigFile();
					if (success)
					{
						System.out.println("New API key successfully saved");
					}
					else
					{
						System.out.println("Error writing configuration file");
					}
				}
			}
			
			// Exit
			else if (command.equalsIgnoreCase("exit"))
			{
				break;
			}
			
			// Unrecognized command
			else
			{
				printCommands();
			}
		}
		
		scanner.close();
	}

	// Helpers
	
	private static void printCommands()
	{
		System.out.println("Commands");
		System.out.println("--------");
		System.out.println("config : view/set current API key");
		System.out.println("exit   : exit application");
	}

	private static boolean saveConfigFile()
	{
		String json = gson.toJson(config);
		try
		{
			FileWriter fileWriter = new FileWriter(configFile);
			fileWriter.write(json);
			fileWriter.close();
		}
		catch (IOException e)
		{
			return false;
		}
		
		return true;
	}
}
