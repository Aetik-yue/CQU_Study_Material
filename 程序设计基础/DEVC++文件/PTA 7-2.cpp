#include <iostream>
#include <cmath>
#include <iomanip>
using namespace std;

int main() {
	int N;
	double fangcha;
	fangcha = 0;
	float sum = 0;
	cin >> N;
	int A[N];
	for (int i = 0; i < N; i++) {
		cin >> A[i];
		sum = sum + A[i];
	}
	double avg = sum / N;
	for (int p = 0; p < N; p++) {
		double bb = pow(A[p] - avg, 2) / N;
		fangcha = fangcha + bb;
	}
	double biaozhuncha = sqrt(fangcha);
	cout << fixed << setprecision(5) << biaozhuncha << endl;
	return 0;
}