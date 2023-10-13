package com.complie;

import java.util.*;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

public class GrammarProcessUtil {
    /**
     * �ķ�
     */
    private static final String[] grammarRules = {
            "E��TE'",
            "E'��+TE'|-TE'|��",
            "T��FT'",
            "T'��*FT'|/FT'|��",
            "F��(E)|i",
            "i��1i|2i|3i|4i|5i|6i|7i|8i|9i|i0|��"
    };
    /**
     * ����ʽ�ķָ���
     */
    private static final String SEPARATOR = "��";
    /**
     * ������ս���� vnList�е�λ��
     */
    private static final Map<String,Integer> vnMap = new HashMap<>();

    /**
     * �����ս���� vtList �е�λ��
     */
    private static final Map<String,Integer> vtMap = new HashMap<>();

    /**
     * ������ս���Ĳ���ʽ
     */
    private static final Map<String,List<String>> productionMap = new HashMap<>();

    /**
     * ���ս����
     */
    private static final List<String> vnList = new ArrayList<>();

    /**
     * �ս����
     */
    private static final List<String> vtList = new ArrayList<>();

    /**
     * ������
     */
    private static String[][] table = null;

    /**
     * ��ѡʽ�ķָ���
     */
    private static final String CANDIDATE_SEPARATOR = "|";

    /**
     * �մ�
     */
    private static final String BLANK_STRING = "��";

    /**
     * FIRST��
     */
    private static final Map<String, Set<String>> first = new HashMap<>();

    /**
     * FOLLOW��
     */
    private static final Map<String,Set<String>> follow = new HashMap<>();

    private TextArea outputArea;

//    static {
    public  GrammarProcessUtil(TextArea outputArea) {
        this.outputArea = outputArea;

        for (String rule : grammarRules) {
            String[] split = rule.split(SEPARATOR);
            String left = split[0];
            String right = split[1];
            if (!vnMap.containsKey(left)) {
                vnMap.put(left, vnList.size());
                vnList.add(left);
                productionMap.put(left,splitBySeparator(right));
            }
        }
        // ��ȡ���ս����
        for(String vn:vnList) {
            // ��ȡ���ս������ʽ���Ҳ��ĺ�ѡʽ
            List<String> vnRight = productionMap.get(vn);
            for(String right:vnRight) {
                // ��һ������ѡʽ��ֳ��ַ����������� E �� S'a�������
                List<String> characters = getCharacter(right);
                for(String c:characters) {
                    // ���Ƿ��ս�� �� δ�����뵽�� �ս����
                    if(!vnMap.containsKey(c) && !vtMap.containsKey(c)
                            && !CANDIDATE_SEPARATOR.equals(c)) {
                        vtMap.put(c,vtList.size());
                        vtList.add(c);
                    }
                }
            }
        }
        // ��ӡ�ս���� �� ���ս����

//        Platform.runLater(() -> {
//            outputArea.appendText("\n�ս����" + vtList);
//            outputArea.appendText("\n���ս����" + vnList);
//        });
        System.out.println("�ս����" + vtList);
        System.out.println("���ս����" + vnList);
        // ��ȡFIRST��
        initFirst();
        // ��ӡFIRST��
        printFirst();
        // ��ȡFOLLOW��
        initFollow();
        // ��ӡFOLLOW��
        printFollow();
        // ��ȡ������
        getTable();
        // ��ӡ������
        printTable();

    }

    /**
     * ������ʽ���պ�ѡʽ�ķָ������зָ�
     * ����str:"E'a|��" => ["E'a","��"]
     *    str:��E'a" => ["E'a"]
     */
    private static List<String> splitBySeparator(String str) {
        List<String> ans = new ArrayList<>();
        if(!str.contains(CANDIDATE_SEPARATOR)) {
            // �������ָ��������ɷָ�
            ans.add(str);
            return ans;
        } else {
            // �����ָ��������зָ�
            String[] split = str.split("\\|");
            Collections.addAll(ans,split);
            return ans;
        }
    }

