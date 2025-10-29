//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.

import java.util.*;

public class AnalyzerV3 extends IAnalyzer{
    boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isAlpha(String texto) {
        return texto.matches("[A-Za-z0-9]+");
    }

    public static boolean isExpression(String texto) {
        return texto.matches("[A-Za-z0-9+\\-*/^\\. ]+");
    }

    @Override
    public Expression analise(FileReader file) {

        String line;
        Expression expression = new Expression();
        HashMap<String, Expression> assignments = new HashMap<>();

        while((line = file.readLine()) != null && !isFinalExpression(line)) {
            // System.out.println("Read line: " + line);
            // Process the line as needed
            String[] parts = line.split(" ", 2);
            Expression exp = new Expression(1);

            if (parts[0].trim().equalsIgnoreCase("si")){
                // Extraigo propiedades del Si
                String condition = parts[1].trim();
                int contador = 1;

                if (
                        condition.toLowerCase().contains("y")
                        || condition.toLowerCase().contains("o")
                ) {
                    contador = 2;
                }
                exp = exp.multiply(contador);

                // Extraer la expresion dentro del Si
                exp = exp.add(analiseIf(file));
            } else if (parts[0].trim().equalsIgnoreCase("para")) {
                // Extraigo propiedades del Para
                exp = analiseFor(file, parts[1].trim());
            } else if (line.contains("=")) { // de la forma a= 2+3 ó b = 3+c*5
                String[] assignParts = line.split("=", 2);
                String varName = assignParts[0].trim();
                String exprString = assignParts[1].trim();

                if (isAlpha(varName) && isExpression(exprString)) {
                    Expression assignedExp = new Expression(exprString);

                    // Reemplazar variables en la expresion asignada
                    for (Map.Entry<String, Expression> entry : assignments.entrySet()) {
                        assignedExp = assignedExp.replaceVariable(entry.getKey(), entry.getValue());
                    }

                    assignments.put(varName.toLowerCase(), assignedExp);
                }
            }
//            System.out.println("part0: " + parts[0].trim() + ", extracted expression: " + exp);
//            System.out.println("Current expression before adding: " + expression);
            expression = expression.add(exp);
//            System.out.println("part0: " + parts[0].trim() + ", expression so far: " + expression);
        }

        // Reemplazar variables en la expresion final
        for (Map.Entry<String, Expression> entry : assignments.entrySet()) {
            expression = expression.replaceVariable(entry.getKey(), entry.getValue());
        }
        return expression;
    }

    public String analiseExtended(FileReader file) {

        String line;
        StringBuilder expression = new StringBuilder();
        int sum = 0;

        while((line = file.readLine()) != null && !isFinalExpression(line)) {
            // System.out.println("Read line: " + line);
            // Process the line as needed
            String[] parts = line.split(" ", 2);

            if (parts[0].trim().equalsIgnoreCase("si")){
                // Extraigo propiedades del Si
                String condition = parts[1].trim();
                int contador = 1;

                if (
                        condition.toLowerCase()
                                .replaceAll("\\s+", "")
                                .contains(")y(")
                                || condition.toLowerCase()
                                .replaceAll("\\s+", "")
                                .contains(")o(")
                ) {
                    contador = 2;
                }

                // Extraer la expresion dentro del Si
                contador += (int)analiseIf(file).getConstantCoefficient();

                if (expression.length() > 0) {
                    expression.append(" + ");
                }

                if (sum > 0) {
                    expression.append(sum).append(" + ");
                    sum = 0;
                }

                expression.append(contador);
            } else if (parts[0].trim().equalsIgnoreCase("para")) {
                // Extraigo propiedades del Para
//                exp = analiseFor(file, parts[1].trim());
                ForProperties properties = new ForProperties(parts[1].trim());
                if (expression.length() > 0) {
                    expression.append(" + ");
                }
                if (sum > 0) {
                    expression.append(sum).append(" + ");
                    sum = 0;
                }

                Expression limSup = new Expression();
                Expression a = new Expression(properties.start);
                Expression b = new Expression(properties.end);
                limSup = limSup.add(b)
                        .add(a.multiply(-1))
                        .multiply(1f/properties.step)
                        .add(1);

                expression.append("2+\\sum_{")
                        .append(properties.variable)
                        .append("=1}^{")
                        .append(limSup)
                        .append("}(2+");
                expression.append(analiseExtended(file));
                expression.append(")");
            } else {
                sum += 1;
            }
        }


        if (sum > 0) {
            if (expression.length() > 0) {
                expression.append(" + ");
            }
            expression.append(sum);
        }
        return expression.toString();
    }

