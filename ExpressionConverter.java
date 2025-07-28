package com.example.expressionconverter;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import javafx.scene.effect.ColorAdjust;

public class ExpressionConverter extends Application {

    private TextField infixInput = new TextField();
    private TextField postfixOutput = new TextField();
    private TextArea variablesArea = new TextArea();
    private TextField resultOutput = new TextField();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Set up UI components
        primaryStage.setTitle("Infix to Postfix Converter and Evaluator");

        // Set application icon
        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
        } catch (Exception e) {
            System.out.println("Could not load icon: " + e.getMessage());
        }

        GridPane root = new GridPane();
        root.setPadding(new Insets(20, 20, 20, 20));
        root.setVgap(10);
        root.setHgap(10);

        // Create title with icon (kept but modified)
        HBox titleBox = new HBox(5);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        try {
            Image originalIcon = new Image(getClass().getResourceAsStream("/images/icon.png"));

            // Apply 50% contrast effect
            ColorAdjust colorAdjust = new ColorAdjust();
            colorAdjust.setContrast(-0.5);

            ImageView titleIconView = new ImageView(originalIcon);
            titleIconView.setEffect(colorAdjust);
            titleIconView.setFitHeight(32);
            titleIconView.setFitWidth(32);
            titleIconView.setPreserveRatio(true);

            Label titleLabel = new Label("Infix to Postfix Converter and Evaluator");
            titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
            titleBox.getChildren().addAll(titleIconView, titleLabel);
        } catch (Exception e) {
            System.out.println("Could not load title icon: " + e.getMessage());
            // Fallback without icon
            Label titleLabel = new Label("Infix to Postfix Converter and Evaluator");
            titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
            titleBox.getChildren().add(titleLabel);
        }
        GridPane.setColumnSpan(titleBox, 2);
        root.add(titleBox, 0, 0);

        // Infix Expression - SIMPLIFIED (no icon)
        root.add(new Label("Infix Expression:"), 0, 1);
        root.add(infixInput, 1, 1);  // Just the text field, no icon

        // Rest of your original UI code remains exactly the same
        Button convertButton = new Button("Convert to Postfix");
        convertButton.setStyle("-fx-base: #C2A4A1; -fx-text-fill: black;");
        GridPane.setColumnSpan(convertButton, 2);
        root.add(convertButton, 0, 2);

        // ... [Rest of your code remains unchanged] ...

        root.add(new Label("Postfix Notation:"), 0, 3);
        postfixOutput.setEditable(false);
        root.add(postfixOutput, 1, 3);

        root.add(new Label("Variables (format: a=2 b=3):"), 0, 4);
        variablesArea.setPrefRowCount(3);
        root.add(variablesArea, 1, 4);

        Button evaluateButton = new Button("Evaluate Expression");
        evaluateButton.setStyle("-fx-base: #C2A4A1; -fx-text-fill: black;");
        GridPane.setColumnSpan(evaluateButton, 2);
        root.add(evaluateButton, 0, 5);

        root.add(new Label("Result:"), 0, 6);
        resultOutput.setEditable(false);
        root.add(resultOutput, 1, 6);

        // Set button actions
        convertButton.setOnAction(e -> convertToPostfix());
        evaluateButton.setOnAction(e -> evaluateExpression());

        // Create scene with CSS
        Scene scene = new Scene(root, 900, 500);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        //   Create a simple Thread to focus into the root pane
        Platform.runLater(() -> {
            root.requestFocus();
        });

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void convertToPostfix() {
        try {
            String infix = infixInput.getText().trim();
            String postfix = convertInfixToPostfix(infix);
            postfixOutput.setText(postfix);
        } catch (Exception e) {
            showAlert("Conversion Error", e.getMessage());
        }
    }

    private void evaluateExpression() {
        try {
            String postfix = postfixOutput.getText().trim();
            if (postfix.isEmpty()) {
                throw new Exception("Please convert to postfix first");
            }

            String variablesText = variablesArea.getText().trim();
            double result = evaluatePostfix(postfix, variablesText);
            resultOutput.setText(String.format("%.2f", result));
        } catch (Exception e) {
            showAlert("Evaluation Error", e.getMessage());
        }
    }

    // Infix to Postfix Conversion
    private String convertInfixToPostfix(String infix) throws Exception {
        if (infix.isEmpty()) throw new Exception("Empty expression");

        StringBuilder postfix = new StringBuilder();     // تخزين الناتج النهائي
        Stack<Character> stack = new Stack<>();     //  حفظ الأقواس و المعاملات مؤقتاً

        // Process Each Character in the Infix Input
        for (int i = 0; i < infix.length(); i++) {
            char c = infix.charAt(i);

            // Skip Whitespace Characters
            if (Character.isWhitespace(c)) continue;

            // Handle Operands (Numbers and Variables)
            if (Character.isLetterOrDigit(c)) {
                while (i < infix.length() && Character.isLetterOrDigit(infix.charAt(i))) {
                    postfix.append(infix.charAt(i++));
                }
                postfix.append(' ');
                i--;
            }
            // الأقواس المفتوحة
            else if (c == '(') {
                stack.push(c);
            }
            // الأقواس المغلقة
            else if (c == ')') {
                while (!stack.isEmpty() && stack.peek() != '(') {
                    postfix.append(stack.pop()).append(' ');
                }
                if (stack.isEmpty()) throw new Exception("Mismatched parentheses");
                stack.pop();
            }
            // مقارنة اولوية العمليات الحسابية في الستاك
            else if (isOperator(c)) {
                while (!stack.isEmpty() && precedence(c) <= precedence(stack.peek())) {
                    postfix.append(stack.pop()).append(' ');
                }
                stack.push(c);
            }
            // مدخل خاطئ
            else {
                throw new Exception("Invalid character: " + c);
            }
        }

        // افراغ الستاك بعد جميع المعالجات
        while (!stack.isEmpty()) {
            if (stack.peek() == '(') throw new Exception("Mismatched parentheses");
            postfix.append(stack.pop()).append(' ');
        }

        // Return the Final Result
        return postfix.toString().trim();
    }

    // Postfix Evaluation
    private double evaluatePostfix(String postfix, String variablesText) throws Exception {
        Map<String, Double> variables = parseVariables(variablesText); // وضع قيمة لأي حرف لتتم العملية الحسابية
        Stack<Double> stack = new Stack<>();

        String[] tokens = postfix.split("\\s+"); //  Split Postfix into Tokens    "5 3 +" → ["5", "3", "+"]

        // Process Each Token
        for (String token : tokens) {
            if (token.isEmpty()) continue;

            // 6 >> double 6.0
            if (isNumeric(token)) {
                stack.push(Double.parseDouble(token));
            }

            //  Checks if it exists in variables.
            else if (Character.isLetter(token.charAt(0))) {
                if (!variables.containsKey(token)) {
                    throw new Exception("Undefined variable: " + token);
                }
                stack.push(variables.get(token));
            }
            //  +, -, *, /, ,, 5+3 --> 8
            else if (isOperator(token)) {
                if (stack.size() < 2) throw new Exception("Insufficient operands");
                double b = stack.pop();
                double a = stack.pop();
                stack.push(applyOperator(a, b, token));
            }
            else {
                throw new Exception("Invalid token: " + token);
            }
        }

        // Final Check & Return Result
        if (stack.size() != 1) throw new Exception("Invalid expression");
        return stack.pop();
    }

    // Helper methods
    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '^';
    }

    private boolean isOperator(String s) {
        return s.length() == 1 && isOperator(s.charAt(0));
    }

    // معالجة الأولوية ( القيود )
    private int precedence(char operator) {
        switch (operator) {
            case '+': case '-': return 1;
            case '*': case '/': return 2;
            case '^': return 3;
            default: return -1;
        }
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private double applyOperator(double a, double b, String op) throws Exception {
        switch (op) {
            case "+": return a + b;
            case "-": return a - b;
            case "*": return a * b;
            case "/":
                if (b == 0) throw new Exception("Division by zero");
                return a / b;
            case "^": return Math.pow(a, b);
            default: throw new Exception("Unknown operator: " + op);
        }
    }

    //      Map<String, Double> (variable names mapped to their values)
    private Map<String, Double> parseVariables(String variablesText) throws Exception {

        Map<String, Double> variables = new HashMap<>(); // Creates an empty HashMap to store variables.
        if (variablesText.isEmpty()) return variables;

        String[] assignments = variablesText.split("\\s+"); // "A=5 B=10" → ["A=5", "B=10"]

        //    Process Each Assignment
        for (String assignment : assignments) {
            String[] parts = assignment.split("="); // Split into (A,B,C,etc) and Value
            if (parts.length != 2) throw new Exception("Invalid variable assignment: " + assignment);

            // Contains only letters
            String name = parts[0].trim();
            if (!name.matches("[a-zA-Z]+")) throw new Exception("Invalid variable name: " + name);

            //Parse and Store the Value
            try {
                variables.put(name, Double.parseDouble(parts[1].trim()));
            } catch (NumberFormatException e) {
                throw new Exception("Invalid value for variable " + name + ": " + parts[1]);
            }
        }
        // Return the Parsed Variables
        return variables;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}