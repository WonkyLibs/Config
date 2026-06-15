package com.wonkglorg.minecraft.config.lang.parser.bool;

import java.util.ArrayList;
import java.util.List;

public final class Lexer {

    private final String input;
    private int pos = 0;
    private final int length;

    public Lexer(String input) {
        this.input = input;
        this.length = input.length();
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (!isAtEnd()) {
            char c = peek();

            // skip whitespace
            if (Character.isWhitespace(c)) {
                advance();
                continue;
            }

            // multi-char operators first
            if (match("&&")) {
                tokens.add(new Token(TokenType.AND, "&&"));
                continue;
            }
            if (match("||")) {
                tokens.add(new Token(TokenType.OR, "||"));
                continue;
            }
            if (match("==")) {
                tokens.add(new Token(TokenType.EQ, "=="));
                continue;
            }
            if (match("!=")) {
                tokens.add(new Token(TokenType.NEQ, "!="));
                continue;
            }
            if (match(">=")) {
                tokens.add(new Token(TokenType.GTE, ">="));
                continue;
            }
            if (match("<=")) {
                tokens.add(new Token(TokenType.LTE, "<="));
                continue;
            }

            // single-char tokens
            switch (c) {
                case '!' -> {
                    advance();
                    tokens.add(new Token(TokenType.NOT, "!"));
                }
                case '>' -> {
                    advance();
                    tokens.add(new Token(TokenType.GT, ">"));
                }
                case '<' -> {
                    advance();
                    tokens.add(new Token(TokenType.LT, "<"));
                }
                case '(' -> {
                    advance();
                    tokens.add(new Token(TokenType.LPAREN, "("));
                }
                case ')' -> {
                    advance();
                    tokens.add(new Token(TokenType.RPAREN, ")"));
                }
                default -> {
                    if (Character.isDigit(c)) {
                        tokens.add(number());
                    } else {
                        tokens.add(identifier());
                    }
                }
            }
        }

        tokens.add(new Token(TokenType.EOF, ""));
        return tokens;
    }

    private Token number() {
        int start = pos;

        while (!isAtEnd() && Character.isDigit(peek())) {
            advance();
        }

        return new Token(TokenType.NUMBER, input.substring(start, pos));
    }

    private Token identifier() {
        int start = pos;

        while (!isAtEnd() && (Character.isLetterOrDigit(peek()) || peek() == '_')) {
            advance();
        }

        return new Token(TokenType.IDENTIFIER, input.substring(start, pos));
    }

    private boolean match(String expected) {
        if (input.startsWith(expected, pos)) {
            pos += expected.length();
            return true;
        }
        return false;
    }

    private char peek() {
        return input.charAt(pos);
    }

    private void advance() {
        pos++;
    }

    private boolean isAtEnd() {
        return pos >= length;
    }
}