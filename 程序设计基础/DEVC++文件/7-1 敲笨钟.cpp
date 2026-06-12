#include <iostream>
using namespace std;

int main() {
	int n;
	cin >> n;
	getchar();
	string s;
	while (n--) {
		getline(cin, s);
		if (s.find("ong,") == string::npos || s.find("ong.") == string::npos) {
			cout << "Skipped" << endl;
			continue;
		}
		int pos, k = 0;
		for (pos = s.length() - 1; pos >= 0; pos--) {
			if (s[pos] != ' ')
				continue;
			k++;
			if (k == 3)
				break;
		}
		s.replace(pos + 1, s.length() - pos - 1, "qiao ben zhong.");
		cout << s << endl;
	}
	return 0;
}



/*#include <iostream>
#include <string>
using namespace std;
string gai(string str);

int main() {
	int N;
	cin >> N;
	bool found = false;
	//先用字符串数组，再用for循环对每句诗进行输入
	//再挨个对每句进行检索，逗号“，”和句号“。”前三个字符是“ong”即可

	while (N--) {
		string str;
		cin >> str;
		for (int j = 0; j < str.size(); j++) {
			int x = str.size() - 1;
			if (str[j] = ',') {
				if (str[j - 1] == 'g' && str[j - 2] == 'n' && str[j - 3] == 'o' && str[x - 1] == 'g' && str[x - 2] == 'n'
				        && str[x - 3] == 'o'
				   )
					found = true;

			}
		}
		if (found)
			cout << gai(str) << endl;
		if (!found)
			cout << "Skipped" << endl;
	}
	return 0;
}

string gai(string s) {
	int a = s.size() - 1;
	for (int i = 0; i < s.size(); i++) {
		if (s[i] == ',') {
			int b = i + 2;
			int num = 0;
			for (int j = s.size(); j > b; j--) {
				if (s[j] = ' ')
					num++;
				if (num == 3) {
					int z = j;
					string arr = " qiao ben zhong.";
					for (int p = z + 1; p < s.size(); p++) {
						if (p > z + 15) {
							s.erase(p, s.size() - p - 1);
							s = s + arr;
							break;
						}

					}
				}
			}
		}
	}
	return s;
}