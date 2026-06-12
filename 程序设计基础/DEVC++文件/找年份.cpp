#include <iostream>
#include <string>
#include <unordered_set>
#include <iomanip>

using namespace std;

// 函数用于检查年份中不同数字的个数
int countUniqueDigits(int year) {
	unordered_set<char> digits;
	string yearStr = to_string(year);
	for (char c : yearStr) {
		digits.insert(c);
	}
	return digits.size();
}

int main() {
	int y, n;
	cin >> y >> n;

	int age = 0;
	int currentYear = y;
	while (true) {
		int uniqueDigits = countUniqueDigits(currentYear);
		if (uniqueDigits == n) {
			cout << age << " " << setfill('0') << setw(4) << currentYear << endl;
			break;
		}
		currentYear++;
		age++;
	}
	return 0;
}