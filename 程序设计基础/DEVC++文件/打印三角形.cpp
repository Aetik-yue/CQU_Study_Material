#include <iostream>
#include <iomanip>
#include <string>
using namespace std;

int main() {
	int N;
	string str ;
	string C;
	cin >> N >> C;
	for (int i = 1; i < N + 1; i++) {
		int x = 1;
		int y = i;
		int j = 2 * y;
		str = "";
		while (x != j) {
			str = str + C;
			x++;
		}
		if (i == N) {
			cout << setw((N - 1) + i)  << str;
			break;
		}
		cout <<  setw((N - 1) + i) << str << endl;
	}
	return 0;
}