    /**
     * ���ַ����ָ��һ�����ַ�
     * ע����Ҫ�Ǵ���������ŵ��ַ�
     * ����str:"E'aT" => ["E'","a","T"]
     */
    private static List<String> getCharacter(String str) {
        List<String> ans = new ArrayList<>();
        int end = str.length();
        for(int start = str.length()-1;start >= 0;start--) {
            if(str.charAt(start) == '\'') {
                continue;
            } else {
                String substring = str.substring(start, end);
                ans.add(substring);
                end = start;
            }
        }
        return ans;
    }

    /**
     * �ж��ַ���str�Ƿ���ȫ���� regex�������±�
     * ����str:"aEb",regex="E" => 1
     *    str:"aE'b",regex="E" => -1
     *    str:"aE'b",regex="E'" => 1
     */
    private static int contains(String str,String regex) {
        char[] ch = str.toCharArray();
        if(regex.length() == 2) {
            for (int i = 0; i < ch.length - 1; i++) {
                if (ch[i] == regex.charAt(0) && ch[i + 1] == regex.charAt(1)) {
                    return i;
                }
            }
        } else if(regex.length() == 1) {
            for (int i = 0; i < ch.length; i++) {
                if ((i == ch.length-1 && ch[i] == regex.charAt(0))
                        || (ch[i] == regex.charAt(0) && ch[i + 1] != '\'')) {
                    return i;
                }
            }
        } else {
            throw new UnsupportedOperationException("int contains(String str,String regex):regex����ֻ֧��1��2");
        }
        return -1;
    }

    /*�жϸ����ķ��� vt �Ƿ�Ϊ�ս��*/
    public boolean containsVt(String vt) {
        if("#".equals(vt))
            return false;
        return vtMap.containsKey(vt);
    }

    private static void printFirst() {
        System.out.println("\nFIRST���ϣ�");
        for(String vn:vnList) {
            Set<String> strings = first.get(vn);
            StringBuilder sb = new StringBuilder();
            sb.append("FIRST(");
            sb.append(vn);
            sb.append(") = {");
            for(String s:strings) {
                sb.append(s).append(",");
            }
            sb.deleteCharAt(sb.length()-1);
            sb.append("}");
            System.out.println(sb);
        }
    }

    private static void printFollow() {
        System.out.println("\nFOLLOW���ϣ�");
        for(String vn:vnList) {
            Set<String> strings = follow.get(vn);
            StringBuilder sb = new StringBuilder();
            sb.append("FOLLOW(");
            sb.append(vn);
            sb.append(") = {");
            for(String s:strings) {
                sb.append(s).append(",");
            }
            sb.deleteCharAt(sb.length()-1);
            sb.append("}");
            System.out.println(sb);
        }
    }

    private static void printTable() {
        int blankIndex = vtMap.getOrDefault(BLANK_STRING,-1);
        System.out.print("\nԤ�������\n");
        for(int i = 0; i < vtList.size(); i++) {
            if(i != blankIndex) {
                System.out.print("\t\t  " + vtList.get(i));
            }
        }
        System.out.println("\t\t #");
        for(int i = 0; i < table.length; i++) {
            System.out.print(vnList.get(i)+"\t");
            int j;
            for( j = 0; j < table[0].length - 1; j++) {
                if(j != blankIndex) {
                    System.out.print("\t" + table[i][j]);
                }
            }
            System.out.println("\t"+table[i][j]);
        }
    }

    /**
     * ��ʼ��FIRST��
     */
    private static void initFirst() {
        for(String vn:vnList) {
            getFirst(vn);
        }
    }

