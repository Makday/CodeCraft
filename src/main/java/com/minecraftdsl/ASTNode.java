package com.minecraftdsl;

import java.util.List;

public abstract class ASTNode {
    public abstract String toTree(String indent);

    @Override
    public String toString() {
        return toTree("");
    }

    public static class Program extends ASTNode {
        public final List<ASTNode> statements;
        public Program(List<ASTNode> statements) { this.statements = statements; }

        @Override public String toTree(String indent) {
            StringBuilder sb = new StringBuilder(indent + "Program\n");
            for (ASTNode s : statements) sb.append(s.toTree(indent + "  ")).append("\n");
            return sb.toString().stripTrailing();
        }
    }

    public static class Block extends ASTNode {
        public final List<ASTNode> statements;
        public Block(List<ASTNode> statements) { this.statements = statements; }

        @Override public String toTree(String indent) {
            StringBuilder sb = new StringBuilder(indent + "Block\n");
            for (ASTNode s : statements) sb.append(s.toTree(indent + "  ")).append("\n");
            return sb.toString().stripTrailing();
        }
    }

    // variable = expression
    public static class Assignment extends ASTNode {
        public final String name;
        public final ASTNode value;
        public Assignment(String name, ASTNode value) { this.name = name; this.value = value; }

        @Override public String toTree(String indent) {
            return indent + "Assign(" + name + ")\n" + value.toTree(indent + "  ");
        }
    }

    // for <expr> times { block }
    public static class ForCount extends ASTNode {
        public final ASTNode count;
        public final Block body;
        public ForCount(ASTNode count, Block body) { this.count = count; this.body = body; }

        @Override public String toTree(String indent) {
            return indent + "ForCount\n"
                    + indent + "  count:\n" + count.toTree(indent + "    ") + "\n"
                    + body.toTree(indent + "  ");
        }
    }

    // for <ident> from <expr> to <expr> { block }
    public static class ForRange extends ASTNode {
        public final String variable;
        public final ASTNode from;
        public final ASTNode to;
        public final Block body;
        public ForRange(String variable, ASTNode from, ASTNode to, Block body) {
            this.variable = variable; this.from = from; this.to = to; this.body = body;
        }

        @Override public String toTree(String indent) {
            return indent + "ForRange(" + variable + ")\n"
                    + indent + "  from:\n" + from.toTree(indent + "    ") + "\n"
                    + indent + "  to:\n"   + to.toTree(indent + "    ")   + "\n"
                    + body.toTree(indent + "  ");
        }
    }

    // while <condition> { block }
    public static class WhileLoop extends ASTNode {
        public final Condition condition;
        public final Block body;
        public WhileLoop(Condition condition, Block body) { this.condition = condition; this.body = body; }

        @Override public String toTree(String indent) {
            return indent + "While\n"
                    + indent + "  cond:\n" + condition.toTree(indent + "    ") + "\n"
                    + body.toTree(indent + "  ");
        }
    }

    // if <condition> { block } [ else { block } ]
    public static class IfStmt extends ASTNode {
        public final Condition condition;
        public final Block thenBlock;
        public final Block elseBlock; // may be null
        public IfStmt(Condition condition, Block thenBlock, Block elseBlock) {
            this.condition = condition; this.thenBlock = thenBlock; this.elseBlock = elseBlock;
        }

        @Override public String toTree(String indent) {
            StringBuilder sb = new StringBuilder(indent + "If\n");
            sb.append(indent).append("  cond:\n").append(condition.toTree(indent + "    ")).append("\n");
            sb.append(indent).append("  then:\n").append(thenBlock.toTree(indent + "    "));
            if (elseBlock != null) sb.append("\n").append(indent).append("  else:\n").append(elseBlock.toTree(indent + "    "));
            return sb.toString();
        }
    }

    // Abstract base for all condition nodes.
    // using a dedicated type means callers never need to cast — if you have a
    // {@code Condition} you know it came from {@code parseCondition()}.
    public static abstract class Condition extends ASTNode {}

