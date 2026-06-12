#include <iostream>
#include <string>
using namespace std;

int main() {
    long m, n, s4, s62;
    cin >> m >> n;
    s4 = 0;
    s62 = 0;
    int arr[n - m + 1];
    for (int i = 0; i < (n - m + 1); i++) {
        arr[i] = m + i;
        string A = to_string(arr[i]);
        for (int j = 0; j < A.size(); j++) {
            if (A[j] == '4') {
                s4++;
                break;
            }
            if (A[j] == '6' && A[j + 1] == '2') {
                s62++;
                break;
            }
        }
    }
    int a;
    a = n - m + 1 - s4 - s62;
    cout << a << endl;

    return 0;
}