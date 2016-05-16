import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class LSDownloader
{
	private static String s_summonerEndpoint = "https://na.api.pvp.net/api/lol/na/v1.4/summoner/by-name/";
	private static String s_apiKeyQuery = "api_key=";

	private static Gson s_gson = new Gson();
	
	private String m_apiKey;
	
	
	public LSDownloader(String apiKey)
	{
		m_apiKey = apiKey;
	}
	
	// Downloads summoner ID, returns error description if it fails
	public long downloadSummonerId(String summonerName) throws LSDownloaderException
	{
		try
		{
			URL endpointUrl = new URL(s_summonerEndpoint + summonerName + "?" + s_apiKeyQuery + m_apiKey);
			HttpURLConnection endpointConnection = (HttpURLConnection)endpointUrl.openConnection();
			int responseCode = endpointConnection.getResponseCode();
			
			switch (responseCode)
			{
			case 200:
				break;
			case 400:
				throw new LSDownloaderException("Failed to download summoner ID: Bad request");
			case 401:
				throw new LSDownloaderException("Failed to download summoner ID: Unauthorized");
			case 404:
				throw new LSDownloaderException("Failed to download summoner ID: No summoner data found for summoner name");
			case 429:
				throw new LSDownloaderException("Failed to download summoner ID: Rate limit exceeded");
			case 500:
				throw new LSDownloaderException("Failed to download summoner ID: Internal server error");
			case 503:
				throw new LSDownloaderException("Failed to download summoner ID: Service unavailable");
			default:
				throw new LSDownloaderException("Failed to download summoner ID: Unknown response code");
			}

			InputStream endpointResponse = endpointConnection.getInputStream();
			InputStreamReader reader = new InputStreamReader(endpointResponse);

			HashMap<String, HashMap<String, Object>> summonerMap = s_gson.fromJson(reader, new TypeToken<HashMap<String, HashMap<String, Object>>>(){}.getType());
			if (summonerMap.size() != 1)
			{
				throw new LSDownloaderException("Failed to download summoner ID: Incorrect summoner map size");
			}
			
			HashMap.Entry<String, HashMap<String, Object>> summonerEntry = summonerMap.entrySet().iterator().next();
			HashMap<String, Object> summonerInfo = summonerEntry.getValue();
			
			// GSON parses this as a Double
			Double summonerId = (Double)summonerInfo.get("id");
			if (summonerId == null)
			{
				throw new LSDownloaderException("Failed to download summoner ID: No ID in summoner entry");
			}

			return summonerId.longValue();
		}
		catch (MalformedURLException e)
		{
			throw new LSDownloaderException("Failed to download summoner ID: Malformed URL Exception");
		}
		catch (IOException e)
		{
			throw new LSDownloaderException("Failed to download summoner ID: Could not open URL stream");
		}
	}
	
	// Downloads summoner match history, returns HTTP response code
	public int downloadMatchHistory(long summonerId)
	{
		return 200;
	}
}
