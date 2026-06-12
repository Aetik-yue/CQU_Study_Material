/*  
7-1 模拟队列
分数 10
作者 hyt
单位 郑州航空工业管理学院
实现一个队列，队列初始为空，支持四种操作：

1.push x – 向队尾插入一个数 x；
2.pop – 从队头弹出一个数；
3.empty – 判断队列是否为空；
4.query – 查询队头元素。
现在要对队列进行 M 个操作，其中的每个操作 3 和操作 4 都要输出相应的结果。

输入格式:
第一行包含整数 M，表示操作次数。

接下来 M 行，每行包含一个操作命令，操作命令为 push x，pop，empty，query 中的一种。

输出格式:
对于每个 empty 和 query 操作都要输出一个查询结果，每个结果占一行。

其中，empty 操作的查询结果为 YES 或 NO，query 操作的查询结果为一个整数，表示队头元素的值。

数据范围：
1≤M≤100000,
1≤x≤10 
9
 ,*/

#include <iostream>
#include <queue>
#include <string>

using namespace std;

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);

    int M;
    cin >> M;
    queue<int> q;

    while (M--) {
        string op;
        cin >> op;

        if (op == "push") {
            int x;
            cin >> x;
            q.push(x);
        } else if (op == "pop") {
            q.pop();
        } else if (op == "empty") {
            cout << (q.empty() ? "YES" : "NO") << '\n';
        } else if (op == "query") {
            cout << q.front() << '\n';
        }
    }

    return 0;
}