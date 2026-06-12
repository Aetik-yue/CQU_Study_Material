/*#include <iostream>
#include <math.h>
#include <algorithm>
using namespace std;

/*long mi(long q, long p) {
	long y;
	if (q > p)
		y = p;
	if (q < p)
		y = q;
	return y;
}

int main() {
	long m, n, a ;
	cin >> m >> n;
	long num[m];
	for (int i = 1; i <= m; i++) {
		cin >> num[i - 1];

	}
	for (int j = 1; j <= n; j++) {
		int start, end;
		cin >> start >> end;
		start--;
		end--;
		long long minm = 100000000;
		do {
			if (minm > num[start]) {

				minm = num[start];
				start++;
			}
		} while (start != end);
		cout << minm << endl;
	}
	return 0;
}*/

/*int main() {
	int number, N, i = 0, x;
	scanf("%d %d", &number, &N);
	do {
		i++;
		scanf("%d", &x);
		if (x < 0)
			printf("Game Over"), x = number;
		else if (i <= N) {
			if (x > number)
				printf("Too big\n");
			else if (x == number) {
				if (i == 1)
					printf("Bingo!");
				else if (i <= 3)
					printf("Lucky You!");
				else
					printf("Good Guess!");
			} else
				printf("Too small\n");
		} else
			printf("Game Over"), x = number;
	} while (x != number);
	return 0;
}*/
/*#include <iostream>
#include <vector>
#include <algorithm>

using namespace std;

int main() {
	int m, n;
	cin >> m >> n;

	// 读取账目数据
	vector<int> accounts(m);
	for (int i = 0; i < m; ++i) {
		cin >> accounts[i];
	}

	// 处理每个查询问题
	for (int i = 0; i < n; ++i) {
		int a, b;
		cin >> a >> b;
		// 将问题转换为0-based索引
		a--;
		b--;
		// 使用STL的min_element函数找到[a, b]范围内的最小值
		// 注意：min_element返回的是迭代器，需要解引用得到值
		int min_value = *min_element(accounts.begin() + a, accounts.begin() + b + 1);
		cout << min_value << endl;
	}

	return 0;
}*/
/*#include <iostream>
#include <math.h>
#include <algorithm>
using namespace std;



int main() {
	long m, n, a ;
	cin >> m >> n;
	long num[m];
	for (int i = 0; i < m; i++) {
		cin >> a;
		num[i] = a;

	}
	for (int j = 1; j <= n; j++) {
		int start, end;
		cin >> start >> end;
		start--;
		end--;
		long long minm = 100000000;
		do {
			if (minm > num[start]) {

				minm = num[start];
			}
			start++;
		} while (start != end);
		cout << minm << endl;
	}
	return 0;
}*/
#include <iostream>
#include <math.h>
#include <algorithm>
using namespace std;



int main() {
	long m, n, a ;
	cin >> m >> n;
	long num[m];
	for (int i = 0; i < m; i++) {
		cin >> a;
		num[i] = a;

	}
	for (int j = 1; j <= n; j++) {
		int start, end;
		cin >> start >> end;
		start--;
		end--;
		long  minm = *min_element(num + start, num + end);

		cout << minm << endl;
	}
	return 0;
}