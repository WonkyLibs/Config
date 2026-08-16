package com.wonkglorg.utilitylib.config.lang.parser.bool;

import com.wonkglorg.minecraft.config.lang.parser.bool.BooleanConditionParser;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class BooleanConditionParserTest{
	
	@Test
	void parsesTrueLiteral() {
		assertTrue(parse("true"));
	}
	
	@Test
	void parsesFalseLiteral() {
		assertFalse(parse("false"));
	}
	
	@Test
	void parsesUnknownIdentifierAsFalse() {
		assertFalse(parse("something"));
	}
	
	@Test
	void parsesNumberAsFalse() {
		assertFalse(parse("123"));
	}
	
	// -------------------------------------------------------------------------
	// Equality
	// -------------------------------------------------------------------------
	
	@Test
	void evaluatesEqualStrings() {
		assertTrue(parse("foo == foo"));
	}
	
	@Test
	void evaluatesUnequalStrings() {
		assertFalse(parse("foo == bar"));
	}
	
	@Test
	void evaluatesNotEqualStrings() {
		assertTrue(parse("foo != bar"));
	}
	
	@Test
	void evaluatesEqualNumbers() {
		assertTrue(parse("10 == 10"));
	}
	
	@Test
	void evaluatesUnequalNumbers() {
		assertFalse(parse("10 == 20"));
	}
	
	@Test
	void evaluatesNotEqualNumbers() {
		assertTrue(parse("10 != 20"));
	}
	
	// -------------------------------------------------------------------------
	// Numeric comparisons
	// -------------------------------------------------------------------------
	
	@Test
	void evaluatesGreaterThan() {
		assertTrue(parse("10 > 5"));
		assertFalse(parse("5 > 10"));
	}
	
	@Test
	void evaluatesLessThan() {
		assertTrue(parse("5 < 10"));
		assertFalse(parse("10 < 5"));
	}
	
	@Test
	void evaluatesGreaterThanOrEqual() {
		assertTrue(parse("10 >= 10"));
		assertTrue(parse("10 >= 5"));
		assertFalse(parse("5 >= 10"));
	}
	
	@Test
	void evaluatesLessThanOrEqual() {
		assertTrue(parse("10 <= 10"));
		assertTrue(parse("5 <= 10"));
		assertFalse(parse("10 <= 5"));
	}
	
	@Test
	void evaluatesNegativeNumericComparison() {
		assertTrue(parse("-5 < 0"));
	}
	
	// -------------------------------------------------------------------------
	// NOT
	// -------------------------------------------------------------------------
	
	@Test
	void parsesNot() {
		assertFalse(parse("!true"));
		assertTrue(parse("!false"));
	}
	
	@Test
	void parsesDoubleNot() {
		assertTrue(parse("!!true"));
		assertFalse(parse("!!false"));
	}
	
	@Test
	void parsesNotWithComparison() {
		assertFalse(parse("!(10 == 10)"));
		assertTrue(parse("!(10 == 5)"));
	}
	
	// -------------------------------------------------------------------------
	// AND
	// -------------------------------------------------------------------------
	
	@Test
	void parsesAnd() {
		assertTrue(parse("true && true"));
		assertFalse(parse("true && false"));
		assertFalse(parse("false && true"));
		assertFalse(parse("false && false"));
	}
	
	@Test
	void parsesMultipleAndExpressions() {
		assertTrue(parse("true && true && true"));
		assertFalse(parse("true && true && false"));
	}
	
	@Test
	void parsesAndWithComparisons() {
		assertTrue(parse("10 > 5 && 20 > 10"));
		assertFalse(parse("10 > 5 && 5 > 20"));
	}
	
	// -------------------------------------------------------------------------
	// OR
	// -------------------------------------------------------------------------
	
	@Test
	void parsesOr() {
		assertTrue(parse("true || true"));
		assertTrue(parse("true || false"));
		assertTrue(parse("false || true"));
		assertFalse(parse("false || false"));
	}
	
	@Test
	void parsesMultipleOrExpressions() {
		assertTrue(parse("false || false || true"));
		assertFalse(parse("false || false || false"));
	}
	
	@Test
	void parsesOrWithComparisons() {
		assertTrue(parse("10 > 20 || 10 < 20"));
		assertFalse(parse("10 > 20 || 20 < 10"));
	}
	
	// -------------------------------------------------------------------------
	// Parentheses
	// -------------------------------------------------------------------------
	
	@Test
	void parsesParentheses() {
		assertTrue(parse("(true)"));
		assertFalse(parse("(false)"));
	}
	
	@Test
	void parsesNestedParentheses() {
		assertTrue(parse("((true))"));
		assertFalse(parse("(((false)))"));
	}
	
	@Test
	void parenthesesOverridePrecedence() {
		assertTrue(parse("(true || false) && true"));
		assertFalse(parse("(true || false) && false"));
	}
	
	// -------------------------------------------------------------------------
	// Operator precedence
	// -------------------------------------------------------------------------
	
	@Test
	void andHasHigherPrecedenceThanOr() {
		// true || (false && false)
		assertTrue(parse("true || false && false"));
		
		// false || (true && false)
		assertFalse(parse("false || true && false"));
		
		// false || (true && true)
		assertTrue(parse("false || true && true"));
	}
	
	@Test
	void notHasHigherPrecedenceThanAnd() {
		// (!false) && true
		assertTrue(parse("!false && true"));
		
		// (!true) && true
		assertFalse(parse("!true && true"));
	}
	
	@Test
	void parenthesesCanChangePrecedence() {
		// (false || true) && true
		assertTrue(parse("(false || true) && true"));
		
		// false || (true && false)
		assertFalse(parse("false || true && false"));
		
		// (false || true) && false
		assertFalse(parse("(false || true) && false"));
	}
	
	// -------------------------------------------------------------------------
	// Whitespace
	// -------------------------------------------------------------------------
	
	@Test
	void ignoresWhitespace() {
		assertTrue(parse("  true  "));
		assertTrue(parse(" 10   ==   10 "));
		assertTrue(parse("true\t&&\ntrue"));
	}
	
	// -------------------------------------------------------------------------
	// Complete expression consumption
	// -------------------------------------------------------------------------
	
	@Test
	void rejectsTrailingTokens() {
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> parse("true false"));
		
		assertTrue(exception.getMessage().contains("Unexpected token"));
	}
	
	@Test
	void rejectsTrailingComparison() {
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> parse("true == false == true"));
		
		assertTrue(exception.getMessage().contains("Unexpected token"));
	}
	
	// -------------------------------------------------------------------------
	// Invalid expressions
	// -------------------------------------------------------------------------
	
	@Test
	void incompleteEqualityExpressionReturnsFalse() {
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> parse("true =="));
		
		assertTrue(exception.getMessage().contains("Expected value"));
	}
	
	@Test
	void incompleteAndExpressionReturnsFalse() {
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> parse("true &&"));
		
		assertTrue(exception.getMessage().contains("Expected value"));
	}
	
	@Test
	void incompleteOrExpressionReturnsFalse() {
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> parse("true ||"));
		
		assertTrue(exception.getMessage().contains("Expected value"));
	}
	
	@Test
	void incompleteNotExpressionReturnsFalse() {
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> parse("!"));
		
		assertTrue(exception.getMessage().contains("Expected value"));
	}
	
	@Test
	void missingClosingParenthesisThrows() {
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> parse("(true"));
		
		assertTrue(exception.getMessage().contains("Expected RPAREN"));
	}
	
	@Test
	void unexpectedClosingParenthesisThrows() {
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> parse("true)"));
		
		assertTrue(exception.getMessage().contains("Unexpected token"));
	}
	
	// -------------------------------------------------------------------------
	// Placeholder input
	// -------------------------------------------------------------------------
	
	@Test
	void rejectsUnresolvedPlaceholder() {
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> parse("%stock% == 0"));
		
		assertTrue(exception.getMessage().contains("Unexpected character '%'"));
	}
	
	// -------------------------------------------------------------------------
	// Helper
	// -------------------------------------------------------------------------
	
	private static boolean parse(String expression) {
		return new BooleanConditionParser(expression).parse();
	}
}