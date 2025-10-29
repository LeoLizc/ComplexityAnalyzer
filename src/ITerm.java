import java.util.HashMap;

public abstract class ITerm {

    abstract float getCoefficient();
    abstract HashMap<String, Integer> getVariables();
    abstract int getVariableExponent(String varName);
    abstract String getLiteral();
    abstract String getFixedLiteral();


    public int getTotalExponent() {
        int total = 0;
        for (int exp : getVariables().values()) {
            total += exp;
        }
        return total;
    }

    boolean isConstant() {
        return getVariables().isEmpty();
    }
}
