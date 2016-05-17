import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class LSDownloader
{
	private static String s_endpointScheme = "https";
	private static String s_endpointHost = "na.api.pvp.net";
	private static String s_endpointSummonerPathPrefix = "/api/lol/na/v1.4/summoner/by-name/";
	private static String s_endpointMatchSummaryPathPrefix = "/api/lol/na/v2.2/matchlist/by-summoner/";
	private static String s_endpointMatchPathPrefix = "/api/lol/na/v2.2/match/";
	private static String s_apiKeyQueryPrefix = "api_key=";

	private static String s_matchSummaryFolder = "matchSummary/";
	private static String s_matchesFolder = "matches/";

	private static Gson s_gson = new Gson();

	private String m_apiKey;


	public LSDownloader(String apiKey)
	{
		m_apiKey = apiKey;
	}

	// Downloads summoner ID, throws error description if it fails
	public long downloadSummonerId(String summonerName) throws LSDownloaderException
	{
		try
		{
			URI endpointUri = new URI(s_endpointScheme,s_endpointHost, s_endpointSummonerPathPrefix + summonerName, s_apiKeyQueryPrefix + m_apiKey, null);
			URL endpointUrl = endpointUri.toURL();
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

			HashMap<String, LSSummoner> summonerMap = s_gson.fromJson(reader, new TypeToken<HashMap<String, LSSummoner>>(){}.getType());
			if (summonerMap.size() != 1)
			{
				throw new LSDownloaderException("Failed to download summoner ID: Incorrect summoner map size");
			}

			HashMap.Entry<String, LSSummoner> summonerEntry = summonerMap.entrySet().iterator().next();
			LSSummoner summoner = summonerEntry.getValue();
			
			Long summonerId = summoner.getId();
			if (summonerId == null)
			{
				throw new LSDownloaderException("Failed to download summoner ID: No ID in summoner entry");
			}

			endpointResponse.close();
			return summonerId;
		}
		catch (URISyntaxException e)
		{
			throw new LSDownloaderException("Failed to download summoner ID: URI Syntax Exception");
		}
		catch (IOException e)
		{
			throw new LSDownloaderException("Failed to download summoner ID: I/O Exception");
		}
	}

	// Downloads summoner match summary, throws error description if it fails
	public void downloadMatchSummary(Long summonerId) throws LSDownloaderException
	{
		try
		{
			URI endpointUri = new URI(s_endpointScheme, s_endpointHost, s_endpointMatchSummaryPathPrefix + summonerId, s_apiKeyQueryPrefix + m_apiKey, null);
			URL endpointUrl = endpointUri.toURL();
			HttpURLConnection endpointConnection = (HttpURLConnection)endpointUrl.openConnection();
			int responseCode = endpointConnection.getResponseCode();

			switch (responseCode)
			{
				case 200:
					break;
				case 400:
					throw new LSDownloaderException("Failed to download match summary: Bad request");
				case 401:
					throw new LSDownloaderException("Failed to download match summary: Unauthorized");
				case 404:
					throw new LSDownloaderException("Failed to download match summary: No game data found for summoner ID");
				case 224:
					throw new LSDownloaderException("Failed to download match summary: No game data since 2013 found for summoner ID");
				case 429:
					throw new LSDownloaderException("Failed to download match summary: Rate limit exceeded");
				case 500:
					throw new LSDownloaderException("Failed to download match summary: Internal server error");
				case 503:
					throw new LSDownloaderException("Failed to download match summary: Service unavailable");
				default:
					throw new LSDownloaderException("Failed to download match summary: Unknown response code");
			}

			// Create our folder if it doesn't already exist
			File matchSummaryFolder = new File(s_matchSummaryFolder);
			if (!matchSummaryFolder.exists())
			{
				System.out.println("Match summary folder does not exist, creating");
				try
				{
					matchSummaryFolder.mkdir();
				}
				catch (SecurityException e)
				{
					throw new LSDownloaderException("Failed to download match summary: Could not create match summary folder");
				}
			}

			File summonerMatchSummary = new File(s_matchSummaryFolder + summonerId + ".json");

			InputStream endpointResponse = endpointConnection.getInputStream();
			ReadableByteChannel byteChannel = Channels.newChannel(endpointResponse);
			FileOutputStream outputStream = new FileOutputStream(summonerMatchSummary);

			// Only transfers the first Long.MAX_VALUE bytes...hopefully we never need more than that
			outputStream.getChannel().transferFrom(byteChannel, 0, Long.MAX_VALUE);

			outputStream.close();
			endpointResponse.close();
		}
		catch (URISyntaxException e)
		{
			throw new LSDownloaderException("Failed to download match summary: URI Syntax Exception");
		}
		catch (IOException e)
		{
			throw new LSDownloaderException("Failed to download match summary: I/O Exception");
		}
	}
	
	public boolean matchIsCached(Long matchId)
	{
		File match = new File(s_matchesFolder + matchId + ".json");
		return match.exists();
	}

	public void downloadMatch(Long matchId) throws LSDownloaderException
	{
		try
		{
			URI endpointUri = new URI(s_endpointScheme, s_endpointHost, s_endpointMatchPathPrefix + matchId, s_apiKeyQueryPrefix + m_apiKey, null);
			URL endpointUrl = endpointUri.toURL();
			HttpURLConnection endpointConnection = (HttpURLConnection)endpointUrl.openConnection();
			int responseCode = endpointConnection.getResponseCode();

			switch (responseCode)
			{
				case 200:
					break;
				case 400:
					throw new LSDownloaderException("Failed to download match: Bad request");
				case 401:
					throw new LSDownloaderException("Failed to download match: Unauthorized");
				case 404:
					throw new LSDownloaderException("Failed to download match: Match not found");
				case 429:
					throw new LSDownloaderException("Failed to download match: Rate limit exceeded");
				case 500:
					throw new LSDownloaderException("Failed to download match: Internal server error");
				case 503:
					throw new LSDownloaderException("Failed to download match: Service unavailable");
				default:
					throw new LSDownloaderException("Failed to download match: Unknown response code");
			}

			// Create our folder if it doesn't already exist
			File matchFolder = new File(s_matchesFolder);
			if (!matchFolder.exists())
			{
				System.out.println("Match folder does not exist, creating");
				try
				{
					matchFolder.mkdir();
				}
				catch (SecurityException e)
				{
					throw new LSDownloaderException("Failed to download match: Could not create match folder");
				}
			}

			File summonerMatchSummary = new File(s_matchesFolder + matchId + ".json");

			InputStream endpointResponse = endpointConnection.getInputStream();
			ReadableByteChannel byteChannel = Channels.newChannel(endpointResponse);
			FileOutputStream outputStream = new FileOutputStream(summonerMatchSummary);

			// Only transfers the first Long.MAX_VALUE bytes...hopefully we never need more than that
			outputStream.getChannel().transferFrom(byteChannel, 0, Long.MAX_VALUE);

			outputStream.close();
			endpointResponse.close();
		}
		catch (URISyntaxException e)
		{
			throw new LSDownloaderException("Failed to download match: URI Syntax Exception");
		}
		catch (IOException e)
		{
			throw new LSDownloaderException("Failed to download match: I/O Exception");
		}
	}
}
