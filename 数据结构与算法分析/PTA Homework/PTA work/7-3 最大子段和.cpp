/* 给定n个整数（可能为负数）组成的序列a[1],a[2],a[3],…,a[n]，求该序列如a[i]+a[i+1]+…+a[j]的子段和的最大值。当所给的整数均为负数时，定义子段和为0。

要求算法的时间复杂度为O(n)。

输入格式:
输入有两行：

第一行是n值（1<=n<=10000)；

第二行是n个整数。

输出格式:
输出最大子段和。

输入样例:
在这里给出一组输入。例如：

6
-2 11 -4 13 -5 -2
输出样例:
在这里给出相应的输出。例如：

20*/

//卡恩斯算法神了，太巧妙了我靠
//我什么时候能想出来这么巧妙的算法啊！！！
#include<iostream>
#include<vector>
using namespace std;
int main() {
    // 提高C++的I/O效率
    std::ios_base::sync_with_stdio(false);
    std::cin.tie(NULL);

    int n;
    cin >> n;

    std::vector<int> a(n);
    for (int i = 0; i < n; ++i) {
        cin >> a[i];
    }

    int global_max = 0;      // 全局最大子段和，根据题意初始化为0
    int current_max = 0;     // 当前位置的最大子段和

    for (int i = 0; i < n; ++i) {
        // 核心：决定是延续之前的子段，还是从当前元素开始一个新的子段
        current_max = max(a[i], current_max + a[i]);

        // 更新全局最大值
        global_max = max(global_max, current_max);
    }

    cout << global_max <<endl;

    return 0;
}