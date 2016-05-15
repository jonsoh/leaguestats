import java.util.LinkedList;
import java.util.Date;

public class LSCourtesyEngine
{
	// List of past requests in milliseconds since 1970, latest first
	private LinkedList<Long> pastRequestTimes = new LinkedList<Long>();
	
	// Maximum length of pastRequestTimes array
	private static int maxCachedValues = 500;
	
	void willSendRequest()
	{
		Date now = new Date();
		long requestTime = now.getTime();
		pastRequestTimes.addFirst(requestTime);
		
		if (pastRequestTimes.size() > maxCachedValues)
		{
			pastRequestTimes.removeLast();
		}
	}
	
	long msUntilNextAvailableRequest()
	{
		Date now = null;
		long msLeft = 0;
		
		// Maximum rate of 10 requests per 10 seconds
		if (pastRequestTimes.size() >= 10)
		{
			if (now == null)
			{
				now = new Date();
			}
			long cutoffTime = now.getTime() - (10 * 1000);

			long referenceTime = pastRequestTimes.get(9);
			
			long delay = referenceTime - cutoffTime;
			if (delay > msLeft)
			{
				msLeft = delay;
			}
		}
		
		// Maximum rate of maxCachedValues per 10 minutes
		if (pastRequestTimes.size() == maxCachedValues)
		{
			if (now == null)
			{
				now = new Date();
			}
			long cutoffTime = now.getTime() - (10 * 60 * 1000);

			long referenceTime = pastRequestTimes.getLast();
			
			long delay = referenceTime - cutoffTime;
			if (delay > msLeft)
			{
				msLeft = delay;
			}
		}	
		
		return msLeft;
	}
}
