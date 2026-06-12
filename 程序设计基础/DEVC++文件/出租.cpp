#include <iostream>
using namespace std;

int main() {
	string tel;
	int n = 0;
	int sum = 0;
	int num[11]; //过渡数组？
	int index[11];
	cin >> tel;
	for (int i = 0; i < 11; i++) {
		num[i] = tel[i] - '0';
	}
	for (int j = 0; j < 10; j++) {
		for (int i = 0; i < 11; i++) {
			if (num[i] == j) {
				sum++;
				break;
			}
		}
	}
	int arr[sum];
	for (int j = 0; j < 10; j++) {
		for (int i = 0; i < 11; i++) {
			if (num[i] == j) {
				arr[n] = j;
				n++;
				break;
			}

		}
		if (n == sum) {
			break;
		}
	}
	//得到了arr数组，接下来进行冒泡排序，按递减顺序输出；
	//冒泡排序;
	int t;       //冒泡排序中间量；
	for (int j = 0; j < sum - 1; j++) {
		for (int i = 0; i < sum - 1 - j; i++) {
			if (arr[i] < arr[i + 1]) {
				t = arr[i];
				arr[i] = arr[i + 1];
				arr[i + 1] = t;
			}
		}
	}
	//冒泡排序结束；
	//开始查找index数组对应的数；
	for (int i = 0; i < sum; i++) {
		for (int j = 0; j < 11; j++) {
			if (num[j] == arr[i]) {
				index[j] = i;
			}
		}
	}
	cout << "int[] arr = new int[]{";
	for (int i = 0; i < sum; i++) {
		if (i == sum - 1) {
			cout << arr[i];
			break;
		}
		cout << arr[i];

		cout << "," ;
	}
	cout << "};" << endl;
	cout << "int[] index = new int[] {";
	for (int i = 0; i < 11; i++) {
		if (i == 10) {
			cout << index[i];
			break;
		}
		cout << index[i];
		cout << ",";
	}
	cout << "};" ;

	return 0;
}