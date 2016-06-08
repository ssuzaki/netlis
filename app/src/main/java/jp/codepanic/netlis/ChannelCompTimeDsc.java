package jp.codepanic.netlis;

import java.util.Comparator;

public class ChannelCompTimeDsc implements Comparator<Channel>{

	@Override
	public int compare(Channel lhs, Channel rhs) {
		if(!MainActivity._favoriteTop || (lhs.isFavorite() && rhs.isFavorite()) || (!lhs.isFavorite() && !rhs.isFavorite()))
			return rhs._TIMS.compareTo(lhs._TIMS);
		
		if(lhs.isFavorite())
			return -1;
		
		return 1;
	}
	
}
