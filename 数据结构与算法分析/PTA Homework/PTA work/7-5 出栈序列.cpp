#include <iostream>
#include <stack>
#include <string>

using namespace std;

int main() {
    string in, out;
    cin >> in >> out;

    stack<char> s;
    string ops; // 存储操作序列（P和O）
    int i = 0, j = 0; // i遍历入栈序列，j遍历结果序列

    while (i < in.size() || j < out.size()) {
        // 若栈不为空且栈顶元素与当前出栈元素匹配，则出栈
        if (!s.empty() && s.top() == out[j]) {
            s.pop();
            ops += 'O';
            j++;
        } else {
            // 若还有入栈元素未处理，则入栈
            if (i < in.size()) {
                s.push(in[i]);
                ops += 'P';
                i++;
            } else {
                // 无入栈元素且栈顶不匹配，无法继续
                break;
            }
        }
    }

    // 检查是否所有元素都匹配
    if (j == out.size() && s.empty()) {
        cout << "right" << endl;
        cout << ops << endl;
    } else {
        cout << "wrong" << endl;
        // 提取栈中剩余元素（从底到顶）
        string remaining;
        stack<char> temp;
        // 先将栈中元素弹出到临时栈（反转顺序）
        while (!s.empty()) {
            temp.push(s.top());
            s.pop();
        }
        // 从临时栈弹出，得到从底到顶的顺序
        while (!temp.empty()) {
            remaining += temp.top();
            temp.pop();
        }
        cout << remaining << endl;
    }

    return 0;
}