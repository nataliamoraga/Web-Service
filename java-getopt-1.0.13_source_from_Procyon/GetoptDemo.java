import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

// 
// Decompiled by Procyon v0.5.36
// 

public class GetoptDemo
{
    public static void main(final String[] array) {
        final LongOpt[] array2 = new LongOpt[3];
        final StringBuffer sb = new StringBuffer();
        array2[0] = new LongOpt("help", 0, null, 104);
        array2[1] = new LongOpt("outputdir", 1, sb, 111);
        array2[2] = new LongOpt("maximum", 2, null, 2);
        final Getopt getopt = new Getopt("testprog", array, "-:bc::d:hW;", array2);
        getopt.setOpterr(false);
        int getopt2;
        while ((getopt2 = getopt.getopt()) != -1) {
            switch (getopt2) {
                case 0: {
                    final String optarg = getopt.getOptarg();
                    System.out.println("Got long option with value '" + (char)(int)new Integer(sb.toString()) + "' with argument " + ((optarg != null) ? optarg : "null"));
                    continue;
                }
                case 1: {
                    System.out.println("I see you have return in order set and that a non-option argv element was just found with the value '" + getopt.getOptarg() + "'");
                    continue;
                }
                case 2: {
                    final String optarg2 = getopt.getOptarg();
                    System.out.println("I know this, but pretend I didn't");
                    System.out.println("We picked option " + array2[getopt.getLongind()].getName() + " with value " + ((optarg2 != null) ? optarg2 : "null"));
                    continue;
                }
                case 98: {
                    System.out.println("You picked plain old option " + (char)getopt2);
                    continue;
                }
                case 99:
                case 100: {
                    final String optarg3 = getopt.getOptarg();
                    System.out.println("You picked option '" + (char)getopt2 + "' with argument " + ((optarg3 != null) ? optarg3 : "null"));
                    continue;
                }
                case 104: {
                    System.out.println("I see you asked for help");
                    continue;
                }
                case 87: {
                    System.out.println("Hmmm. You tried a -W with an incorrect long option name");
                    continue;
                }
                case 58: {
                    System.out.println("Doh! You need an argument for option " + (char)getopt.getOptopt());
                    continue;
                }
                case 63: {
                    System.out.println("The option '" + (char)getopt.getOptopt() + "' is not valid");
                    continue;
                }
                default: {
                    System.out.println("getopt() returned " + getopt2);
                    continue;
                }
            }
        }
        for (int i = getopt.getOptind(); i < array.length; ++i) {
            System.out.println("Non option argv element: " + array[i] + "\n");
        }
    }
}
