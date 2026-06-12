/*7-1 冰雹猜想。
分数 10
作者 赵静静
单位 浙江工贸职业技术学院
冰雹猜想的内容是：任何一个大于1的整数n，按照n为偶数则除等2，n为奇数则乘3后再加1的规则不断变化，最终都可以变化为1。
例如，n等于20，变化过程为：20、10、5、16、8、4、2、1。编写程序，用户输入n，输出变化过程以及变化的次数。*/
#include<iostream>
using namespace std;
int main(){
    int n;
    cin>>n;
    int count=1;
    while(n!=1){
        cout<<n<<" ";
        if(n%2==0){
            n=n/2;
        }
        else{
            n=(n*3)+1;
        }
        
        count+=1;
        }   
    cout<<"1";
    cout<<endl;
    cout<<"count = "<<count;
    return 0;
}
