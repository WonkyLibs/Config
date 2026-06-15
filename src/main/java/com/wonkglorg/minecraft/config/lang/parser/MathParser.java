package com.wonkglorg.minecraft.config.lang.parser;

public final class MathParser{
	
	private final String input;
	private int pos = -1;
	private int ch;
	
	public MathParser(String input) {
		this.input = input;
	}
	
	public double parse() {
		nextChar();
		
		double value = parseExpression();
		
		if(pos < input.length()){
			throw new IllegalArgumentException("Unexpected character: " + (char) ch);
		}
		
		return value;
	}
	
	private void nextChar() {
		ch = (++pos < input.length()) ? input.charAt(pos) : -1;
	}
	
	private boolean eat(int charToEat) {
		
		while(ch == ' '){
			nextChar();
		}
		
		if(ch == charToEat){
			nextChar();
			return true;
		}
		
		return false;
	}
	
	private double parseExpression() {
		
		double value = parseTerm();
		
		while(true){
			
			if(eat('+')){
				value += parseTerm();
			} else if(eat('-')){
				value -= parseTerm();
			} else {
				return value;
			}
		}
	}
	
	private double parseTerm() {
		
		double value = parseFactor();
		
		while(true){
			
			if(eat('*')){
				value *= parseFactor();
			} else if(eat('/')){
				value /= parseFactor();
			} else {
				return value;
			}
		}
	}
	
	private double parseFactor() {
		
		if(eat('+')){
			return parseFactor();
		}
		
		if(eat('-')){
			return -parseFactor();
		}
		
		double value;
		
		int startPos = this.pos;
		
		if(eat('(')){
			
			value = parseExpression();
			
			if(!eat(')')){
				throw new IllegalArgumentException("Missing closing parenthesis");
			}
			
		} else if((ch >= '0' && ch <= '9') || ch == '.'){
			
			while((ch >= '0' && ch <= '9') || ch == '.'){
				nextChar();
			}
			
			value = Double.parseDouble(input.substring(startPos, this.pos));
			
		} else {
			
			throw new IllegalArgumentException("Unexpected character: " + (char) ch);
		}
		
		return value;
	}
}