    Expression analiseIf(FileReader file) {
//        String line;
        Expression expression;
        float ifCount = 0, elseCount = 0;

        expression = analise(file);
        ifCount = expression.getConstantCoefficient();

        if (file.getLastLine().equalsIgnoreCase("sino")) {
            expression = analise(file);
            elseCount = expression.getConstantCoefficient();

            if (elseCount > ifCount) {
                ifCount = elseCount;
            }
        }
//        System.out.println("Last line after If analysis: " + file.getLastLine());
        return new Expression(ifCount);
    }

    Expression analiseFor(FileReader file, String props) {
//        String line;
        Expression expression;
        ForProperties properties = new ForProperties(props);

//        System.out.println("Analysing For Loop: " + properties);
//
//        System.out.println("sumatoria es como:");
//        System.out.printf("from 1 to %s/%d%n", properties.end, properties.step);
//
//        if (!properties.start.equalsIgnoreCase("1")){
//            System.out.printf("menos from 1 to %s%n", properties.start);
//        }

        expression = analise(file);
        expression = expression.add(2);

//        System.out.println("Expression inside For: " + expression);

        // Para cada termino en la expresion, aplicamos las propiedades del For
        Expression finalExpression = new Expression();
        for (Term term : expression.terms) {
            Expression appliedExp = applyForProps(term, properties);
            finalExpression = finalExpression.add(appliedExp);
        }
        finalExpression = finalExpression.add(2);

//        System.out.println("Final Expression after For: " + finalExpression);

        return finalExpression;
    }

    Expression applyForProps(Term term, ForProperties props) {
        Expression result;

        if (term.hasVariable(props.variable)) {
//            System.out.println("Aplicando tiene variable");
            result = applyForPropsI(term, props);
        } else {
//            System.out.println("Aplicando no tiene variable");
            result = applyForPropsC(term, props);
//            System.out.println("Result after applyForPropsC: " + result);
        }

        return result;
    }

    public Expression applyForPropsI(Term term, ForProperties props) {
        Expression result = new Expression();
        Term independentTerm = term.removeVariable(props.variable);
        int exponent = term.getVariableExponent(props.variable);
//        Term variableTerm = new Term(1, props.variable + (exponent > 1 ? "^" + exponent : ""));

        if (props.step == 1) {
            result = result.add(applyPureForPropsI(exponent, props.end));
            result = result.add(applyPureForPropsI(exponent, props.start).multiply(-1));
            if (exponent == 1) {
                if (isNumeric(props.start)) {
                    int startValue = Integer.parseInt(props.start);
                    result = result.add(startValue);
                } else {
                    result = result.add(new Term(1, props.start));
                }
            } else {
                // Caso de i^2
                if (isNumeric(props.start)) {
                    int startValue = Integer.parseInt(props.start);
                    result= result.add(startValue*startValue);
                } else {
                    result = result.add(new Term(1, props.start + "^2"));
                }
            }
            result = result.multiply(independentTerm);
        } else {
            // Paso > 1
            // Construir nuevo límite superior
            Expression sup = new Expression();
            if (isNumeric(props.end)){
                float endValue = Float.parseFloat(props.end);
                sup = sup.add(endValue / props.step);
            } else {
                sup = sup.add(new Term(1f/ props.step, props.end));
            }

            if (isNumeric(props.start)){
                float startValue = Float.parseFloat(props.start);
                sup = sup.add(-startValue / props.step);
            } else {
                sup = sup.add(new Term(-1f/props.step, props.start));
            }

            if (exponent == 1) {
                result = result.add(sup.add(1).multiply(new Expression(props.start)));
                result = result.add(
                  sup.multiply(sup.add(1)).multiply(props.step/2f)
                );
            } else {
                // exponent == 2
                Expression startExp = new Expression(props.start);
                // a^2(n+1)
                result = result.add(
                        startExp.multiply(startExp).multiply(sup.add(1))
                );
                // a*step*n(n+1)
                result = result.add(
                        sup.multiply(sup.add(1)).multiply(startExp).multiply(props.step)
                );
                // step^2*n(n+1)(2n+1)/6
                result = result.add(
                        sup.multiply(sup.add(1))
                                .multiply(sup.multiply(2).add(1))
                                .multiply(props.step * props.step / 6f)
                );
            }

            result = result.multiply(independentTerm);
        }

        return result;
    }

