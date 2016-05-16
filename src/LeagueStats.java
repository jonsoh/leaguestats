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

	private static String s_apiKey = null;
	private static Gson s_gson = new Gson();
	private static LSConfig s_config = null;
	private static LSCourtesyEngine s_courtesyEngine = new LSCourtesyEngine();
	private static LSDownloader s_downloader = null;

	private static Scanner s_scanner = null;

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
		s_apiKey = s_config.getApiKey();
		s_downloader = new LSDownloader(s_apiKey);

		// Print a welcome message
		System.out.println("Welcome to LeagueStats");
		if (s_apiKey != null)
		{
			System.out.println("Using API Key: " + s_apiKey + ". To change, use config");
		}
		else
		{
			System.out.println("No API Key specified, specify one using config");
		}
		printCommands();

		s_scanner = new Scanner(System.in);

		// Main run loop
		while (true)
		{
			String command = s_scanner.nextLine();
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
				commandConfig();
			}

			// Update
			else if (command.equalsIgnoreCase("update"))
			{
				if (commandArg == null)
				{
					printCommands();
				}
				else
				{
					commandUpdate(commandArg);
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

		s_scanner.close();
		s_scanner = null;
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

	private static void commandConfig()
	{
		s_apiKey = s_config.getApiKey();
		if (s_apiKey != null)
		{
			System.out.print("Enter new API key (currently " + s_apiKey + "): ");
		}
		else
		{
			System.out.print("Enter new API key (blank to cancel): ");
		}

		String newApiKey = s_scanner.nextLine();
		if (newApiKey.isEmpty())
		{
			System.out.println("No key entered, using existing API key");
		}
		else
		{
			s_apiKey = newApiKey;
			s_config.setApiKey(s_apiKey);
			s_downloader = new LSDownloader(s_apiKey);
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

	private static void commandUpdate(String summonerName)
	{
		// Check if we have this summoner cached, retrieve summoner ID if not
		Long summonerId = s_config.getSummonerId(summonerName);
		if (summonerId == null)
		{
			System.out.println("Summoner not cached, retrieving data");
			if (delayUntilNextAvailableRequest())
			{
				try
				{
					s_courtesyEngine.willSendRequest();
					summonerId = s_downloader.downloadSummonerId(summonerName);

					s_config.setSummonerId(summonerName, summonerId);
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
			System.out.println("Downloading match summary for summoner " + summonerId + " (" + summonerName + ")");

			if (delayUntilNextAvailableRequest())
			{
				try
				{
					s_courtesyEngine.willSendRequest();
					s_downloader.downloadMatchSummary(summonerId);

					System.out.println("Successfully retrieved match summary");
				}
				catch (LSDownloaderException e)
				{
					System.out.println(e.getMessage());
				}
			}

			System.out.println("Updating match history for summoner " + summonerId + " (" + summonerName + ")");
			// TODO: Download individual matches
		}
	}

	private static boolean delayUntilNextAvailableRequest()
	{
		long delay = s_courtesyEngine.msUntilNextAvailableRequest();
		if (delay > 0)
		{
			try
			{
				Thread.sleep(delay);
			}
			catch (InterruptedException e)
			{
				System.out.println("Error during thread sleep");
				return false;
			}
		}

		return true;
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
