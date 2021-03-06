package com.calculator.core;

import com.calculator.core.exception.*;

import java.util.*;

import org.mockito.*;
import org.mockito.internal.stubbing.answers.ReturnsElementsOf;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.*;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.closeTo;

public class PostfixExpressionCalculatorTest {
	@Mock
	ExpressionTokenSplitter expressionTokenSplitter;
	@Mock
	NumberChecker numberChecker;
	Expression resultExpression;
	Double calculationResult;
	PostfixExpressionCalculator calculator;

	@Before
	public void setUp() {
		initMocks(this);

		calculator = new PostfixExpressionCalculator(expressionTokenSplitter, numberChecker);
	}

	@Test
	public void verifyOrderOfExpressionMethodCalls() throws CalculatorException {
		mockTokensInExpression("1");
		mockNumberCheckingByOrderOfTokens(true);

		Expression expression = this.getFormattedExpression("1");
		calculator.process(expression);

		InOrder mockOrder = inOrder(expressionTokenSplitter);

		mockOrder.verify(expressionTokenSplitter).getExpressionTokens(any());

		mockOrder.verifyNoMoreInteractions();
	}

	@Test
	public void twoOperators_process() throws CalculatorException {
		mockTokensInExpression("1", "2", "+");
		mockNumberCheckingByOrderOfTokens(true, true, false);

		Expression expression = this.getFormattedExpression("1 2 +");
		assertThat(getExpressionCalculationResult(expression), closeTo(3.0, 0.0001));
	}

	@Test
	public void allExpression_process() throws CalculatorException {
		mockTokensInExpression("1", "2", "3", "4", "5", "+", "*", "/", "^");
		mockNumberCheckingByOrderOfTokens(true, true, true, true, true, false, false, false, false);

		Expression expression = this.getFormattedExpression("1 2 3 4 5 + * / ^");
		assertThat(getExpressionCalculationResult(expression), closeTo(1 ^ (2 / (3 * (4 + 5))), 0.0001));
	}

	@Test(expected = InvalidOperatorException.class)
	public void invalidOperator_process() throws CalculatorException {
		mockTokensInExpression("1", "A", "5");
		mockNumberCheckingByOrderOfTokens(true, false, true);

		Expression expression = this.getFormattedExpression("1 A 5");
		calculator.process(expression);
	}

	@Test(expected = NumberMisplacementException.class)
	public void tooManyNumbers_process() throws CalculatorException {
		mockTokensInExpression("1", "2", "3", "+");
		mockNumberCheckingByOrderOfTokens(true, true, true, false);

		Expression expression = this.getFormattedExpression("1 2 3 +");
		calculator.process(expression);
	}

	@Test(expected = NumberMisplacementException.class)
	public void tooFewNumbers_process() throws CalculatorException {
		mockTokensInExpression("1", "+");
		mockNumberCheckingByOrderOfTokens(true, false);

		Expression expression = this.getFormattedExpression("1 +");
		calculator.process(expression);
	}

	private void mockTokensInExpression(String... tokens) {
		when(expressionTokenSplitter.getExpressionTokens(any())).thenReturn(tokens);
	}

	private void mockNumberCheckingByOrderOfTokens(Boolean... isNumberValues) {
		List<Boolean> isNumberValuesAsList = Arrays.asList(isNumberValues);
		when(numberChecker.isNumber(anyString())).then(new ReturnsElementsOf(isNumberValuesAsList));
	}

	private double getExpressionCalculationResult(Expression expression) throws CalculatorException {
		resultExpression = calculator.process(expression);
		calculationResult = Double.valueOf(resultExpression.getContent());

		return calculationResult;
	}
	
	private FormattedExpression getFormattedExpression(String content) {
		return new FormattedExpression(content);
	}
}
