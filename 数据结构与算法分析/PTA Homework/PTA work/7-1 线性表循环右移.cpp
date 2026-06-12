/*  给定顺序表 A=(a 
1
​
 ,a 
2
​
 ,⋯,a 
n
​
 )，请设计一个时间和空间上尽可能高效的算法将该线性表循环右移指定的 m 位。例如，(1,2,5,7,3,4,6,8) 循环右移 3 位（m=3) 后的结果是 (4,6,8,1,2,5,7,3)。

输入格式：
第一行输入 n ( 1≤n≤10 
5
 )、m（m≥0）；第二行输入 n 个整数。所有数字在 int 型整数范围内，同行数字间以空格分隔。

输出格式：
输出循环右移 m 位以后的整数序列。数字间以 1 个空格分隔，行首尾不得有多余空格。*/

#include <iostream>
#include <vector>
using namespace std;
int main(){
    int n,m;
    cin>>n>>m;
    vector<int> vec(n);
    for(int i=0;i<n;i++){
        cin>>vec[i];
    }
    if(m>=n) m=m%n;
    vector<int> vec2(n);
    for(int i=0;i<n;i++){
        vec2[(i+m)%n]=vec[i];
    }
    for(int i=0;i<n;i++){
        if(i!=0) cout<<" ";
        cout<<vec2[i];
    }
    return 0;
}
