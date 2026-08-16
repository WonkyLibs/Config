package com.wonkglorg.utilitylib.config.lang.parser.bool;

import com.wonkglorg.minecraft.config.lang.parser.bool.Lexer;
import com.wonkglorg.minecraft.config.lang.parser.bool.Token;
import com.wonkglorg.minecraft.config.lang.parser.bool.TokenType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import java.util.List;

class LexerTest{
	
	@Test
	void tokenizesEmptyInput() {
		List<Token> tokens = new Lexer("").tokenize();
		
		assertEquals(1, tokens.size());
		assertToken(tokens.get(0), TokenType.EOF, "");
	}
	
	@Test
	void tokenizesIdentifier() {
		List<Token> tokens = new Lexer("stock").tokenize();
		
		assertEquals(2, tokens.size());
		assertToken(tokens.get(0), TokenType.IDENTIFIER, "stock");
		assertToken(tokens.get(1), TokenType.EOF, "");
	}
	
	@Test
	void tokenizesIdentifierWithNumbersAndUnderscores() {
		List<Token> tokens = new Lexer("stock_123").tokenize();
		
		assertEquals(2, tokens.size());
		assertToken(tokens.get(0), TokenType.IDENTIFIER, "stock_123");
		assertToken(tokens.get(1), TokenType.EOF, "");
	}
	
	@Test
	void tokenizesNumber() {
		List<Token> tokens = new Lexer("12345").tokenize();
		
		assertEquals(2, tokens.size());
		assertToken(tokens.get(0), TokenType.NUMBER, "12345");
		assertToken(tokens.get(1), TokenType.EOF, "");
	}
	
	@Test
	void tokenizesBooleanLiterals() {
		List<Token> tokens = new Lexer("true false").tokenize();
		
		assertEquals(3, tokens.size());
		
		assertToken(tokens.get(0), TokenType.IDENTIFIER, "true");
		assertToken(tokens.get(1), TokenType.IDENTIFIER, "false");
		assertToken(tokens.get(2), TokenType.EOF, "");
	}
	
	@Test
	void tokenizesEqualityOperators() {
		List<Token> tokens = new Lexer("== !=").tokenize();
		
		assertEquals(3, tokens.size());
		
		assertToken(tokens.get(0), TokenType.EQ, "==");
		assertToken(tokens.get(1), TokenType.NEQ, "!=");
		assertToken(tokens.get(2), TokenType.EOF, "");
	}
	
	@Test
	void tokenizesComparisonOperators() {
		List<Token> tokens = new Lexer("> < >= <=").tokenize();
		
		assertEquals(5, tokens.size());
		
		assertToken(tokens.get(0), TokenType.GT, ">");
		assertToken(tokens.get(1), TokenType.LT, "<");
		assertToken(tokens.get(2), TokenType.GTE, ">=");
		assertToken(tokens.get(3), TokenType.LTE, "<=");
		assertToken(tokens.get(4), TokenType.EOF, "");
	}
	
	@Test
	void tokenizesLogicalOperators() {
		List<Token> tokens = new Lexer("&& ||").tokenize();
		
		assertEquals(3, tokens.size());
		
		assertToken(tokens.get(0), TokenType.AND, "&&");
		assertToken(tokens.get(1), TokenType.OR, "||");
		assertToken(tokens.get(2), TokenType.EOF, "");
	}
	
	@Test
	void tokenizesNotOperator() {
		List<Token> tokens = new Lexer("!").tokenize();
		
		assertEquals(2, tokens.size());
		
		assertToken(tokens.get(0), TokenType.NOT, "!");
		assertToken(tokens.get(1), TokenType.EOF, "");
	}
	
	@Test
	void tokenizesParentheses() {
		List<Token> tokens = new Lexer("( )").tokenize();
		
		assertEquals(3, tokens.size());
		
		assertToken(tokens.get(0), TokenType.LPAREN, "(");
		assertToken(tokens.get(1), TokenType.RPAREN, ")");
		assertToken(tokens.get(2), TokenType.EOF, "");
	}
	
