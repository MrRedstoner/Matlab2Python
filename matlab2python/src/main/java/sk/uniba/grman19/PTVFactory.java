package sk.uniba.grman19;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.stringtemplate.v4.STGroup;

import sk.uniba.grman19.util.ErrorWrappingTranslator;

public class PTVFactory {
	private Supplier<PythonTranslatorVisitor> ptvNew;
	
	public PTVFactory(boolean debug, STGroup templates, Optional<Set<String>>indexIgnore) {
		if(debug) {
			ptvNew = ()->new PythonTranslatorVisitor(templates,indexIgnore);
		} else {
			ptvNew = ()->new ErrorWrappingTranslator(templates,indexIgnore);
		}
	}
	
	public PythonTranslatorVisitor getNew() {
		return ptvNew.get();
	}
}
