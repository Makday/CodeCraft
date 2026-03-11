package com.minecraftdsl;

public class Token {
    public final TokenType type;
    public final String literal;
    public final int line;
    public final int col;

    public Token(TokenType type, String literal, int line, int col) {
        this.type = type;
        this.literal = literal;
        this.line = line;
        this.col = col;
    }

    public Token(TokenType type, int line, int col) {
        this.type = type;
        this.literal = null;
        this.line = line;
        this.col = col;
    }

    @Override
    public String toString() {
        if (literal != null)
            return "Token(" + type + ", " + literal + ")";
        return "Token(" + type + ")";
    }
}