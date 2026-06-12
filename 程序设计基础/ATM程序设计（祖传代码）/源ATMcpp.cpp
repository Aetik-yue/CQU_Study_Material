#define _CRT_SECURE_NO_WARNINGS
#include <iostream>
#include <string>
#include <fstream>
#include <math.h>
#include <windows.h> //system
using namespace std;

class amount {
public:
	string id;
	string name;
	string hao;
	int password;
	double money;
	amount(string a, int b, string c, string x, double y) {
		y = 10000;
	flag0:
		cout << "请设置一个六位数的初始密码：";
		cin >> b;
		if (b / 100000 == 0) {
			system("cls");//命令行清屏
			cout << "密码不是六位数，请重新设置" << endl;
			cout << "正在跳转至上一界面，请稍后~";
			Sleep(3000);
			system("cls");//命令行清屏
			goto flag0;
		}
		id = a;
		name = x;
		password = b;
		hao = c;
		money = y;
	}

};

int main() {
	ifstream ifs;
	ifs.open("123456", ios::out);
	string idd;
	string namem;
	string haoo;
	int pass;
	double mon;
	ifs >> idd >> namem >> haoo >> pass >> mon;
	amount a(idd, pass, haoo, namem, mon);
	system("cls");//命令行清屏
	cout << "--------------------------------------------------------" << endl;
	cout << "--------------欢迎使用中国银行自助ATM机-----------------" << endl;
	cout << "----------------------请您插卡--------------------------" << endl;
	cout << "--------------------------------------------------------" << endl;
	Sleep(2000);
	system("cls");//命令行清屏
	cout << "正在读卡中，请稍后~" << endl;
	Sleep(2000);
	system("cls");//命令行清屏
	cout << "读卡成功，您的账号是" << "1231231231231231231" << endl << "请输入六位数字密码：" << endl;
	int b{};//标记输入密码错误次数
flag2:
	int mima; //密码
	cin >> mima;

	if (mima == a.password) {
		system("cls");//命令行清屏
		cout << "--------密码正确----------" << endl;
		Sleep(2000);
	}
	else {
		system("cls");//命令行清屏
		b++;
		cout << "------------您输入的密码有误，请重新输入------------" << endl;
		cout << "温馨提示：若密码再错误" << (3 - b) << "次，您的账户将被冻结" << endl;
		if (b < 3) {
			goto flag2;
		}
	}
	if (b == 3) {
		system("cls");//命令行清屏
		cout << "-----------您的账户已经被冻结------------" << endl;

	}
	if (b < 3) {
		system("cls");//命令行清屏
	flag:
		cout << "-----------------------------------" << endl;
		cout << "           ****主菜单****          " << endl;
		cout << "                                   " << endl;
		cout << "            1.余额查询             " << endl;
		cout << "            2.提取存款             " << endl;
		cout << "            3.进行转账             " << endl;
		cout << "            4.修改密码             " << endl;
		cout << "              5.出卡               " << endl;
		cout << "-----------------------------------" << endl;
		cout << "请输入您的选择： " << endl;
		int choise;
		cin >> choise;
		//开始选择


		//余额查询功能
		if (choise == 1) {
			system("cls");//命令行清屏
			cout << "账户： " << "1234561234561234567" << endl;
			cout << "您的余额为： " << a.money << ".00" << endl;
			cout << "返回主菜单请按0，出卡请按9" << endl;
			int s;
			cin >> s;
			if (s == 0) {
				system("cls");//命令行清屏
				goto flag;
			}
			if (s == 9) {
				system("cls");//命令行清屏
				cout << "--------------------------" << endl;
				cout << "谢谢您的使用，感谢下次光临" << endl;
				cout << "--------------------------" << endl;
			}
		}


		//提取存款功能
		if (choise == 2) {

			int sum = 0;
		flag1:
			system("cls");//命令行清屏
			cout << "当前余额为： " << a.money << ".00" << endl;
			cout << "请选择您要提取的钱款数目：    " << endl;
			cout << "注意：您提取的钱款数目需要为100的整数倍" << endl;
			cout << "注意：您的单笔金额限制上限为1000元" << endl;
			cout << "注意：您的单日提款金额上限为5000元" << endl;
			cout << "返回主菜单请按0，出卡请按9" << endl;

			int choise;//进行提取金额的选择
			cin >> choise;

			if (choise % 100 > 0 && choise != 0 && choise != 9) {
				system("cls");//命令行清屏
				cout << "对不起，您提取的钱款数目不是100的整数倍,提款失败" << endl;
				cout << "正在跳转至上一界面，请稍后~" << endl;
				Sleep(2000);
				goto flag1;
			}

			if (a.money < choise && choise > 100) {
				system("cls");//命令行清屏
				cout << "对不起，您的余额不足,提款失败" << endl;
				cout << "正在跳转至上一界面，请稍后~" << endl;
				Sleep(2000);
				goto flag1;
			}

			if ((sum + choise) > 5000 && choise > 100) {
				system("cls");//命令行清屏
				cout << "对不起，您的单日提款金额已经超过上限5000元，提款失败" << endl;
				cout << "正在跳转至上一界面，请稍后~" << endl;
				Sleep(2000);
				goto flag1;
			}

			if (choise > 1000) {
				system("cls");//命令行清屏
				cout << "对不起，您的单笔金额已经超过限制上限1000元，提款失败" << endl;
				cout << "正在跳转至上一界面，请稍后~" << endl;
				Sleep(2000);
				goto flag1;
			}

			if (choise % 100 == 0 && a.money > choise && (sum + choise) <= 5000 && choise <= 1000 && choise >= 100) {
				system("cls");//命令行清屏
				a.money = a.money - choise;
				cout << "恭喜您取款成功" << endl;
				sum = sum + choise;
				cout << "当前余额为： " << a.money << ".00" << endl;
				cout << "正在跳转至上一界面，请稍后~" << endl;
				Sleep(2000);
				system("cls");//命令行清屏
				goto flag1;
			}

			if (choise == 0) {
				system("cls");//命令行清屏
				goto flag;
			}
			if (choise == 9) {
				system("cls");//命令行清屏
				cout << "--------------------------" << endl;
				cout << "谢谢您的使用，欢迎下次光临" << endl;
				cout << "--------------------------" << endl;
			}

		}

		//进行转账功能
		if (choise == 3) {
			system("cls");//命令行清屏
		flag9:
			cout << "请输入您要转入的帐户：" << endl;
			int u;
			cin >> u;
			cout << "请再次确认您的转入账户：" << endl;
			int zx;
			cin >> zx;
			if (zx == u) {
				system("cls");//命令行清屏
				cout << "--------------------------------------------------" << endl;
				cout << "当前余额为： " << a.money << ".00" << endl;
				cout << "**注意，您的单次转款金额上限为伍千元,且不可超过账户余额**" << endl;
				cout << "--------------------------------------------------" << endl;
				cout << "请输入您的转款金额：" << endl;
				int z_money;
				cin >> z_money;
				if (z_money <= 5000) {
					system("cls");//命令行清屏
					cout << "恭喜您转账成功" << endl;
					a.money = a.money - z_money;
					cout << "当前余额为： " << a.money << ".00" << endl;
					cout << "返回主菜单请按0，出卡请按9" << endl;
					int z_money1;
					cin >> z_money1;
					if (z_money1 == 0) {
						system("cls");//命令行清屏
						goto flag;

					}
					if (z_money1 == 9) {
						system("cls");//命令行清屏
						cout << "--------------------------" << endl;
						cout << "感谢您的使用，欢迎下次光临" << endl;
						cout << "--------------------------" << endl;
					}

				}
				if (z_money > 5000 && z_money < a.money) {
					system("cls");//命令行清屏
					cout << "对不起，您的转入金额超过单笔转入金额上限" << endl;
					cout << "请返回转账初始界面重新输入" << endl;
					system("cls");//命令行清屏
					goto flag9;
				}
				if (z_money > a.money) {
					system("cls");//命令行清屏
					cout << "您的账户余额不足，请重试" << endl;
					system("cls");//命令行清屏
					goto flag9;
				}
			}
			if (zx != u) {
				system("cls");//命令行清屏
				cout << "对不起，您两次输入的账户不一样，正在跳转至上一界面，请稍后~" << endl;
				Sleep(2000);
				system("cls");//命令行清屏
				goto flag9;
			}

		}

		//修改密码功能
		if (choise == 4) {
			system("cls");//命令行清屏
			cout << "请输入您的新密码：" << endl;
			int p;
			cin >> p;
			cout << "请再次输入您的新密码：" << endl;
			int t;
			cin >> t;
			if (p == t) {
				system("cls");//命令行清屏
				cout << "密码修改成功！" << endl;
				a.password = t;
				cout << "返回主菜单请按0，出卡请按9" << endl;
				int o;
				cin >> o;
				if (o == 0) {
					system("cls");//命令行清屏
					goto flag;
				}
				if (o == 9) {
					system("cls");//命令行清屏
					cout << "--------------------------" << endl;
					cout << "感谢您的使用，欢迎下次光临" << endl;
					cout << "--------------------------" << endl;
				}
			}
			else {
				system("cls");//命令行清屏
				cout << "密码修改失败！" << endl;
				cout << "返回主菜单请按0，出卡请按9" << endl;
				int o;
				cin >> o;
				if (o == 0) {
					system("cls");//命令行清屏
					goto flag;
				}
				if (o == 9) {
					system("cls");//命令行清屏
					cout << "--------------------------" << endl;
					cout << "感谢您的使用，欢迎下次光临" << endl;
					cout << "--------------------------" << endl;
				}
			}


		}


		//出卡功能
		if (choise == 5) {
			system("cls");//命令行清屏
			cout << "--------------------------" << endl;
			cout << "感谢您的使用，欢迎下次光临" << endl;
			cout << "--------------------------" << endl;
		}
	}
}