    public static class ComparisonCondition extends Condition {
        public final String op;
        public final ASTNode left;
        public final ASTNode right;
        public ComparisonCondition(String op, ASTNode left, ASTNode right) {
            this.op = op; this.left = left; this.right = right;
        }

        @Override public String toTree(String indent) {
            return indent + "Comparison(" + op + ")\n"
                    + left.toTree(indent + "  ") + "\n"
                    + right.toTree(indent + "  ");
        }
    }

    // not <condition>
    public static class NotCondition extends Condition {
        public final Condition operand;
        public NotCondition(Condition operand) { this.operand = operand; }

        @Override public String toTree(String indent) {
            return indent + "Not\n" + operand.toTree(indent + "  ");
        }
    }

    public static class BooleanCondition extends Condition {
        public final ASTNode expr;
        public BooleanCondition(ASTNode expr) { this.expr = expr; }

        @Override public String toTree(String indent) {
            return indent + "BoolCondition\n" + expr.toTree(indent + "  ");
        }
    }

    public static class StopStmt extends ASTNode {
        @Override public String toTree(String indent) { return indent + "Stop"; }
    }

    public static class Comment extends ASTNode {
        public final String text;
        public Comment(String text) { this.text = text; }

        @Override public String toTree(String indent) { return indent + "Comment(" + text.trim() + ")"; }
    }

    // Expressions

    public static class Identifier extends ASTNode {
        public final String name;
        public Identifier(String name) { this.name = name; }

        @Override public String toTree(String indent) { return indent + "Ident(" + name + ")"; }
    }

    public static class SpecialObject extends ASTNode {
        public final String object;
        public final String field;
        public SpecialObject(String object, String field) { this.object = object; this.field = field; }

        @Override public String toTree(String indent) { return indent + "SpecialObj(" + object + "." + field + ")"; }
    }

    public static class NumberLiteral extends ASTNode {
        public final double value;
        public NumberLiteral(double value) { this.value = value; }

        @Override public String toTree(String indent) {
            if (value == Math.floor(value)) return indent + "Number(" + (long) value + ")";
            return indent + "Number(" + value + ")";
        }
    }

    public static class StringLiteral extends ASTNode {
        public final String value;
        public StringLiteral(String value) { this.value = value; }

        @Override public String toTree(String indent) { return indent + "String(\"" + value + "\")"; }
    }

    public static class BooleanLiteral extends ASTNode {
        public final boolean value;
        public BooleanLiteral(boolean value) { this.value = value; }

        @Override public String toTree(String indent) { return indent + "Bool(" + value + ")"; }
    }

    // left op right  (binary arithmetic or comparison)
    public static class BinaryOp extends ASTNode {
        public final String op;
        public final ASTNode left;
        public final ASTNode right;
        public BinaryOp(String op, ASTNode left, ASTNode right) {
            this.op = op; this.left = left; this.right = right;
        }

        @Override public String toTree(String indent) {
            return indent + "BinaryOp(" + op + ")\n"
                    + left.toTree(indent + "  ") + "\n"
                    + right.toTree(indent + "  ");
        }
    }

    // -factor  (unary negation)
    public static class UnaryMinus extends ASTNode {
        public final ASTNode operand;
        public UnaryMinus(ASTNode operand) { this.operand = operand; }

        @Override public String toTree(String indent) {
            return indent + "UnaryMinus\n" + operand.toTree(indent + "  ");
        }
    }

    // identifier( arg, arg, … )
    public static class FunctionCall extends ASTNode {
        public final String name;
        public final List<ASTNode> arguments;
        public FunctionCall(String name, List<ASTNode> arguments) {
            this.name = name; this.arguments = arguments;
        }

        @Override public String toTree(String indent) {
            StringBuilder sb = new StringBuilder(indent + "Call(" + name + ")");
            for (ASTNode arg : arguments) sb.append("\n").append(arg.toTree(indent + "  "));
            return sb.toString();
        }
    }
}