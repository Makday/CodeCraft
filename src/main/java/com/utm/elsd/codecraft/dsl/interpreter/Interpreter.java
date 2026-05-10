package com.utm.elsd.codecraft.dsl.interpreter;

import com.utm.elsd.codecraft.api.Action;
import com.utm.elsd.codecraft.context.MinecraftContext;
import com.utm.elsd.codecraft.dsl.ast.ASTNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Walks the AST and executes the program.
 *
 * The interpreter is intentionally data-driven:
 * - AST evaluation is handled here.
 * - Runtime functions are provided via {@link FunctionRegistry}.
 * - Special object resolution is provided via {@link SpecialObjectResolver}.
 *
 * This keeps the interpreter extensible without modifying the parser or AST.
 */
public final class Interpreter {
    private final FunctionRegistry functionRegistry;
    private final SpecialObjectResolver specialObjectResolver;
    private final Consumer<String> logger;

    private Interpreter(FunctionRegistry functionRegistry, SpecialObjectResolver specialObjectResolver, Consumer<String> logger) {
        this.functionRegistry = Objects.requireNonNull(functionRegistry, "functionRegistry");
        this.specialObjectResolver = Objects.requireNonNull(specialObjectResolver, "specialObjectResolver");
        this.logger = logger;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Interpreter standard() {
        return builder().withStandardLibrary().build();
    }

    public InterpreterResult execute(ASTNode.Program program) {
        return execute(program, null);
    }

    public InterpreterResult execute(ASTNode.Program program, MinecraftContext minecraftContext) {
        Objects.requireNonNull(program, "program");

        Environment environment = new Environment();
        List<Action> actions = new ArrayList<>();
        List<String> logs = new ArrayList<>();
        Value lastValue = Value.nullValue();
        boolean stopped = false;

        try {
            lastValue = executeStatements(program.statements, environment, minecraftContext, actions, logs);
        } catch (StopSignal stopSignal) {
            stopped = true;
        }

        return new InterpreterResult(lastValue, actions, environment.snapshot(), logs, stopped);
    }

    private Value executeStatements(
            List<ASTNode> statements,
            Environment environment,
            MinecraftContext minecraftContext,
            List<Action> actions,
            List<String> logs
    ) {
        Value last = Value.nullValue();
        for (ASTNode statement : statements) {
            last = executeStatement(statement, environment, minecraftContext, actions, logs);
        }
        return last;
    }

    private Value executeStatement(
            ASTNode node,
            Environment environment,
            MinecraftContext minecraftContext,
            List<Action> actions,
            List<String> logs
    ) {
        if (node instanceof ASTNode.Program program) {
            return executeStatements(program.statements, environment, minecraftContext, actions, logs);
        }
        if (node instanceof ASTNode.Block block) {
            return executeStatements(block.statements, environment, minecraftContext, actions, logs);
        }
        if (node instanceof ASTNode.Comment) {
            return Value.nullValue();
        }
        if (node instanceof ASTNode.Assignment assignment) {
            Value value = evaluateExpression(assignment.value, environment, minecraftContext, actions, logs);
            environment.assign(assignment.name, value);
            return value;
        }
        if (node instanceof ASTNode.ForCount forCount) {
            long count = evaluateExpression(forCount.count, environment, minecraftContext, actions, logs).asLong();
            if (count < 0) {
                throw new InterpreterException("for ... times expects a non-negative count, got: " + count);
            }

            Value last = Value.nullValue();
            for (long i = 0; i < count; i++) {
                last = executeStatements(forCount.body.statements, environment, minecraftContext, actions, logs);
            }
            return last;
        }
        if (node instanceof ASTNode.ForRange forRange) {
            long from = evaluateExpression(forRange.from, environment, minecraftContext, actions, logs).asLong();
            long to = evaluateExpression(forRange.to, environment, minecraftContext, actions, logs).asLong();

            // The DSL examples treat the upper bound as exclusive.
            Value last = Value.nullValue();
            for (long i = from; i < to; i++) {
                environment.assign(forRange.variable, Value.of(i));
                last = executeStatements(forRange.body.statements, environment, minecraftContext, actions, logs);
            }
            return last;
        }
        if (node instanceof ASTNode.WhileLoop whileLoop) {
            Value last = Value.nullValue();
            while (evaluateCondition(whileLoop.condition, environment, minecraftContext, actions, logs).isTruthy()) {
                last = executeStatements(whileLoop.body.statements, environment, minecraftContext, actions, logs);
            }
            return last;
        }
        if (node instanceof ASTNode.IfStmt ifStmt) {
            boolean condition = evaluateCondition(ifStmt.condition, environment, minecraftContext, actions, logs).isTruthy();
            if (condition) {
                return executeStatements(ifStmt.thenBlock.statements, environment, minecraftContext, actions, logs);
            }
            if (ifStmt.elseBlock != null) {
                return executeStatements(ifStmt.elseBlock.statements, environment, minecraftContext, actions, logs);
            }
            return Value.nullValue();
        }
        if (node instanceof ASTNode.StopStmt) {
            throw StopSignal.INSTANCE;
        }
        if (node instanceof ASTNode.FunctionCall functionCall) {
            return invokeFunction(functionCall, environment, minecraftContext, actions, logs);
        }
        if (node instanceof ASTNode.Condition condition) {
            return evaluateCondition(condition, environment, minecraftContext, actions, logs);
        }

        return evaluateExpression(node, environment, minecraftContext, actions, logs);
    }

    private Value evaluateCondition(
            ASTNode.Condition condition,
            Environment environment,
            MinecraftContext minecraftContext,
            List<Action> actions,
            List<String> logs
    ) {
        if (condition instanceof ASTNode.NotCondition notCondition) {
            return Value.of(!evaluateCondition(notCondition.operand, environment, minecraftContext, actions, logs).isTruthy());
        }
        if (condition instanceof ASTNode.LogicalCondition logical) {
            if ("and".equals(logical.op)) {
                // short-circuit AND
                boolean left = evaluateCondition(logical.left, environment, minecraftContext, actions, logs).isTruthy();
                if (!left) return Value.of(false);
                return Value.of(evaluateCondition(logical.right, environment, minecraftContext, actions, logs).isTruthy());
            } else if ("or".equals(logical.op)) {
                // short-circuit OR
                boolean left = evaluateCondition(logical.left, environment, minecraftContext, actions, logs).isTruthy();
                if (left) return Value.of(true);
                return Value.of(evaluateCondition(logical.right, environment, minecraftContext, actions, logs).isTruthy());
            } else {
                throw new InterpreterException("Unsupported logical operator: " + logical.op);
            }
        }
        if (condition instanceof ASTNode.ComparisonCondition comparisonCondition) {
            Value left = evaluateExpression(comparisonCondition.left, environment, minecraftContext, actions, logs);
            Value right = evaluateExpression(comparisonCondition.right, environment, minecraftContext, actions, logs);
            return Value.of(compare(comparisonCondition.op, left, right));
        }
        if (condition instanceof ASTNode.BooleanCondition booleanCondition) {
            return Value.of(evaluateExpression(booleanCondition.expr, environment, minecraftContext, actions, logs).isTruthy());
        }

        throw new InterpreterException("Unsupported condition type: " + condition.getClass().getSimpleName());
    }

    private Value evaluateExpression(
            ASTNode expression,
            Environment environment,
            MinecraftContext minecraftContext,
            List<Action> actions,
            List<String> logs
    ) {
        if (expression instanceof ASTNode.NumberLiteral numberLiteral) {
            return Value.of(numberLiteral.value);
        }
        if (expression instanceof ASTNode.StringLiteral stringLiteral) {
            return Value.of(stringLiteral.value);
        }
        if (expression instanceof ASTNode.BooleanLiteral booleanLiteral) {
            return Value.of(booleanLiteral.value);
        }
        if (expression instanceof ASTNode.Identifier identifier) {
            return environment.get(identifier.name);
        }
        if (expression instanceof ASTNode.SpecialObject specialObject) {
            ExecutionContext context = baseContext(environment, minecraftContext, actions, logs, List.of());
            return specialObjectResolver.resolve(specialObject.object, specialObject.field, context);
        }
        if (expression instanceof ASTNode.UnaryMinus unaryMinus) {
            return Value.of(-evaluateExpression(unaryMinus.operand, environment, minecraftContext, actions, logs).asLong());
        }
        if (expression instanceof ASTNode.BinaryOp binaryOp) {
            Value left = evaluateExpression(binaryOp.left, environment, minecraftContext, actions, logs);
            Value right = evaluateExpression(binaryOp.right, environment, minecraftContext, actions, logs);
            return switch (binaryOp.op) {
                case "+" -> add(left, right);
                case "-" -> Value.of(left.asLong() - right.asLong());
                case "*" -> Value.of(left.asLong() * right.asLong());
                case "/" -> {
                    long divisor = right.asLong();
                    if (divisor == 0) {
                        throw new InterpreterException("Division by zero");
                    }
                    yield Value.of(left.asLong() / divisor);
                }
                case "%" -> {
                    long divisor = right.asLong();
                    if (divisor == 0) {
                        throw new InterpreterException("Modulo by zero");
                    }
                    yield Value.of(left.asLong() % divisor);
                }
                default -> throw new InterpreterException("Unsupported operator: " + binaryOp.op);
            };
        }
        if (expression instanceof ASTNode.FunctionCall functionCall) {
            return invokeFunction(functionCall, environment, minecraftContext, actions, logs);
        }
        if (expression instanceof ASTNode.Block block) {
            return executeStatements(block.statements, environment, minecraftContext, actions, logs);
        }
        if (expression instanceof ASTNode.Program program) {
            return executeStatements(program.statements, environment, minecraftContext, actions, logs);
        }

        throw new InterpreterException("Unsupported expression type: " + expression.getClass().getSimpleName());
    }

    private Value invokeFunction(
            ASTNode.FunctionCall functionCall,
            Environment environment,
            MinecraftContext minecraftContext,
            List<Action> actions,
            List<String> logs
    ) {
        List<Value> arguments = new ArrayList<>(functionCall.arguments.size());
        for (ASTNode argument : functionCall.arguments) {
            arguments.add(evaluateExpression(argument, environment, minecraftContext, actions, logs));
        }

        ExecutionContext context = baseContext(environment, minecraftContext, actions, logs, arguments);
        return functionRegistry.invoke(functionCall.name, context);
    }

    private ExecutionContext baseContext(
            Environment environment,
            MinecraftContext minecraftContext,
            List<Action> actions,
            List<String> logs,
            List<Value> arguments
    ) {
        return new ExecutionContext(this, environment, minecraftContext, actions, logs, arguments, logger);
    }

    private Value add(Value left, Value right) {
        if (left.isString() || right.isString()) {
            return Value.of(left.asString() + right.asString());
        }
        return Value.of(left.asLong() + right.asLong());
    }

    private boolean compare(String operator, Value left, Value right) {
        return switch (operator) {
            case "==" -> valuesEqual(left, right);
            case "!=" -> !valuesEqual(left, right);
            case "<" -> compareOrder(left, right) < 0;
            case ">" -> compareOrder(left, right) > 0;
            case "<=" -> compareOrder(left, right) <= 0;
            case ">=" -> compareOrder(left, right) >= 0;
            default -> throw new InterpreterException("Unsupported comparison operator: " + operator);
        };
    }

    private boolean valuesEqual(Value left, Value right) {
        if (left.isNumber() && right.isNumber()) {
            return Double.compare(left.asLong(), right.asLong()) == 0;
        }
        if (left.isBoolean() && right.isBoolean()) {
            return left.asBoolean() == right.asBoolean();
        }
        if (left.isString() || right.isString()) {
            return left.asString().equals(right.asString());
        }
        return Objects.equals(left.raw(), right.raw());
    }

    private int compareOrder(Value left, Value right) {
        if (left.isNumber() && right.isNumber()) {
            return Long.compare(left.asLong(), right.asLong());
        }
        if (left.isBoolean() && right.isBoolean()) {
            return Boolean.compare(left.asBoolean(), right.asBoolean());
        }
        if (left.isString() || right.isString()) {
            return left.asString().compareTo(right.asString());
        }
        throw new InterpreterException("Values are not comparable: " + left.describe() + " and " + right.describe());
    }

    private static final class StopSignal extends RuntimeException {
        private static final StopSignal INSTANCE = new StopSignal();

        private StopSignal() {
            super(null, null, false, false);
        }
    }

    public static final class Builder {
        private final FunctionRegistry functionRegistry = new FunctionRegistry();
        private SpecialObjectResolver specialObjectResolver = StandardLibrary.defaultSpecialObjectResolver();
        private Consumer<String> logger = null;

        private Builder() {
        }

        public Builder registerFunction(String name, InterpreterFunction function) {
            functionRegistry.register(name, function);
            return this;
        }

        public Builder withStandardLibrary() {
            StandardLibrary.install(this);
            return this;
        }

        public Builder specialObjectResolver(SpecialObjectResolver resolver) {
            this.specialObjectResolver = Objects.requireNonNull(resolver, "resolver");
            return this;
        }

        public Builder logger(Consumer<String> logger) {
            this.logger = logger;
            return this;
        }

        public Interpreter build() {
            return new Interpreter(functionRegistry.copy(), specialObjectResolver, logger);
        }
    }
}


