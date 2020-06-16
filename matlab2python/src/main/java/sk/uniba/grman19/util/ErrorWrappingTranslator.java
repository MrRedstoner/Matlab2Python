package sk.uniba.grman19.util;

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
import sk.uniba.grman19.PythonTranslatorVisitor;

public class ErrorWrappingTranslator extends PythonTranslatorVisitor {
	private final STGroup templates;
	
	private Fragment error(String text) {
		return new Fragment(templates.getInstanceOf("literal").add("text", "<error: "+text+">"));
	}

	public ErrorWrappingTranslator(STGroup templates) {
		super(templates);
		this.templates=templates;
	}

	@Override
	public Fragment visit(ParseTree tree) {
		try {
			return super.visit(tree);
		}catch(Exception e) {
			return error(e.getClass().getName());
		}
	}

	@Override
	public Fragment visitChildren(RuleNode node) {
		try {
			return super.visitChildren(node);
		}catch(Exception e) {
			return error(e.getClass().getName());
		}
	}

	@Override
	public Fragment visitTerminal(TerminalNode node) {
		try {
			return super.visitTerminal(node);
		}catch(Exception e) {
			return error(e.getClass().getName());
		}
	}

	@Override
	public Fragment visitErrorNode(ErrorNode node) {
		try {
			return super.visitErrorNode(node);
		}catch(Exception e) {
			return error(e.getClass().getName());
		}
	}

	@Override
	public Fragment visitPrimary_expression(Primary_expressionContext ctx) {
		try {
			return super.visitPrimary_expression(ctx);
		}catch(Exception e) {
			return error(e.getClass().getName());
		}
	}

	@Override
	public Fragment visitPostfix_expression(Postfix_expressionContext ctx) {
		try {
			return super.visitPostfix_expression(ctx);
		}catch(Exception e) {
			return error(e.getClass().getName());
		}
	}

	@Override
	public Fragment visitIndex_expression(Index_expressionContext ctx) {
		try {
			return super.visitIndex_expression(ctx);
		}catch(Exception e) {
			return error(e.getClass().getName());
		}
	}

	@Override
	public Fragment visitIndex_expression_list(Index_expression_listContext ctx) {
		try {
			return super.visitIndex_expression_list(ctx);
		}catch(Exception e) {
			return error(e.getClass().getName());
		}
	}

	@Override
	public Fragment visitArray_expression(Array_expressionContext ctx) {
		try {
			return super.visitArray_expression(ctx);
		}catch(Exception e) {
			return error(e.getClass().getName());
		}
	}

	@Override
	public Fragment visitUnary_expression(Unary_expressionContext ctx) {
		try {
			return super.visitUnary_expression(ctx);
		}catch(Exception e) {
			return error(e.getClass().getName());
		}
	}

	@Override
	public Fragment visitUnary_operator(Unary_operatorContext ctx) {
		try {
			return super.visitUnary_operator(ctx);
		}catch(Exception e) {
			return error(e.getClass().getName());
		}
	}

	@Override
	public Fragment visitMultiplicative_expression(Multiplicative_expressionContext ctx) {
		try {
			return super.visitMultiplicative_expression(ctx);
		}catch(Exception e) {
			return error(e.getClass().getName());
		}
	}

	@Override
	public Fragment visitAdditive_expression(Additive_expressionContext ctx) {
		try {
			return super.visitAdditive_expression(ctx);
		}catch(Exception e) {
			return error(e.getClass().getName());
		}
	}

	@Override
	public Fragment visitRelational_expression(Relational_expressionContext ctx) {
		try {
			return super.visitRelational_expression(ctx);
		}catch(Exception e) {
			return error(e.getClass().getName());
		}
	}

	@Override
	public Fragment visitEquality_expression(Equality_expressionContext ctx) {
		try {
			return super.visitEquality_expression(ctx);
		}catch(Exception e) {
			return error(e.getClass().getName());
		}
	}

	@Override
	public Fragment visitAnd_expression(And_expressionContext ctx) {
		try {
			return super.visitAnd_expression(ctx);
		}catch(Exception e) {
			return error(e.getClass().getName());
		}
	}

	@Override
	public Fragment visitOr_expression(Or_expressionContext ctx) {
		try {
			return super.visitOr_expression(ctx);
		}catch(Exception e) {
			return error(e.getClass().getName());
		}
	}

	@Override
	public Fragment visitExpression(ExpressionContext ctx) {
		try {
			return super.visitExpression(ctx);
		}catch(Exception e) {
			return error(e.getClass().getName());
		}
	}

