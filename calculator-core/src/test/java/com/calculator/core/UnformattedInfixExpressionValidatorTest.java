package com.calculator.core;

import com.calculator.core.exception.*;

import org.mockito.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.*;

public class UnformattedInfixExpressionValidatorTest {
	public Expression expression;
	@Mock
	public ExpressionModifier expressionModifier;
	public UnformattedInfixExpressionValidator validator;

	@Before
	public void setUp() {
		initMocks(this);

		// initializing with a correct stub value
		validator = new UnformattedInfixExpressionValidator(expressionModifier);
	}

	@Test
	public void verifyOrderOfExpressionMethodCalls_process() throws Exception {
		when(expressionModifier.getExpressionWrappedWithBrackets(any(Expression.class)))
				.thenReturn(new Expression("(1)"));
		when(expressionModifier.getExpressionWithStrippedWhiteSpaces(any(Expression.class)))
				.thenReturn(new Expression("(1)"));

		Expression expression = new Expression("1");
		validator.process(expression);

		InOrder mockOrder = inOrder(expressionModifier);

		mockOrder.verify(expressionModifier).getExpressionWrappedWithBrackets(any());
		mockOrder.verify(expressionModifier).getExpressionWithStrippedWhiteSpaces(any());

		mockOrder.verifyNoMoreInteractions();
	}

	@Test(expected = NumberMisplacementException.class)
	public void spacedConsecutiveNumbers_process() throws Exception {
		stubDependenciesOfProcess("(1 2)", "(12)");

		validator.process(getExpression("1 2"));
	}

	@Test(expected = NumberMisplacementException.class)
	public void consecutive_process() throws Exception {
		stubDependenciesOfProcess("(2+4.55.6)", "(2+4.55.6)");

		validator.process(getExpression("2+4.55.6"));
	}

	@Test(expected = EmptyExpressionException.class)
	public void emptyExpression_process() throws Exception {
		stubDependenciesOfProcess("()", "()");

		validator.process(getExpression(""));
	}

	@Test(expected = OperatorMisplacementException.class)
	public void manyLegitimateOperatorsAtBeginning_process() throws Exception {
		stubDependenciesOfProcess("(++1+2)", "(++1+2)");

		validator.process(getExpression("++1+2"));
	}

	@Test(expected = OperatorMisplacementException.class)
	public void consecutiveOperators_process() throws Exception {
		stubDependenciesOfProcess("(2+*3)", "(2+*3)");

		validator.process(getExpression("2+*3"));
	}

	@Test(expected = InvalidOperatorException.class)
	public void invalidTokenException_process() throws Exception {
		stubDependenciesOfProcess("(1A2)", "(1A2)");

		validator.process(getExpression("1A2"));
	}

	@Test(expected = BracketsException.class)
	public void numberGluedToTheLeftOfABracketedExpression_process() throws Exception {
		stubDependenciesOfProcess("(2(3+4))", "(2(3+4))");

		validator.process(getExpression("2(3+4)"));
	}

	@Test(expected = BracketsException.class)
	public void numberGluedToTheRightOfABracketedExpression_process() throws Exception {
		stubDependenciesOfProcess("((3+4)5)", "((3+4)5)");

		validator.process(getExpression("(3+4)5"));
	}

	@Test(expected = OperatorMisplacementException.class)
	public void misplacedOperatorAtStart_process() throws Exception {
		stubDependenciesOfProcess("(*1+2)", "(*1+2)");

		validator.process(getExpression("*1+2"));
	}

	@Test(expected = OperatorMisplacementException.class)
	public void manyMisplacedOperatorsAtBeginning_process() throws Exception {
		stubDependenciesOfProcess("(**/1/2)", "(**/1/2)");

		validator.process(getExpression("**/1/2"));
	}

	@Test(expected = OperatorMisplacementException.class)
	public void misplacedOperatorAtEnd_process() throws Exception {
		stubDependenciesOfProcess("(1/2*)", "(1/2*)");

		validator.process(getExpression("1/2*"));
	}

	@Test(expected = OperatorMisplacementException.class)
	public void manyMisplacedOperatorsAtEnd_process() throws Exception {
		stubDependenciesOfProcess("(1/2*/*/*)", "(1/2*/*/*)");

		validator.process(getExpression("1/2*/*/*"));
	}

	@Test(expected = OperatorMisplacementException.class)
	public void singleOperator_process() throws Exception {
		stubDependenciesOfProcess("(+)", "(+)");

		validator.process(getExpression("+"));
	}

	@Test(expected = OperatorMisplacementException.class)
	public void misplacedOperatorsStraightAfterOpeningBracket_process() throws Exception {
		stubDependenciesOfProcess("((*3))", "((*3))");

		validator.process(getExpression("(*3)"));
	}

	@Test(expected = OperatorMisplacementException.class)
	public void misplacedOperatorsAtEndsWithinBrackets_process() throws Exception {
		stubDependenciesOfProcess("((4+5*))", "((4+5*))");

		validator.process(getExpression("(4+5*)"));
	}

	@Test(expected = OperatorMisplacementException.class)
	public void singleOperatorWithinBrackets_process() throws Exception {
		stubDependenciesOfProcess("((+))", "((+))");

		validator.process(getExpression("(+)"));
	}

	@Test(expected = EmptyExpressionException.class)
	public void emptyBrackets_process() throws Exception {
		stubDependenciesOfProcess("(1+())", "(1+())");

		validator.process(getExpression("1+()"));
	}

	private Expression getExpression(String expressionContent) {
		return new Expression(expressionContent);
	}

	private void stubDependenciesOfProcess(String wrappedExpression, String wrappedExpressionWithStrippedWhiteSpaces) {
		when(expressionModifier.getExpressionWrappedWithBrackets(any()))
				.thenReturn(new Expression(wrappedExpression));
		when(expressionModifier.getExpressionWithStrippedWhiteSpaces(any()))
				.thenReturn(new Expression(wrappedExpressionWithStrippedWhiteSpaces));
	}
}
