package com.utm.elsd.codecraft.dsl.interpreter;

import com.utm.elsd.codecraft.implementation.inventory.atoms.ToolBarAction;
import com.utm.elsd.codecraft.implementation.movement.atoms.MoveForwardAction;
import com.utm.elsd.codecraft.implementation.player.atoms.TurnAction;
import com.utm.elsd.codecraft.implementation.player.atoms.UseAction;
import com.utm.elsd.codecraft.dsl.ast.ASTNode;
import com.utm.elsd.codecraft.dsl.lexer.Lexer;
import com.utm.elsd.codecraft.dsl.parser.Parser;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InterpreterTest {

    @Test
    void supportsWhileNotAndAssignments() {
        String source = """
                counter = 0
                while not counter == 3
                    counter = counter + 1
                check(counter)
                """;

        List<String> seen = new ArrayList<>();
        Interpreter interpreter = Interpreter.builder()
                .registerFunction("check", ctx -> {
                    seen.add(ctx.argument(0).asString());
                    return Value.nullValue();
                })
                .build();

        InterpreterResult result = interpreter.execute(parse(source));

        assertEquals(3L, result.variables().get("counter").asLong());
        assertEquals(List.of("3"), seen);
        assertFalse(result.stopped());
    }

    @Test
    void stopBreaksOutOfNestedControlFlow() {
        String source = """
                value = 0
                for 5 times
                    value = value + 1
                    if value == 3
                        stop
                after(value)
                """;

        List<String> seen = new ArrayList<>();
        Interpreter interpreter = Interpreter.builder()
                .registerFunction("after", ctx -> {
                    seen.add(ctx.argument(0).asString());
                    return Value.nullValue();
                })
                .build();

        InterpreterResult result = interpreter.execute(parse(source));

        assertEquals(3L, result.variables().get("value").asLong());
        assertTrue(result.stopped());
        assertTrue(seen.isEmpty());
    }

    @Test
    void standardLibraryTurnsDslCallsIntoActions() {
        ASTNode.Program program = parse("""
                tool_bar(2)
                move_forward(3)
                turn_left()
                turn_right()
                use()
                """);

        InterpreterResult result = Interpreter.standard().execute(program);

        assertEquals(5, result.actions().size());
        assertInstanceOf(ToolBarAction.class, result.actions().get(0));
        assertInstanceOf(MoveForwardAction.class, result.actions().get(1));
        assertInstanceOf(TurnAction.class, result.actions().get(2));
        assertEquals(-90, ((TurnAction) result.actions().get(2)).degrees());
        assertInstanceOf(TurnAction.class, result.actions().get(3));
        assertEquals(90, ((TurnAction) result.actions().get(3)).degrees());
        assertInstanceOf(UseAction.class, result.actions().get(4));
    }

    @Test
    void supportsModuloOperator() {
        ASTNode.Program program = parse("""
                value = 10 % 3
                check(value)
                """);

        List<String> seen = new ArrayList<>();
        Interpreter interpreter = Interpreter.builder()
                .registerFunction("check", ctx -> {
                    seen.add(ctx.argument(0).asString());
                    return Value.nullValue();
                })
                .build();

        InterpreterResult result = interpreter.execute(program);

        assertEquals(1L, result.variables().get("value").asLong());
        assertEquals(List.of("1"), seen);
        assertFalse(result.stopped());
    }

    @Test
    void moduloByZeroFails() {
        ASTNode.Program program = parse("""
                value = 10 % 0
                """);

        Interpreter interpreter = Interpreter.standard();

        assertThrows(InterpreterException.class, () -> interpreter.execute(program));
    }

    private ASTNode.Program parse(String source) {
        return new Parser(new Lexer(source).tokenize()).parseProgram();
    }
}

