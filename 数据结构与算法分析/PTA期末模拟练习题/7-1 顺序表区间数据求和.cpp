// 若一个线性表L采用顺序存储结构存储，其中所有的元素为整数。设计一个算法，求元素值在[x,y]之间的所有元素之和。

// 输入格式:
// 三行数据，第一行是顺序表的元素个数，第二行是顺序表的元素，第三行是x和y。

// 输出格式:
// 元素值在[x,y]之间的所有元素之和。

// 输入样例:
// 10  
// 5 1 9 9 67 12 8 33 6 2  
// 3 10
// 输出样例:
// 37




#include<iostream>
#include<vector>
using namespace std;
int main(){
    int Number;
    cin>>Number;
    vector<int> element(Number);
    int x,y;

    for(int i=0;i<Number;i++){
        cin>>element[i];
    }
    cin>>x>>y;
    int sum=0;
    for(int i=0;i<Number;i++){
        if(element[i]>=x&&element[i]<=y){
            sum =sum+element[i];
        }
    }
    cout<<sum;
    return 0;
}