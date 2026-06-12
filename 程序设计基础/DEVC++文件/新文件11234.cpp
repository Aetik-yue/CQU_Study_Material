#include <iostream>
#include <algorithm>
#include <iomanip>

void Swap(int a[], int i, int j) {
	int temp = a[i];
	a[i] = a[j];
	a[j] = temp;
}
using namespace std;


int main() {
	int n, a;
	cin >> n;
	int i;
	int nums[n];
	for ( i = 0; i < n; i++) {
		cin >> a;
		nums[i] = a;
	}
	int t, q;
	int minm = min_element(nums.begin(), nums.end()) - nums.begin();
	int maxm = max_element(nums.begin(), nums.end()) - nums.begin();
	swap(nums[0], nums[minmt]);
	if (maxm == 0) {
		maxm = minm;
	}
	swap(nums[n - 1], nums[maxm]);
	for (i = 0; i < n; i++) {
		cout << nums[i] << " ";
	}

	return 0;
}