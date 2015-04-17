package com.ramonli.stockforum.noise;

public class Main {

    public static void main(String[] args) throws Exception {
        ForumParser parser = new ForumParser();
        parser.parse("600405", // 动力源
                "000009", // 中国宝安
                "002284", // 亞太股份
                "603766", //隆鑫通用
                "002273", // 水晶光電
                "002121"// 科陸電子
        );
    }
}
