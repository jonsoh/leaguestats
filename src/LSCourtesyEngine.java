import java.util.LinkedList;
import java.util.Date;

public class LSCourtesyEngine
{
	// Maximum length of pastRequestTimes array
	private static int s_maxCachedValues = 500;

	// List of past requests in milliseconds since 1970, latest first
	private LinkedList<Long> m_pastRequestTimes = new LinkedList<Long>();

	public void willSendRequest()
	{
		Date now = new Date();
		long requestTime = now.getTime();
		m_pastRequestTimes.addFirst(requestTime);

		if (m_pastRequestTimes.size() > s_maxCachedValues)
		{
			m_pastRequestTimes.removeLast();
		}
	}

	public long msUntilNextAvailableRequest()
	{
		Date now = null;
		long msLeft = 0;

		// Maximum rate of 10 requests per 10 seconds
		if (m_pastRequestTimes.size() >= 10)
		{
			if (now == null)
			{
				now = new Date();
			}
			long cutoffTime = now.getTime() - (10 * 1000);

			long referenceTime = m_pastRequestTimes.get(9);

			long delay = referenceTime - cutoffTime;
			if (delay > msLeft)
			{
				msLeft = delay;
			}
		}

		// Maximum rate of maxCachedValues per 10 minutes
		if (m_pastRequestTimes.size() == s_maxCachedValues)
		{
			if (now == null)
			{
				now = new Date();
			}
			long cutoffTime = now.getTime() - (10 * 60 * 1000);

			long referenceTime = m_pastRequestTimes.getLast();

			long delay = referenceTime - cutoffTime;
			if (delay > msLeft)
			{
				msLeft = delay;
			}
		}	

		return msLeft;
	}
}
