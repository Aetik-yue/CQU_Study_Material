#include <iostream>
#include <string>
#include <vector>
#include <algorithm>
#include <map>
using namespace std;

class Apple {
	public:
		string id;
		int weight;
		int diameter;
		Apple(string i, int w, int d) : id(i), weight(w), diameter(d) {}
};

bool compare(const pair<int, Apple> &a, const pair<int, Apple> &b) {
	return a.first < b.first;
}

int main() {
	int n;
	cin >> n;
	// 使用map来存储每个篮筐号对应的苹果信息，篮筐号为键，Apple对象为值
	// 因为map内部会按照键自动排序（从小到大），方便后续输出
	map<int, Apple> basketApples;
	for (int i = 0; i < n; ++i) {
		int basketNum;
		string id;
		int weight;
		int diameter;
		cin >> basketNum >> id >> weight >> diameter;
		if (basketApples.find(basketNum) == basketApples.end() ||
		        weight > basketApples[basketNum].weight) {
			basketApples[basketNum] = Apple(id, weight, diameter);
		}
	}

	// 将map中的键值对转换为vector，方便按照格式要求输出
	vector<pair<int, Apple>> result(basketApples.begin(), basketApples.end());
	for (const auto &p : result) {
		cout << p.first << " " << p.second.id << " " << p.second.weight << " " << p.second.diameter << endl;
	}

	return 0;
}