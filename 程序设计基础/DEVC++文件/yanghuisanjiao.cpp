#include <iostream>
using namespace std;

int main() {
	int arr[3][3];
	int yu = 45;
	for (int x = 0; x < 3; x++) {
		for (int y = 0; y < 3; y++) {
			cin >> arr[x][y];
		}
	}
	for (int x = 0; x < 3; x++) {
		for (int y = 0; y < 3; y++) {
			if (arr[x][y] == 0) {
				for (int i = 0; i < 3; i++) {
					for (int j = 0; j < 3; j++) {
						while (arr[i][j] != 0) {
							int a = 0;
							arr[i][j]--;
							a++;
							yu = yu - a;
						}

					}

				}
				arr[x][y] = yu;
			}
		}
	}
	int i1, j1, i2, j2, i3, j3, sum, n;
	cin >> i1 >> j1 >> i2 >> j2 >> i3 >> j3;
	cin >> n;
	switch (n) {
		case 1:
			sum = arr[0][0] + arr[0][1] + arr[0][2];
			break;
		case 2:
			sum = arr[1][0] + arr[1][1] + arr[1][2];
			break;
		case 3:
			sum = arr[2][0] + arr[2][1] + arr[2][2];
			break;
		case 4:
			sum = arr[0][0] + arr[1][0] + arr[2][0];
			break;
		case 5:
			sum = arr[0][1] + arr[1][1] + arr[2][1];
			break;
		case 6:
			sum = arr[0][2] + arr[1][2] + arr[2][2];
			break;
		case 7:
			sum = arr[0][0] + arr[1][1] + arr[2][2];
			break;
		case 8:
			sum = arr[0][2] + arr[1][1] + arr[2][0];
			break;
	}
	int price;
	switch (sum) {
		case 6:
			price = 10000;
			break;
		case 7:
			price = 36;
			break;
		case 8:
			price = 720;
			break;
		case 9:
			price = 360;
			break;
		case 10:
			price = 80;
			break;
		case 11:
			price = 252;
			break;
		case 12:
			price = 108;
			break;
		case 13:
			price = 72;
			break;
		case 14:
			price = 54;
			break;
		case 15:
			price = 180;
			break;
		case 16:
			price = 72;
			break;
		case 17:
			price = 180;
			break;
		case 18:
			price = 119;
			break;
		case 19:
			price = 36;
			break;
		case 20:
			price = 306;
			break;
		case 21:
			price = 1080;
			break;
		case 22:
			price = 144;
			break;
		case 23:
			price = 1800;
			break;
		case 24:
			price = 3600;
			break;
	}
	cout << arr[i1 - 1][j1 - 1] << endl;
	cout << arr[i2 - 1][j2 - 1] << endl;
	cout << arr[i3 - 1][j3 - 1] << endl;
	cout << price << endl;
	return 0;
}