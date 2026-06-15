package com.wonkglorg.minecraft.config.lang.parser.bool;

import java.util.List;

public final class BooleanConditionParser{
	
	private final List<Token> tokens;
	private int pos = 0;
	
	public BooleanConditionParser(String input) {
		this.tokens = new Lexer(input).tokenize();
	}
	
	public boolean parse() {
		return parseOr();
	}
	
	private boolean parseOr() {
		boolean result = parseAnd();
		
		while(match(TokenType.OR)){
			result = result || parseAnd();
		}
		
		return result;
	}
	
	private boolean parseAnd() {
		boolean result = parseFactor();
		
		while(match(TokenType.AND)){
			result = result && parseFactor();
		}
		
		return result;
	}
	
	private boolean parseFactor() {
		
		if(match(TokenType.NOT)){
			return !parseFactor();
		}
		
		if(match(TokenType.LPAREN)){
			boolean result = parseOr();
			expect(TokenType.RPAREN);
			return result;
		}
		
		return parseValue();
	}
	
	private boolean parseValue() {
		
		Token left = next();
		if(left.type() == TokenType.EOF){
			return false;
		}
		
		Token op = peek();
		
		if(isOperator(op.type())){
			next(); // consume operator
			
			Token right = next();
			if(right.type() == TokenType.EOF){
				return false;
			}
			
			return evaluate(left.text(), op.type(), right.text());
		}
		
		// interpret literal
		if(left.type() == TokenType.IDENTIFIER || left.type() == TokenType.NUMBER){
			return Boolean.parseBoolean(left.text());
		}
		
		return false;
	}
	
	private boolean evaluate(String left, TokenType op, String right) {
		
		return switch(op) {
			
			case EQ -> left.equals(right);
			case NEQ -> !left.equals(right);
			
			case GT -> toDouble(left) > toDouble(right);
			case LT -> toDouble(left) < toDouble(right);
			case GTE -> toDouble(left) >= toDouble(right);
			case LTE -> toDouble(left) <= toDouble(right);
			
			default -> false;
		};
	}
	
	private double toDouble(String value) {
		try{
			return Double.parseDouble(value);
		} catch(Exception ex){
			return 0;
		}
	}
	
	private boolean isOperator(TokenType type) {
		return switch(type) {
			case EQ, NEQ, GT, LT, GTE, LTE -> true;
			default -> false;
		};
	}
	
	private boolean match(TokenType type) {
		if(peek().type() == type){
			pos++;
			return true;
		}
		return false;
	}
	
	private void expect(TokenType type) {
		if(!match(type)){
			throw new IllegalStateException("Expected " + type + " but found " + peek().type());
		}
	}
	
	private Token peek() {
		return pos < tokens.size() ? tokens.get(pos) : new Token(TokenType.EOF, "");
	}
	
	private Token next() {
		return pos < tokens.size() ? tokens.get(pos++) : new Token(TokenType.EOF, "");
	}
}