package jp.codepanic.netlis;

import java.util.Comparator;

// ※CLNが同じ場合０を返さないと
//   Caused by: java.lang.IllegalArgumentException: Comparison method violates its general contract!

public class ChannelCompListenerDsc implements Comparator<Channel>{

	@Override
	public int compare(Channel lhs, Channel rhs) {
		
		int l = Integer.parseInt(lhs._CLN);
		int r = Integer.parseInt(rhs._CLN);
		
		if(!MainActivity._favoriteTop || (lhs.isFavorite() && rhs.isFavorite()) || (!lhs.isFavorite() && !rhs.isFavorite())){
			if(l == r)
				return 0;
			
			return l > r ? -1 : 1;
		}
			
		if(lhs.isFavorite())
			return -1;
		
		return 1;
	}
	
}
