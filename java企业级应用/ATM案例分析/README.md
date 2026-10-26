# ATM 案例分析（课堂作业）

《Java 企业级应用》课堂作业：按自己的思路对 ATM 案例做需求分析与面向对象设计，
并给出完整可运行的 Java 实现。最终交付：

- **文档**：`doc/ATM案例分析报告.docx`（同目录附 PDF 版），含需求分析、设计原则
  （用例图 / 类图 / 活动图 / 时序图 / 状态图，共 12 张插图）、代码分析（设计模式、
  优缺点、改进方向）与总结。
- **代码**：`src/com/atm/`，35 个源文件，纯 Java SE（JDK 17+ 特性：sealed、record、
  switch 表达式），无第三方依赖。

## 目录结构

```
ATM案例分析/
├── build.bat / run.bat            # 编译、运行脚本
├── src/com/atm/                   # 源代码（六个包，见报告 3.1 节）
│   ├── ATM.java / AtmApplication.java
│   ├── bank/                      # 银行数据层（账户、卡、流水、单例数据库）
│   ├── policy/                    # 取款限额策略（策略模式）
│   ├── transaction/               # 交易家族（sealed 抽象父类 + 5 个子类 + 工厂）
│   ├── hardware/ + hardware/console/   # 硬件接口与控制台实现
│   └── ui/                        # 泛型菜单组件
├── test/
│   ├── SmokeTest.java             # 冒烟测试（55 项断言，用测试替身替换全部硬件）
│   ├── session-main.txt 等 4 个    # 端到端输入脚本（主流程/吞卡/边界/VIP）
│   └── out-*.txt / smoke-result.txt  # 运行输出记录（报告附录引用）
└── doc/
    ├── ATM案例分析报告.docx / .pdf
    ├── gen_doc.py                 # 报告生成脚本（python-docx；改内容后重跑即可）
    └── diagrams/                  # 12 张 UML 图（.puml 源文件 + .png）
```

## 编译与运行

```bat
build.bat    rem 编译（源码 UTF-8，须 -encoding UTF-8）
run.bat      rem 运行（控制台 ATM）
```

冒烟测试与端到端脚本（详见报告 3.5 与附录 B）：

```bat
javac -encoding UTF-8 -d out-test -cp out test\SmokeTest.java
java -Dstdout.encoding=UTF-8 -cp "out;out-test" SmokeTest
java -Dstdout.encoding=UTF-8 -cp out com.atm.AtmApplication < test\session-main.txt
```

## 演示数据（内存预置，三个账户）

| 卡号 | 账户 | 户名 | 密码 | 可用余额 | 总余额 | 今日已取 | 限额策略 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 62220001 | 1001 | 张伟 | 123456 | 1000.00 | 1200.00 | 0.00 | 普通（单笔2500/日5000） |
| 62220002 | 1002 | 李娜 | 654321 | 8000.00 | 9000.00 | 4000.00 | 普通 |
| 62220003 | 1003 | 王强 | 888888 | 50000.00 | 50000.00 | 0.00 | VIP（单笔2万/日5万） |

钞箱初始 100 张 × 100 元 = 10000 元；密码连续输错 3 次吞卡。

## 重新生成文档

修改正文内容后，重新生成报告与插图：

```bat
java -jar plantuml.jar -tpng -charset UTF-8 doc\diagrams\*.puml   rem 需要 plantuml.jar
python doc\gen_doc.py                                            rem 需要 python-docx
```
