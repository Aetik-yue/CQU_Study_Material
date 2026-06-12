#include <iostream>
#include <iomanip>
using namespace std;

// 交换两个整数
void swap(int *a, int *b) {
	int temp = *a;
	*a = *b;
	*b = temp;
}

int main() {
	int m, n;
	cin >> m >> n;
	// 动态分配二维数组来存储评委给选手的分数
	int **scores = new int *[m];
	for (int i = 0; i < m; ++i) {
		scores[i] = new int[n];
	}

	// 输入每个评委给选手的分数
	for (int i = 0; i < m; ++i) {
		for (int j = 0; j < n; ++j) {
			cin >> scores[i][j];
		}
	}

	// 计算每个选手的最终得分
	double *finalScores = new double[m];
	for (int i = 0; i < m; ++i) {
		int maxScore = scores[i][0];
		int minScore = scores[i][0];
		int sum = 0;
		for (int j = 0; j < n; ++j) {
			sum += scores[i][j];
			if (scores[i][j] > maxScore) {
				maxScore = scores[i][j];
			}
			if (scores[i][j] < minScore) {
				minScore = scores[i][j];
			}
		}
		finalScores[i] = (double)(sum - maxScore - minScore) / (n - 2);
	}

	// 使用冒泡排序根据最终得分对选手进行排名
	for (int i = 0; i < m - 1; ++i) {
		for (int j = 0; j < m - i - 1; ++j) {
			if (finalScores[j] < finalScores[j + 1]) {
				// 交换选手的最终得分
				swap(&finalScores[j], &finalScores[j + 1]);
				// 交换选手编号（这里的编号就是数组下标，实际可以对应具体的选手标识）
				swap(&j, &(j + 1));
			}
		}
	}

	// 输出排名后的选手编号（从高到低）
	for (int i = 0; i < m; ++i) {
		cout << setw(3) << (i + 1);
	}
	cout << endl;

	// 释放动态分配的内存
	for (int i = 0; i < m; ++i) {
		delete[] scores[i];
	}
	delete[] scores;
	delete[] finalScores;

	return 0;
}