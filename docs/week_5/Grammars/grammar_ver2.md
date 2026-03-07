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
                      | stop NEWLINE
                      | <function_call> NEWLINE
                      | <comment>

<assignment>        ::= <identifier> = <expression>

<for_count>         ::= for <expression> times NEWLINE <block>
<for_range>         ::= for <identifier> from <expression> to <expression> NEWLINE <block>
<while_loop>        ::= while <condition> NEWLINE <block>
<if_stmt>           ::= if <condition> NEWLINE <block>
                      | if <condition> NEWLINE <block> else NEWLINE <block>

<block>             ::= INDENT <statement_list> DEDENT

-- Function call
<function_call>     ::= <identifier>()
                      | <identifier>(<argument_list>)

<argument_list>     ::= <expression>
                      | <expression> , <argument_list>

<condition>         ::= <expression> <comparison_op> <expression>
                      | not <condition>
                      | <function_call>
                      | <identifier>

<comparison_op>     ::= == | != | < | > | <= | >=


<expression>        ::= <term>
                      | <expression> + <term>
                      | <expression> - <term>

<term>              ::= <factor>
                      | <term> * <factor>
                      | <term> / <factor>

<factor>            ::= <literal>
                      | <identifier>
                      | <state_access>
                      | <items_access>
                      | <function_call>
                      | ( <expression> )
                      | - <factor>


<state_access>      ::= state.<identifier>
<items_access>      ::= items.<identifier>

<literal>           ::= <number>
                      | <string>
                      | <boolean>

<boolean>           ::= true | false
<number>            ::= <digit> | <digit> <number>
<digit>             ::= 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9
<string>            ::= "<characters>"
<characters>        ::= EMPTY | <character> <characters>
<character>         ::= any character except '"'

<identifier>        ::= <letter> | <letter> <id_tail>
<id_tail>           ::= <id_char> | <id_char> <id_tail>
<id_char>           ::= <letter> | <digit> | _

<letter>            ::= <lower> | <upper>

<lower>             ::= a | b | c | d | e | f | g | h | i | j | k | l | m
                      | n | o | p | q | r | s | t | u | v | w | x | y | z

<upper>             ::= A | B | C | D | E | F | G | H | I | J | K | L | M
                      | N | O | P | Q | R | S | T | U | V | W | X | Y | Z

<comment>           ::= -- <characters> NEWLINE

```