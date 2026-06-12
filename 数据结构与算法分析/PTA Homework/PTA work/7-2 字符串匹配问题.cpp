/*  7-2 字符串匹配问题(strs)
分数 10
作者 刘昆
单位 中国矿业大学徐海学院
字符串中只含有括号 (),[],<>,{},判断输入的字符串中括号是否匹配。如果括号有互相包含的形式，从内到外必须是<>,(),[],{}，例如。输入: [()] 输出 YES，而输入([])，([)]都应该输出 NO。

输入格式:
第一行为一个整数 n，表示以下有多少个由括号组成的字符串。接下来的 n 行，每行都是一个由括号组成的长度不超过 255 的字符串。

输出格式:
输出有 n 行，每行都是 YES 或 NO。*/

#include<iostream>
#include<string>
#include<stack>
using namespace std;

int cp(char c) {
    if(c=='<'||c=='>') return 1;
    if(c=='('||c==')') return 2;
    if(c=='['||c==']') return 3;
    if(c=='{'||c=='}') return 4;
    return 0;
}

int main(){
   int n,i,j;
   cin>>n;
   string s;
   for(i=0;i<n;i++){
    cin>>s;
    stack<char>a;
    bool d=true;
    for(j=0;j<s.length();j++){
        if(s[j]=='('||s[j]=='['||s[j]=='{'||s[j]=='<'){
            if(a.empty())
                a.push(s[j]);    
            else{
            if(cp(s[j])>cp(a.top())){
            d=false;
            break;
            }
            else
            a.push(s[j]);
            }
        }
        else{
            if(a.empty()){
                d=false;
                break;
            }
            if((s[j]==')'&&a.top()=='(')||(s[j]==']'&&a.top()=='[')||
            (s[j]=='}'&&a.top()=='{')||(s[j]=='>'&&a.top()=='<'))
            a.pop();
        }
    }
    if(a.empty()&&d)
    cout<<"YES"<<endl;
    else
    cout<<"NO"<<endl;
   }
   return 0;
}