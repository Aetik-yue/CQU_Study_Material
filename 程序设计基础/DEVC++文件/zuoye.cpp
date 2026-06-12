#include <iostream>
#include <iomanip>
using namespace std;

int main() {
	int m, n;
	cin >> m >> n;
	int arr[m][n];
	for (int i = 0; i < m; i++) {
		for (int j = 0; j < n; j++) {
			cin >> arr[i][j];
		}
	}
	//先求出每一位选手的分数
	//然后再定义一个排名数组num【】
	//通过冒泡排序按分数高低的选手序号给num【】赋值
	int score[m];
	for (int i = 0; i < m; i++) {
		int max = 0;
		int min = 1000;
		int sum = 0;
		for (int j = 0; j < n; j++) {
			if (arr[i][j] > max)
				max = arr[i][j];
			if (arr[i][j] < min)
				min = arr[i][j];
		}
		for (int j = 0; j < n; j++) {
			sum = sum + arr[i][j];
		}
		sum = sum - min - max;
		score[i] = sum;
	}
	int num[m];
	int t;
	for (int i = 0; i < m; i++) {
		int max = 0;
		int min = 1000;
		int sum = 0;
		for (int j = 0; j < n; j++) {
			if (arr[i][j] > max)
				max = arr[i][j];
			if (arr[i][j] < min)
				min = arr[i][j];
		}
		for (int j = 0; j < n; j++) {
			sum = sum + arr[i][j];
		}
		sum = sum - min - max;
		num[i] = sum;
	}
	for (int x = 0; x < m - 1; x++) {
		for (int y = 0; y < m - x - 1; y++) {
			if (score[y] < score[y + 1])
				t = score[y + 1], score[y + 1] = score[y], score[y] = t;
		}
	}
	for (int i = 0; i < m; i++) {
		for (int j = 0; j < m; j++) {
			if (score[i + 1] == score[i]) {
				break;
			}
			if (num[j] == score[i]) {
				cout << setw(3) << j + 1;
			}
		}
	}
	return 0;
}