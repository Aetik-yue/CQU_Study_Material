#include <bits/stdc++.h>
using namespace std;

int prec(char op) {
    if (op == '+' || op == '-') return 1;
    if (op == '*' || op == '/') return 2;
    return 0;
}

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);

    string s;
    if (!getline(cin, s)) return 0;

    vector<string> out;
    stack<char> st;

    for (size_t i = 0; i < s.size(); ) {
        char c = s[i];
        if (isdigit((unsigned char)c)) {
            // parse multi-digit number
            string num;
            while (i < s.size() && isdigit((unsigned char)s[i])) {
                num.push_back(s[i]);
                ++i;
            }
            out.push_back(num);
            continue;
        }
        if (c == '(') {
            st.push(c);
        } else if (c == ')') {
            while (!st.empty() && st.top() != '(') {
                string t(1, st.top()); st.pop();
                out.push_back(t);
            }
            if (!st.empty() && st.top() == '(') st.pop();
        } else if (c == '+' || c == '-' || c == '*' || c == '/') {
            while (!st.empty() && st.top() != '(' &&
                   (prec(st.top()) >= prec(c))) {
                string t(1, st.top()); st.pop();
                out.push_back(t);
            }
            st.push(c);
        }
        ++i;
    }

    while (!st.empty()) {
        if (st.top() != '(' && st.top() != ')') {
            string t(1, st.top()); out.push_back(t);
        }
        st.pop();
    }

    // print with single spaces, no trailing space
    for (size_t i = 0; i < out.size(); ++i) {
        if (i) cout << ' ';
        cout << out[i];
    }
    cout << '\n';
    return 0;
}
