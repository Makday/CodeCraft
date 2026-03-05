package com.minecraftdsl;

import java.util.List;

public abstract class ASTNode {
    public abstract String toTree(String indent);

    @Override
    public String toString() {
        return toTree("");
    }

    // Root node that holds a list of top-level statements
    public static class Program extends ASTNode {
        public final List<ASTNode> statements;
        public Program(List<ASTNode> statements) { this.statements = statements; }

        @Override public String toTree(String indent) {
            StringBuilder sb = new StringBuilder(indent + "Program\n");
            for (ASTNode s : statements) sb.append(s.toTree(indent + "  ")).append("\n");
            return sb.toString().stripTrailing();
        }
    }

    // Holding an ordered list of statements
    public static class Block extends ASTNode {
        public final List<ASTNode> statements;
        public Block(List<ASTNode> statements) { this.statements = statements; }

        @Override public String toTree(String indent) {
            StringBuilder sb = new StringBuilder(indent + "Block\n");
            for (ASTNode s : statements) sb.append(s.toTree(indent + "  ")).append("\n");
            return sb.toString().stripTrailing();
        }
    }

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
        public final ASTNode condition;
        public final Block body;
        public WhileLoop(ASTNode condition, Block body) { this.condition = condition; this.body = body; }

        @Override public String toTree(String indent) {
            return indent + "While\n"
                    + indent + "  cond:\n" + condition.toTree(indent + "    ") + "\n"
                    + body.toTree(indent + "  ");
        }
    }


}