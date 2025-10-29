//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.

import java.util.*;

public class AnalyzerV1 extends IAnalyzer{
    boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public String analiseExtended(FileReader file) {
        return "Not implemented yet";
    }

    @Override
    public Expression analise(FileReader file) {

        String line;
        Expression expression = new Expression();

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
                        || condition.toLowerCase().contains("o")) {
                    contador = 2;
                }
                exp.multiply(contador);

                // Extraer la expresion dentro del Si
                exp.add(analiseIf(file));
            } else if (parts[0].trim().equalsIgnoreCase("para")) {
                // Extraigo propiedades del Para
                exp = analiseFor(file, parts[1].trim());
            }

            expression.add(exp);
        }

        return expression;
    }

    Expression analiseIf(FileReader file) {
        String line;
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
        String line;
        Expression expression = new Expression();
        ForProperties properties = new ForProperties(props);

        System.out.println("Analysing For Loop: " + properties);
//
//        System.out.println("sumatoria es como:");
//        System.out.printf("from 1 to %s/%d%n", properties.end, properties.step);
//
//        if (!properties.start.equalsIgnoreCase("1")){
//            System.out.printf("menos from 1 to %s%n", properties.start);
//        }

        expression = analise(file);
        expression.add(2);

        System.out.println("Expression inside For: " + expression);

        // Para cada termino en la expresion, aplicamos las propiedades del For
        Expression finalExpression = new Expression();
        for (Term term : expression.terms) {
            Expression appliedExp = applyForProps(term, properties);
            finalExpression.add(appliedExp);
        }
        finalExpression.add(2);

//        System.out.println("Final Expression after For: " + finalExpression);

        return finalExpression;
    }

    Expression applyForProps(Term term, ForProperties props) {
        Expression result;

        if (term.hasVariable(props.variable)) {
            System.out.println("Aplicando tiene variable");
            result = applyForPropsI(term, props);
        } else {
            System.out.println("Aplicando no tiene variable");
            result = applyForPropsC(term, props);
            System.out.println("Result after applyForPropsC: " + result);
        }

        return result;
    }

    public Expression applyForPropsI(Term term, ForProperties props) {
        Expression result = new Expression();
        Term independentTerm = term.removeVariableNew(props.variable);
        int exponent = term.getVariableExponent(props.variable);
//        Term variableTerm = new Term(1, props.variable + (exponent > 1 ? "^" + exponent : ""));

        if (props.step == 1) {
            result.add(applyPureForPropsI(exponent, props.end));
            result.add(applyPureForPropsI(exponent, props.start).multiply(-1));
            if (exponent == 1) {
                if (isNumeric(props.start)) {
                    int startValue = Integer.parseInt(props.start);
                    result.add(startValue);
                } else {
                    result.add(new Term(1, props.start));
                }
            } else {
                // Caso de i^2
                if (isNumeric(props.start)) {
                    int startValue = Integer.parseInt(props.start);
                    result.add(startValue*startValue);
                } else {
                    result.add(new Term(1, props.start + "^2"));
                }
            }
            result.multiply(independentTerm);
        }

        return result;
    }

    public Expression applyPureForPropsI(int exponent, String end) {
        Expression result = new Expression();
//        int exponent = term.getVariableExponent(props.variable);

        if (exponent == 1) {
            if (isNumeric(end)){
                int endValue = Integer.parseInt(end);
                result.add(endValue*(endValue + 1f) / 2);
            } else {
                result.add(new Term(1f/2, end + "^2") );
                result.add(new Term(1f/2, end) );
            }
        } else {
            // Caso de i^2
            if (isNumeric(end)){
                int endValue = Integer.parseInt(end);
                int terms = (endValue * (endValue + 1) * (2 * endValue + 1)) / 6;
                result.add(terms);
            } else {
                result.add(new Term(1f/3, end + "^3") );
                result.add(new Term(1f/2, end + "^2") );
                result.add(new Term(1f/6, end) );
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
                result.add(end);
            } else {
                result.add(new Term(1, props.end));
            }

            // Verificamos inicio
            if (!props.start.equalsIgnoreCase("1")){
                if (isNumeric(props.start)){
                    int start = Integer.parseInt(props.start);
                    result.add(-start);
                } else {
                    result.add(new Term(-1, props.start));
                }

                result.add(1);
            }
        } else {
            // Paso > 1
            if (isNumeric(props.end)){
                float end = Float.parseFloat(props.end);
                float terms = end / props.step;
                result.add(terms);
            } else {
                result.add(new Term(1f/ props.step, props.end));
            }

            // Verificamos inicio
            if (isNumeric(props.start)){
                float start = Float.parseFloat(props.start);
                float terms = start / props.step;
                result.add(-terms);
            } else {
                result.add(new Term(-1f/props.step, props.start));
            }

            result.add(1);
        }

        result.multiply(term);

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
            parseLiteral(literal.toLowerCase());
        }

        Term(float coefficient) {
            this.coefficient = coefficient;
            this.variables = new HashMap<>();
        }

        boolean hasVariable(String varName) {
            return this.variables.containsKey(varName);
        }

        Term removeVariableNew(String varName) {
            if (!this.variables.containsKey(varName)) {
                return this;
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

        private void parseLiteral(String literal) {
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
            if (this.getFixedLiteral().equals(other.getFixedLiteral())) {
                this.coefficient += other.coefficient;
            } else {
                throw new IllegalArgumentException("Cannot add terms with different variables");
            }
            return this;
        }

        Term subtract(Term other) {
            if (this.getFixedLiteral().equals(other.getFixedLiteral())) {
                this.coefficient -= other.coefficient;
            } else {
                throw new IllegalArgumentException("Cannot subtract terms with different variables");
            }
            return this;
        }

        Term multiply(Term other) {
            this.coefficient *= other.coefficient;
            for (Map.Entry<String, Integer> entry : other.variables.entrySet()) {
                this.variables.put(entry.getKey(), this.variables.getOrDefault(entry.getKey(), 0) + entry.getValue());
            }

            return this;
        }

        Term multiply(float scalar) {
            this.coefficient *= scalar;
            return this;
        }

        String toStringTerm() {
            StringBuilder str = new StringBuilder();
            if (coefficient > 0) {
                str.append("+");
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

        float getConstantCoefficient() {
            for (Term term : terms) {
                if (term.isConstant()) {
                    return term.coefficient;
                }
            }
            return 0;
        }

        public Expression add(Term term) {
            if (term.coefficient == 0) {
                return this;
            }

            Term resultTerm = null;
            for (Term t : terms) {
                if (t.getFixedLiteral().equals(term.getFixedLiteral())) {
                    resultTerm = t.add(term);
                    break;
                }
            }
            if (resultTerm == null) {
                terms.add(term);
            } else if (resultTerm.coefficient == 0) {
                terms.remove(resultTerm);
            }

            return this;
        }

        void add(Expression expr) {
            for (Term term : expr.terms) {
                this.add(term);
            }
        }

        void add(float scalar) {
            this.add(new Term(scalar));
        }

        void multiply(Term term) {
            if (term.coefficient == 0) {
                terms.clear();
                return;
            }

            terms.replaceAll(term1 -> term1.multiply(term));
        }

        Expression multiply(float scalar) {
            if (scalar == 0) {
                terms.clear();
                return this;
            }

            for (Term term : terms) {
                term.multiply(scalar);
            }

            return this;
        }

        String toStringExpression() {
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

