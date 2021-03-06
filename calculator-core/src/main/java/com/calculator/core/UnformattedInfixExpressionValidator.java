package com.calculator.core;

import com.calculator.core.exception.*;

import java.util.regex.Pattern;

public class UnformattedInfixExpressionValidator implements IExpressionFilter {
	ExpressionModifier expressionModifier;

	public UnformattedInfixExpressionValidator(ExpressionModifier expressionModifier) {
		this.expressionModifier = expressionModifier;
	}

	public Expression process(Expression expression) throws CalculatorException {

		expression = expressionModifier.getExpressionWrappedWithBrackets(expression);
		String expressionContent = expression.getContent();

		validateIfAnyConsecutiveNumbers(expressionContent);
		validateNumbersFormat(expressionContent);

		expression = expressionModifier.getExpressionWithStrippedWhiteSpaces(expression);
		expressionContent = expression.getContent();

		validateIfExpressionHasEmptySubexpressions(expressionContent);
		validateOperatorSequence(expressionContent);
		validateTokens(expressionContent);
		validateIfAnyNumbersAreGluedAroundBracketedExpression(expressionContent);
		validateSubexpressionEnds(expressionContent);

		return expression;
	}

	private void validateIfAnyConsecutiveNumbers(String expressionContent) throws NumberMisplacementException {
		if (hasConsecutiveNumbers(expressionContent)) {
			throw new NumberMisplacementException(
					"Invalid expression. Numbers should be separated by a valid operator.");
		}
	}

	private boolean hasConsecutiveNumbers(String expressionContent) {
		return expressionContent.matches(".*[0-9.][ ]+[0-9.].*");
	}

	private void validateNumbersFormat(String expressionContent) throws NumberMisplacementException {
		if (hasAnInvalidNumber(expressionContent)) {
			throw new NumberMisplacementException("The given expression contains invalid numbers.");
		}
	}

	private boolean hasAnInvalidNumber(String expressionContent) {
		// checks if there are two floating point numbers one after another
		// following the mantissa_1.exponent_1mantissa_2.exponent_2
		return Pattern.matches(".*[0-9]+\\.[0-9]*\\..*", expressionContent);
	}

	private void validateIfExpressionHasEmptySubexpressions(String expressionContent) throws EmptyExpressionException {
		if (expressionContent.contains("()")) {
			throw new EmptyExpressionException("An empty expression is not a valid one.");
		}
	}

	private void validateOperatorSequence(String expressionContent) throws OperatorMisplacementException {
		if (hasConsecutiveOperators(expressionContent)) {
			throw new OperatorMisplacementException("The given expression contains consecutive operators.");
		}
	}

	private boolean hasConsecutiveOperators(String expressionContent) {
		// There should be no two consecutive operators -, +, *, / or ^
		return Pattern.matches(".*[-+*/^]{2,}.*", expressionContent);
	}

	private void validateTokens(String expressionContent) throws InvalidOperatorException {
		if (hasInvalidTokens(expressionContent)) {
			throw new InvalidOperatorException("The given expression contains invalid tokens.");
		}
	}

	private boolean hasInvalidTokens(String expressionContent) {
		// An expression should only contain the symbols -, +, *, /, ^, (, ), ., 0-9
		return Pattern.matches(".*[^-+*/^()0-9.].*", expressionContent);
	}

	private void validateIfAnyNumbersAreGluedAroundBracketedExpression(String expressionContent)
			throws BracketsException {
		if (hasNumbersGluedAroundBracketExpression(expressionContent)) {
			throw new BracketsException(
					"It is not allowed to have a number right before a '(' symbol or right after a ')' symbol.");
		}
	}

	private boolean hasNumbersGluedAroundBracketExpression(String expressionContent) {
		// Prevents having expressions with the format number1([sub_expression])number2
		return expressionContent.matches(".*([0-9.]\\(|\\)[0-9]).*");
	}

	private void validateSubexpressionEnds(String expressionContent) throws CalculatorException {
		validateExpressionAfterOpeningBrackets(expressionContent);
		validateExpressionBeforeClosingBrackets(expressionContent);
	}

	private void validateExpressionAfterOpeningBrackets(String expressionContent) throws OperatorMisplacementException {
		// there shouldn't be an *, / or ^ symbol right after an opening bracket
		validateStringByPattern(expressionContent, ".*\\([*\\/^].*");
	}

	private void validateExpressionBeforeClosingBrackets(String expressionContent)
			throws OperatorMisplacementException {
		// A there should be no arithmetic operators right before a closing bracket
		validateStringByPattern(expressionContent, ".*[-+*\\/^]\\).*");
	}

	private void validateStringByPattern(String string, String invalidPattern) throws OperatorMisplacementException {
		if (Pattern.matches(invalidPattern, string)) {
			throw new OperatorMisplacementException("Expression is not ordered properly.");
		}
	}
}
