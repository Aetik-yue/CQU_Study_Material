/* 输入打印图形的行数，然后用空格和星号打印出指定的图案。
输入
输入行数。
输出格式:
图形最后一行顶格,详细图案见输出
输入样例:
在这里给出一组输入。例如：
2
输出样例:
在这里给出相应的输出。例如：
*  *
****
输入样例:
在这里给出一组输入。例如：
5
输出样例:
在这里给出相应的输出。例如：
*        *
**      **
***    ***
****  ****
********** */
#include <iostream>
#include <vector>
using namespace std;   
int main(){
    int n;  //n为行数
    cin>>n;
    vector<vector<string>> arr(n, vector<string>(n*2)); // 使用vector定义二维数组
    for(int i=0;i<n;i++){  //行
        for(int j=0;j<n*2;j++){ //列
            if(j<=i||j>=n*2-i-1){
                arr[i][j]="*";  //星号
            }
            else{
                arr[i][j]=" ";  //空格
            }
        }
    }
    for(int i=0;i<n;i++){
        for(int j=0;j<n*2;j++){
            cout<<arr[i][j];
        }
        if(i==n-1) break;
        cout<<endl;
    }
    return 0;
}