    /**
     * ��ȡFIRSTS��
     */
    private static Set<String> getFirst(String vn) {
        if(first.containsKey(vn)) {
            return first.get(vn);
        }
        Set<String> set = new HashSet<>();
        // ����ǰ���ս�����Ϊ�Ѽ���
        first.put(vn, set);

        // ��������ʽ���Ҳ��ĺ�ѡʽ
        List<String> vnRight = productionMap.get(vn);
        for(String right:vnRight) {
            int index = 0;
            // ��ѡʽ�ĵ�һ���ַ�
            String firstCharacter = String.valueOf(right.charAt(index++));
            if (vtMap.containsKey(firstCharacter)) {
                // ���� E��a...,a����FIRST��
                set.add(firstCharacter);
            } else if (vnMap.containsKey(firstCharacter)) {
                // ���� E��S...��S��vt,FIRST(S)\{��}����FIRST(E)
                Set<String> s = getFirst(firstCharacter);
                // FIRST(S)�Ƿ�����մ�
                boolean hasBlankString = false;
                for(String str:s) {
                    if(!BLANK_STRING.equals(str)) {
                        set.add(str);
                    } else {
                        hasBlankString = true;
                    }
                }
                // ��¼�ò���ʽ�մ�����
                int blankStringNumber = hasBlankString ? 1 : 0;
                // E��S1S2S3... FIRST(S1)�����մ�,������һ���ַ�
                while(hasBlankString && index < right.length()
                        && vnMap.containsKey(String.valueOf(right.charAt(index)))) {
                    Set<String> nextFirst = getFirst(String.valueOf(right.charAt(index)));
                    for(String str:nextFirst) {
                        if(!BLANK_STRING.equals(str)) {
                            set.add(str);
                        } else {
                            blankStringNumber++;
                            hasBlankString = true;
                        }
                    }
                    index++;
                }
                // E��S1S2...Sn�е�ÿ���ַ��������Ƶ����մ����մ�����FIRST��
                if(blankStringNumber == right.length()) {
                    set.add(BLANK_STRING);
                }
            }
        }
//        first.put(vn,set);
        return set;
    }

    /**
     * ��ʼ��FOLLOW��
     */
    private static void initFollow() {
        for(String vn:vnList) {
            getFollow(vn);
        }
    }

    /**
     * ��ȡFOLLOW��
     */
    private static Set<String> getFollow(String vn) {
        // FOLLOW(vn)�Ѿ����ڣ�ֱ�ӷ���
        if(follow.containsKey(vn) && !follow.get(vn).isEmpty()) return follow.get(vn);
        Set<String> set = new HashSet<>();
        // �ķ���ʼ����#����FOLLOW��
        if(vnList.get(0).equals(vn)) {
            set.add("#");
        }
        // �鿴vn����������ʽ���Ҳ����ֹ�
        for(String left:vnList) {
            // �Լ���FOLLOW�����ù�
            if(left.equals(vn)) {
                continue;
            }
            // ��ȡvn��Ӧ�Ĳ���ʽ
            List<String> vnRight = productionMap.get(left);
            for(String item:vnRight) {
                int index = contains(item,vn);
                // �ڲ���ʽ�Ҳ����ֹ�
                if(index != -1) {
                    // ��������Ϊ��ʶ�� E'
                    if(index + vn.length() == item.length()) {
                        // left��...S S�����ڲ���ʽ���Ҳ���ʹ�ù���������FOLLOW(left)����FOLLOW(S)
                        Set<String> itemFollow = getFollow(left);
                        set.addAll(itemFollow);
                    } else {
                        // ����ʹ�ù����
                        // ��ȡnext������Ҫ����E'
                        String next = null;
                        int e1 = index + vn.length();
                        if(e1 + 1< item.length() && item.charAt(e1+1) == '\'') {
                            // SE',��һ���ַ���������
                            next = item.substring(e1,e1+2);
                        } else {
                            // SE,��һ���ַ�����������
                            next = item.substring(e1,e1+1);
                        }
                        if(vtMap.containsKey(next)) {
                            // ���next���ս������FIRST(next) = {next}��ֱ�Ӽ���
                            set.add(next);
                        } else {
                            // FIRST(next)\{��}����FOLLOW(vn)
                            Set<String> first = getFirst(next);
                            boolean hasBlankString = first.contains(BLANK_STRING);
                            for(String s:first) {
                                if(!BLANK_STRING.equals(s)) {
                                    set.add(s);
                                }
                            }
                            // FIRST(next)���Цţ�ʹ�ù�����
                            if(hasBlankString && !next.equals(vn)) {
                                Set<String> follow = getFollow(next);
                                set.addAll(follow);
                            }
                        }
                    }
                }
            }
        }
        follow.put(vn,set);
        return set;
    }