	@Override
	public Fragment visitAssignment_expression(Assignment_expressionContext ctx) {
		try {
			return super.visitAssignment_expression(ctx);
		}catch(Exception e) {
			return error(e.getClass().getName());
		}
	}

	@Override
	public Fragment visitEostmt(EostmtContext ctx) {
		try {
			return super.visitEostmt(ctx);
		}catch(Exception e) {
			return error(e.getClass().getName());
		}
	}

	@Override
	public Fragment visitStatement(StatementContext ctx) {
		try {
			return super.visitStatement(ctx);
		}catch(Exception e) {
			return error(e.getClass().getName());
		}
	}

	@Override
	public Fragment visitStatement_list(Statement_listContext ctx) {
		try {
			return super.visitStatement_list(ctx);
		}catch(Exception e) {
			return error(e.getClass().getName());
		}
	}

	@Override
	public Fragment visitIdentifier_list(Identifier_listContext ctx) {
		try {
			return super.visitIdentifier_list(ctx);
		}catch(Exception e) {
			return error(e.getClass().getName());
		}
	}

	@Override
	public Fragment visitGlobal_statement(Global_statementContext ctx) {
		try {
			return super.visitGlobal_statement(ctx);
		}catch(Exception e) {
			return error(e.getClass().getName());
		}
	}

	@Override
	public Fragment visitClear_statement(Clear_statementContext ctx) {
		try {
			return super.visitClear_statement(ctx);
		}catch(Exception e) {
			return error(e.getClass().getName());
		}
	}

	@Override
	public Fragment visitExpression_statement(Expression_statementContext ctx) {
		try {
			return super.visitExpression_statement(ctx);
		}catch(Exception e) {
			return error(e.getClass().getName());
		}
	}

	@Override
	public Fragment visitAssignment_statement(Assignment_statementContext ctx) {
		try {
			return super.visitAssignment_statement(ctx);
		}catch(Exception e) {
			return error(e.getClass().getName());
		}
	}

	@Override
	public Fragment visitArray_element(Array_elementContext ctx) {
		try {
			return super.visitArray_element(ctx);
		}catch(Exception e) {
			return error(e.getClass().getName());
		}
	}

	@Override
	public Fragment visitArray_list(Array_listContext ctx) {
		try {
			return super.visitArray_list(ctx);
		}catch(Exception e) {
			return error(e.getClass().getName());
		}
	}

	@Override
	public Fragment visitSelection_statement(Selection_statementContext ctx) {
		try {
			return super.visitSelection_statement(ctx);
		}catch(Exception e) {
			return error(e.getClass().getName());
		}
	}

	@Override
	public Fragment visitElseif_clause(Elseif_clauseContext ctx) {
		try {
			return super.visitElseif_clause(ctx);
		}catch(Exception e) {
			return error(e.getClass().getName());
		}
	}

	@Override
	public Fragment visitIteration_statement(Iteration_statementContext ctx) {
		try {
			return super.visitIteration_statement(ctx);
		}catch(Exception e) {
			return error(e.getClass().getName());
		}
	}

	@Override
	public Fragment visitJump_statement(Jump_statementContext ctx) {
		try {
			return super.visitJump_statement(ctx);
		}catch(Exception e) {
			return error(e.getClass().getName());
		}
	}

	@Override
	public Fragment visitTranslation_unit(Translation_unitContext ctx) {
		try {
			return super.visitTranslation_unit(ctx);
		}catch(Exception e) {
			return error(e.getClass().getName());
		}
	}

	@Override
	public Fragment visitFunc_ident_list(Func_ident_listContext ctx) {
		try {
			return super.visitFunc_ident_list(ctx);
		}catch(Exception e) {
			return error(e.getClass().getName());
		}
	}

	@Override
	public Fragment visitFunc_return_list(Func_return_listContext ctx) {
		try {
			return super.visitFunc_return_list(ctx);
		}catch(Exception e) {
			return error(e.getClass().getName());
		}
	}

	@Override
	public Fragment visitFunction_declare_lhs(Function_declare_lhsContext ctx) {
		try {
			return super.visitFunction_declare_lhs(ctx);
		}catch(Exception e) {
			return error(e.getClass().getName());
		}
	}

	@Override
	public Fragment visitFunction_declare(Function_declareContext ctx) {
		try {
			return super.visitFunction_declare(ctx);
		}catch(Exception e) {
			return error(e.getClass().getName());
		}
	}
}
