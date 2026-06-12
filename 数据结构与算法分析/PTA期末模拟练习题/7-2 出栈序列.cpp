// 堆栈是一种经典的后进先出的线性结构，相关的操作主要有“入栈”（在堆栈顶插入一个元素）和“出栈”（将栈顶元素返回并从堆栈中删除）。
// 我们用P表示入栈操作，用O表示出栈操作。给定一个入栈序列和一个结果序列，假如入栈过程中允许出栈，请判断结果序列是否是一个合法的出栈序列。
// 例如入栈序列为1,2,3,4,5，则3,4,5,2,1是一个合法的出栈序列，对应的操作序列为PPPOPOPOOO，
// 而3,4,5,1,2不是一个合法的出栈序列，因为子序列3,4,5可以经过PPPOPOPO操作序列得到，但是此时栈顶元素为2，无法与结果序列剩余子序列匹配，则判断结果不合法，
// 此时堆栈里面剩余序列从栈底到栈顶依次为2,1。

// 输入格式:
// 输入为两行，第一行为入栈序列，第二行为结果序列，两个序列都是长度不超过10仅有0-9组成的字符串。

// 输出格式:
// 输出为两行，如果结果序列是合法的出栈序列，则第一行输出“right”，第二行输出入栈出栈操作的顺序序列，否则，第一行输出“wrong”，
// 第二行按照从底到顶的顺序输出无法继续进行入栈出栈操作时堆栈里面的剩余元素。

// 输入样例1:
// 12345
// 34521
// 输出样例1:
// right
// PPPOPOPOOO
// 输入样例2:
// 12345
// 34512
// 输出样例2:
// wrong
// 12

#include <iostream>
#include <stack>
#include <string>
#include <vector>
using namespace std;

int main()
{
    string pushSeq, popSeq;
    cin >> pushSeq >> popSeq;
    
    stack<char> s;  // 用栈模拟过程
    string operations = "";  // 记录操作序列
    int pushIndex = 0;  // 入栈序列指针
    int popIndex = 0;   // 出栈序列指针
    
    // 模拟整个过程
    while (popIndex < popSeq.length()) {
        // 如果栈为空或栈顶不是目标元素，则继续入栈
        if (s.empty() || s.top() != popSeq[popIndex]) {
            if (pushIndex < pushSeq.length()) {
                s.push(pushSeq[pushIndex]);
                operations += "P";
                pushIndex++;
            } else {
                // 已经没有元素可入栈，但还需要出栈，说明不合法
                break;
            }
        }
        
        // 如果栈顶是要出栈的元素，则执行出栈
        if (!s.empty() && s.top() == popSeq[popIndex]) {
            s.pop();
            operations += "O";
            popIndex++;
        } else if (pushIndex >= pushSeq.length()) {
            // 没有更多元素入栈，但当前栈顶不是目标元素
            break;
        }
    }
    
    // 判断是否完全匹配
    if (popIndex == popSeq.length()) {
        // 所有出栈序列都匹配成功
        cout << "right" << endl;
        cout << operations << endl;
    } else {
        // 匹配失败
        cout << "wrong" << endl;
        
        // 输出栈中剩余元素（从底到顶）
        if (!s.empty()) {
            vector<char> remaining;
            while (!s.empty()) {
                remaining.push_back(s.top());
                s.pop();
            }
            // 从底到顶输出，所以要逆序
            for (int i = remaining.size() - 1; i >= 0; i--) {
                cout << remaining[i];
            }
            if (remaining.empty()) cout << "";
        } else {
            cout << "";
        }
        cout << endl;
    }
    
    return 0;
}