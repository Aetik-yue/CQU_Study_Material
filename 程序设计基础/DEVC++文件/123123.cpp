#include <iostream>
using namespace std;

int main() {
	int n;
	int sum = 0;
	cin >> n;
	int shuzu[n][n];
	for (int i = 0; i < n; i++) {
		for (int j = 0; j < n; j++) {
			cin >> shuzu[i][j];
			sum = sum + shuzu[i][j];
		}
	}
	double A;
	A = static_cast<double>sum / (n * n);
	bool found = false;
	for (int i = 0; i < n; i++) {
		for (int j = 0; j < n; j++) {
			if (shuzu[i][j] > A)
				cout << shuzu[i][j] << " " << i << " " << j << endl;
			found = true;
		}
	}
	if (!found) {
		cout << 0 << endl;
	}

	return 0;
}