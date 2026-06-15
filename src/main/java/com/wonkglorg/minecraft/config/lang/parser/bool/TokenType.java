package com.wonkglorg.minecraft.config.lang.parser.bool;

public enum TokenType{
	IDENTIFIER,
	NUMBER,
	
	AND,
	OR,
	NOT,
	
	EQ,
	NEQ,
	GT,
	LT,
	GTE,
	LTE,
	
	LPAREN,
	RPAREN,
	
	EOF
}