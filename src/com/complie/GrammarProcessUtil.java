package com.complie;

import java.util.*;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

public class GrammarProcessUtil {
    /**
     * 文法
     */
    private static final String[] grammarRules = {
            "E→TE'",
            "E'→+TE'|-TE'|ε",
            "T→FT'",
            "T'→*FT'|/FT'|ε",
            "F→(E)|i",
            "i→1i|2i|3i|4i|5i|6i|7i|8i|9i|i0|ε"
    };
    /**
     * 产生式的分隔符
     */
    private static final String SEPARATOR = "→";
    /**
     * 保存非终结符在 vnList中的位置
     */
    private static final Map<String,Integer> vnMap = new HashMap<>();

    /**
     * 保存终结符在 vtList 中的位置
     */
    private static final Map<String,Integer> vtMap = new HashMap<>();

    /**
     * 保存非终结符的产生式
     */
    private static final Map<String,List<String>> productionMap = new HashMap<>();

    /**
     * 非终结符集
     */
    private static final List<String> vnList = new ArrayList<>();

    /**
     * 终结符集
     */
    private static final List<String> vtList = new ArrayList<>();

    /**
     * 分析表
     */
    private static String[][] table = null;

    /**
     * 候选式的分隔符
     */
    private static final String CANDIDATE_SEPARATOR = "|";

    /**
     * 空串
     */
    private static final String BLANK_STRING = "ε";

    /**
     * FIRST集
     */
    private static final Map<String, Set<String>> first = new HashMap<>();

    /**
     * FOLLOW集
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
        // 获取非终结符集
        for(String vn:vnList) {
            // 获取非终结符产生式的右部的候选式
            List<String> vnRight = productionMap.get(vn);
            for(String right:vnRight) {
                // 将一个个候选式拆分成字符（处理类似 E → S'a的情况）
                List<String> characters = getCharacter(right);
                for(String c:characters) {
                    // 不是非终结符 且 未曾加入到过 终结符集
                    if(!vnMap.containsKey(c) && !vtMap.containsKey(c)
                            && !CANDIDATE_SEPARATOR.equals(c)) {
                        vtMap.put(c,vtList.size());
                        vtList.add(c);
                    }
                }
            }
        }
        // 打印终结符集 与 非终结符集

//        Platform.runLater(() -> {
//            outputArea.appendText("\n终结符：" + vtList);
//            outputArea.appendText("\n非终结符：" + vnList);
//        });
        System.out.println("终结符：" + vtList);
        System.out.println("非终结符：" + vnList);
        // 获取FIRST集
        initFirst();
        // 打印FIRST集
        printFirst();
        // 获取FOLLOW集
        initFollow();
        // 打印FOLLOW集
        printFollow();
        // 获取分析表
        getTable();
        // 打印分析表
        printTable();

    }

    /**
     * 将产生式按照候选式的分隔符进行分割
     * 例：str:"E'a|ε" => ["E'a","ε"]
     *    str:“E'a" => ["E'a"]
     */
    private static List<String> splitBySeparator(String str) {
        List<String> ans = new ArrayList<>();
        if(!str.contains(CANDIDATE_SEPARATOR)) {
            // 不包含分隔符，不可分割
            ans.add(str);
            return ans;
        } else {
            // 包含分隔符，进行分割
            String[] split = str.split("\\|");
            Collections.addAll(ans,split);
            return ans;
        }
    }

    /**
     * 将字符串分割成一个个字符
     * 注：主要是处理带单引号的字符
     * 例：str:"E'aT" => ["E'","a","T"]
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
     * 判断字符串str是否完全包含 regex，返回下标
     * 例：str:"aEb",regex="E" => 1
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
            throw new UnsupportedOperationException("int contains(String str,String regex):regex长度只支持1或2");
        }
        return -1;
    }

    /*判断给定的符号 vt 是否为终结符*/
    public boolean containsVt(String vt) {
        if("#".equals(vt))
            return false;
        return vtMap.containsKey(vt);
    }

