import com.google.gson.annotations.SerializedName;

public class LSSummoner
{
	@SerializedName("id")
	private Long m_id;

	@SerializedName("name")
	private String m_name;

	@SerializedName("profileIconId")
	private Integer m_profileIconId;

	@SerializedName("revisionDate")
	private Long m_revisionDate;

	@SerializedName("summonerLevel")
	private Long m_summonerLevel;

	public Long getId()
	{
		return m_id;
	}
	
	public String getName()
	{
		return m_name;
	}
	
	public Integer getProfileIconId()
	{
		return m_profileIconId;
	}
	
	public Long getRevisionDate()
	{
		return m_revisionDate;
	}
	
	public Long getSummonerLevel()
	{
		return m_summonerLevel;
	}
}
