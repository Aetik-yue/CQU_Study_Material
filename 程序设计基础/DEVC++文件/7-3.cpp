#include <iostream>
#include <iomanip>
using namespace std;

int main() {
	int a1, b1, a2, b2;
	float c1, c2;
	char d1, d2;
	cin >> a1 >> d1 >> b1 >> a2 >> d2 >> b2;
	if (d1 == '/' && d2 == '/') {
		cin.ignore(); // Ignore the '/' character
	}
	c1 = float(a1) / float(b1);
	c2 = float(a2) / float(b2);
	if (c1 < c2) {
		cout << a1 << "/" << b1 << " < " << a2 << "/" << b2 << endl;
	} else if (c1 > c2) {
		cout << a1 << "/" << b1 << " > " << a2 << "/" << b2 << endl;
	} else if (c1 = c2) {
		cout << a1 << "/" << b1 << " = " << a2 << "/" << b2 << endl;
	}
	return 0;
}