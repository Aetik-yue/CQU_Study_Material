/*  若一个线性表L采用顺序存储结构存储，其中所有的元素为整数。设计一个算法，求元素值在[x,y]之间的所有元素之和。

输入格式:
三行数据，第一行是顺序表的元素个数，第二行是顺序表的元素，第三行是x和y。

输出格式:
元素值在[x,y]之间的所有元素之和。*/
#include <iostream>
#include <vector>
using namespace std;
int main(){
    vector<int> a;
    int number;
    cin>>number;
    for(int i=0;i<number;i++){
        int temp;
        cin>>temp;
        a.push_back(temp);
    }
    int x,y;
    cin>>x>>y;
    int sum=0;
    for(int i=0;i<number;i++){
        if(a[i]>=x && a[i]<=y){
            sum+=a[i];
        }
    }
    cout<<sum;
    return 0;
}