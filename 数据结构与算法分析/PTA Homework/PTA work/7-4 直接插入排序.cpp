#include<iostream>
#include<vector>
using namespace std;
int main(){
    int n,i,j,k,c;
    while(cin>>n){
        int a[100];
        for(i=0;i<n;i++)
        cin>>a[i];
        for(i=1;i<n;i++){
            c=a[i];
            j=i-1;
            while(j>=0&&a[j]>c){
                a[j+1]=a[j];
                j--;
            }
            a[j+1]=c;
        for(k=0;k<n;k++){
            if(k>0)
            cout<<" ";
            cout<<a[k];
            }
        cout<<endl;
        }
    }
    return 0;
}