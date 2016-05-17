import com.google.gson.annotations.SerializedName;

public class LSMatchSummary
{
	@SerializedName("matchId")
	private Long m_matchId;

	@SerializedName("timestamp")
	private Long m_timestamp;

	@SerializedName("region")
	private String m_region;

	@SerializedName("platformId")
	private String m_platformId;

	@SerializedName("season")
	private String m_season;

	@SerializedName("queue")
	private String m_queue;

	@SerializedName("champion")
	private Long m_champion;

	@SerializedName("role")
	private String m_role;

	@SerializedName("lane")
	private String m_lane;

	public Long getMatchId()
	{
		return m_matchId;
	}

	public Long getTimestamp()
	{
		return m_timestamp;
	}

	public String getRegion()
	{
		return m_region;
	}

	public String getPlatformId()
	{
		return m_platformId;
	}

	public String getSeason()
	{
		return m_season;
	}
	
	public String getQueue()
	{
		return m_queue;
	}
	
	public Long getChampion()
	{
		return m_champion;
	}
	
	public String getRole()
	{
		return m_role;
	}
	
	public String getLane()
	{
		return m_lane;
	}
}