	@Test
	void tokenizesCompleteCondition() {
		List<Token> tokens = new Lexer("stock == 0").tokenize();
		
		assertEquals(4, tokens.size());
		
		assertToken(tokens.get(0), TokenType.IDENTIFIER, "stock");
		assertToken(tokens.get(1), TokenType.EQ, "==");
		assertToken(tokens.get(2), TokenType.NUMBER, "0");
		assertToken(tokens.get(3), TokenType.EOF, "");
	}
	
	@Test
	void tokenizesComplexCondition() {
		List<Token> tokens = new Lexer("(stock == 0 || stock > 10) && enabled").tokenize();
		
		assertEquals(12, tokens.size());
		
		assertToken(tokens.get(0), TokenType.LPAREN, "(");
		assertToken(tokens.get(1), TokenType.IDENTIFIER, "stock");
		assertToken(tokens.get(2), TokenType.EQ, "==");
		assertToken(tokens.get(3), TokenType.NUMBER, "0");
		assertToken(tokens.get(4), TokenType.OR, "||");
		assertToken(tokens.get(5), TokenType.IDENTIFIER, "stock");
		assertToken(tokens.get(6), TokenType.GT, ">");
		assertToken(tokens.get(7), TokenType.NUMBER, "10");
		assertToken(tokens.get(8), TokenType.RPAREN, ")");
		assertToken(tokens.get(9), TokenType.AND, "&&");
		assertToken(tokens.get(10), TokenType.IDENTIFIER, "enabled");
		assertToken(tokens.get(11), TokenType.EOF, "");
	}
	
	@Test
	void ignoresWhitespace() {
		List<Token> tokens = new Lexer("  stock   ==   0 \n\t").tokenize();
		
		assertEquals(4, tokens.size());
		
		assertToken(tokens.get(0), TokenType.IDENTIFIER, "stock");
		assertToken(tokens.get(1), TokenType.EQ, "==");
		assertToken(tokens.get(2), TokenType.NUMBER, "0");
		assertToken(tokens.get(3), TokenType.EOF, "");
	}
	
	@Test
	void prioritizesMultiCharacterOperators() {
		List<Token> tokens = new Lexer("&& || == != >= <=").tokenize();
		
		assertEquals(7, tokens.size());
		
		assertToken(tokens.get(0), TokenType.AND, "&&");
		assertToken(tokens.get(1), TokenType.OR, "||");
		assertToken(tokens.get(2), TokenType.EQ, "==");
		assertToken(tokens.get(3), TokenType.NEQ, "!=");
		assertToken(tokens.get(4), TokenType.GTE, ">=");
		assertToken(tokens.get(5), TokenType.LTE, "<=");
		assertToken(tokens.get(6), TokenType.EOF, "");
	}
	
	@Test
	void rejectsPercentCharacter() {
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> new Lexer("%").tokenize());
		
		assertEquals("Unexpected character '%' at position 0", exception.getMessage());
	}
	
	@Test
	void rejectsPlaceholderSyntax() {
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> new Lexer("%stock%==0").tokenize());
		
		assertEquals("Unexpected character '%' at position 0", exception.getMessage());
	}
	
	@Test
	void rejectsHyphen() {
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> new Lexer("stock-value").tokenize());
		
		assertEquals("Unexpected character '-' at position 5", exception.getMessage());
	}
	
	@Test
	void rejectsDecimalNumbers() {
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> new Lexer("1.5").tokenize());
		
		assertEquals("Unexpected character '.' at position 1", exception.getMessage());
	}
	
	private static void assertToken(Token token, TokenType expectedType, String expectedText) {
		assertEquals(expectedType, token.type());
		assertEquals(expectedText, token.text());
	}
}