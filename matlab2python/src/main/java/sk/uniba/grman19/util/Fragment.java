package sk.uniba.grman19.util;

import java.util.EnumSet;

import org.stringtemplate.v4.ST;

/**
Combines a StringTemplate, a list of imports and a list of defs needed by template contents
*/
public class Fragment {
	private final ST template;
	private final EnumSet<PythonImport> imports;
	private final EnumSet<PythonDef> defs;
	private boolean error=false;
	
	public Fragment(ST template){
		this(template,EnumSet.noneOf(PythonImport.class),EnumSet.noneOf(PythonDef.class));
	}
	
	public Fragment(ST template,EnumSet<PythonImport> imports,EnumSet<PythonDef> defs){
		this.template=template;
		this.imports=imports;
		this.defs=defs;
	}
	
	public boolean hadError() {
		return error;
	}

	/**@return this to allow chaining*/
	public Fragment setError() {
		error=true;
		return this;
	}
	
	/**@return this to allow chaining*/
	public Fragment add(String key, Fragment value) {
		template.add(key, value.template);
		imports.addAll(value.imports);
		defs.addAll(value.defs);
		error|=value.error;
		return this;
	}
	
	/**@return this to allow chaining*/
	public Fragment add(String key, String value) {
		template.add(key, value);
		return this;
	}
	
	/**@return this to allow chaining*/
	public Fragment addImport(PythonImport value) {
		imports.add(value);
		return this;
	}
	
	/**@return this to allow chaining*/
	public Fragment addDef(PythonDef value) {
		defs.add(value);
		return this;
	}
	
	public String getFullTranslation(ST fullTranslation) {
		for(PythonImport pimport:imports) {
			fullTranslation.add("imports", pimport.getRepresentation());
		}
		
		for(PythonDef pdef:defs) {
			fullTranslation.add("defs", pdef.getRepresentation());
		}
		
		fullTranslation.add("translation_unit", template);
		
		return fullTranslation.render();
	}
}
