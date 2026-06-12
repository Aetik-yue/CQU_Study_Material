#include<iostream>
#include<stack>
using namespace std;
int main(){
    int N,i;
    cin>>N;
    stack<int> s1;
    stack<int> s2;
    for(int i=0;i<N;i++){
        char op;
        cin>>op;
        if(op=='I'){
            int element;
            cin>>element;
            s1.push(element);
        }
        else{
            int t=0;
            if(s2.empty()){
                if(s1.empty()){
                    cout<<"ERROR"<<endl;
                    continue;
                }
                while(!s1.empty()){
                    s2.push(s1.top());
                    s1.pop();
                    t=t+2;
                }
            }
            int o=s2.top();
            s2.pop();
            t=t+1;
            cout<<o<<" "<<t<<endl;
        }
    }
    return 0;
}