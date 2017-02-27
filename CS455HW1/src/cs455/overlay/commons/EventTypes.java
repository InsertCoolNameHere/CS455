package cs455.overlay.commons;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EventTypes {
	
	public static final Map<Integer, String> myMap;
	
    static {
        Map<Integer, String> aMap = new HashMap<Integer, String>();
        
        aMap.put(1, "REGISTER_REQUEST");
        aMap.put(2, "REGISTER_RESPONSE");
        
        myMap = Collections.unmodifiableMap(aMap);
    }
	
}

