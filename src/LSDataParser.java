import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

public class LSDataParser
{
	private static String s_matchSummaryFolder = "matchSummary/";

	private static Gson s_gson = new Gson();

	public LinkedList<LSMatchSummary> getMatchSummariesForSummonerId(Long summonerId) throws LSDataParserException
	{
		File summonerMatchSummary = new File(s_matchSummaryFolder + summonerId + ".json");

		LinkedList<LSMatchSummary> matchSummary = null;
		try
		{
			JsonParser jsonParser = new JsonParser();
			JsonReader jsonReader = new JsonReader(new FileReader(summonerMatchSummary));
			JsonObject root = jsonParser.parse(jsonReader).getAsJsonObject();
			JsonElement matches = root.get("matches");
			matchSummary = s_gson.fromJson(matches,  new TypeToken<LinkedList<LSMatchSummary>>(){}.getType());
			jsonReader.close();
		}
		catch (FileNotFoundException e)
		{
			throw new LSDataParserException("Failed to parse summoner match summary: File not found");
		}
		catch (IOException e)
		{
			// Well, we tried to close our stream...
		}

		if (matchSummary == null)
		{
			throw new LSDataParserException("Failed to parse summoner match summary: No matches returned");
		}

		return matchSummary;
	}
}