    private static void printFirst() {
        System.out.println("\nFIRST集合：");
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
        System.out.println("\nFOLLOW集合：");
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
        System.out.print("\n预测分析表：\n");
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
     * 初始化FIRST集
     */
    private static void initFirst() {
        for(String vn:vnList) {
            getFirst(vn);
        }
    }

    /**
     * 获取FIRSTS集
     */
    private static Set<String> getFirst(String vn) {
        if(first.containsKey(vn)) {
            return first.get(vn);
        }
        Set<String> set = new HashSet<>();
        // 将当前非终结符标记为已计算
        first.put(vn, set);

        // 遍历产生式的右部的候选式
        List<String> vnRight = productionMap.get(vn);
        for(String right:vnRight) {
            int index = 0;
            // 候选式的第一个字符
            String firstCharacter = String.valueOf(right.charAt(index++));
            if (vtMap.containsKey(firstCharacter)) {
                // 满足 E→a...,a加入FIRST集
                set.add(firstCharacter);
            } else if (vnMap.containsKey(firstCharacter)) {
                // 满足 E→S...，S∈vt,FIRST(S)\{ε}加入FIRST(E)
                Set<String> s = getFirst(firstCharacter);
                // FIRST(S)是否包含空串
                boolean hasBlankString = false;
                for(String str:s) {
                    if(!BLANK_STRING.equals(str)) {
                        set.add(str);
                    } else {
                        hasBlankString = true;
                    }
                }
                // 记录该产生式空串个数
                int blankStringNumber = hasBlankString ? 1 : 0;
                // E→S1S2S3... FIRST(S1)包含空串,则检查下一个字符
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
                // E→S1S2...Sn中的每个字符都可以推导出空串，空串加入FIRST集
                if(blankStringNumber == right.length()) {
                    set.add(BLANK_STRING);
                }
            }
        }
//        first.put(vn,set);
        return set;
    }

    /**
     * 初始化FOLLOW集
     */
    private static void initFollow() {
        for(String vn:vnList) {
            getFollow(vn);
        }
    }

    /**
     * 获取FOLLOW集
     */
    private static Set<String> getFollow(String vn) {
        // FOLLOW(vn)已经存在，直接返回
        if(follow.containsKey(vn) && !follow.get(vn).isEmpty()) return follow.get(vn);
        Set<String> set = new HashSet<>();
        // 文法开始符，#加入FOLLOW集
        if(vnList.get(0).equals(vn)) {
            set.add("#");
        }
        // 查看vn在哪条产生式的右部出现过
        for(String left:vnList) {
            // 自己的FOLLOW集不用管
            if(left.equals(vn)) {
                continue;
            }
            // 获取vn对应的产生式
            List<String> vnRight = productionMap.get(left);
            for(String item:vnRight) {
                int index = contains(item,vn);
                // 在产生式右部出现过
                if(index != -1) {
                    // 这样做是为了识别 E'
                    if(index + vn.length() == item.length()) {
                        // left→...S S出现在产生式最右部，使用规则三，将FOLLOW(left)加入FOLLOW(S)
                        Set<String> itemFollow = getFollow(left);
                        set.addAll(itemFollow);
                    } else {
                        // 否则使用规则二
                        // 获取next，这里要处理E'
                        String next = null;
                        int e1 = index + vn.length();
                        if(e1 + 1< item.length() && item.charAt(e1+1) == '\'') {
                            // SE',下一个字符带单引号
                            next = item.substring(e1,e1+2);
                        } else {
                            // SE,下一个字符不带单引号
                            next = item.substring(e1,e1+1);
                        }
                        if(vtMap.containsKey(next)) {
                            // 如果next是终结符，则FIRST(next) = {next}，直接加入
                            set.add(next);
                        } else {
                            // FIRST(next)\{ε}加入FOLLOW(vn)
                            Set<String> first = getFirst(next);
                            boolean hasBlankString = first.contains(BLANK_STRING);
                            for(String s:first) {
                                if(!BLANK_STRING.equals(s)) {
                                    set.add(s);
                                }
                            }
                            // FIRST(next)含有ε，使用规则三
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
     * 将vn与vt对应的产生式存入分析表
     */
    private static void fillTable(String vn,String vt,String production) {
        if(table == null) {
            throw new IllegalStateException("调用fillTable前请确保table已初始化！");
        }
        // 获取vn与vt分别在vnList与vtList的下标
        int vnIndex = vnMap.getOrDefault(vn,-1);
        int vtIndex = -1;
        if("#".equals(vt)) {
            vtIndex = table[0].length - 1;
        } else {
            vtIndex = vtMap.getOrDefault(vt, -1);
        }
        if(vnIndex == -1 || vtIndex == -1) {
            throw new IllegalArgumentException("fillTable(vn="+vn+",vt="+vt+",production="+production+")不合法");
        }
        // 拼接产生式
        String trueProduction = vn+SEPARATOR+production;
        // 存入分析表
        table[vnIndex][vtIndex] = trueProduction;
    }

    /**
     * 返回vn对应的产生式，且产生式中包含vt
     * 例：E→iESS'|a,vn:E,vt:i => iESS'
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
     * 获取分析表
     * 前提：FIRST集与FOLLOW集已构造完成
     */
    private static String[][] getTable() {
        if(table != null)
            return table;
        table = new String[vnList.size()][vtList.size()+1];
        // 遍历非终结符
        for(String vn:vnList) {
            // 获取对应非终结符的FIRST集
            Set<String> vnFirst = first.get(vn);
            // 对于FIRST集的每一个元素，找到对应产生式，加入分析表
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
            // FIRST集包含空串，则FOLLOW集加入
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
     * 根据非终结符与终结符获取分析表中的产生式
     */
    public String get(String vn,String vt) {
        if(table == null) {
            throw new IllegalStateException("调用fillTable前请确保table已初始化！");
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
     * 获取文法开始符
     */
    public String getGrammarStart() {
        return vnList.get(0);
    }

    /**
     * 根据一个非终结符与终结符获取产生式
     */
    public String getProduction(String vn,String vt) {
        return get(vn,vt);
    }

    /**
     * 检查语法
     */
    public boolean check(String str) {
        // 实现中缀式的语法检查逻辑
        printTitle(str);
        // 初始时 # 与 文法开始符压符号栈
        Deque<String> stack = new LinkedList<>();
        stack.push("#");
        stack.push(getGrammarStart());
        // 输入串的下一个位置
        int i = 0;
        int errorPosition = i - 1;
        // 输入串当前指向的字符
        String cur = String.valueOf(str.charAt(i++));
        // 栈顶符号
        String top;
        do{
            // 打印
            print(stack,str,i,cur);
            top = stack.pop();
            if(top.equals("#") && cur.equals("#"))
                break;
            // 栈顶符号 ∈ Vt，判断栈顶与输入串当前字符是否相等
            if(containsVt(top)) {
                // x == a != "#"，则字符串指针移动
                if(top.equalsIgnoreCase(cur)) {
                    if(!"#".equals(cur)) {
                        cur = "#";
                        Platform.runLater(() -> {
                            // 输出产生式
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
            /*输入串当前字符不为 # */
            else {
                // 根据栈顶查表
                String t = getProduction(top,cur);
                // 找不到，识别失败
                if(t == null) {
                    // 识别失败
                    InfixCalculator.setErrorPosition(errorPosition);
                    return false;
                }
                Platform.runLater(() -> {
                    // 输出产生式
                    outputArea.appendText(padWhitespaceRight(t,8) + "\n");
                });
                // 获取产生式的右部
                String sentence = t.split("→")[1];
                // 将产生式的右部逆序压栈
                int end = sentence.length();
                for(int start = sentence.length()-1; start >= 0; start--) {
                    if(sentence.charAt(start) == '\'') {
                        continue;
                    } else {
                        String substring = sentence.substring(start, end);
                        // ε不压栈
                        if(!"ε".equals(substring)) {
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
            outputArea.appendText("\n\n" + str + "解析流程：\n\n");
            int len = str.length() + 3;
            String[] titles = new String[] {"符号栈","当前输入符号\t","输入串","所用产生式"};
            for(String s:titles) {
                outputArea.appendText(padWhitespaceRight(s,len));
            }
            outputArea.appendText("\n");
        });
    }

    private void print(Deque<String> stack, String str, int i, String cur) {
        int len = str.length() + 10;
        StringBuilder sb = new StringBuilder();
        // 拼接栈内的符号
        Iterator<String> iterator = stack.descendingIterator();
        while(iterator.hasNext()) {
            sb.append(iterator.next());
        }
        Platform.runLater(() -> {
            // 输出栈
            outputArea.appendText(padWhitespaceRight(sb.toString(),len));

            // 输出当前输入符号
            outputArea.appendText(padWhitespaceLeft(cur,len) + "\t");

            // 输出输入串
            // 创建一个 StringBuilder 对象用于构建输出字符串
            StringBuilder outputBuilder = new StringBuilder();
            // 判断是否还有输入串的字符需要输出
            if (i < str.length()) {
                // 获取需要输出的字符串，并进行对齐处理
                String substring = padWhitespaceRight(str.substring(i - 1), len);
                // 添加对齐后的字符串
                outputBuilder.append(substring);
            } else {
                // 添加对齐的 "#" 字符串
                outputBuilder.append(padWhitespaceRight("#", len));
            }
            // 将构建好的字符串追加到 outputArea 中
            outputArea.appendText(" " + outputBuilder.toString());
        });
    }

    /**
     * 右对齐
     */
    public static String padWhitespaceLeft(String s, int len) {
        return String.format("%1$" + len + "s", s);
    }

    /**
     * 左对齐
     */
    public static String padWhitespaceRight(String s, int len) {
        return String.format("%1$-" + len + "s", s);
    }
//    public static void main(String[] args) {
//        // 可以在这里使用 vnMap、vnList、productionMap、vtMap、vtList
//        System.out.println("非终结符：" + vnList);
//        System.out.println("保存非终结符在vnList中的位置：" + vnMap);
//        System.out.println("保存非终结符的产生式：" + productionMap);
//        System.out.println("终结符：" + vtList);
//        System.out.println("保存终结符在vtList中的位置：" + vtMap);
//    }
}