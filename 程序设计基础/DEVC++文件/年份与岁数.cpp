#include <iostream>
#include <string>
#include <unordered_set>
using namespace std;

// 函数用于检查年份中不同数字的个数
int countUniqueDigits(const string &year) {
	unordered_set<char> digits;
	for (char c : year) {
		digits.insert(c);
	}
	return digits.size();
}

// 函数用于找到满足条件的年份
bool findYear(int y, int n, string &result) {
	string startYear = to_string(y);
	while (true) {
		int uniqueCount = countUniqueDigits(startYear);
		if (uniqueCount == n) {
			result = startYear;
			return true;
		}
		int yearInt = stoi(startYear);
		if (yearInt >= 3000) {
			return false; // 超过3000年，停止搜索
		}
		startYear = to_string(yearInt + 1);
	}
}

int main() {
	int y, n;
	cin >> y >> n; // 输入出生年份和不同数字的个数

	string result;
	if (findYear(y, n, result)) {
		cout << countUniqueDigits(result) << " " << result << endl; // 输出结果
	} else {
		cout << "No solution" << endl; // 如果没有找到符合条件的年份
	}

	return 0;
}