    public Expression applyPureForPropsI(int exponent, String end) {
        Expression result = new Expression();
//        int exponent = term.getVariableExponent(props.variable);

        if (exponent == 1) {
            if (isNumeric(end)){
                int endValue = Integer.parseInt(end);
                result = result.add(endValue*(endValue + 1f) / 2);
            } else {
                result = result.add(new Term(1f/2, end + "^2") );
                result = result.add(new Term(1f/2, end) );
            }
        } else {
            // Caso de i^2
            if (isNumeric(end)){
                int endValue = Integer.parseInt(end);
                int terms = (endValue * (endValue + 1) * (2 * endValue + 1)) / 6;
                result = result.add(terms);
            } else {
                result = result.add(new Term(1f/3, end + "^3") );
                result = result.add(new Term(1f/2, end + "^2") );
                result = result.add(new Term(1f/6, end) );
            }
        }

        return result;
    }

    public Expression applyForPropsC(Term term, ForProperties props) {
        Expression result = new Expression();

        // Casos
        // Paso = 1
        if (props.step == 1) {
            // Verificamos final
            if (isNumeric(props.end)){
                int end = Integer.parseInt(props.end);
                result = result.add(end);
            } else {
                result = result.add(new Term(1, props.end));
            }

            // Verificamos inicio
            if (!props.start.equalsIgnoreCase("1")){
                if (isNumeric(props.start)){
                    int start = Integer.parseInt(props.start);
                    result = result.add(-start);
                } else {
                    result = result.add(new Term(-1, props.start));
                }

                result = result.add(1);
            }
        } else {
            // Paso > 1
            if (isNumeric(props.end)){
                float end = Float.parseFloat(props.end);
                float terms = end / props.step;
                result = result.add(terms);
            } else {
                result = result.add(new Term(1f/ props.step, props.end));
            }

            // Verificamos inicio
            if (isNumeric(props.start)){
                float start = Float.parseFloat(props.start);
                float terms = start / props.step;
                result = result.add(-terms);
            } else {
                result = result.add(new Term(-1f/props.step, props.start));
            }

            result = result.add(1);
        }

        result = result.multiply(term);

        return result;
    }

    boolean isFinalExpression(String line) {
        return line.equalsIgnoreCase("Pare")
                || line.equalsIgnoreCase("Fsi")
                || line.equalsIgnoreCase("sino")
                || line.equalsIgnoreCase("Fpara");
    }

    //---------------   Custom Clases   ------------------
    public class ForProperties {
        String variable;
        String start;
        String end;
        int step;

        ForProperties(String variable, String start, String end, int step) {
            this.variable = variable;
            this.start = start;
            this.end = end;
            this.step = step;
        }

        ForProperties(String encodedProps) {
            // Example encodedProps: "variable=1,10,+2"
            // Example encodedProps: "variable=10,1,-2"
            // Structure: "var=inicio,fin,incremento"

            String[] varAndRange = encodedProps.split("=");
            this.variable = varAndRange[0].trim();
            String[] rangeParts = varAndRange[1].split(",");
            this.start = rangeParts[0].trim();
            this.end = rangeParts[1].trim();
            this.step = Integer.parseInt(rangeParts[2].trim());

            if (this.step < 0) {
                // Swap start and end for negative step
                String temp = this.start;
                this.start = this.end;
                this.end = temp;
                this.step = -this.step;
            }
        }

        @Override
        public String toString() {
            return "ForProperties{" +
                    "variable='" + variable + '\'' +
                    ", start=" + start +
                    ", end=" + end +
                    ", step=" + step +
                    '}';
        }
    }

