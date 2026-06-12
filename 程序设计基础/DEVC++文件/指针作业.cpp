#include <iostream>
#include <string>
using namespace std;

int main() {
	int T;
	cin >> T;
	while (T != 0) {
		string s;
		string str;
		char t;
		str = "";
		cin >> s >> t;
		for (int i = 0; i < s.size(); i++) {
			if (s[i] == t) {
				str = str;
				continue;
			}
			str = str + s[i];

		}
		if (str == "") {
			cout << "NULL";
		}
		cout << str;
		if (T == 1)
			break;
		cout << "" << endl;
		T--;
	}
	return 0;
}