#include <iostream>
#include <iomanip>
using namespace std;

int main() {
	unsigned int v;
	cin >> v;
	unsigned int *p = &v;
	cout << hex << *p << endl;
	unsigned char *p2 = reinterpret_cast<unsigned char *>(&v);
	cout << hex << setw(1) << setfill('0') << (int)p2[0] << " " << setw(1) << setfill('0') << (int)p2[1] << " " << setw(
	         1) << setfill('0') << (int)p2[2] << " " << setw(1) << setfill('0') << (int)p2[3];
	return 0;
}