    public class Term extends ITerm {
        float coefficient;
        HashMap<String, Integer> variables; // variable name to exponent

        float getCoefficient() {
            return this.coefficient;
        }

        HashMap<String, Integer> getVariables() {
            return this.variables;
        }

        Term(float coefficient, HashMap<String, Integer> variables) {
            this.coefficient = coefficient;
            this.variables = new HashMap<>(variables);
        }

        Term(float coefficient, String literal) {
            this.coefficient = coefficient;
            this.variables = new HashMap<>();
            parseSingleLiteral(literal.toLowerCase());
        }

        Term(float coefficient, List<String> varList) {
            this.coefficient = coefficient;
            this.variables = new HashMap<>();
            for (String var : varList) {
                parseSingleLiteral(var.toLowerCase());
            }
        }

        Term(float coefficient) {
            this.coefficient = coefficient;
            this.variables = new HashMap<>();
        }

        Term(Term term) {
            this.coefficient = term.coefficient;
            this.variables = new HashMap<>(term.variables);
        }

        Term(String termString) {//For a string like "-3.5x^2y"
            this.variables = new HashMap<>();
            if (termString == null) {
                throw new IllegalArgumentException("Null term string");
            }
            String s = termString.trim().toLowerCase();
            if (s.isEmpty()) {
                throw new IllegalArgumentException("Empty term string");
            }
            int n = s.length();
            int i = 0;
            while (i < n && Character.isWhitespace(s.charAt(i))) i++;
            int sign = 1;
            if (i < n && (s.charAt(i) == '+' || s.charAt(i) == '-')) {
                if (s.charAt(i) == '-') sign = -1;
                i++;
            }
            while (i < n && Character.isWhitespace(s.charAt(i))) i++;
            int numStart = i;
            boolean hasNumber = false;
            while (i < n && (Character.isDigit(s.charAt(i)) || s.charAt(i) == '.')) {
                i++;
                hasNumber = true;
            }
            float parsedCoef;
            if (hasNumber) {
                String numStr = s.substring(numStart, i);
                if (numStr.equals(".") || numStr.isEmpty()) {
                    throw new IllegalArgumentException("Invalid coefficient in term: " + termString);
                }
                parsedCoef = Float.parseFloat(numStr) * sign;
            } else {
                parsedCoef = 1.0f * sign;
            }
            while (i < n && Character.isWhitespace(s.charAt(i))) i++;
            String varPart = "";
            if (i < n) {
                varPart = s.substring(i).replaceAll("\\s+", "");
            }
            this.coefficient = parsedCoef;
            if (!varPart.isEmpty()) {
                parseSingleLiteral(varPart);
            }
        }

        Term copy() {
            return new Term(this);
        }

        boolean hasVariable(String varName) {
            return this.variables.containsKey(varName);
        }

        Term removeVariable(String varName) {
            if (!this.variables.containsKey(varName)) {
                return new Term(this);
            }

            HashMap<String, Integer> newVars = new HashMap<>(this.variables);
            newVars.remove(varName);
            return new Term(this.coefficient, newVars);
        }

        int getVariableExponent(String varName) {
            return this.variables.getOrDefault(varName, 0);
        }

        boolean isConstant() {
            return this.variables.isEmpty();
        }

        private void parseSingleLiteral(String literal) {
            int i = 0;
            while (i < literal.length()) {
                StringBuilder varName = new StringBuilder();
                while (i < literal.length() && Character.isLetter(literal.charAt(i))) {
                    varName.append(literal.charAt(i));
                    i++;
                }
                int exponent = 1;
                if (i < literal.length() && literal.charAt(i) == '^') {
                    i++; // skip '^'
                    StringBuilder expStr = new StringBuilder();
                    while (i < literal.length() && Character.isDigit(literal.charAt(i))) {
                        expStr.append(literal.charAt(i));
                        i++;
                    }
                    exponent = Integer.parseInt(expStr.toString());
                }
                variables.put(varName.toString(), exponent);
            }
        }

