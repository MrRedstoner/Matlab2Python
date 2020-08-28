/*
from https://github.com/antlr/grammars-v4/blob/master/matlab/matlab.g4
with modified NUMBER to permit .5 as alternative style to 0.5
added support for:
 - comments
 - hold on/off
 - lambda_definitions
 - no argument function calls
 - line continuation
 - ' operator including multiple on a line
 - switch statement
*/
/*
BSD License
Copyright (c) 2013, Tom Everett
All rights reserved.
Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:
1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.
3. Neither the name of Tom Everett nor the names of its contributors
   may be used to endorse or promote products derived from this software
   without specific prior written permission.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
/*
* Grammar based on yacc-keable for matlab by Danny Luk 12/1995
*
* http://www.angelfire.com/ar/CompiladoresUCSE/images/MATLAB.zip
*/

grammar Matlab;

@lexer::members {
//based on https://stackoverflow.com/a/62483386/5511972
//from https://github.com/bkiers/ecmascript-parser/blob/01b6c20917b16d2e7a9837809d7d9c6565b92673/src/main/antlr4/nl/bigo/ecmascriptparser/ECMAScript.g4#L773
private Token lastToken;

@Override
public Token nextToken() {
	// Get the next token.
	Token next = super.nextToken();
	if (next.getChannel() == Token.DEFAULT_CHANNEL) {
		// Keep track of the last token on the default channel.
		this.lastToken = next;
	}
	return next;
}

private boolean isStringPossible() {
	if (this.lastToken == null) {
		// Start of the input, a String is valid here
		return true;
	}
	
	switch (this.lastToken.getType()) {
	//TODO check what others are needed here
	case IDENTIFIER: 
		// After these, no string literal can follow.
		return false;
	default:
		// In all other cases, a string literal _is_ possible.
		return true;
	}
}
}

primary_expression
   : IDENTIFIER
   | CONSTANT
   | STRING_LITERAL
   | '(' expression ')'
   | '[' ']'
   | '[' array_list ']'
   ;

postfix_expression
   : primary_expression
   | array_expression
   | postfix_expression TRANSPOSE
   | postfix_expression NCTRANSPOSE
   ;

index_expression
   : ':'
   | expression
   ;

index_expression_list
   : index_expression
   | index_expression_list ',' index_expression
   ;

array_expression
   : IDENTIFIER '(' index_expression_list ')'
   | IDENTIFIER '()'
   ;

unary_expression
   : postfix_expression
   | unary_operator postfix_expression
   | postfix_expression '\''
   ;

unary_operator
   : '+'
   | '-'
   | '~'
   ;

array_mul_expression
   : unary_expression
   | array_mul_expression ARRAYMUL unary_expression
   | array_mul_expression ARRAYDIV unary_expression
   | array_mul_expression ARRAYRDIV unary_expression
   | array_mul_expression ARRAYPOW unary_expression
   | array_mul_expression '^' unary_expression
   ;

multiplicative_expression
   : array_mul_expression
   | multiplicative_expression '*' array_mul_expression
   | multiplicative_expression '/' array_mul_expression
   | multiplicative_expression '\\' array_mul_expression
   ;

additive_expression
   : multiplicative_expression
   | additive_expression '+' multiplicative_expression
   | additive_expression '-' multiplicative_expression
   ;

relational_expression
   : additive_expression
   | relational_expression '<' additive_expression
   | relational_expression '>' additive_expression
   | relational_expression LE_OP additive_expression
   | relational_expression GE_OP additive_expression
   ;

equality_expression
   : relational_expression
   | equality_expression EQ_OP relational_expression
   | equality_expression NE_OP relational_expression
   ;

and_expression
   : equality_expression
   | and_expression '&' equality_expression
   ;

or_expression
   : and_expression
   | or_expression '|' and_expression
   ;

expression
   : or_expression
   | expression ':' or_expression
   | lambda_definition
   ;

lambda_definition
   : '@(' index_expression_list ')' expression
   ;

assignment_expression
   : postfix_expression '=' expression
   ;

eostmt
   : ','
   | ';'
   | CR
   ;

statement
   : global_statement
   | clear_statement
   | close_statement
   | assignment_statement
   | expression_statement
   | selection_statement
   | iteration_statement
   | jump_statement
   | COMMENT_STATEMENT
   | hold_statement
   | switch_statement
   ;

