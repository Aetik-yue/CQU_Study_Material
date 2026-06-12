#include <iostream>
#include <vector>
#include <map>
#include <string>
#include <algorithm>
using namespace std;

class Apple {
	public:
		string id;
		int weight;
		int diameter;
		int basketNumber;
		Apple() : id(""), weight(0), diameter(0), basketNumber(0) {}
		Apple(string id, int weight, int diameter, int basketNumber)
			: id(id), weight(weight), diameter(diameter), basketNumber(basketNumber) {}
		bool operator<(Apple other) { // 定义比较函数，用于排序
			return this->weight < other.weight;
		}
};

int main() {
	int n;
	cin >> n;
	map<int, Apple> heaviestApples; // 存储每个篮筐中最重的苹果
	for (int i = 0; i < n; ++i) {
		int basketNumber;
		string id;
		int weight, diameter;
		cin >> basketNumber >> id >> weight >> diameter;
		Apple apple(id, weight, diameter, basketNumber);
		if (heaviestApples.find(basketNumber) == heaviestApples.end() ||
		        heaviestApples[basketNumber].weight < apple.weight) {
			heaviestApples[basketNumber] = apple;
		}
	}
// 按篮筐号排序并输出结果
	for (map<int, Apple>::iterator it = heaviestApples.begin(); it != heaviestApples.end(); ++it) {
		cout << it->first << " " << it->second.id << " " << it->second.weight << " " <<
		     it->second.diameter << endl;
	}
	return 0;
}