

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
        primaryStage.setTitle("��׺���ʽ������");

        // ���������Ͱ�ť
        Label expressionLabel = new Label("��׺���ʽ��");
        infixExpressionField = new TextField();
        Button evaluateButton = new Button("����");

        // �����������
        Label outputLabel = new Label("�����");
        outputArea = new TextArea();
        outputArea.setEditable(false);

        // ���ð�ť�ĵ���¼�
        evaluateButton.setOnAction(e -> evaluateInfixExpression());

        // �������ֲ���ӿؼ�
        VBox root = new VBox();
        root.setSpacing(10);
        root.setPadding(new Insets(10));
        // �������������ڹ��������
        ScrollPane outputScrollPane = new ScrollPane(outputArea);
        outputScrollPane.setFitToWidth(true);
        outputScrollPane.setFitToHeight(true);
        // �����������Ĵ�ֱ��������
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

        // Step 1: ��׺ʽ������
        GrammarProcessUtil grammarProcess = new GrammarProcessUtil (outputArea);

        boolean check = grammarProcess.check(infixExpression);
        Platform.runLater(() -> {
            if (check) {
                outputArea.appendText("\n�������" + check);
            }
            else {
                outputArea.appendText("\n�������" + check);
                outputArea.appendText("\n����λ�ã�" + errorPosition);
            }
        });

        // Step 2: ��׺ʽ����Ϊ�沨��ʽ
        String postfixExpression = infixToPostfix(infixExpression);
        outputArea.setText("�沨��ʽ��" + postfixExpression + "\n");

        // Step 3: �沨��ʽ����ֵ
        double result = evaluatePostfixExpression(postfixExpression);
//        int intValue = (int) result;
        outputArea.appendText("\n��������" + result + "\n");
    }


    private String infixToPostfix(String infixExpression) {
        // ʵ����׺���ʽ���沨��ʽ��ת���߼�
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
                    stack.pop(); // ����������
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
        // ����ַ��Ƿ�Ϊ�����������ֻ���ĸ�ȣ�
        return Character.isLetterOrDigit(c);
    }

    private int getPrecedence(char operator) {
        // ��������������ȼ�
        if (operator == '+' || operator == '-') {
            return 1;
        } else if (operator == '*' || operator == '/') {
            return 2;
        } else {
            return 0;
        }
    }

    private boolean hasHigherPrecedence(char operator1, char operator2) {
        // ���operator1�����ȼ��Ƿ����operator2
        int precedence1 = getPrecedence(operator1);
        int precedence2 = getPrecedence(operator2);
        return precedence1 >= precedence2;
    }


    private double evaluatePostfixExpression(String postfixExpression) {
        // ʵ���沨��ʽ����ֵ�߼�
        Stack<Double> stack = new Stack<>();

        for (int i = 0; i < postfixExpression.length(); i++) {
            char c = postfixExpression.charAt(i);

            if (isOperand(c)) {
                // ����ǲ�����������ת��Ϊ���ֲ�����ջ��
                double operand = Double.parseDouble(String.valueOf(c));
                stack.push(operand);
            } else if (isOperator(c)) {
                // ��������������ջ�е��������������������㣬�����������ջ��
                double operand2 = stack.pop();
                double operand1 = stack.pop();
                double result = performOperation(operand1, operand2, c);
                stack.push(result);
            }
        }

        return stack.pop();
    }

    private double performOperation(double operand1, double operand2, char operator) {
        // ���������ִ����Ӧ������
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
