#include <iostream>
using namespace std;

int main() {
	int T;
	int count = 0;
	cin >> T;
	while (T != 0) {
		int n;
		cin >> n;
		int scores[n];
		for (int i = 0; i < n; i++) {
			cin >> scores[i];
		}

		int sum = 0;
		for (int num : scores) {
			sum += num;
		}
		double average = static_cast<double>(sum) / n;


		for (int num : scores) {
			if (num > average) {
				count++;
			}
		}
		T--;
	}
	cout << count << endl;

	return 0;
}