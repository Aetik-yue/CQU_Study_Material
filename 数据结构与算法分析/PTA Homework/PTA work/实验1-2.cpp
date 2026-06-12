#include<iostream>
#include<stack>
#include<vector>
using namespace std;
int main(){
    int T;
    cin>>T;
    while(T--){
        int n;
        cin>>n;
        stack<int> station;
        vector<int> a(n);
        for(int i=0;i<n;i++){
            cin>>a[i];
        }
        int up_num=1;
        int no_train=0;
        for(int i=0;i<n;i++){
            if(a[i]==up_num){
                up_num=up_num+1;
                no_train=no_train+1;
            }

            else if(!station.empty() && station.top()==up_num){
                while(!station.empty() && station.top()==up_num){
                    station.pop();
                    up_num++;
                }
                if(a[i]==up_num){
                    up_num=up_num+1;
                    no_train=no_train+1;
                }
                else{
                    station.push(a[i]);
                }
            }
            else{
                station.push(a[i]);
            }
        }
        while(!station.empty() && station.top()==up_num){
            station.pop();
            up_num++;
        }
        if(up_num==n+1){
            cout<<"Yes"<<endl;
            cout<<no_train<<endl;
        }
        else{
            cout<<"No"<<endl;
        }
    }
    return 0;
}