public class ConstantsGenerator {

    public static void main(String[] args) {
	int max = (int) Math.pow(3, 9);
	String[] values = new String[max];
	for (int i = 0; i < max; i++) {
	    String ternary = toTernaryString(i);
	    values[i] = ternary;
	}
	System.out.println(values[Integer.parseInt(args[0])]);
    }

    public static String toTernaryString(int i) {
	StringBuilder sb = new StringBuilder();
	while (i != 0) {
	    int digit = i % 3;
	    i /= 3;
	    sb.insert(0, Integer.toString(digit));
	}
	while (sb.length() < 4) {
	    sb.insert(0, "0");
	}
	return sb.toString();
    }

}