        String getLiteral() {
            StringBuilder literal = new StringBuilder();
            for (Map.Entry<String, Integer> entry : variables.entrySet()) {
                literal.append(entry.getKey());
                if (entry.getValue() != 1) {
                    literal.append("^").append(entry.getValue());
                }
            }
            return literal.toString();
        }

        String getFixedLiteral() {
            List<String> varList = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : variables.entrySet()) {
                if (entry.getValue() == 1) {
                    varList.add(entry.getKey());
                } else {
                    varList.add(entry.getKey() + "^" + entry.getValue());
                }
            }
            Collections.sort(varList);
            return String.join("", varList);
        }

        Term add(Term other) {
            if (!this.getFixedLiteral().equals(other.getFixedLiteral())) {
                throw new IllegalArgumentException("Cannot add terms with different variables");
            }
            float newCoefficient = this.coefficient + other.coefficient;
            HashMap<String, Integer> newVars = new HashMap<>(this.variables);
            return new Term(newCoefficient, newVars);
        }

        Term subtract(Term other) {
            if (!this.getFixedLiteral().equals(other.getFixedLiteral())) {
                throw new IllegalArgumentException("Cannot subtract terms with different variables");
            }
            float newCoefficient = this.coefficient - other.coefficient;
            HashMap<String, Integer> newVars = new HashMap<>(this.variables);
            return new Term(newCoefficient, newVars);
        }

        Term multiply(Term other) {
            float newCoefficient = this.coefficient * other.coefficient;
            HashMap<String, Integer> newVars = new HashMap<>(this.variables);
            for (Map.Entry<String, Integer> entry : other.variables.entrySet()) {
                newVars.put(entry.getKey(), newVars.getOrDefault(entry.getKey(), 0) + entry.getValue());
            }
            return new Term(newCoefficient, newVars);
        }

        Term multiply(float scalar) {
            return new Term(this.coefficient * scalar, new HashMap<>(this.variables));
        }

        String toStringTerm() {
            StringBuilder str = new StringBuilder();
            if (coefficient > 0) {
                str.append("+");
            }
            if (variables.isEmpty()) {
                str.append(coefficient);
                return str.toString();
            }

            if (coefficient != 1 && coefficient != -1) {
                str.append(coefficient);
            } else if (coefficient == -1) {
                str.append("-");
            }
            str.append(getLiteral());
            return str.toString();
        }

