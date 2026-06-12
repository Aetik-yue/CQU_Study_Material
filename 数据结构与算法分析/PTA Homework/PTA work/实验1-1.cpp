#include<iostream>
using namespace std;
int main(){
    int n;
    while(1){
        cin>>n;
        int m=0;
        int i=2;
        int num=i;
        if(m==0&n==0) break;
        else{
            for(int i=2;i<=n;i++){
                num=(num+m)%i;
                cout<<num+1<<endl;
            }
    }
}
    return 0;
}