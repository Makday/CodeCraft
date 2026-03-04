package com.minecraftdsl;

public class Main {
    public static void main(String[] args) {
        System.out.println("Whatever");
        TokenType tp = Keywords.lookup("for");
        System.out.println(tp.toString());

        //just a template for future finished program
    }
}
