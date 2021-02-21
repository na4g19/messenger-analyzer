/**
 * Unescapes unicode sequences
 */
public class UnicodeParser {

    public static String unescapeString(String oldstr) {

        StringBuffer newstr = new StringBuffer(oldstr.length());

        boolean sawBackslash = false;

        for (int i = 0; i < oldstr.length(); i++) {

            int cp = oldstr.codePointAt(i);
            if (oldstr.codePointAt(i) > Character.MAX_VALUE) {
                i++;
            }

            if (!sawBackslash) {
                if (cp == '\\') {
                    sawBackslash = true;
                } else {
                    newstr.append(Character.toChars(cp));
                }
                continue;
            }

            if (cp == '\\') {
                sawBackslash = false;
                newstr.append('\\');
                newstr.append('\\');
                continue;
            }

            switch (cp) {

                case 'r':  newstr.append('\r');
                    break;

                case 'n':  newstr.append('\n');
                    break;

                case 'f':  newstr.append('\f');
                    break;

                case 'b':  newstr.append("\\b");
                    break;

                case 't':  newstr.append('\t');
                    break;

                case 'a':  newstr.append('\007');
                    break;

                case 'e':  newstr.append('\033');
                    break;

                case 'c':   {
                    if (++i == oldstr.length()) { die("trailing \\c"); }
                    cp = oldstr.codePointAt(i);

                    if (cp > 0x7f) { die("expected ASCII after \\c"); }
                    newstr.append(Character.toChars(cp ^ 64));
                    break;
                }

                case '8':
                case '9': die("illegal octal digit");
                    /* NOTREACHED */

                    /*
                     * may be 0 to 2 octal digits following this one
                     * so back up one for fallthrough to next case;
                     * unread this digit and fall through to next case.
                     */
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7': --i;
                    /* FALLTHROUGH */

                    /*
                     * Can have 0, 1, or 2 octal digits following a 0
                     * this permits larger values than octal 377, up to
                     * octal 777.
                     */
                case '0': {
                    if (i+1 == oldstr.length()) {

                        /* found \0 at end of string */
                        newstr.append(Character.toChars(0));
                        break;
                    }
                    i++;
                    int digits = 0;
                    int j;
                    for (j = 0; j <= 2; j++) {
                        if (i+j == oldstr.length()) {
                            break;
                        }

                        int ch = oldstr.charAt(i+j);
                        if (ch < '0' || ch > '7') {
                            break;
                        }
                        digits++;
                    }
                    if (digits == 0) {
                        --i;
                        newstr.append('\0');
                        break;
                    }
                    int value = 0;
                    try {
                        value = Integer.parseInt(
                                oldstr.substring(i, i+digits), 8);
                    } catch (NumberFormatException nfe) {
                        die("invalid octal value for \\0 escape");
                    }
                    newstr.append(Character.toChars(value));
                    i += digits-1;
                    break;
                } /* end case '0' */

                case 'x':  {
                    if (i+2 > oldstr.length()) {
                        die("string too short for \\x escape");
                    }
                    i++;
                    boolean saw_brace = false;
                    if (oldstr.charAt(i) == '{') {
                        /* ^^^^^^ ok to ignore surrogates here */
                        i++;
                        saw_brace = true;
                    }
                    int j;
                    for (j = 0; j < 8; j++) {

                        if (!saw_brace && j == 2) {
                            break;
                        }

                        /*
                         * ASCII test also catches surrogates
                         */
                        int ch = oldstr.charAt(i+j);
                        if (ch > 127) {
                            die("illegal non-ASCII hex digit in \\x escape");
                        }

                        if (saw_brace && ch == '}') { break; /* for */ }

                        if (! ( (ch >= '0' && ch <= '9')
                                ||
                                (ch >= 'a' && ch <= 'f')
                                ||
                                (ch >= 'A' && ch <= 'F')
                        )
                        )
                        {
                            die(String.format(
                                    "illegal hex digit #%d '%c' in \\x", ch, ch));
                        }

                    }
                    if (j == 0) { die("empty braces in \\x{} escape"); }
                    int value = 0;
                    try {
                        value = Integer.parseInt(oldstr.substring(i, i+j), 16);
                    } catch (NumberFormatException nfe) {
                        die("invalid hex value for \\x escape");
                    }
                    newstr.append(Character.toChars(value));
                    if (saw_brace) { j++; }
                    i += j-1;
                    break;
                }

                case 'u': {
                    if (i+4 > oldstr.length()) {
                        die("string too short for \\u escape");
                    }
                    i++;
                    int j;
                    for (j = 0; j < 4; j++) {
                        /* this also handles the surrogate issue */
                        if (oldstr.charAt(i+j) > 127) {
                            die("illegal non-ASCII hex digit in \\u escape");
                        }
                    }
                    int value = 0;
                    try {
                        value = Integer.parseInt( oldstr.substring(i, i+j), 16);
                    } catch (NumberFormatException nfe) {
                        die("invalid hex value for \\u escape");
                    }
                    newstr.append(Character.toChars(value));
                    i += j-1;
                    break;
                }

                case 'U': {
                    if (i+8 > oldstr.length()) {
                        die("string too short for \\U escape");
                    }
                    i++;
                    int j;
                    for (j = 0; j < 8; j++) {
                        /* this also handles the surrogate issue */
                        if (oldstr.charAt(i+j) > 127) {
                            die("illegal non-ASCII hex digit in \\U escape");
                        }
                    }
                    int value = 0;
                    try {
                        value = Integer.parseInt(oldstr.substring(i, i+j), 16);
                    } catch (NumberFormatException nfe) {
                        die("invalid hex value for \\U escape");
                    }
                    newstr.append(Character.toChars(value));
                    i += j-1;
                    break;
                }

                default:   newstr.append('\\');
                    newstr.append(Character.toChars(cp));
                    break;

            }
            sawBackslash = false;
        }

        if (sawBackslash) {
            newstr.append('\\');
        }

        return newstr.toString();
    }

    /*
     * Return a string "U+XX.XXX.XXXX" etc, where each XX set is the
     * xdigits of the logical Unicode code point.
     */
    public static String uniplus(String s) {

        if (s.length() == 0) {
            return "";
        }
        /* This is just the minimum; sb will grow as needed. */
        StringBuffer sb = new StringBuffer(2 + 3 * s.length());
        sb.append("U+");

        for (int i = 0; i < s.length(); i++) {

            sb.append(String.format("%X", s.codePointAt(i)));

            if (s.codePointAt(i) > Character.MAX_VALUE) {
                i++;
            }
            if (i+1 < s.length()) {
                sb.append(".");
            }
        }
        return sb.toString();
    }

    private static void die(String foa) {
        throw new IllegalArgumentException(foa);
    }

}
