#include <iostream>
#include <string>
#include <algorithm> // 用于reverse函数

using namespace std;

// 函数功能：实现两个大整数的加法
string addStrings(string num1, string num2) {
    string result;
    int i = num1.size() - 1; // 指向num1的最后一个字符（个位）
    int j = num2.size() - 1; // 指向num2的最后一个字符（个位）
    int carry = 0;           // 进位标志
    
    // 从个位开始逐位相加，直到两个数都处理完且没有进位
    while (i >= 0 || j >= 0 || carry > 0) {
        int sum = carry; // 先加上进位
        
        // 累加num1当前位的数字（若存在）
        if (i >= 0) {
            sum += (num1[i] - '0'); // 将字符转换为对应的整数
            i--;
        }
        
        // 累加num2当前位的数字（若存在）
        if (j >= 0) {
            sum += (num2[j] - '0'); // 将字符转换为对应的整数
            j--;
        }
        
        // 计算当前位的结果和新的进位
        result.push_back((sum % 10) + '0'); // 将当前位结果转换为字符存入
        carry = sum / 10;                   // 更新进位
    }
    
    // 由于结果是从个位开始存储的，需要反转得到正确的顺序
    reverse(result.begin(), result.end());
    return result;
}

int main() {
    string A, B;
    // 循环读取输入，直到文件结束（处理多组测试数据）
    while (cin >> A >> B) {
        cout << addStrings(A, B) << endl;
    }
    return 0;
}