statement_list
   : statement
   | statement_list statement
   ;

identifier_list
   : IDENTIFIER
   | identifier_list IDENTIFIER
   ;

global_statement
   : GLOBAL identifier_list eostmt
   ;

clear_statement
   : CLEAR identifier_list eostmt
   | CLC
   ;

close_statement
   : CLOSE identifier_list eostmt
   ;

expression_statement
   : eostmt
   | expression eostmt
   ;

assignment_statement
   : assignment_expression eostmt
   ;

array_element
   : array_expression
   | expression
   ;

array_sub_list
   : array_element (',' array_element) *
   | array_element +
   ;

array_list
   : array_sub_list (';' array_sub_list) *
   ;

selection_statement
   : IF expression expend statement_list END eostmt
   | IF expression statement_list END eostmt
   | IF expression expend statement_list ELSE statement_list END eostmt
   | IF expression expend statement_list elseif_clause END eostmt
   | IF expression expend statement_list elseif_clause ELSE statement_list END eostmt
   ;

switch_statement
   : SWITCH IDENTIFIER expend switch_case+ otherwise_case? END eostmt
   ;

switch_case
   : CASE STRING_LITERAL statement_list
   ;

otherwise_case
   : OTHERWISE statement_list
   ;

expend
   : CR
   | eostmt
   ;

elseif_clause
   : ELSEIF expression statement_list
   | elseif_clause ELSEIF expression statement_list
   ;

iteration_statement
   : WHILE expression statement_list END eostmt
   | FOR IDENTIFIER '=' expression statement_list END eostmt
   | FOR '(' IDENTIFIER '=' expression ')' statement_list END eostmt
   ;

jump_statement
   : BREAK eostmt
   | RETURN eostmt
   ;

translation_unit
   : statement_list
   | FUNCTION function_declare eostmt statement_list
   ;

func_ident_list
   : IDENTIFIER
   | func_ident_list ',' IDENTIFIER
   ;

func_return_list
   : IDENTIFIER
   | '[' func_ident_list ']'
   ;

function_declare_lhs
   : IDENTIFIER
   | IDENTIFIER '(' ')'
   | IDENTIFIER '(' func_ident_list ')'
   ;

function_declare
   : function_declare_lhs
   | func_return_list '=' function_declare_lhs
   ;

hold_statement
   : 'hold on' eostmt
   | 'hold off' eostmt
   ;

COMMENT_STATEMENT
   : '%' ( ~ '\n' ) * '\n'
   ;

ARRAYMUL
   : '.*'
   ;


ARRAYDIV
   : '.\\'
   ;


ARRAYRDIV
   : './'
   ;


ARRAYPOW
   : '.^'
   ;


BREAK
   : 'break'
   ;


RETURN
   : 'return'
   ;


FUNCTION
   : 'function'
   ;


FOR
   : 'for'
   ;


WHILE
   : 'while'
   ;


END
   : 'end'
   ;


GLOBAL
   : 'global'
   ;


IF
   : 'if'
   ;


CLEAR
   : 'clear'
   ;

CLOSE
   : 'close'
   ;

CLC
   : 'clc'
   ;

ELSE
   : 'else'
   ;


ELSEIF
   : 'elseif'
   ;

SWITCH
   : 'switch'
   ;

CASE
   : 'case'
   ;

OTHERWISE
   : 'otherwise'
   ;

LE_OP
   : '<='
   ;


GE_OP
   : '>='
   ;


EQ_OP
   : '=='
   ;


NE_OP
   : '~='
   ;


TRANSPOSE
   : 'transpose'
   ;


NCTRANSPOSE
   : '.\''
   ;


STRING_LITERAL
   : {isStringPossible()}? '\'' ( ~ ( '\'' | '\n' ) | '\'\'') * '\''
   ;


IDENTIFIER
   : [a-zA-Z] [a-zA-Z0-9_]*
   ;


CONSTANT
   : NUMBER (E SIGN? NUMBER)?
   ;


fragment NUMBER
   : ('0' .. '9') + ('.' ('0' .. '9') +)?
   | '.' ('0' .. '9') +
   ;


fragment E
   : 'E' | 'e'
   ;


fragment SIGN
   : ('+' | '-')
   ;


CR
   : [\r\n] +
   ;


LINE_CONTINUATION
: '...\n' -> skip
;

WS
   : [ \t] + -> skip
   ;