package sk.uniba.grman19;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Optional;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import sk.uniba.grman19.util.ErrorWrappingTranslator;

public class Runner {
	private static Options getOptions() {
		Options options=new Options();
		options
			.addOption("i", "input", true, "Input file path, or - to read from stdin")
			.addOption("o", "output", true, "Override default output directory")
			.addOption("l", "logfile", true, "Redirect stderr")
			.addOption("v", "verbose", false, "More verbose output");
		return options;
	}
	
	/**@return true for success*/
	private static boolean translate(STGroup templates, CharStream input, PrintStream out) {
		PythonTranslatorVisitor ptv=new ErrorWrappingTranslator(templates);
		
		MatlabParser parser;
		try {
			MatlabLexer lexer=new MatlabLexer(input);
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			parser = new MatlabParser(tokens);
			ParseTree tree = parser.translation_unit();
			
			out.println(ptv.visit(tree).getFullTranslation(templates.getInstanceOf("fullTranslation")));
			
			return parser.getNumberOfSyntaxErrors()==0;
		} catch (RecognitionException e) {
			e.printStackTrace();
			
			return false;
		}
	}
	
	public static void main(String[]args) throws ParseException {
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(getOptions(), args);
		
		STGroup templates = new STGroupFile("Python.stg");
		
		String fromFile=Optional.ofNullable(cmd.getOptionValue("input")).orElse(".");
		String toFile=cmd.getOptionValue("output");
		boolean verbose=cmd.hasOption("verbose");
		if(cmd.hasOption("logfile")) {
			try {
				System.setErr(new PrintStream(new File(cmd.getOptionValue("logfile"))));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.exit(4);
			}
		}
		
		if("-".equals(fromFile)) {
			//read from stdin, output to stdout or -o if given
			if(toFile==null) {
				//output to stdout
				try {
					if(verbose)System.out.println("Translating stdin to stdout");
					
					boolean fine=translate(templates,CharStreams.fromStream(System.in),System.out);
					
					if(!fine)System.exit(1);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(2);
				}
			} else {
				//output to the file/default-named file if directory
				File outFile=new File(toFile);
				if(outFile.isDirectory()) {
					outFile=new File(outFile, "output.py");
				}
				
				try {
					if(verbose)System.out.println("Translating stdin to "+outFile.getAbsolutePath());
					
					boolean fine=translate(templates,CharStreams.fromStream(System.in),new PrintStream(outFile));
					
					if(!fine)System.exit(1);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(2);
				}
			}
		} else {
			//read from fromFile, or from all fromFile/*.m if directory
			File inFile=new File(fromFile);
			File outFile;
			if(inFile.isDirectory()) {
				//output must be a directory if given
				if(toFile==null) {
					//use the same dir
					outFile=inFile;
				} else {
					outFile=new File(toFile);
					if(!outFile.isDirectory()) {
						System.err.println(toFile+" must be a directory!");
						System.exit(3);
					}
				}
				
				if(verbose)System.out.println("Translating contents of directory "+inFile.getAbsolutePath());
				
				boolean fine=true;
				boolean exception=false;
				
				for(File matlab:inFile.listFiles(s->{return s.getName().endsWith(".m");})) {
					File python=new File(outFile,matlab.getName().replaceAll("\\.m$", ".py"));
					
					try {
						if(verbose)System.out.println("Translating "+matlab.getAbsolutePath()+" to "+python.getAbsolutePath());
						
						fine&=translate(templates,CharStreams.fromPath(matlab.toPath()),new PrintStream(python));
						
					} catch (IOException e) {
						e.printStackTrace();
						exception=true;
					}
				}

				if(exception)System.exit(2);
				if(!fine)System.exit(1);
			} else {
				//just translate the one file
				if(toFile==null) {
					//use the same dir
					outFile=inFile;
				} else {
					outFile=new File(toFile);
				}
				
				if(outFile.isDirectory()) {
					outFile=new File(outFile,inFile.getName().replaceAll("\\.m$", ".py"));
				}
				
				try {
					if(verbose)System.out.println("Translating "+inFile.getAbsolutePath()+" to "+outFile.getAbsolutePath());
					
					boolean fine=translate(templates,CharStreams.fromPath(inFile.toPath()),new PrintStream(outFile));
					
					if(!fine)System.exit(1);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(2);
				}
			}
		}
	}
}
