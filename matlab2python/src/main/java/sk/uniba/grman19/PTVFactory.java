package sk.uniba.grman19;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.stringtemplate.v4.STGroup;

import sk.uniba.grman19.util.ErrorWrappingTranslator;
import sk.uniba.grman19.util.IdentifierTypeStorage;

public class PTVFactory {
	private Supplier<PythonTranslatorVisitor> ptvNew;
	
	public PTVFactory(boolean debug, STGroup templates) {
		this(debug, templates, Optional.empty(), new IdentifierTypeStorage());
	}
	
	public PTVFactory(boolean debug, STGroup templates, Optional<Set<String>>indexIgnore, IdentifierTypeStorage identType) {
		if(debug) {
			ptvNew = ()->new PythonTranslatorVisitor(templates, indexIgnore, identType.getCopy());
		} else {
			ptvNew = ()->new ErrorWrappingTranslator(templates, indexIgnore, identType.getCopy());
		}
	}
	
	public PythonTranslatorVisitor getNew() {
		return ptvNew.get();
	}
}
