#include <iostream>
using namespace std;

int main() {
	char a = 'C';
	const char & p = a;
	cout << p;
	return 0;
}
