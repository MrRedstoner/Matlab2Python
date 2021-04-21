package sk.uniba.grman19.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class IdentifierTypeStorage {
	private final Map<String, IdentifierType> map = new HashMap<String, IdentifierType>();
	
	public IdentifierTypeStorage(){
		this(Collections.emptySet(),Collections.emptySet());
	}

	public IdentifierTypeStorage(Set<String>functions, Set<String>arrays){
		functions.forEach(this::registerFunction);
		arrays.forEach(this::registerArray);
	}
	
	public IdentifierTypeStorage getCopy() {
		IdentifierTypeStorage copy=new IdentifierTypeStorage();
		copy.map.putAll(map);
		return copy;
	}
	
	public IdentifierType getType(String identifier) {
		return map.getOrDefault(identifier, IdentifierType.UNKNOWN);
	}
	
	private void register(String identifier, IdentifierType type) {
		Objects.requireNonNull(identifier);
		IdentifierType ret = map.put(identifier, type);
		if((ret!=null)&&(ret!=type)) {
			throw new RuntimeException(identifier+" is already a "+ret);
		}
	}
	
	public void registerFunction(String identifier) {
		register(identifier, IdentifierType.FUNCTION);
	}
	
	public void registerArray(String identifier) {
		register(identifier, IdentifierType.ARRAY);
	}
}
