#include<iostream>
using namespace std;
int main(){
    const int M=1000000;
    int st_a[M];
    int st_t=-1;
    int a[M];
    int m[M+1];
    int n,i;
    cin>>n;
    for(i=0;i<n;i++){
        cin>>a[i];
    }
    m[n]=0;
    for(int j=n-1;j>=0;j--){
        m[j]=(a[j]>m[j+1]?a[j]:m[j+1]);
    }
    for(int i=0;i<n;i++){
        st_t++;
        st_a[st_t]=a[i];
        while(st_t!=-1&&st_a[st_t]>m[i+1]){
            cout<<st_a[st_t]<<"";
            st_t--;
        }
    }
}