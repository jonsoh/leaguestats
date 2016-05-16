import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class LeagueStats
{
	private static String s_configFile = "config.json";
	
	private static Gson s_gson = new Gson();
	private static LSConfig s_config;
	private static LSCourtesyEngine s_courtesyEngine = new LSCourtesyEngine();
	private static LSDownloader s_downloader;
	
	public static void main(String[] args)
	{
		// Load our configuration file
		try
		{
			JsonReader jsonReader = new JsonReader(new FileReader(s_configFile));
			s_config = s_gson.fromJson(jsonReader, LSConfig.class);
			jsonReader.close();
		}
		catch (FileNotFoundException e)
		{
			// Just create an empty configuration object
			s_config = new LSConfig();
		}
		catch (IOException e)
		{
			// Well, we tried to close our stream...
		}

		// Set up our LSDownloader with our API key
		String apiKey = s_config.getApiKey();
		s_downloader = new LSDownloader(apiKey);
		
		// Print a welcome message
		System.out.println("Welcome to LeagueStats");
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
			String[] commandArgs = command.split("\\s+", 2);
			
			String commandArg = null;
			if (commandArgs.length > 1)
			{
				command = commandArgs[0];
				commandArg = commandArgs[1];
			}
			
			// Configuration
			if (command.equalsIgnoreCase("config"))
			{
				apiKey = s_config.getApiKey();
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
					s_config.setApiKey(newApiKey);
					s_downloader = new LSDownloader(apiKey);
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
			
			// Update Match History
			else if (command.equalsIgnoreCase("updateMatchHistory"))
			{
				if (commandArg == null)
				{
					printCommands();
				}
				else
				{
					// Check if we have this summoner cached, retrieve summoner ID if not
					Long summonerId = s_config.getSummonerId(commandArg);
					if (summonerId == null)
					{
						System.out.println("Summoner not cached, retrieving data");
						long delay = s_courtesyEngine.msUntilNextAvailableRequest();
						if (delay > 0)
						{
							try
							{
								Thread.sleep(delay);
								delay = 0;
							}
							catch (InterruptedException e)
							{
								System.out.println("Error during thread sleep");
							}
						}
						
						if (delay == 0)
						{
							try
							{
								s_courtesyEngine.willSendRequest();
								summonerId = s_downloader.downloadSummonerId(commandArg);
								
								s_config.setSummonerId(commandArg, summonerId);
								saveConfigFile();
								
								System.out.println("Successfully retrieved summoner information");
							}
							catch (LSDownloaderException e)
							{
								System.out.println(e.getMessage());
							}
						}
					}
					
					if (summonerId != null)
					{
						System.out.println("Downloading match history for summoner " + summonerId + " (" + commandArg + ")");
						
						// TODO: Download match history
						// TODO: Download individual matches
					}
				}
			}
			
			// TODO: Add command to repair (download missing matches)
			
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
		System.out.println("config                : view/set current API key");
		System.out.println("update <summonerName> : update match history for given summoner name");
		System.out.println("exit                  : exit application");
	}

	private static boolean saveConfigFile()
	{
		String json = s_gson.toJson(s_config);
		try
		{
			FileWriter fileWriter = new FileWriter(s_configFile);
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
