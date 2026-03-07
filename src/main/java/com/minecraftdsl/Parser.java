package com.minecraftdsl;

import java.util.ArrayList;
import java.util.List;

/**
 * Grammar:
 *   <program>       ::= <statement_list>
 *   <statement>     ::= <assignment> | <for_count> | <for_range> | <while_loop>
 *                     | <if_stmt> | "stop" | <function_call> | <comment>
 *   <block>         ::= INDENT <statement_list> DEDENT
 *   <expression>    ::= <term> (("+"|"-") <term>)*
 *   <term>          ::= <factor> (("*"|"/") <factor>)*
 *   <factor>        ::= <literal> | <identifier> | <special_object>
 *                     | <function_call> | "(" <expression> ")" | "-" <factor>
 *   <condition>     ::= "not" <condition>
 *                     | <expression> <comparison_op> <expression>
 *                     | <function_call>
 *                     | <identifier>
 */

public class Parser {

    // State
    private final List<Token> tokens;
    private int cursor = -1;
    private Token current;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        advance(); // prime the pump
    }

    // Token navigation helpers
    // Move to the next token and return it.
    private Token advance() {
        cursor++;
        if (cursor < tokens.size()) {
            current = tokens.get(cursor);
        } else {
            current = new Token(TokenType.EOF, null);
        }
        return current;
    }

    // Peek ahead
    private Token peek(int offset) {
        int idx = cursor + offset;
        if (idx < tokens.size()) return tokens.get(idx);
        return new Token(TokenType.EOF, null);
    }

    // Return true if the current token has the given type
    private boolean check(TokenType type) {
        return current.type == type;
    }

    private Token expect(TokenType type) {
        if (!check(type)) {
            throw new ParseException(
                    "Expected " + type + " but got " + current.type
                            + (current.literal != null ? " ('" + current.literal + "')" : ""));
        }
        Token t = current;
        advance();
        return t;
    }

    // Skip any NEWLINE tokens at the current position
    private void skipNewlines() {
        while (check(TokenType.NEWLINE)) advance();
    }

    // Public entry point
    public ASTNode.Program parseProgram() {
        skipNewlines();
        List<ASTNode> statements = parseStatementList(false);
        expect(TokenType.EOF);
        return new ASTNode.Program(statements);
    }

    // Statement list
    // Parse a sequence of statements; if insideBlock=true, stop when DEDENT (or EOF) is seen.
    private List<ASTNode> parseStatementList(boolean insideBlock) {
        List<ASTNode> stmts = new ArrayList<>();
        while (true) {
            skipNewlines();
            if (check(TokenType.EOF)) break;
            if (insideBlock && check(TokenType.DEDENT)) break;
            ASTNode stmt = parseStatement();
            if (stmt != null) stmts.add(stmt);
        }
        return stmts;
    }

    /**
     * <statement> ::= <assignment> NEWLINE
     *               | <for_count>
     *               | <for_range>
     *               | <while_loop>
     *               | <if_stmt>
     *               | "stop" NEWLINE
     *               | <function_call> NEWLINE
     *               | <comment>
     */
    private ASTNode parseStatement() {
        // Comment  -->  "-- ..." until end of line
        if (check(TokenType.IDENT) && current.literal != null && current.literal.startsWith("--")) {
            return parseComment();
        }

        switch (current.type) {
            case FOR:   return parseFor();
            case WHILE: return parseWhile();
            case IF:    return parseIf();

            case STOP: {
                advance(); // consume "stop"
                consumeNewline();
                return new ASTNode.StopStmt();
            }

            case IDENT: {
                // Look ahead to disambiguate:
                // IDENT "="   - assignment
                // IDENT "("   - function call (statement)
                // IDENT "."   - special-object used as statement
                Token la = peek(1);
                if (la.type == TokenType.ASSIGN) {
                    return parseAssignment();
                } else {
                    // expression statement - must be a function call per grammar
                    ASTNode expr = parseExpression();
                    if (!(expr instanceof ASTNode.FunctionCall)) {
                        throw new ParseException(
                                "Expected a function call or assignment as statement, got: " + expr);
                    }
                    consumeNewline();
                    return expr;
                }
            }

            default:
                throw new ParseException("Unexpected token at start of statement: " + current.type
                        + (current.literal != null ? " ('" + current.literal + "')" : ""));
        }
    }

    // Comments are lexed as a single IDENT token whose literal begins with "--".
    private ASTNode.Comment parseComment() {
        String text = current.literal;
        advance();
        consumeNewline();
        return new ASTNode.Comment(text.substring(2));
    }

    /**
     * <assignment> ::= IDENT "=" <expression>
     */
    private ASTNode.Assignment parseAssignment() {
        String name = expect(TokenType.IDENT).literal;
        expect(TokenType.ASSIGN);
        ASTNode value = parseExpression();
        consumeNewline();
        return new ASTNode.Assignment(name, value);
    }

    /**
     * <for_count> ::= "for" <expression> "times" NEWLINE <block>
     * <for_range> ::= "for" IDENT "from" <expression> "to" <expression> NEWLINE <block>
     */
    private ASTNode parseFor() {
        expect(TokenType.FOR);
        // Disambiguate: "for" IDENT "from" -> range, anything else -> count
        if (check(TokenType.IDENT) && peek(1).type == TokenType.FROM) {
            return parseForRange();
        }
        return parseForCount();
    }

    private ASTNode.ForCount parseForCount() {
        ASTNode count = parseExpression();
        expect(TokenType.TIMES);
        consumeNewline();
        ASTNode.Block body = parseBlock();
        return new ASTNode.ForCount(count, body);
    }

    private ASTNode.ForRange parseForRange() {
        String variable = expect(TokenType.IDENT).literal;
        expect(TokenType.FROM);
        ASTNode from = parseExpression();
        expect(TokenType.TO);
        ASTNode to = parseExpression();
        consumeNewline();
        ASTNode.Block body = parseBlock();
        return new ASTNode.ForRange(variable, from, to, body);
    }

    /**
     * <while_loop> ::= "while" <condition> NEWLINE <block>
     */
    private ASTNode.WhileLoop parseWhile() {
        expect(TokenType.WHILE);
        ASTNode.Condition condition = parseCondition();
        consumeNewline();
        ASTNode.Block body = parseBlock();
        return new ASTNode.WhileLoop(condition, body);
    }

    /**
     * <if_stmt> ::= "if" <condition> NEWLINE <block>
     *             | "if" <condition> NEWLINE <block> "else" NEWLINE <block>
     */
    private ASTNode.IfStmt parseIf() {
        expect(TokenType.IF);
        ASTNode.Condition condition = parseCondition();
        consumeNewline();
        ASTNode.Block thenBlock = parseBlock();

        ASTNode.Block elseBlock = null;
        skipNewlines();
        if (check(TokenType.ELSE)) {
            advance(); // consume "else"
            consumeNewline();
            elseBlock = parseBlock();
        }
        return new ASTNode.IfStmt(condition, thenBlock, elseBlock);
    }

    /**
     * <block> ::= INDENT <statement_list> DEDENT
     */
    private ASTNode.Block parseBlock() {
        expect(TokenType.INDENT);
        List<ASTNode> stmts = parseStatementList(true);
        expect(TokenType.DEDENT);
        return new ASTNode.Block(stmts);
    }

    /**
     * <condition> ::= "not" <condition>                        -> NotCondition
     *               | <expression> <comparison_op> <expression> -> ComparisonCondition
     *               | <function_call>                          -> BooleanCondition
     *               | <identifier>                             -> BooleanCondition
     *
     * Always returns a concrete ASTNode.Condition — no casting needed by callers.
     */
    private ASTNode.Condition parseCondition() {
        // "not" <condition>  ->  NotCondition
        if (check(TokenType.NOT)) {
            advance();
            ASTNode.Condition operand = parseCondition();
            return new ASTNode.NotCondition(operand);
        }

        // Parse left-hand expression first
        ASTNode left = parseExpression();

        // expr <comparison_op> expr  ->  ComparisonCondition
        if (isComparisonOp(current.type)) {
            String op = comparisonOpString(current.type);
            advance();
            ASTNode right = parseExpression();
            return new ASTNode.ComparisonCondition(op, left, right);
        }

        // bare identifier or function call used as boolean  ->  BooleanCondition
        return new ASTNode.BooleanCondition(left);
    }

    private boolean isComparisonOp(TokenType t) {
        switch (t) {
            case EQ: case NEQ: case LT: case GT: case LTE: case GTE: return true;
            default: return false;
        }
    }

    private String comparisonOpString(TokenType t) {
        switch (t) {
            case EQ:  return "==";
            case NEQ: return "!=";
            case LT:  return "<";
            case GT:  return ">";
            case LTE: return "<=";
            case GTE: return ">=";
            default:  throw new ParseException("Not a comparison op: " + t);
        }
    }

    /**
     * <expression> ::= <term> (("+"|"-") <term>)*
     */
    private ASTNode parseExpression() {
        ASTNode left = parseTerm();
        while (check(TokenType.PLUS) || check(TokenType.MINUS)) {
            String op = check(TokenType.PLUS) ? "+" : "-";
            advance();
            ASTNode right = parseTerm();
            left = new ASTNode.BinaryOp(op, left, right);
        }
        return left;
    }

    /**
     * <term> ::= <factor> (("*"|"/") <factor>)*
     */
    private ASTNode parseTerm() {
        ASTNode left = parseFactor();
        while (check(TokenType.STAR) || check(TokenType.SLASH)) {
            String op = check(TokenType.STAR) ? "*" : "/";
            advance();
            ASTNode right = parseFactor();
            left = new ASTNode.BinaryOp(op, left, right);
        }
        return left;
    }

    /**
     * <factor> ::= <literal>
     *            | <identifier>
     *            | <special_object>    IDENT "." IDENT
     *            | <function_call>     IDENT "(" ... ")"
     *            | "(" <expression> ")"
     *            | "-" <factor>
     */
    private ASTNode parseFactor() {
        // Unary minus
        if (check(TokenType.MINUS)) {
            advance();
            ASTNode operand = parseFactor();
            return new ASTNode.UnaryMinus(operand);
        }

        // Parenthesised expression
        if (check(TokenType.LPAREN)) {
            advance();
            ASTNode expr = parseExpression();
            expect(TokenType.RPAREN);
            return expr;
        }

        // Numeric literal
        if (check(TokenType.NUMBER)) {
            int val = Integer.parseInt(current.literal);
            advance();
            return new ASTNode.NumberLiteral(val);
        }

        // String literal
        if (check(TokenType.STRING)) {
            String val = current.literal;
            advance();
            return new ASTNode.StringLiteral(val);
        }

        // Boolean literals
        if (check(TokenType.TRUE)) {
            advance();
            return new ASTNode.BooleanLiteral(true);
        }
        if (check(TokenType.FALSE)) {
            advance();
            return new ASTNode.BooleanLiteral(false);
        }

        // IDENT "(" ... ")"  - function call
        // IDENT "." IDENT    - special object
        // IDENT              - plain identifier
        if (check(TokenType.IDENT)) {
            String name = current.literal;
            advance();

            if (check(TokenType.LPAREN)) {
                return parseFunctionCallTail(name);
            }

            if (check(TokenType.DOT)) {
                advance(); // consume "."
                String field = expect(TokenType.IDENT).literal;
                return new ASTNode.SpecialObject(name, field);
            }

            return new ASTNode.Identifier(name);
        }

        throw new ParseException("Unexpected token in expression: " + current.type
                + (current.literal != null ? " ('" + current.literal + "')" : ""));
    }

    /**
     * Called after the function name has already been consumed.
     * <function_call> ::= IDENT "()" | IDENT "(" <argument_list> ")"
     */
    private ASTNode.FunctionCall parseFunctionCallTail(String name) {
        expect(TokenType.LPAREN);
        List<ASTNode> args = new ArrayList<>();
        if (!check(TokenType.RPAREN)) {
            args = parseArgumentList();
        }
        expect(TokenType.RPAREN);
        return new ASTNode.FunctionCall(name, args);
    }

    /**
     * <argument_list> ::= <expression> ("," <expression>)*
     */
    private List<ASTNode> parseArgumentList() {
        List<ASTNode> args = new ArrayList<>();
        args.add(parseExpression());
        while (check(TokenType.COMMA)) {
            advance(); // consume ","
            args.add(parseExpression());
        }
        return args;
    }

    // Consume a NEWLINE token if present. At EOF a missing NEWLINE is tolerated.
    private void consumeNewline() {
        if (check(TokenType.NEWLINE)) {
            advance();
        } else if (!check(TokenType.EOF)) {
            throw new ParseException("Expected NEWLINE after statement, got: " + current.type);
        }
    }

    // Unchecked exception thrown on any syntax error
    public static class ParseException extends RuntimeException {
        public ParseException(String message) { super(message); }
    }
}