    /**
     * ��vn��vt��Ӧ�Ĳ���ʽ���������
     */
    private static void fillTable(String vn,String vt,String production) {
        if(table == null) {
            throw new IllegalStateException("����fillTableǰ��ȷ��table�ѳ�ʼ����");
        }
        // ��ȡvn��vt�ֱ���vnList��vtList���±�
        int vnIndex = vnMap.getOrDefault(vn,-1);
        int vtIndex = -1;
        if("#".equals(vt)) {
            vtIndex = table[0].length - 1;
        } else {
            vtIndex = vtMap.getOrDefault(vt, -1);
        }
        if(vnIndex == -1 || vtIndex == -1) {
            throw new IllegalArgumentException("fillTable(vn="+vn+",vt="+vt+",production="+production+")���Ϸ�");
        }
        // ƴ�Ӳ���ʽ
        String trueProduction = vn+SEPARATOR+production;
        // ���������
        table[vnIndex][vtIndex] = trueProduction;
    }

    /**
     * ����vn��Ӧ�Ĳ���ʽ���Ҳ���ʽ�а���vt
     * ����E��iESS'|a,vn:E,vt:i => iESS'
     */
    private static String findProduction(String vn,String vt) {
        List<String> list = productionMap.get(vn);
        for(String p:list) {
            if(p.startsWith(vt) || p.equals("i")) {
                return p;
            }
        }
        return null;
    }

    /**
     * ��ȡ������
     * ǰ�᣺FIRST����FOLLOW���ѹ������
     */
    private static String[][] getTable() {
        if(table != null)
            return table;
        table = new String[vnList.size()][vtList.size()+1];
        // �������ս��
        for(String vn:vnList) {
            // ��ȡ��Ӧ���ս����FIRST��
            Set<String> vnFirst = first.get(vn);
            // ����FIRST����ÿһ��Ԫ�أ��ҵ���Ӧ����ʽ�����������
            for(String vt:vnFirst) {
                if(!BLANK_STRING.equals(vt)) {
                    String production;
                    if (productionMap.get(vn).size() == 1) {
                        production = productionMap.get(vn).get(0);
                    } else {
                        production = findProduction(vn, vt);
                    }
                    fillTable(vn, vt, production);
                }
            }
            // FIRST�������մ�����FOLLOW������
            if(vnFirst.contains(BLANK_STRING)) {
                Set<String> vnFollow = follow.get(vn);
                for(String vt:vnFollow) {
                    fillTable(vn, vt, BLANK_STRING);
                }
            }
        }
        return table;
    }

    /**
     * ���ݷ��ս�����ս����ȡ�������еĲ���ʽ
     */
    public String get(String vn,String vt) {
        if(table == null) {
            throw new IllegalStateException("����fillTableǰ��ȷ��table�ѳ�ʼ����");
        }
        int vnIndex = vnMap.getOrDefault(vn,-1);
        int vtIndex = -1;
        if("#".equals(vt)) {
            vtIndex = table[0].length - 1;
        } else {
            vtIndex = vtMap.getOrDefault(vt, -1);
        }
        if(vnIndex == -1 || vtIndex == -1) {
            return null;
        }
        return table[vnIndex][vtIndex];
    }

    /**
     * ��ȡ�ķ���ʼ��
     */
    public String getGrammarStart() {
        return vnList.get(0);
    }

    /**
     * ����һ�����ս�����ս����ȡ����ʽ
     */
    public String getProduction(String vn,String vt) {
        return get(vn,vt);
    }

