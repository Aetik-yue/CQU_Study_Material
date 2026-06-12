#include <iostream>
using namespace std;

int main() {
	int n;
	cin >> n;
	int arr[n][n];
	for (int i = 0; i < n; i++) {
		for (int j = 0; j < n; j++) {
			cin >> arr[n][n];
		}
	}
	int maxn, minm, x, y, z, j1, i1, i, j;
	maxn = 0;
	minm = 100000;
	bool cunzai = false;
	for (int i = 0; i < n; i++) {
		for (j = 0; j < n; j++) {
			if (maxn < arr[i][j]) {
				maxn = arr[i][j];
			}
		}//找到了第i行的最大值
		for (i1 = 0; i1 < n; i1++) {
			for (j1 = 0; j1 < n; j1++) {
				if (maxn == arr[i1][j1]) {
					x = i1;
					y = j1;
					break;//确定行数和列数
				}
			}
			if (maxn == arr[i1][j1]) {
				x = i1;
				y = j1;
				break;
			}
		}
		//列数不变，行数检索
		for ( z = 0; z < n; z++) {
			if (minm > arr[z][y])
				minm = arr[z][y];
		}//找到该行的最小值
		if (maxn == minm) {//进行比较
			cunzai = true;
			cout << z << " " << y;
		}
		if (maxn != minm)
			continue;
	}
	if (cunzai == false)
		cout << "NONE";
	return 0;
}