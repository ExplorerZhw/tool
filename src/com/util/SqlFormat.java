package com.util;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlFormat {
    private static final Set<String> BEGIN_CLAUSES = new HashSet<String>();
    private static final Set<String> END_CLAUSES = new HashSet<String>();
    private static final Set<String> LOGICAL = new HashSet<String>();
    private static final Set<String> QUANTIFIERS = new HashSet<String>();
    private static final Set<String> DML = new HashSet<String>();
    private static final Set<String> MISC = new HashSet<String>();
    static final String indentString = "    ";
    static final String initial = "\n    ";

    public static String format(String source) {
        String s = new FormatProcess(source).perform().trim();
        return s.replaceAll("(insert|INSERT)", "\n\n\nINSERT")
                .replaceAll("(update|UPDATE)", "\n\n\nUPDATE");
    }

    static {
        BEGIN_CLAUSES.add("left");
        BEGIN_CLAUSES.add("right");
        BEGIN_CLAUSES.add("inner");
        BEGIN_CLAUSES.add("outer");
        BEGIN_CLAUSES.add("group");
        BEGIN_CLAUSES.add("order");

        END_CLAUSES.add("where");
        END_CLAUSES.add("set");
        END_CLAUSES.add("having");
        END_CLAUSES.add("join");
        END_CLAUSES.add("from");
        END_CLAUSES.add("by");
        END_CLAUSES.add("join");
        END_CLAUSES.add("into");
        END_CLAUSES.add("union");

        LOGICAL.add("and");
        LOGICAL.add("or");
        LOGICAL.add("when");
        LOGICAL.add("else");
        LOGICAL.add("end");

        QUANTIFIERS.add("in");
        QUANTIFIERS.add("all");
        QUANTIFIERS.add("exists");
        QUANTIFIERS.add("some");
        QUANTIFIERS.add("any");

        DML.add("insert");
        DML.add("update");
        DML.add("delete");

        MISC.add("select");
        MISC.add("on");
    }

    private static class FormatProcess {
        boolean beginLine = true;
        boolean afterBeginBeforeEnd = false;
        boolean afterByOrSetOrFromOrSelect = false;
        boolean afterValues = false;
        boolean afterOn = false;
        boolean afterBetween = false;
        boolean afterInsert = false;
        int inFunction = 0;
        int parensSinceSelect = 0;
        private LinkedList<Integer> parenCounts = new LinkedList<Integer>();
        private LinkedList<Boolean> afterByOrFromOrSelects = new LinkedList<Boolean>();

        int indent = 1;

        StringBuffer result = new StringBuffer();
        StringTokenizer tokens;
        String lastToken;
        String token;
        String lcToken;

        public FormatProcess(String sql) {
            this.tokens = new StringTokenizer(sql, "()+*/-=<>‘`\"[], \n\r\f\t", true);
        }

        public String perform() {
            this.result.append("\n    ");

            while (this.tokens.hasMoreTokens()) {
                this.token = this.tokens.nextToken();
                this.lcToken = this.token.toLowerCase();

                if ("‘".equals(this.token)) {
                    String t;
                    do {
                        t = this.tokens.nextToken();
                        this.token += t;
                    } while ((!"‘".equals(t)) && (this.tokens.hasMoreTokens()));
                } else if ("\"".equals(this.token)) {
                    String t;
                    do {
                        t = this.tokens.nextToken();
                        this.token += t;
                    } while (!"\"".equals(t));
                }

                if ((this.afterByOrSetOrFromOrSelect) && (",".equals(this.token))) {
                    commaAfterByOrFromOrSelect();
                } else if ((this.afterOn) && (",".equals(this.token))) {
                    commaAfterOn();
                } else if ("(".equals(this.token)) {
                    openParen();
                } else if (")".equals(this.token)) {
                    closeParen();
                } else if (SqlFormat.BEGIN_CLAUSES.contains(this.lcToken)) {
                    beginNewClause();
                } else if (SqlFormat.END_CLAUSES.contains(this.lcToken)) {
                    endNewClause();
                } else if ("select".equals(this.lcToken)) {
                    select();
                } else if (SqlFormat.DML.contains(this.lcToken)) {
                    updateOrInsertOrDelete();
                } else if ("values".equals(this.lcToken)) {
                    values();
                } else if ("on".equals(this.lcToken)) {
                    on();
                } else if ((this.afterBetween) && (this.lcToken.equals("and"))) {
                    misc();
                    this.afterBetween = false;
                } else if (SqlFormat.LOGICAL.contains(this.lcToken)) {
                    logical();
                } else if (isWhitespace(this.token)) {
                    white();
                } else {
                    misc();
                }

                if (!isWhitespace(this.token)) {
                    this.lastToken = this.lcToken;
                }
            }

            return this.result.toString();
        }

        private void commaAfterOn() {
            out();
            this.indent -= 1;
            newline();
            this.afterOn = false;
            this.afterByOrSetOrFromOrSelect = true;
        }

        private void commaAfterByOrFromOrSelect() {
            out();
            newline();
        }

        private void logical() {
            if ("end".equals(this.lcToken)) {
                this.indent -= 1;
            }
            newline();
            out();
            this.beginLine = false;
        }

        private void on() {
            this.indent += 1;
            this.afterOn = true;
            newline();
            out();
            this.beginLine = false;
        }

        private void misc() {
            out();
            if ("between".equals(this.lcToken)) {
                this.afterBetween = true;
            }
            if (this.afterInsert) {
                newline();
                this.afterInsert = false;
            } else {
                this.beginLine = false;
                if ("case".equals(this.lcToken))
                    this.indent += 1;
            }
        }

        private void white() {
            if (!this.beginLine)
                this.result.append(" ");
        }

        private void updateOrInsertOrDelete() {
            out();
            this.indent += 1;
            this.beginLine = false;
            if ("update".equals(this.lcToken)) {
                newline();
            }
            if ("insert".equals(this.lcToken))
                this.afterInsert = true;
        }

        private void select() {
            out();
            this.indent += 1;
            newline();
            this.parenCounts.addLast(new Integer(this.parensSinceSelect));
            this.afterByOrFromOrSelects.addLast(Boolean.valueOf(this.afterByOrSetOrFromOrSelect));
            this.parensSinceSelect = 0;
            this.afterByOrSetOrFromOrSelect = true;
        }

        private void out() {
            this.result.append(this.token);
        }

        private void endNewClause() {
            if (!this.afterBeginBeforeEnd) {
                this.indent -= 1;
                if (this.afterOn) {
                    this.indent -= 1;
                    this.afterOn = false;
                }
                newline();
            }
            out();
            if (!"union".equals(this.lcToken)) {
                this.indent += 1;
            }
            newline();
            this.afterBeginBeforeEnd = false;
            this.afterByOrSetOrFromOrSelect = (("by".equals(this.lcToken)) || ("set".equals(this.lcToken))
                    || ("from".equals(this.lcToken)));
        }

        private void beginNewClause() {
            if (!this.afterBeginBeforeEnd) {
                if (this.afterOn) {
                    this.indent -= 1;
                    this.afterOn = false;
                }
                this.indent -= 1;
                newline();
            }
            out();
            this.beginLine = false;
            this.afterBeginBeforeEnd = true;
        }

        private void values() {
            this.indent -= 1;
            newline();
            out();
            this.indent += 1;
            newline();
            this.afterValues = true;
        }

        private void closeParen() {
            this.parensSinceSelect -= 1;
            if (this.parensSinceSelect < 0) {
                this.indent -= 1;
                this.parensSinceSelect = ((Integer) this.parenCounts.removeLast()).intValue();
                this.afterByOrSetOrFromOrSelect = ((Boolean) this.afterByOrFromOrSelects.removeLast()).booleanValue();
            }
            if (this.inFunction > 0) {
                this.inFunction -= 1;
                out();
            } else {
                if (!this.afterByOrSetOrFromOrSelect) {
                    this.indent -= 1;
                    newline();
                }
                out();
            }
            this.beginLine = false;
        }

        private void openParen() {
            if ((isFunctionName(this.lastToken)) || (this.inFunction > 0)) {
                this.inFunction += 1;
            }
            this.beginLine = false;
            if (this.inFunction > 0) {
                out();
            } else {
                out();
                if (!this.afterByOrSetOrFromOrSelect) {
                    this.indent += 1;
                    newline();
                    this.beginLine = true;
                }
            }
            this.parensSinceSelect += 1;
        }

        private static boolean isFunctionName(String tok) {
            char begin = tok.charAt(0);
            boolean isIdentifier = (Character.isJavaIdentifierStart(begin));
            return (isIdentifier) && (!SqlFormat.LOGICAL.contains(tok))
                    && (!SqlFormat.END_CLAUSES.contains(tok))
                    && (!SqlFormat.QUANTIFIERS.contains(tok)) && (!SqlFormat.DML.contains(tok))
                    && (!SqlFormat.MISC.contains(tok));
        }

        private static boolean isWhitespace(String token) {
            return " \n\r\f\t".indexOf(token) >= 0;
        }

        private void newline() {
            this.result.append("\n");
            for (int i = 0; i < this.indent; i++) {
                this.result.append("    ");
            }
            this.beginLine = true;
        }
    }

    public static void main(String[] args) {

        String sql = "UPDATE \"GTMIS\".\"SYS_MENU\" SET \"MENU_ID\"='402881215ec678c8015ed0d8abe702b3', \"SUBSYSTEM_ID\"='402880415ddc0d0e015ddf169ca10012', \"MENU_NAME\"='缺陷管理', \"SHOW_NAME\"='缺陷管理', \"MENU_TIP\"=NULL, \"MENU_LEVEL\"='1', \"MENU_HINT\"=NULL, \"MENU_CLASS_STYLE\"=NULL, \"MENU_ICON\"=NULL, \"FID\"=NULL, \"ORDER_NUM\"='4', \"ID_PATH\"='/402881215ec678c8015ed0d8abe702b3/', \"ACTION_URL\"=NULL, \"IS_LEAF\"='Y', \"IS_SHOW\"='Y', \"PERM_CODE\"=NULL, \"MEMO\"=NULL, \"MODEL_CLASS_NAME\"=NULL, \"MODEL_PIC\"=NULL, \"MODEL_PARAM\"=NULL, \"ISDIALOGFORM\"=NULL, \"CREATE_DATETIME\"=TO_DATE('2017-09-30 11:33:34', 'SYYYY-MM-DD HH24:MI:SS'), \"CREATE_BY\"=NULL, \"UPDATE_DATETIME\"=TO_DATE('2017-10-19 10:51:25', 'SYYYY-MM-DD HH24:MI:SS')," +
                " \"UPDATE_BY\"=NULL, \"ROWID\"='AAASDRAAFAAAF2bAAH' WHERE ROWID = 'AAASDRAAFAAAF2bAAH';\n";

        sql = searchByRegex(sql, "(SET|set)\\s*?\".*?\"='.*?'");

        System.out.println(sql);
    }


    // 根据正则匹配内容
    public static String searchByRegex(String source, String reg) {
        Pattern p = Pattern.compile(reg);
        Matcher m = p.matcher(source);
        String result = "";
        if (m.find()) {
//            System.out.println(m.groupCount());
            result = m.group(0);
        }
        return result;
    }

    // 根据正则匹配内容
    public static String searchByRegexEnd(String source, String reg) {
        Pattern p = Pattern.compile(reg);
        Matcher m = p.matcher(source);
        String result = "";
        while (m.find()) {
//            System.out.println(m.groupCount());
            result = m.group(0);
        }
        return result;
    }
}