# BNF GRAMMAR
```
<program>           ::= <statement_list>

<statement_list>    ::= <statement>
                      | <statement> <statement_list>

<statement>         ::= <assignment> NEWLINE
                      | <for_count>
                      | <for_range>
                      | <while_loop>
                      | <if_stmt>
                      | "stop" NEWLINE
                      | <function_call> NEWLINE
                      | <comment>

<assignment>        ::= <identifier> <optional_space> "=" <optional_space> <expression>

<for_count>         ::= "for" <space> <expression> <space> "times" NEWLINE <block>
<for_range>         ::= "for" <space> <identifier> <space> "from" <space> <expression> <space> "to" <space> <expression> NEWLINE <block>
<while_loop>        ::= "while" <space> <condition> NEWLINE <block>
<if_stmt>           ::= "if" <space> <condition> NEWLINE <block>
                      | "if" <space> <condition> NEWLINE <block> "else" NEWLINE <block>

<block>             ::= INDENT <statement_list> DEDENT

-- Function call
<function_call>     ::= <identifier> "()"
                      | <identifier> "(" <argument_list> ")"

<argument_list>     ::= <expression>
                      | <expression> <optional_space> "," <optional_space> <argument_list>

<condition>         ::= <expression> <optional_space> <comparison_op> <optional_space> <expression>
                      | "not" <space> <condition>
                      | <function_call>
                      | <identifier>

<comparison_op>     ::= "==" | "!=" | "<" | ">" | "<=" | ">="

<expression>        ::= <term>
                      | <expression> <optional_space> "+" <optional_space> <term>
                      | <expression> <optional_space> "-" <optional_space> <term>

<term>              ::= <factor>
                      | <term> <optional_space> "*" <optional_space> <factor>
                      | <term> <optional_space> "/" <optional_space> <factor>
```

<div style="page-break-before:always">&nbsp;</div>

```
<factor>            ::= <literal>
                      | <identifier>
                      | <special_object>
                      | <function_call>
                      | "(" <optional_space> <expression> <optional_space> ")"
                      | "-" <factor>

<special_object>    ::= <identifier>"."<identifier>

<literal>           ::= <number>
                      | <string>
                      | <boolean>

<boolean>           ::= "true" | "false"
<number>            ::= <digit> | <digit> <number>
<digit>             ::= "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"
<string>            ::= "\"" <characters> "\""
<characters>        ::= ɛ | <character> <characters>
<character>         ::= <letter> | <special_char>

<identifier>        ::= <letter> | <letter> <id_tail>
<id_tail>           ::= <id_char> | <id_char> <id_tail>
<id_char>           ::= <letter> | <digit> | "_"

<letter>            ::= <lower> | <upper>

<lower>             ::= "a" | "b" | "c" | "d" | "e" | "f" | "g" | "h" | "i" | "j" | "k" | "l" | "m"
                      | "n" | "o" | "p" | "q" | "r" | "s" | "t" | "u" | "v" | "w" | "x" | "y" | "z"

<upper>             ::= "A" | "B" | "C" | "D" | "E" | "F" | "G" | "H" | "I" | "J" | "K" | "L" | "M"
                      | "N" | "O" | "P" | "Q" | "R" | "S" | "T" | "U" | "V" | "W" | "X" | "Y" | "Z"

<special_char>      ::= "!" | "\"" | "#" | "$" | "%" | "&" | "'" | "(" | ")" 
                      | "*" | "+" | "," | "-" | "." | "/" | ":" | ";" | "<" 
                      | "=" | ">" | "?" | "@" | "[" | "\\" | "]" | "^" | "_" 
                      | "`" | "{" | "|" | "}" | "~" | <space>

<space>             ::= " "
<optional_space>    ::= <space> | ɛ

<comment>           ::= "--" <characters> NEWLINE

```