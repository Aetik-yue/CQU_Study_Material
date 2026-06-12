#include <iostream>
using namespace std;

int main() {
	int *p;
	int a = 100;
	p = &a;
	cout << "p is" << p << endl;
	cout << "a is" << a << endl;
	cout << "&p is" << " " << &p << endl;
	*p = 20;
	cout << "a is" << a << endl;

	return 0;
}
