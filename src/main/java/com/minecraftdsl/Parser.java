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
 *                     | <expression> [<comparison_op> <expression>]
 *
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
    // Parse a sequence of statements if true, stop when DEDENT (or EOF) is seen.
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
     * Parse exactly one statement.
     *
     * <pre>
     * <statement> ::= <assignment> NEWLINE
     *               | <for_count>
     *               | <for_range>
     *               | <while_loop>
     *               | <if_stmt>
     *               | "stop" NEWLINE
     *               | <function_call> NEWLINE
     *               | <comment>
     * </pre>
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
                    // expression statement
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

    // Assignment
    /**
     * <pre>
     * <assignment> ::= IDENT "=" <expression>
     * </pre>
     */
    private ASTNode.Assignment parseAssignment() {
        String name = expect(TokenType.IDENT).literal;
        expect(TokenType.ASSIGN);
        ASTNode value = parseExpression();
        consumeNewline();
        return new ASTNode.Assignment(name, value);
    }


    // For loops
    /**
     * <pre>
     * <for_count> ::= "for" <expression> "times" NEWLINE <block>
     * <for_range> ::= "for" IDENT "from" <expression> "to" <expression> NEWLINE <block>
     * </pre>
     */
    private ASTNode parseFor() {
        expect(TokenType.FOR);

        // Is this a for-range? "for" IDENT "from" …
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


    // While loop
    /**
     * <pre>
     * <while_loop> ::= "while" <condition> NEWLINE <block>
     * </pre>
     */
    private ASTNode.WhileLoop parseWhile() {
        expect(TokenType.WHILE);
        ASTNode condition = parseCondition();
        consumeNewline();
        ASTNode.Block body = parseBlock();
        return new ASTNode.WhileLoop(condition, body);
    }


    // If statement
    /**
     * <pre>
     * <if_stmt> ::= "if" <condition> NEWLINE <block>
     *             | "if" <condition> NEWLINE <block> "else" NEWLINE <block>
     * </pre>
     */
    private ASTNode.IfStmt parseIf() {
        expect(TokenType.IF);
        ASTNode condition = parseCondition();
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


    // Block
    /**
     * <pre>
     * <block> ::= INDENT <statement_list> DEDENT
     * </pre>
     */
    private ASTNode.Block parseBlock() {
        expect(TokenType.INDENT);
        List<ASTNode> stmts = parseStatementList(true);
        expect(TokenType.DEDENT);
        return new ASTNode.Block(stmts);
    }


    // Condition
    /**
     * <pre>
     * <condition> ::= "not" <condition>
     *               | <expression> [<comparison_op> <expression>]
     *               | <function_call>
     *               | <identifier>
     * </pre>
     */
    private ASTNode parseCondition() {
        // "not" <condition>
        if (check(TokenType.NOT)) {
            advance();
            ASTNode operand = parseCondition();
            return new ASTNode.NotExpr(operand);
        }

        ASTNode left = parseExpression();

        // optional comparison operator
        if (isComparisonOp(current.type)) {
            String op = comparisonOpString(current.type);
            advance();
            ASTNode right = parseExpression();
            return new ASTNode.BinaryOp(op, left, right);
        }

        return left;
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

    // Expression  (left-recursive addition/subtraction)
    /**
     * <pre>
     * <expression> ::= <term> (("+"|"-") <term>)*
     * </pre>
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

    // Term
    /**
     * <pre>
     * <term> ::= <factor> (("*"|"/") <factor>)*
     * </pre>
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

    // Factor
    /**
     * <pre>
     * <factor> ::= <literal>
     *            | <identifier>
     *            | <special_object>       -- IDENT "." IDENT
     *            | <function_call>        -- IDENT "(" … ")"
     *            | "(" <expression> ")"
     *            | "-" <factor>
     * </pre>
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
            double val = Double.parseDouble(current.literal);
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

        // IDENT "(" … ")"   - function call
        // IDENT "." IDENT   - special object
        // IDENT             - plain identifier
        if (check(TokenType.IDENT)) {
            String name = current.literal;
            advance();

            // function call
            if (check(TokenType.LPAREN)) {
                return parseFunctionCallTail(name);
            }

            // special object  (IDENT "." IDENT)
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


    // Function call
    /**
     * Called after the function name has already been consumed.
     *
     * <pre>
     * <function_call> ::= IDENT "()"
     *                   | IDENT "(" <argument_list> ")"
     * </pre>
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
     * <pre>
     * <argument_list> ::= <expression> ("," <expression>)*
     * </pre>
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

    // Utility
    // Consume a NEWLINE token if present. At EOF a missing NEWLINE is tolerated
    private void consumeNewline() {
        if (check(TokenType.NEWLINE)) {
            advance();
        } else if (!check(TokenType.EOF)) {
            throw new ParseException("Expected NEWLINE after statement, got: " + current.type);
        }
    }

    // ParseException
    // Unchecked exception thrown on any syntax error
    public static class ParseException extends RuntimeException {
        public ParseException(String message) { super(message); }
    }
}
