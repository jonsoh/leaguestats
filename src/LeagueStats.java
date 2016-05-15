import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class LeagueStats
{
	private static String configFile = "config.json";
	private static LSConfig config;
	
	public static void main(String[] args)
	{
		Gson gson = new Gson();
		
		// Load our API key from our configuration file
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
		
		String arg = "";
		if (args.length > 0)
		{
			arg = args[0];
		}
		
		if (arg.equalsIgnoreCase("config"))
		{
			String apiKey = config.getApiKey();
			if (apiKey != null)
			{
				System.out.print("Enter new API key (currently " + apiKey + "): ");
			}
			else
			{
				System.out.print("Enter new API key: ");
			}

			Scanner scanner = new Scanner(System.in);
			String newApiKey = scanner.nextLine();
			scanner.close();
			
			config.setApiKey(newApiKey);
			
			String json = gson.toJson(config);
			try
			{
				FileWriter fileWriter = new FileWriter(configFile);
				fileWriter.write(json);
				fileWriter.close();
				
				System.out.println("New API key successfully saved");
			}
			catch (IOException e)
			{
				System.err.println("Error writing configuration file");
			}
		}
		else
		{
			System.out.println("Commands");
			System.out.println("config : Enter or change API key");
		}
	}
}
