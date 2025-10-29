import java.util.ArrayList;
import java.util.List;

public abstract class IExpression {
    public abstract String toString();

    public abstract List<ITerm> getTerms();

    public List<ITerm> getBiggestTerms() {
        int maxExponent = -1;
//        System.out.println("All Terms: " + getTerms());
        for (ITerm term : getTerms()) {
            int totalExp = term.getTotalExponent();
            if (totalExp > maxExponent) {
                maxExponent = totalExp;
            }
        }

        List<ITerm> biggestTerms = new java.util.ArrayList<>();
        for (ITerm term : getTerms()) {
            if (term.getTotalExponent() == maxExponent) {
                biggestTerms.add(term);
            }
        }

        return biggestTerms;
    }

    public String getComplexity() {
        List<ITerm> biggestTerms = getBiggestTerms();
        ArrayList<ITerm> terms = new ArrayList<>();

        for (ITerm term : biggestTerms) {
            if (term.getCoefficient() > 0) {
                terms.add(term);
            }
        }

//        System.out.println("Biggest Terms: " + biggestTerms);

        if (terms.isEmpty()) {
            return "O(1)";
        }

        if (terms.size() == 1 && terms.get(0).isConstant()) {
            return "O(1)";
        }

        if (terms.size() == 1) {
            ITerm term = terms.get(0);
            return "O(" + term.getFixedLiteral() + ")";
        }

        StringBuilder complexity = new StringBuilder("O(");
        for (int i = 0; i < terms.size(); i++) {
            ITerm term = terms.get(i);
            complexity.append(term.getFixedLiteral());
            if (i < terms.size() - 1) {
                complexity.append(" + ");
            }
        }
        complexity.append(")");
        return complexity.toString();
    }

    public String getMaxedComplexity() {
        List<ITerm> biggestTerms = getBiggestTerms();
        ArrayList<ITerm> terms = new ArrayList<>();

        for (ITerm term : biggestTerms) {
            if (term.getCoefficient() > 0) {
                terms.add(term);
            }
        }

//        System.out.println("Biggest Terms: " + biggestTerms);

        if (terms.isEmpty()) {
            return "O(1)";
        }

        if (terms.size() == 1 && terms.get(0).isConstant()) {
            return "O(1)";
        }

        if (terms.size() == 1) {
            ITerm term = terms.get(0);
            return "O(" + term.getFixedLiteral() + ")";
        }

        StringBuilder complexity = new StringBuilder("max(");
        for (int i = 0; i < terms.size(); i++) {
            ITerm term = terms.get(i);
            complexity.append("O(").append(term.getFixedLiteral()).append(")");
            if (i < terms.size() - 1) {
                complexity.append(", ");
            }
        }
        complexity.append(")");
        return complexity.toString();
    }


}
