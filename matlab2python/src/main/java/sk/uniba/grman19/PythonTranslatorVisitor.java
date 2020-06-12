package sk.uniba.grman19;

import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.stringtemplate.v4.STGroup;

import sk.uniba.grman19.MatlabParser.Additive_expressionContext;
import sk.uniba.grman19.MatlabParser.And_expressionContext;
import sk.uniba.grman19.MatlabParser.Array_elementContext;
import sk.uniba.grman19.MatlabParser.Array_expressionContext;
import sk.uniba.grman19.MatlabParser.Array_listContext;
import sk.uniba.grman19.MatlabParser.Assignment_expressionContext;
import sk.uniba.grman19.MatlabParser.Assignment_statementContext;
import sk.uniba.grman19.MatlabParser.Clear_statementContext;
import sk.uniba.grman19.MatlabParser.Elseif_clauseContext;
import sk.uniba.grman19.MatlabParser.EostmtContext;
import sk.uniba.grman19.MatlabParser.Equality_expressionContext;
import sk.uniba.grman19.MatlabParser.ExpressionContext;
import sk.uniba.grman19.MatlabParser.Expression_statementContext;
import sk.uniba.grman19.MatlabParser.Func_ident_listContext;
import sk.uniba.grman19.MatlabParser.Func_return_listContext;
import sk.uniba.grman19.MatlabParser.Function_declareContext;
import sk.uniba.grman19.MatlabParser.Function_declare_lhsContext;
import sk.uniba.grman19.MatlabParser.Global_statementContext;
import sk.uniba.grman19.MatlabParser.Identifier_listContext;
import sk.uniba.grman19.MatlabParser.Index_expressionContext;
import sk.uniba.grman19.MatlabParser.Index_expression_listContext;
import sk.uniba.grman19.MatlabParser.Iteration_statementContext;
import sk.uniba.grman19.MatlabParser.Jump_statementContext;
import sk.uniba.grman19.MatlabParser.Multiplicative_expressionContext;
import sk.uniba.grman19.MatlabParser.Or_expressionContext;
import sk.uniba.grman19.MatlabParser.Postfix_expressionContext;
import sk.uniba.grman19.MatlabParser.Primary_expressionContext;
import sk.uniba.grman19.MatlabParser.Relational_expressionContext;
import sk.uniba.grman19.MatlabParser.Selection_statementContext;
import sk.uniba.grman19.MatlabParser.StatementContext;
import sk.uniba.grman19.MatlabParser.Statement_listContext;
import sk.uniba.grman19.MatlabParser.Translation_unitContext;
import sk.uniba.grman19.MatlabParser.Unary_expressionContext;
import sk.uniba.grman19.MatlabParser.Unary_operatorContext;
import sk.uniba.grman19.util.Fragment;

public class PythonTranslatorVisitor implements MatlabVisitor<Fragment> {
	
	private final STGroup templates;
	
	private Fragment template(String name) {
		return new Fragment(templates.getInstanceOf(name));
	}
	
	private Fragment literal(String value) {
		return template("literal").add("text", value);
	}
	
	PythonTranslatorVisitor(STGroup templates){
		this.templates=templates;
	}

	@Override
	public Fragment visit(ParseTree tree) {
		return tree.accept(this);
	}

