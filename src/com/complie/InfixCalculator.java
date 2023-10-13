package com.complie;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Stack;

public class InfixCalculator extends Application {

    private TextField infixExpressionField;
    private TextArea outputArea;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("中缀表达式计算器");

        // 创建输入框和按钮
        Label expressionLabel = new Label("中缀表达式：");
        infixExpressionField = new TextField();
        Button evaluateButton = new Button("计算");

        // 创建输出区域
        Label outputLabel = new Label("输出：");
        outputArea = new TextArea();
        outputArea.setEditable(false);

        // 设置按钮的点击事件
        evaluateButton.setOnAction(e -> evaluateInfixExpression());

        // 创建布局并添加控件
        VBox root = new VBox();
        root.setSpacing(10);
        root.setPadding(new Insets(10));
        // 将输出区域放置在滚动面板中
        ScrollPane outputScrollPane = new ScrollPane(outputArea);
        outputScrollPane.setFitToWidth(true);
        outputScrollPane.setFitToHeight(true);
        // 设置输出区域的垂直增长策略
        VBox.setVgrow(outputScrollPane, Priority.ALWAYS);
        root.getChildren().addAll(expressionLabel, infixExpressionField, evaluateButton, outputLabel, outputScrollPane);

        primaryStage.setScene(new Scene(root, 400, 650));
        primaryStage.show();
    }

    private static int errorPosition;

    public static void setErrorPosition(int position) {
        errorPosition = position + 1;
    }

    private void evaluateInfixExpression() {
        String infixExpression = infixExpressionField.getText();

        // Step 1: 中缀式错误检查
        GrammarProcessUtil grammarProcess = new GrammarProcessUtil (outputArea);

        boolean check = grammarProcess.check(infixExpression);
        Platform.runLater(() -> {
            if (check) {
                outputArea.appendText("\n检查结果：" + check);
            }
            else {
                outputArea.appendText("\n检查结果：" + check);
                outputArea.appendText("\n错误位置：" + errorPosition);
            }
        });

        // Step 2: 中缀式翻译为逆波兰式
        String postfixExpression = infixToPostfix(infixExpression);
        outputArea.setText("逆波兰式：" + postfixExpression + "\n");

        // Step 3: 逆波兰式的求值
        double result = evaluatePostfixExpression(postfixExpression);
//        int intValue = (int) result;
        outputArea.appendText("\n计算结果：" + result + "\n");
    }


    private String infixToPostfix(String infixExpression) {
        // 实现中缀表达式到逆波兰式的转换逻辑
        StringBuilder postfixExpression = new StringBuilder();
        Stack<Character> stack = new Stack<>();

        for (int i = 0; i < infixExpression.length(); i++) {
            char c = infixExpression.charAt(i);

            if (isOperand(c)) {
                postfixExpression.append(c);
            } else if (isOperator(c)) {
                while (!stack.isEmpty() && stack.peek() != '(' && hasHigherPrecedence(stack.peek(), c)) {
                    postfixExpression.append(stack.pop());
                }
                stack.push(c);
            } else if (c == '(') {
                stack.push(c);
            } else if (c == ')') {
                while (!stack.isEmpty() && stack.peek() != '(') {
                    postfixExpression.append(stack.pop());
                }
                if (!stack.isEmpty() && stack.peek() == '(') {
                    stack.pop(); // 弹出左括号
                }
            }
        }

        while (!stack.isEmpty()) {
            postfixExpression.append(stack.pop());
        }

        return postfixExpression.toString();
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '%' || c == '^';
    }

    private boolean isOperand(char c) {
        // 检查字符是否为操作数（数字或字母等）
        return Character.isLetterOrDigit(c);
    }

    private int getPrecedence(char operator) {
        // 返回运算符的优先级
        if (operator == '+' || operator == '-') {
            return 1;
        } else if (operator == '*' || operator == '/') {
            return 2;
        } else {
            return 0;
        }
    }

    private boolean hasHigherPrecedence(char operator1, char operator2) {
        // 检查operator1的优先级是否高于operator2
        int precedence1 = getPrecedence(operator1);
        int precedence2 = getPrecedence(operator2);
        return precedence1 >= precedence2;
    }


    private double evaluatePostfixExpression(String postfixExpression) {
        // 实现逆波兰式的求值逻辑
        Stack<Double> stack = new Stack<>();

        for (int i = 0; i < postfixExpression.length(); i++) {
            char c = postfixExpression.charAt(i);

            if (isOperand(c)) {
                // 如果是操作数，将其转换为数字并推入栈中
                double operand = Double.parseDouble(String.valueOf(c));
                stack.push(operand);
            } else if (isOperator(c)) {
                // 如果是运算符，从栈中弹出两个操作数进行运算，并将结果推入栈中
                double operand2 = stack.pop();
                double operand1 = stack.pop();
                double result = performOperation(operand1, operand2, c);
                stack.push(result);
            }
        }

        return stack.pop();
    }

    private double performOperation(double operand1, double operand2, char operator) {
        // 根据运算符执行相应的运算
        switch (operator) {
            case '+':
                return operand1 + operand2;
            case '-':
                return operand1 - operand2;
            case '*':
                return operand1 * operand2;
            case '/':
                return operand1 / operand2;
            default:
                return 0.0;
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
