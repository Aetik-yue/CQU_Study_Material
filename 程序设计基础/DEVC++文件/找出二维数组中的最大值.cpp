#include <iostream>
#include <algorithm>
using namespace std;

int main() {
	int n;
	cin >> n;
	int arr[n][n];
	for (int i = 0; i < n; i++) {
		for (int j = 0; j < n; j++) {
			cin >> arr[i][j];
		}
	}
	int m = n * n;
	int maxn = **max_element(arr[n][n], arr[n][n] + m);
	for (int i = 0; i < n; i++) {
		for (int j = 0; j < n; j++) {
			if (maxn == arr[i][j])
				cout << i << " " << j << endl;
		}
	}
	return 0;
}