    /**
     * ����﷨
     */
    public boolean check(String str) {
        // ʵ����׺ʽ���﷨����߼�
        printTitle(str);
        // ��ʼʱ # �� �ķ���ʼ��ѹ����ջ
        Deque<String> stack = new LinkedList<>();
        stack.push("#");
        stack.push(getGrammarStart());
        // ���봮����һ��λ��
        int i = 0;
        int errorPosition = i - 1;
        // ���봮��ǰָ����ַ�
        String cur = String.valueOf(str.charAt(i++));
        // ջ������
        String top;
        do{
            // ��ӡ
            print(stack,str,i,cur);
            top = stack.pop();
            if(top.equals("#") && cur.equals("#"))
                break;
            // ջ������ �� Vt���ж�ջ�������봮��ǰ�ַ��Ƿ����
            if(containsVt(top)) {
                // x == a != "#"�����ַ���ָ���ƶ�
                if(top.equalsIgnoreCase(cur)) {
                    if(!"#".equals(cur)) {
                        cur = "#";
                        Platform.runLater(() -> {
                            // �������ʽ
                            outputArea.appendText("\n");
                        });
                        if(i < str.length()) {
                            cur = String.valueOf(str.charAt(i));
                            i++;
                            errorPosition = i - 1;

                        }
                    }
                }
                else {
                    return false;
                }
            }
            /*���봮��ǰ�ַ���Ϊ # */
            else {
                // ����ջ�����
                String t = getProduction(top,cur);
                // �Ҳ�����ʶ��ʧ��
                if(t == null) {
                    // ʶ��ʧ��
                    InfixCalculator.setErrorPosition(errorPosition);
                    return false;
                }
                Platform.runLater(() -> {
                    // �������ʽ
                    outputArea.appendText(padWhitespaceRight(t,8) + "\n");
                });
                // ��ȡ����ʽ���Ҳ�
                String sentence = t.split("��")[1];
                // ������ʽ���Ҳ�����ѹջ
                int end = sentence.length();
                for(int start = sentence.length()-1; start >= 0; start--) {
                    if(sentence.charAt(start) == '\'') {
                        continue;
                    } else {
                        String substring = sentence.substring(start, end);
                        // �Ų�ѹջ
                        if(!"��".equals(substring)) {
                            stack.push(substring);
                            end = start;
                        }
                    }
                }
            }
        } while(!"#".equals(top));
        return true;
    }

    private void printTitle(String str) {
        Platform.runLater(() -> {
            outputArea.appendText("\n\n" + str + "�������̣�\n\n");
            int len = str.length() + 3;
            String[] titles = new String[] {"����ջ","��ǰ�������\t","���봮","���ò���ʽ"};
            for(String s:titles) {
                outputArea.appendText(padWhitespaceRight(s,len));
            }
            outputArea.appendText("\n");
        });
    }

    private void print(Deque<String> stack, String str, int i, String cur) {
        int len = str.length() + 10;
        StringBuilder sb = new StringBuilder();
        // ƴ��ջ�ڵķ���
        Iterator<String> iterator = stack.descendingIterator();
        while(iterator.hasNext()) {
            sb.append(iterator.next());
        }
        Platform.runLater(() -> {
            // ���ջ
            outputArea.appendText(padWhitespaceRight(sb.toString(),len));

            // �����ǰ�������
            outputArea.appendText(padWhitespaceLeft(cur,len) + "\t");

            // ������봮
            // ����һ�� StringBuilder �������ڹ�������ַ���
            StringBuilder outputBuilder = new StringBuilder();
            // �ж��Ƿ������봮���ַ���Ҫ���
            if (i < str.length()) {
                // ��ȡ��Ҫ������ַ����������ж��봦��
                String substring = padWhitespaceRight(str.substring(i - 1), len);
                // ��Ӷ������ַ���
                outputBuilder.append(substring);
            } else {
                // ��Ӷ���� "#" �ַ���
                outputBuilder.append(padWhitespaceRight("#", len));
            }
            // �������õ��ַ���׷�ӵ� outputArea ��
            outputArea.appendText(" " + outputBuilder.toString());
        });
    }

    /**
     * �Ҷ���
     */
    public static String padWhitespaceLeft(String s, int len) {
        return String.format("%1$" + len + "s", s);
    }

    /**
     * �����
     */
    public static String padWhitespaceRight(String s, int len) {
        return String.format("%1$-" + len + "s", s);
    }
//    public static void main(String[] args) {
//        // ����������ʹ�� vnMap��vnList��productionMap��vtMap��vtList
//        System.out.println("���ս����" + vnList);
//        System.out.println("������ս����vnList�е�λ�ã�" + vnMap);
//        System.out.println("������ս���Ĳ���ʽ��" + productionMap);
//        System.out.println("�ս����" + vtList);
//        System.out.println("�����ս����vtList�е�λ�ã�" + vtMap);
//    }
}