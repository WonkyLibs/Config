package com.wonkglorg.utilitylib.config.lang.parser;

import com.wonkglorg.minecraft.config.lang.parser.MathParser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class MathParserTest{
	
	private static final double EPSILON = 0.0000001;
	
	// -------------------------------------------------------------------------
	// Basic numbers
	// -------------------------------------------------------------------------
	
	@Test
	void parsesInteger() {
		assertEquals(42.0, parse("42"), EPSILON);
	}
	
	@Test
	void parsesZero() {
		assertEquals(0.0, parse("0"), EPSILON);
	}
	
	@Test
	void parsesDecimal() {
		assertEquals(3.14, parse("3.14"), EPSILON);
	}
	
	@Test
	void parsesLeadingDecimalPoint() {
		assertEquals(0.5, parse(".5"), EPSILON);
	}
	
	@Test
	void parsesTrailingDecimalPoint() {
		assertEquals(5.0, parse("5."), EPSILON);
	}
	
	// -------------------------------------------------------------------------
	// Addition / subtraction
	// -------------------------------------------------------------------------
	
	@Test
	void addsNumbers() {
		assertEquals(7.0, parse("3 + 4"), EPSILON);
	}
	
	@Test
	void subtractsNumbers() {
		assertEquals(2.0, parse("5 - 3"), EPSILON);
	}
	
	@Test
	void handlesMultipleAdditions() {
		assertEquals(10.0, parse("1 + 2 + 3 + 4"), EPSILON);
	}
	
	@Test
	void handlesMultipleSubtractions() {
		assertEquals(-8.0, parse("1 - 2 - 3 - 4"), EPSILON);
	}
	
	// -------------------------------------------------------------------------
	// Multiplication / division
	// -------------------------------------------------------------------------
	
	@Test
	void multipliesNumbers() {
		assertEquals(12.0, parse("3 * 4"), EPSILON);
	}
	
	@Test
	void dividesNumbers() {
		assertEquals(5.0, parse("10 / 2"), EPSILON);
	}
	
	@Test
	void handlesMultipleMultiplications() {
		assertEquals(24.0, parse("2 * 3 * 4"), EPSILON);
	}
	
	@Test
	void handlesMultipleDivisions() {
		assertEquals(2.0, parse("16 / 4 / 2"), EPSILON);
	}
	
	// -------------------------------------------------------------------------
	// Operator precedence
	// -------------------------------------------------------------------------
	
	@Test
	void multiplicationHasHigherPrecedenceThanAddition() {
		assertEquals(14.0, parse("2 + 3 * 4"), EPSILON);
	}
	
	@Test
	void divisionHasHigherPrecedenceThanSubtraction() {
		assertEquals(8.0, parse("10 - 4 / 2"), EPSILON);
	}
	
	@Test
	void multiplicationAndDivisionHaveHigherPrecedence() {
		assertEquals(14.0, parse("2 * 3 + 8"), EPSILON);
	}
	
	@Test
	void operationsAreEvaluatedLeftToRightAtSamePrecedence() {
		assertEquals(10.0, parse("20 / 2 * 1"), EPSILON);
	}
	
	// -------------------------------------------------------------------------
	// Unary operators
	// -------------------------------------------------------------------------
	
	@Test
	void parsesUnaryMinus() {
		assertEquals(-5.0, parse("-5"), EPSILON);
	}
	
	@Test
	void parsesUnaryPlus() {
		assertEquals(5.0, parse("+5"), EPSILON);
	}
	
	@Test
	void parsesUnaryMinusWithExpression() {
		assertEquals(-7.0, parse("-3 - 4"), EPSILON);
	}
	
	@Test
	void parsesDoubleNegative() {
		assertEquals(5.0, parse("--5"), EPSILON);
	}
	
	@Test
	void parsesMultipleUnaryOperators() {
		assertEquals(-5.0, parse("---5"), EPSILON);
	}
	
	@Test
	void parsesUnaryMinusBeforeParentheses() {
		assertEquals(-10.0, parse("-(2 + 3) * 2"), EPSILON);
	}
	
	// -------------------------------------------------------------------------
	// Parentheses
	// -------------------------------------------------------------------------
	
	@Test
	void parsesParentheses() {
		assertEquals(20.0, parse("(2 + 3) * 4"), EPSILON);
	}
	
	@Test
	void parenthesesOverridePrecedence() {
		assertEquals(24.0, parse("2 * (3 + 9)"), EPSILON);
	}
	
	@Test
	void parsesNestedParentheses() {
		assertEquals(21.0, parse("((2 + 5) * 3)"), EPSILON);
	}
	
	@Test
	void parsesDeeplyNestedParentheses() {
		assertEquals(10.0, parse("(((1 + 2) + 3) + 4)"), EPSILON);
	}
	
	@Test
	void parsesParenthesesAroundSingleValue() {
		assertEquals(5.0, parse("(5)"), EPSILON);
	}
	
	// -------------------------------------------------------------------------
	// Decimal calculations
	// -------------------------------------------------------------------------
	
	@Test
	void addsDecimals() {
		assertEquals(4.0, parse("1.5 + 2.5"), EPSILON);
	}
	
	@Test
	void multipliesDecimals() {
		assertEquals(3.75, parse("1.5 * 2.5"), EPSILON);
	}
	
	@Test
	void dividesDecimals() {
		assertEquals(2.5, parse("5.0 / 2.0"), EPSILON);
	}
	
	@Test
	void calculatesComplexDecimalExpression() {
		assertEquals(7.5, parse("(2.5 * 4) - 2.5"), EPSILON);
	}
	
	// -------------------------------------------------------------------------
	// Whitespace
	// -------------------------------------------------------------------------
	
	@Test
	void ignoresSpaces() {
		assertEquals(7.0, parse(" 3 + 4 "), EPSILON);
	}
	
	@Test
	void ignoresWhitespaceBetweenOperatorsAndValues() {
		assertEquals(14.0, parse(" 2   +   3 * 4 "), EPSILON);
	}
	
	// -------------------------------------------------------------------------
	// Complex expressions
	// -------------------------------------------------------------------------
	
	@Test
	void parsesComplexExpression() {
		assertEquals(17.0, parse("2 + 3 * 5"), EPSILON);
	}
	
	@Test
	void parsesComplexParenthesizedExpression() {
		assertEquals(30.0, parse("(2 + 3) * (4 + 2)"), EPSILON);
	}
	
	@Test
	void parsesComplexExpressionWithDivision() {
		assertEquals(10.0, parse("2 + 12 / 3 * 2"), EPSILON);
	}
	
	@Test
	void parsesNegativeNumbersInExpression() {
		assertEquals(-8.0, parse("-2 * 3 - 2"), EPSILON);
	}
	
	// -------------------------------------------------------------------------
	// Division by zero
	// -------------------------------------------------------------------------
	
	@Test
	void divisionByZeroProducesInfinity() {
		assertEquals(Double.POSITIVE_INFINITY, parse("10 / 0"));
	}
	
	@Test
	void negativeDivisionByZeroProducesNegativeInfinity() {
		assertEquals(Double.NEGATIVE_INFINITY, parse("-10 / 0"));
	}
	
	@Test
	void zeroDividedByZeroProducesNaN() {
		assertTrue(Double.isNaN(parse("0 / 0")));
	}
	
	// -------------------------------------------------------------------------
	// Invalid expressions
	// -------------------------------------------------------------------------
	
	@Test
	void rejectsUnexpectedCharacter() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> parse("2 + x"));
		
		assertTrue(exception.getMessage().contains("Unexpected character"));
	}
	
	@Test
	void rejectsMissingClosingParenthesis() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> parse("(2 + 3"));
		
		assertEquals("Missing closing parenthesis", exception.getMessage());
	}
	
	@Test
	void rejectsUnexpectedClosingParenthesis() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> parse("2 + 3)"));
		
		assertTrue(exception.getMessage().contains("Unexpected character"));
	}
	
	@Test
	void rejectsTwoNumbersWithoutOperator() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> parse("2 3"));
		
		assertTrue(exception.getMessage().contains("Unexpected character"));
	}
	
	@Test
	void rejectsTwoDecimalPoints() {
		assertThrows(NumberFormatException.class, () -> parse("1.2.3"));
	}
	
	@Test
	void rejectsEmptyExpression() {
		assertThrows(IllegalArgumentException.class, () -> parse(""));
	}
	
	// -------------------------------------------------------------------------
	// Helper
	// -------------------------------------------------------------------------
	
	private static double parse(String expression) {
		return new MathParser(expression).parse();
	}
}