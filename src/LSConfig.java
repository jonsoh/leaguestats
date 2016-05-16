import java.util.HashMap;

import com.google.gson.annotations.SerializedName;

public class LSConfig {
	@SerializedName("apiKey")
	private String m_apiKey;
	@SerializedName("summoners")
	private HashMap<String, Long> m_summoners = new HashMap<String, Long>();
	
	String getApiKey()
	{
		return m_apiKey;
	}
	
	void setApiKey(String newApiKey)
	{
		m_apiKey = newApiKey;
	}
	
	Long getSummonerId(String summonerName)
	{
		return m_summoners.get(summonerName);
	}
	
	void setSummonerId(String summonerName, Long summonerId)
	{
		Long existingSummonerId = m_summoners.putIfAbsent(summonerName, summonerId);
		assert(existingSummonerId == null);
	}
}
