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
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import sk.uniba.grman19.util.ErrorWrappingTranslator;
import sk.uniba.grman19.util.Fragment;
import sk.uniba.grman19.util.TreeUtils;

public class Runner {
	private static boolean debug=false;
	
	private static Options getOptions() {
		Options options=new Options();
		options
			.addOption("i", "input", true, "Input file path, or - to read from stdin")
			.addOption("o", "output", true, "Override default output directory")
			.addOption("l", "logfile", true, "Redirect stderr")
			.addOption("v", "verbose", false, "More verbose output")
			.addOption("d", "debug", false, "Most verbose output, implies -v")
			.addOption("h", "help", false, "Print help");
		return options;
	}
	
	/**@return true for success*/
	private static boolean translate(STGroup templates, CharStream input, PrintStream out) {
		PythonTranslatorVisitor ptv;
		if(debug) {
			ptv=new PythonTranslatorVisitor(templates);
		} else {
			ptv=new ErrorWrappingTranslator(templates);
		}
		
		MatlabParser parser;
		try {
			MatlabLexer lexer=new MatlabLexer(input);
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			parser = new MatlabParser(tokens);
			if(debug) {
				parser.setBuildParseTree(true);
			}
			ParseTree tree = parser.translation_unit();
			if(debug) {
				TreeUtils.dump(parser, tree);
			}
			
			Fragment result=ptv.visit(tree);
			
			out.println(result.getFullTranslation(templates.getInstanceOf("fullTranslation")));
			
			return parser.getNumberOfSyntaxErrors()==0 && !result.hadError();
		} catch (RecognitionException e) {
			e.printStackTrace();
			
			return false;
		}
	}
	
	public static void main(String[]args) throws ParseException {
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(getOptions(), args);
		
		STGroup templates = new STGroupFile("Python.stg");
		
		if(cmd.hasOption("help")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java -jar Mat2Pyt.jar", getOptions());
			System.exit(0);
		}
		
		String fromFile=Optional.ofNullable(cmd.getOptionValue("input")).orElse(".");
		String toFile=cmd.getOptionValue("output");
		debug=cmd.hasOption("debug");
		boolean verbose=cmd.hasOption("verbose")|debug;
		
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
					
					if(!fine) {
						if(verbose)System.out.println("There was an error");
						System.exit(1);
					}
				} catch (IOException e) {
					if(debug)e.printStackTrace();
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
					
					if(!fine) {
						if(verbose)System.out.println("There was an error");
						System.exit(1);
					}
				} catch (IOException e) {
					if(debug)e.printStackTrace();
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
						
						boolean fileFine=translate(templates,CharStreams.fromPath(matlab.toPath()),new PrintStream(python));
						
						if(!fileFine) {
							if(verbose)System.out.println("There was an error");
						}
						
						fine&=fileFine;
					} catch (IOException e) {
						if(debug)e.printStackTrace();
						exception=true;
					}
				}

				if(exception)System.exit(2);
				if(!fine) {
					System.exit(1);
				}
			} else {
				//just translate the one file
				if(toFile==null) {
					//use the same dir
					outFile=inFile.getParentFile();
				} else {
					outFile=new File(toFile);
				}
				
				if(outFile.isDirectory()) {
					outFile=new File(outFile,inFile.getName().replaceAll("\\.m$", ".py"));
				}
				
				try {
					if(verbose)System.out.println("Translating "+inFile.getAbsolutePath()+" to "+outFile.getAbsolutePath());
					
					boolean fine=translate(templates,CharStreams.fromPath(inFile.toPath()),new PrintStream(outFile));
					
					if(!fine) {
						if(verbose)System.out.println("There was an error");
						System.exit(1);
					}
				} catch (IOException e) {
					if(debug)e.printStackTrace();
					System.exit(2);
				}
			}
		}
		/**/
	}
}
