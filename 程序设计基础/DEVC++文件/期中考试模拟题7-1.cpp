#include <iostream>
#include <string>
#include <algorithm>
using namespace std;

int main() {
	string s;
	cin >> s;
	int x = 0, y = 0, z = 0;
	for (int i = 0; i < s.size(); i++) {
		if (s[i] == 'c' || s[i] == 'C')
			x++;
		if (s[i] == 'q' || s[i] == 'Q')
			y++;
		if (s[i] == 'u' || s[i] == 'U')
			z++;
	}
	while (x > 0 || y > 0 || z > 0) {
		if (x > 0) {
			cout << "C";
			x--;
		}
		if (y > 0) {
			cout << "Q";
			y--;
		}
		if (z > 0) {
			cout << "U";
			z--;
		}
	}
	return 0;
}
/*int zuida1 = max(x, y);
int zuida2 = max(x, z);
int zuida3;
if (zuida1 > zuida2)
	zuida3 = zuida1;
else
	zuida3 = zuida2;
int zuixiao1 = max(x, y);
int zuixiao2 = max(x, z);
int zuixiao3;
if (zuixiao1 > zuixiao2)
	zuixiao3 = zuixiao1;
else
	zuixiao3 = zuixiao2;
int zhongzhi;
switch (zuixiao3) {
	case x:
		for (int j1 = 0; j1 < x; j1++)
			cout << "CQU";
		switch (zhongzhi) {
			case y:
				for (int j2 = 0; j2 < (y - x); j2++)
					cout << "QU";
				for (int j3 = 0; j3 < (z - y); j3++)
					cout << "U";
				break;
			case z:
				for (int j2 = 0; j2 < (z - x); j2++)
					cout << "QU";
				for (int j3 = 0; j3 < (y - z); j3++)
					cout << "Q";
				break;
		}
		break;
	case y:
		for (int j1 = 0; j1 < x; j1++)
			cout << "CQU";
		switch (zhongzhi) {
			case x:
				for (int j2 = 0; j2 < (x - y); j2++)
					cout << "CU";
				for (int j3 = 0; j3 < (z - x); j3++)
					cout << "U";
				break;
			case z:
				for (int j2 = 0; j2 < (z - y); j2++)
					cout << "CU";
				for (int j3 = 0; j3 < (x - z); j3++)
					cout << "C";
				break;
		}
		break;
	case z:
		for (int j1 = 0; j1 < x; j1++)
			cout << "CQU";
		switch (zhongzhi) {
			case x:
				for (int j2 = 0; j2 < (x - z); j2++)
					cout << "CQ";
				for (int j3 = 0; j3 < (y - x); j3++)
					cout << "Q";
				break;
			case y:
				for (int j2 = 0; j2 < (y - z); j2++)
					cout << "CQ";
				for (int j3 = 0; j3 < (x - y); j3++)
					cout << "C";
				break;
		}
		break;
}*/

/*	if (zuida3 == x && zuixiao3 == z) {
for (int j3 = 0; j3 < z; j3++)
	cout << "CQU";
for (int j1 = 0; j1 < (y - z); j1++)
	cout << "CQ";
for (int j2 = 0; j2 < (x - y); j2++)
	cout << "C";
}

else if (zuida3 == x && zuixiao3 == y) {
for (int j3 = 0; j3 < y; j3++)
	cout << "CQU";
for (int j1 = 0; j1 < (z - y); j1++)
	cout << "CU";
for (int j2 = 0; j2 < (x - z); j2++)
	cout << "C";
} else if (zuida3 == y && zuixiao3 == z) {
for (int j3 = 0; j3 < z; j3++)
	cout << "CQU";
for (int j1 = 0; j1 < (x - z); j1++)
	cout << "CQ";
for (int j2 = 0; j2 < (y - x); j2++)
	cout << "Q";
} else if (zuida3 == y && zuixiao3 == x) {
for (int j3 = 0; j3 < x; j3++)
	cout << "CQU";
for (int j1 = 0; j1 < (z - x); j1++)
	cout << "QU";
for (int j2 = 0; j2 < (y - z); j2++)
	cout << "Q";
} else if (zuida3 == z && zuixiao3 == x) {
for (int j3 = 0; j3 < x; j3++)
	cout << "CQU";
for (int j1 = 0; j1 < (y - x); j1++)
	cout << "QU";
for (int j2 = 0; j2 < (z - y); j2++)
	cout << "U";
} else if (zuida3 == z && zuixiao3 == y) {
for (int j3 = 0; j3 < y; j3++)
	cout << "CQU";
for (int j1 = 0; j1 < (x - y); j1++)
	cout << "CU";
for (int j2 = 0; j2 < (z - x); j2++)
	cout << "U";
}*/

/*	int zuida1 = max(x, y);
	int zuida2 = max(x, z);
	int zuida3;
	if (zuida1 > zuida2)
		zuida3 = zuida1;
	else
		zuida3 = zuida2;
		int zuixiao1 = max(x, y);
	int zuixiao2 = max(x, z);
	int zuixiao3;
	if (zuixiao1 > zuixiao2)
		zuixiao3 = zuixiao1;
	else
		zuixiao3= zuixiao2;
	for (int j = 0; j < zuixiao3; j++) {
		cout << "CQU";
	}*/