        @Override
        public String toString() {
            return toStringTerm();
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Term term = (Term) o;
            return this.coefficient == term.coefficient && Objects.equals(variables, term.variables);
        }

    }

    public class Expression extends IExpression {
        List<Term> terms;

        public List<ITerm> getTerms() {
            List<ITerm> iterms = new ArrayList<>();
            for (Term term : terms) {
                iterms.add(term);
            }
            return iterms;
        }

        Expression() {
            this.terms = new ArrayList<>();
        }

        Expression(float scalar) {
            this.terms = new ArrayList<>();
            this.terms.add(new Term(scalar));
        }

        Expression(Term term) {
            this.terms = new ArrayList<>();
            this.terms.add(term);
        }

        private Expression(List<Term> terms) {
            this.terms = terms;
        }

        Expression(Expression expr) {
            this.terms = new ArrayList<>();
            for (Term term : expr.terms) {
                this.terms.add(new Term(term));
            }
        }

        Expression(String expressionString) {
            this.terms = new ArrayList<>();
            if (expressionString == null || expressionString.trim().isEmpty()) {
                return;
            }

            String expr = expressionString.trim().replaceAll("\\s+", "");
            List<String> termStrings = splitIntoTerms(expr);

            for (String termStr : termStrings) {
                if (!termStr.isEmpty()) {
                    Term term = parseTermFromString(termStr);
                    if (term.coefficient != 0) {
                        this.terms.add(term);
                    }
                }
            }
        }

        private List<String> splitIntoTerms(String expr) {
            List<String> termStrings = new ArrayList<>();
            StringBuilder currentTerm = new StringBuilder();

            for (int i = 0; i < expr.length(); i++) {
                char c = expr.charAt(i);
                if ((c == '+' || c == '-') && i > 0) {
                    termStrings.add(currentTerm.toString());
                    currentTerm = new StringBuilder();
                    if (c == '-') {
                        currentTerm.append(c);
                    }
                } else {
                    currentTerm.append(c);
                }
            }

            if (currentTerm.length() > 0) {
                termStrings.add(currentTerm.toString());
            }

            return termStrings;
        }

        private Term parseTermFromString(String termStr) {
            float coefficient = 1.0f;
//            StringBuilder variables = new StringBuilder();
            ArrayList<String> variables = new ArrayList<>();

            String[] parts = termStr.split("[*/]");
            List<Character> operators = new ArrayList<>();

            for (int i = 0; i < termStr.length(); i++) {
                char c = termStr.charAt(i);
                if (c == '*' || c == '/') {
                    operators.add(c);
                }
            }

            for (int i = 0; i < parts.length; i++) {
                String part = parts[i].trim();
                if (part.isEmpty()) continue;

                if (isNumeric(part)) {
                    float num = Float.parseFloat(part);
                    if (i > 0 && operators.get(i - 1) == '/') {
                        coefficient /= num;
                    } else {
                        coefficient *= num;
                    }
                } else {
                    variables.add(part);
                }
            }

            if (!variables.isEmpty()) {
                return new Term(coefficient, variables);
            } else {
                return new Term(coefficient);
            }
        }

        Expression copy() {
            return new Expression(this);
        }

        float getConstantCoefficient() {
            for (Term term : terms) {
                if (term.isConstant()) {
                    return term.coefficient;
                }
            }
            return 0;
        }

        Expression add(Term term) {
            if (term.coefficient == 0) {
                return this.copy();
            }
            // Retornará siempre una nueva expresion, no this

            List<Term> newTerms = new ArrayList<>();
            boolean added = false;
            for (Term existingTerm : this.terms) {
                if (existingTerm.getFixedLiteral().equals(term.getFixedLiteral())) {
                    Term newTerm = existingTerm.add(term);
                    if (newTerm.coefficient != 0) {
                        newTerms.add(newTerm);
                    }
                    added = true;
                } else {
                    newTerms.add(existingTerm.copy());
                }
            }

            if (!added) {
                newTerms.add(term.copy());
            }

            return new Expression(newTerms);
        }

        Expression add(Expression expr) {
            Expression result = this.copy();
            for (Term term : expr.terms) {
                result = result.add(term);
            }
            return result;
        }

        Expression add(float scalar) {
            return this.add(new Term(scalar));
        }

        Expression multiply(Term term) {
            if (term.coefficient == 0) {
                return new Expression();
            }

            List<Term> newTerms = new ArrayList<>();
            for (Term existingTerm : this.terms) {
                newTerms.add(existingTerm.multiply(term));
            }
            return new Expression(newTerms);
        }

        Expression multiply(float scalar) {
            if (scalar == 0) {
                return new Expression();
            }

            List<Term> newTerms = new ArrayList<>();
            for (Term term : this.terms) {
                newTerms.add(term.multiply(scalar));
            }
            return new Expression(newTerms);
        }

        Expression multiply(Expression expr) {
            List<Term> newTerms = new ArrayList<>();

            for (Term term2 : expr.terms) {
                for (Term term1 : this.terms) {
                    newTerms.add(term1.multiply(term2));
                }
            }

            return new Expression(newTerms);
        }

        Expression replaceVariable(String name, Expression replacement) {
            Expression result = new Expression();

            for (Term term : this.terms) {
                if (term.hasVariable(name)) {
                    Term independentTerm = term.removeVariable(name);
                    int exponent = term.getVariableExponent(name);

                    // Multiplicar replacement por si mismo exponent veces
                    Expression replacedExp = new Expression(replacement);
                    for (int i = 1; i < exponent; i++) {
                        replacedExp = replacedExp.multiply(replacement);
                    }

                    replacedExp = replacedExp.multiply(independentTerm);
                    result = result.add(replacedExp);
                } else {
                    result = result.add(term);
                }
            }

            return result;
        }

        String toStringExpression() {

            if (terms.isEmpty()) {
                return "0";
            }

            StringBuilder str = new StringBuilder();
            for (Term term : terms) {
                str.append(term.toStringTerm());
            }
            return str.toString();
        }

        @Override
        public String toString() {
            return toStringExpression();
        }
    }
}

