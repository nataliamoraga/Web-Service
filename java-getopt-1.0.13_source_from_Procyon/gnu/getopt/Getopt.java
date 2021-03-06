// 
// Decompiled by Procyon v0.5.36
// 

package gnu.getopt;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class Getopt
{
    protected static final int REQUIRE_ORDER = 1;
    protected static final int PERMUTE = 2;
    protected static final int RETURN_IN_ORDER = 3;
    protected String optarg;
    protected int optind;
    protected boolean opterr;
    protected int optopt;
    protected String nextchar;
    protected String optstring;
    protected LongOpt[] long_options;
    protected boolean long_only;
    protected int longind;
    protected boolean posixly_correct;
    protected boolean longopt_handled;
    protected int first_nonopt;
    protected int last_nonopt;
    private boolean endparse;
    protected String[] argv;
    protected int ordering;
    protected String progname;
    private ResourceBundle _messages;
    
    public Getopt(final String s, final String[] array, final String s2) {
        this(s, array, s2, null, false);
    }
    
    public Getopt(final String s, final String[] array, final String s2, final LongOpt[] array2) {
        this(s, array, s2, array2, false);
    }
    
    public Getopt(final String progname, final String[] argv, String optstring, final LongOpt[] long_options, final boolean long_only) {
        this.opterr = true;
        this.optopt = 63;
        this.first_nonopt = 1;
        this.last_nonopt = 1;
        this.endparse = false;
        this._messages = ResourceBundle.getBundle("gnu/getopt/MessagesBundle", Locale.getDefault());
        if (optstring.length() == 0) {
            optstring = " ";
        }
        this.progname = progname;
        this.argv = argv;
        this.optstring = optstring;
        this.long_options = long_options;
        this.long_only = long_only;
        if (System.getProperty("gnu.posixly_correct", null) == null) {
            this.posixly_correct = false;
        }
        else {
            this.posixly_correct = true;
            this._messages = ResourceBundle.getBundle("gnu/getopt/MessagesBundle", Locale.US);
        }
        if (optstring.charAt(0) == '-') {
            this.ordering = 3;
            if (optstring.length() > 1) {
                this.optstring = optstring.substring(1);
            }
        }
        else if (optstring.charAt(0) == '+') {
            this.ordering = 1;
            if (optstring.length() > 1) {
                this.optstring = optstring.substring(1);
            }
        }
        else {
            if (this.posixly_correct) {
                this.ordering = 1;
                return;
            }
            this.ordering = 2;
        }
    }
    
    public void setOptstring(String optstring) {
        if (optstring.length() == 0) {
            optstring = " ";
        }
        this.optstring = optstring;
    }
    
    public int getOptind() {
        return this.optind;
    }
    
    public void setOptind(final int optind) {
        this.optind = optind;
    }
    
    public void setArgv(final String[] argv) {
        this.argv = argv;
    }
    
    public String getOptarg() {
        return this.optarg;
    }
    
    public void setOpterr(final boolean opterr) {
        this.opterr = opterr;
    }
    
    public int getOptopt() {
        return this.optopt;
    }
    
    public int getLongind() {
        return this.longind;
    }
    
    protected void exchange(final String[] array) {
        int first_nonopt = this.first_nonopt;
        final int last_nonopt = this.last_nonopt;
        int optind = this.optind;
        while (optind > last_nonopt && last_nonopt > first_nonopt) {
            if (optind - last_nonopt > last_nonopt - first_nonopt) {
                final int n = last_nonopt - first_nonopt;
                for (int i = 0; i < n; ++i) {
                    final String s = array[first_nonopt + i];
                    array[first_nonopt + i] = array[optind - (last_nonopt - first_nonopt) + i];
                    array[optind - (last_nonopt - first_nonopt) + i] = s;
                }
                optind -= n;
            }
            else {
                final int n2 = optind - last_nonopt;
                for (int j = 0; j < n2; ++j) {
                    final String s2 = array[first_nonopt + j];
                    array[first_nonopt + j] = array[last_nonopt + j];
                    array[last_nonopt + j] = s2;
                }
                first_nonopt += n2;
            }
        }
        this.first_nonopt += this.optind - this.last_nonopt;
        this.last_nonopt = this.optind;
    }
    
    protected int checkLongOption() {
        LongOpt longOpt = null;
        this.longopt_handled = true;
        boolean b = false;
        boolean b2 = false;
        this.longind = -1;
        int beginIndex = this.nextchar.indexOf("=");
        if (beginIndex == -1) {
            beginIndex = this.nextchar.length();
        }
        for (int i = 0; i < this.long_options.length; ++i) {
            if (this.long_options[i].getName().startsWith(this.nextchar.substring(0, beginIndex))) {
                if (this.long_options[i].getName().equals(this.nextchar.substring(0, beginIndex))) {
                    longOpt = this.long_options[i];
                    this.longind = i;
                    b2 = true;
                    break;
                }
                if (longOpt == null) {
                    longOpt = this.long_options[i];
                    this.longind = i;
                }
                else {
                    b = true;
                }
            }
        }
        if (b && !b2) {
            if (this.opterr) {
                System.err.println(MessageFormat.format(this._messages.getString("getopt.ambigious"), this.progname, this.argv[this.optind]));
            }
            this.nextchar = "";
            this.optopt = 0;
            ++this.optind;
            return 63;
        }
        if (longOpt == null) {
            this.longopt_handled = false;
            return 0;
        }
        ++this.optind;
        if (beginIndex != this.nextchar.length()) {
            if (longOpt.has_arg == 0) {
                if (this.opterr) {
                    if (this.argv[this.optind - 1].startsWith("--")) {
                        System.err.println(MessageFormat.format(this._messages.getString("getopt.arguments1"), this.progname, longOpt.name));
                    }
                    else {
                        System.err.println(MessageFormat.format(this._messages.getString("getopt.arguments2"), this.progname, new Character(this.argv[this.optind - 1].charAt(0)).toString(), longOpt.name));
                    }
                }
                this.nextchar = "";
                this.optopt = longOpt.val;
                return 63;
            }
            if (this.nextchar.substring(beginIndex).length() > 1) {
                this.optarg = this.nextchar.substring(beginIndex + 1);
            }
            else {
                this.optarg = "";
            }
        }
        else if (longOpt.has_arg == 1) {
            if (this.optind < this.argv.length) {
                this.optarg = this.argv[this.optind];
                ++this.optind;
            }
            else {
                if (this.opterr) {
                    System.err.println(MessageFormat.format(this._messages.getString("getopt.requires"), this.progname, this.argv[this.optind - 1]));
                }
                this.nextchar = "";
                this.optopt = longOpt.val;
                if (this.optstring.charAt(0) == ':') {
                    return 58;
                }
                return 63;
            }
        }
        this.nextchar = "";
        if (longOpt.flag != null) {
            longOpt.flag.setLength(0);
            longOpt.flag.append(longOpt.val);
            return 0;
        }
        return longOpt.val;
    }
    
    public int getopt() {
        this.optarg = null;
        if (this.endparse) {
            return -1;
        }
        if (this.nextchar == null || this.nextchar.equals("")) {
            if (this.last_nonopt > this.optind) {
                this.last_nonopt = this.optind;
            }
            if (this.first_nonopt > this.optind) {
                this.first_nonopt = this.optind;
            }
            if (this.ordering == 2) {
                if (this.first_nonopt != this.last_nonopt && this.last_nonopt != this.optind) {
                    this.exchange(this.argv);
                }
                else if (this.last_nonopt != this.optind) {
                    this.first_nonopt = this.optind;
                }
                while (this.optind < this.argv.length && (this.argv[this.optind].equals("") || this.argv[this.optind].charAt(0) != '-' || this.argv[this.optind].equals("-"))) {
                    ++this.optind;
                }
                this.last_nonopt = this.optind;
            }
            if (this.optind != this.argv.length && this.argv[this.optind].equals("--")) {
                ++this.optind;
                if (this.first_nonopt != this.last_nonopt && this.last_nonopt != this.optind) {
                    this.exchange(this.argv);
                }
                else if (this.first_nonopt == this.last_nonopt) {
                    this.first_nonopt = this.optind;
                }
                this.last_nonopt = this.argv.length;
                this.optind = this.argv.length;
            }
            if (this.optind == this.argv.length) {
                if (this.first_nonopt != this.last_nonopt) {
                    this.optind = this.first_nonopt;
                }
                return -1;
            }
            if (this.argv[this.optind].equals("") || this.argv[this.optind].charAt(0) != '-' || this.argv[this.optind].equals("-")) {
                if (this.ordering == 1) {
                    return -1;
                }
                this.optarg = this.argv[this.optind++];
                return 1;
            }
            else if (this.argv[this.optind].startsWith("--")) {
                this.nextchar = this.argv[this.optind].substring(2);
            }
            else {
                this.nextchar = this.argv[this.optind].substring(1);
            }
        }
        if (this.long_options != null && (this.argv[this.optind].startsWith("--") || (this.long_only && (this.argv[this.optind].length() > 2 || this.optstring.indexOf(this.argv[this.optind].charAt(1)) == -1)))) {
            final int checkLongOption = this.checkLongOption();
            if (this.longopt_handled) {
                return checkLongOption;
            }
            if (!this.long_only || this.argv[this.optind].startsWith("--") || this.optstring.indexOf(this.nextchar.charAt(0)) == -1) {
                if (this.opterr) {
                    if (this.argv[this.optind].startsWith("--")) {
                        System.err.println(MessageFormat.format(this._messages.getString("getopt.unrecognized"), this.progname, this.nextchar));
                    }
                    else {
                        System.err.println(MessageFormat.format(this._messages.getString("getopt.unrecognized2"), this.progname, new Character(this.argv[this.optind].charAt(0)).toString(), this.nextchar));
                    }
                }
                this.nextchar = "";
                ++this.optind;
                this.optopt = 0;
                return 63;
            }
        }
        final char char1 = this.nextchar.charAt(0);
        if (this.nextchar.length() > 1) {
            this.nextchar = this.nextchar.substring(1);
        }
        else {
            this.nextchar = "";
        }
        String substring = null;
        if (this.optstring.indexOf(char1) != -1) {
            substring = this.optstring.substring(this.optstring.indexOf(char1));
        }
        if (this.nextchar.equals("")) {
            ++this.optind;
        }
        if (substring == null || char1 == ':') {
            if (this.opterr) {
                if (this.posixly_correct) {
                    System.err.println(MessageFormat.format(this._messages.getString("getopt.illegal"), this.progname, new Character(char1).toString()));
                }
                else {
                    System.err.println(MessageFormat.format(this._messages.getString("getopt.invalid"), this.progname, new Character(char1).toString()));
                }
            }
            this.optopt = char1;
            return 63;
        }
        if (substring.charAt(0) != 'W' || substring.length() <= 1 || substring.charAt(1) != ';') {
            if (substring.length() > 1 && substring.charAt(1) == ':') {
                if (substring.length() > 2 && substring.charAt(2) == ':') {
                    if (!this.nextchar.equals("")) {
                        this.optarg = this.nextchar;
                        ++this.optind;
                    }
                    else {
                        this.optarg = null;
                    }
                    this.nextchar = null;
                }
                else {
                    if (!this.nextchar.equals("")) {
                        this.optarg = this.nextchar;
                        ++this.optind;
                    }
                    else if (this.optind == this.argv.length) {
                        if (this.opterr) {
                            System.err.println(MessageFormat.format(this._messages.getString("getopt.requires2"), this.progname, new Character(char1).toString()));
                        }
                        this.optopt = char1;
                        if (this.optstring.charAt(0) == ':') {
                            return 58;
                        }
                        return 63;
                    }
                    else {
                        this.optarg = this.argv[this.optind];
                        ++this.optind;
                        if (this.posixly_correct && this.optarg.equals("--")) {
                            if (this.optind == this.argv.length) {
                                if (this.opterr) {
                                    System.err.println(MessageFormat.format(this._messages.getString("getopt.requires2"), this.progname, new Character(char1).toString()));
                                }
                                this.optopt = char1;
                                if (this.optstring.charAt(0) == ':') {
                                    return 58;
                                }
                                return 63;
                            }
                            else {
                                this.optarg = this.argv[this.optind];
                                ++this.optind;
                                this.first_nonopt = this.optind;
                                this.last_nonopt = this.argv.length;
                                this.endparse = true;
                            }
                        }
                    }
                    this.nextchar = null;
                }
            }
            return char1;
        }
        if (!this.nextchar.equals("")) {
            this.optarg = this.nextchar;
        }
        else if (this.optind == this.argv.length) {
            if (this.opterr) {
                System.err.println(MessageFormat.format(this._messages.getString("getopt.requires2"), this.progname, new Character(char1).toString()));
            }
            this.optopt = char1;
            if (this.optstring.charAt(0) == ':') {
                return 58;
            }
            return 63;
        }
        else {
            this.nextchar = this.argv[this.optind];
            this.optarg = this.argv[this.optind];
        }
        final int checkLongOption2 = this.checkLongOption();
        if (this.longopt_handled) {
            return checkLongOption2;
        }
        this.nextchar = null;
        ++this.optind;
        return 87;
    }
}