	@Override
	public Fragment visitChildren(RuleNode node) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitTerminal(TerminalNode node) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitErrorNode(ErrorNode node) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitPrimary_expression(Primary_expressionContext ctx) {
		if(ctx.IDENTIFIER()!=null) {
			//option IDENTIFIER
			return literal(ctx.getText());
		}
		if(ctx.CONSTANT()!=null) {
			//option CONSTANT
			return literal(ctx.getText());
		}
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitPostfix_expression(Postfix_expressionContext ctx) {
		if(ctx.primary_expression()!=null) {
			//option primary_expression
			return ctx.primary_expression().accept(this);
		}
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitIndex_expression(Index_expressionContext ctx) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitIndex_expression_list(Index_expression_listContext ctx) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitArray_expression(Array_expressionContext ctx) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitUnary_expression(Unary_expressionContext ctx) {
		if(ctx.unary_operator()==null) {
			//option postfix_expression
			return ctx.postfix_expression().accept(this);
		}
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitUnary_operator(Unary_operatorContext ctx) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitMultiplicative_expression(Multiplicative_expressionContext ctx) {
		if(ctx.multiplicative_expression()==null) {
			//option unary_expression
			return ctx.unary_expression().accept(this);
		}
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitAdditive_expression(Additive_expressionContext ctx) {
		if(ctx.additive_expression()==null) {
			//option multiplicative_expression
			return ctx.multiplicative_expression().accept(this);
		}
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitRelational_expression(Relational_expressionContext ctx) {
		if(ctx.relational_expression()==null) {
			//option additive_expression
			return ctx.additive_expression().accept(this);
		}
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitEquality_expression(Equality_expressionContext ctx) {
		if(ctx.equality_expression()==null) {
			//option relational_expression
			return ctx.relational_expression().accept(this);
		}
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitAnd_expression(And_expressionContext ctx) {
		if(ctx.and_expression()==null) {
			//option equality_expression
			return ctx.equality_expression().accept(this);
		} else {
			//option and_expression '&' equality_expression
		}
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitOr_expression(Or_expressionContext ctx) {
		if(ctx.or_expression()==null) {
			//option and_expression
			return ctx.and_expression().accept(this);
		} else {
			//option or_expression '|' and_expression
		}
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitExpression(ExpressionContext ctx) {
		if(ctx.expression()==null) {
			//option or_expression
			return ctx.or_expression().accept(this);
		} else {
			//option expression ':' or_expression
		}
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitAssignment_expression(Assignment_expressionContext ctx) {
		return template("assignment_expression")
					.add("postfix_expression", ctx.postfix_expression().accept(this))
					.add("expression", ctx.expression().accept(this));
	}

	@Override
	public Fragment visitEostmt(EostmtContext ctx) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitStatement(StatementContext ctx) {
		if(ctx.assignment_statement()!=null) {
			//option assignment_statement
			//no need to wrap
			return ctx.assignment_statement().accept(this);
		}
		if(ctx.expression_statement()!=null) {
			//option expression_statement
			//no need to wrap
			return ctx.expression_statement().accept(this);
		}
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitStatement_list(Statement_listContext ctx) {
		if(ctx.statement_list()==null) {
			//option statement
			//no need to wrap
			return ctx.statement().accept(this);
		} else {
			//option statement_list statement
			return template("statement_list")
						.add("statement_list", ctx.statement_list().accept(this))
						.add("statement", ctx.statement().accept(this));
		}
	}

	@Override
	public Fragment visitIdentifier_list(Identifier_listContext ctx) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitGlobal_statement(Global_statementContext ctx) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitClear_statement(Clear_statementContext ctx) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitExpression_statement(Expression_statementContext ctx) {
		if(ctx.expression()==null) {
			//option eostmt
			//noop command
			return template("pass");
		} else {
			//option expression eostmt
			//no need to wrap
			return ctx.expression().accept(this);
		}
	}

	@Override
	public Fragment visitAssignment_statement(Assignment_statementContext ctx) {
		//assignment_expression eostmt
		return ctx.assignment_expression().accept(this);
	}

	@Override
	public Fragment visitArray_element(Array_elementContext ctx) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitArray_list(Array_listContext ctx) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitSelection_statement(Selection_statementContext ctx) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitElseif_clause(Elseif_clauseContext ctx) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitIteration_statement(Iteration_statementContext ctx) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitJump_statement(Jump_statementContext ctx) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitTranslation_unit(Translation_unitContext ctx) {
		if(ctx.FUNCTION()==null) {
			//option: statement_list
			//no need to wrap it
			return ctx.statement_list().accept(this);
		} else {
			//option: FUNCTION function_declare eostmt statement_list
		}
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitFunc_ident_list(Func_ident_listContext ctx) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitFunc_return_list(Func_return_listContext ctx) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitFunction_declare_lhs(Function_declare_lhsContext ctx) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitFunction_declare(Function_declareContext ctx) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}
}
