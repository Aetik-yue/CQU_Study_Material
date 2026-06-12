/*7-3 阶乘的递归实现
分数 10
作者 陈越
单位 浙江大学
请编写程序，求给定非负整数 n 的阶乘。建议用递归方式计算。

输入格式：
输入在一行中给出一个非负整数 n（≤10）。

输出格式：
在一行中输出 n!。*/
#include <iostream>
#include <vector>
using namespace std;
int jiechen(int x){
    if(x==0){
        x=1;
    }
    else{
        x=x*jiechen(int (x-1));
    }
    return x;
}
int main(){
    int n;
    cin>>n;
    cout<<jiechen(n);
    return 0;
}