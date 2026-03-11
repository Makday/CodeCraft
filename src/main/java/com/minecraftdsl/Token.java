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

    // Constructors for backwards compatibility (default position)
    public Token(TokenType type, String literal) {
        this.type = type;
        this.literal = literal;
        this.line = 0;
        this.col = 0;
    }

    public Token(TokenType type) {
        this.type = type;
        this.literal = null;
        this.line = 0;
        this.col = 0;
    }

    @Override
    public String toString() {
        String location = "[" + line + ":" + col + "]";
        if (literal != null)
            return "Token(" + type + ", " + literal + ", " + location + ")";
        return "Token(" + type + ", " + location + ")";
    }
}