package com.minecraftdsl;

public class Main {
    public static void main(String[] args) {
        String src = """
                -- User text something.
                for i from 1 to 5
                \tlog("hello")
                while x > 0
                 x = x - 1
                 y =0
                 if j < 3
                   t =3
                   p=0
                done = true
                """;

        Lexer lexer = new Lexer(src);
        Parser parser = new Parser(lexer.tokenize());
        System.out.println(parser.parseProgram());
    }
}
